// src/main/java/com/empresa/ecommerce_backend/service/impl/CartServiceImpl.java
package com.empresa.ecommerce_backend.service.impl;

import com.empresa.ecommerce_backend.dto.request.AddItemRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateQtyRequest;
import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.mapper.CartMapper;
import com.empresa.ecommerce_backend.model.Cart;
import com.empresa.ecommerce_backend.model.CartItem;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
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

        // Validar que la variante exista y pertenezca al producto
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
