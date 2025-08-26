// src/main/java/com/empresa/ecommerce_backend/service/impl/CartServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.AddItemRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateQtyRequest;
import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.StockTrackingMode;
import com.empresa.ecommerce_backend.exception.NeedsVariantException;
import com.empresa.ecommerce_backend.mapper.CartMapper;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.CartItemRepository;
import com.empresa.ecommerce_backend.repository.CartRepository;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CartMapper cartMapper;


    @Override
    @Transactional
    public ServiceResult<CartResponse> attachCartToUser(String sessionId, Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId requerido");

        // Traemos ambos con lock para evitar carreras
        Optional<Cart> userCartOpt   = cartRepository.lockByUserId(userId);
        Optional<Cart> sessionCartOpt = (sessionId != null && !sessionId.isBlank())
                ? cartRepository.lockBySessionId(sessionId) : Optional.empty();

        // Caso 1: no hay ningun carrito -> crear para el usuario
        if (userCartOpt.isEmpty() && sessionCartOpt.isEmpty()) {
            Cart c = new Cart();
            User u = new User(); u.setId(userId);
            c.setUser(u);
            c.setItems(new HashSet<>());
            Cart saved = cartRepository.save(c);
            return ServiceResult.ok(cartMapper.toResponse(saved));
        }

        // Caso 2: hay sólo carrito de usuario -> devolverlo
        if (userCartOpt.isPresent() && sessionCartOpt.isEmpty()) {
            return ServiceResult.ok(cartMapper.toResponse(userCartOpt.get()));
        }

        // Caso 3: hay sólo carrito de sesión -> lo “adoptamos” al usuario
        if (userCartOpt.isEmpty() && sessionCartOpt.isPresent()) {
            Cart sessionCart = sessionCartOpt.get();
            User u = new User(); u.setId(userId);
            sessionCart.setUser(u);
            Cart saved = cartRepository.save(sessionCart);
            return ServiceResult.ok(cartMapper.toResponse(saved));
        }

        // Caso 4: existen ambos y son distintos -> merge
        Cart userCart = userCartOpt.get();
        Cart sessionCart = sessionCartOpt.get();

        if (!userCart.getId().equals(sessionCart.getId())) {
            for (CartItem si : sessionCart.getItems()) {
                Product product = si.getProduct();     // ya está loaded por JPA (LAZY -> asegúrate de acceso dentro TX)
                ProductVariant variant = si.getVariant();

                Optional<CartItem> existingOpt =
                        cartItemRepository.findByCartAndProductAndVariant(userCart, product, variant);

                int stock = (variant != null) ? variant.getStock() : product.getStock();
                int baseQty = existingOpt.map(CartItem::getQuantity).orElse(0);
                int mergedQty = Math.min(baseQty + si.getQuantity(), stock);

                if (existingOpt.isEmpty()) {
                    if (mergedQty <= 0) continue;
                    CartItem ni = new CartItem();
                    ni.setCart(userCart);
                    ni.setProduct(product);
                    ni.setVariant(variant);
                    ni.setQuantity(mergedQty);
                    // Conservamos el modelo de precios “al agregar”
                    ni.setPriceAtAddition(si.getPriceAtAddition());
                    ni.setDiscountedPriceAtAddition(si.getDiscountedPriceAtAddition());
                    userCart.getItems().add(ni);
                } else {
                    CartItem ei = existingOpt.get();
                    if (mergedQty == 0) {
                        userCart.getItems().remove(ei);
                        cartItemRepository.delete(ei);
                    } else {
                        ei.setQuantity(mergedQty);
                        // Opcional: no tocar priceAtAddition para mantener histórico del item previo
                    }
                }
            }

            // Borramos el carrito de sesión para liberar el UNIQUE de session_id si no lo necesitás más
            // (opcional: podés conservarlo con el mismo user para continuidad post-logout)
            cartItemRepository.deleteAll(sessionCart.getItems());
            sessionCart.getItems().clear();
            cartRepository.delete(sessionCart);
        }

        Cart saved = cartRepository.save(userCart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }


    /* =================== GET/CREATE =================== */
    @Override
    @Transactional
    public ServiceResult<CartResponse> getOrCreateBySession(String sessionIdOpt) {
        Cart cart = (sessionIdOpt != null && !sessionIdOpt.isBlank())
                ? cartRepository.findBySessionId(sessionIdOpt).orElse(null)
                : null;

        if (cart == null) {
            cart = new Cart();
            cart.setSessionId(UUID.randomUUID().toString());
            cart.setItems(new HashSet<>());
            cart.setUpdatedAt(LocalDateTime.now());
            Cart saved = cartRepository.save(cart);
            return ServiceResult.created(cartMapper.toResponse(saved)); // 201
        }
        return ServiceResult.ok(cartMapper.toResponse(cart));
    }

    /* =================== ADD ITEM =================== */
    @Override
    @Transactional
    public ServiceResult<CartResponse> addItem(String sessionId, AddItemRequest dto) {
        Cart cart = resolveOrCreateCart(sessionId);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        if (product.getStockTrackingMode() == StockTrackingMode.VARIANT && dto.getVariantId() == null) {
            throw new NeedsVariantException("Este producto requiere que selecciones una variante", product.getId());
        }

        // Validar variante si viene
        ProductVariant variant = null;
        if (dto.getVariantId() != null) {
            variant = variantRepository.findByIdAndProductId(dto.getVariantId(), product.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "La variante indicada no existe para este producto"));
        }

        BigDecimal listPrice = (variant != null) ? variant.getPrice() : product.getPrice();
        BigDecimal discounted = listPrice; // MVP sin descuentos

        Optional<CartItem> existing = cartItemRepository.findByCartAndProductAndVariant(cart, product, variant);
        CartItem item = existing.orElse(null);

        int newQty = (item != null ? item.getQuantity() : 0) + dto.getQuantity();

        int stock = (variant != null) ? variant.getStock() : product.getStock();
        if (newQty > stock) {
            return ServiceResult.error(HttpStatus.CONFLICT, "Stock insuficiente. Disponible: " + stock);
        }

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(dto.getQuantity());
            item.setPriceAtAddition(listPrice);
            item.setDiscountedPriceAtAddition(discounted);
        } else {
            item.setQuantity(newQty);
        }

        cart.getItems().add(item);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }


    /* =================== UPDATE QTY =================== */
    @Override
    @Transactional
    public ServiceResult<CartResponse> updateQuantity(String sessionId, Long itemId, UpdateQtyRequest dto) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        if (dto.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            int stock = (item.getVariant() != null) ? item.getVariant().getStock() : item.getProduct().getStock();
            if (dto.getQuantity() > stock) {
                return ServiceResult.error(HttpStatus.CONFLICT, "Stock insuficiente. Disponible: " + stock);
            }
            item.setQuantity(dto.getQuantity());
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<CartResponse> incrementItem(String sessionId, Long itemId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        int stock = (item.getVariant() != null) ? item.getVariant().getStock() : item.getProduct().getStock();
        int newQty = item.getQuantity() + 1;

        if (newQty > stock) {
            return ServiceResult.error(HttpStatus.CONFLICT, "Stock insuficiente. Disponible: " + stock);
        }

        item.setQuantity(newQty);
        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<CartResponse> decrementItem(String sessionId, Long itemId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        int newQty = Math.max(1, item.getQuantity() - 1); // 👈 nunca baja de 1
        item.setQuantity(newQty);

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }


    /* =================== REMOVE ITEM =================== */
    @Override
    @Transactional
    public ServiceResult<CartResponse> removeItem(String sessionId, Long itemId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }

    /* =================== CLEAR CART =================== */
    @Override
    @Transactional
    public ServiceResult<CartResponse> clear(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Carrito no encontrado"));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }

    /* =================== HELPERS =================== */
    private Cart resolveOrCreateCart(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        c.setSessionId(sessionId);
                        c.setItems(new HashSet<>());
                        c.setUpdatedAt(LocalDateTime.now());
                        return cartRepository.save(c);
                    });
        }
        Cart c = new Cart();
        c.setSessionId(UUID.randomUUID().toString());
        c.setItems(new HashSet<>());
        c.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(c);
    }
}
