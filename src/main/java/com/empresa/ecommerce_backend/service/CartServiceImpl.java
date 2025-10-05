// src/main/java/com/empresa/ecommerce_backend/service/impl/CartServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.AddItemRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateQtyRequest;
import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
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

    /* =================== ATTACH (fija guest => user) =================== */
    @Override
    @Transactional
    public ServiceResult<CartResponse> attachCartToUser(String sessionId, Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId requerido");

        var userCartOpt    = cartRepository.lockByUserId(userId);
        var sessionCartOpt = (sessionId != null && !sessionId.isBlank())
                ? cartRepository.lockBySessionId(sessionId) : Optional.<Cart>empty();

        // 0) La cookie ya apunta a un carrito del MISMO usuario → no rotar; asegurar sessionId si falta
        if (sessionCartOpt.isPresent()
                && sessionCartOpt.get().getUser() != null
                && sessionCartOpt.get().getUser().getId().equals(userId)) {
            Cart same = sessionCartOpt.get();
            if (same.getSessionId() == null || same.getSessionId().isBlank()) {
                same.setSessionId(UUID.randomUUID().toString());
                same.setUpdatedAt(LocalDateTime.now());
                same = cartRepository.save(same);
                return ServiceResult.created(cartMapper.toResponse(same));
            }
            return ServiceResult.ok(cartMapper.toResponse(same));
        }

        // A) Cookie → carrito de OTRO usuario ⇒ ignorar y usar/crear el del usuario actual
        if (sessionCartOpt.isPresent()
                && sessionCartOpt.get().getUser() != null
                && !sessionCartOpt.get().getUser().getId().equals(userId)) {

            Cart myCart = userCartOpt.orElseGet(() -> {
                Cart c = new Cart();
                User u = new User(); u.setId(userId);
                c.setUser(u);
                c.setItems(new HashSet<>());
                c.setSessionId(UUID.randomUUID().toString());
                c.setUpdatedAt(LocalDateTime.now());
                return cartRepository.save(c);
            });

            // No rotamos si ya tiene sessionId (la cookie cambia igual porque el valor es distinto al de la cookie actual del browser)
            if (myCart.getSessionId() == null || myCart.getSessionId().isBlank()) {
                myCart.setSessionId(UUID.randomUUID().toString());
                myCart.setUpdatedAt(LocalDateTime.now());
                myCart = cartRepository.save(myCart);
                return ServiceResult.created(cartMapper.toResponse(myCart));
            }
            return ServiceResult.ok(cartMapper.toResponse(myCart));
        }

        // B) No hay nada → crear carrito del usuario (cookie nueva)
        if (userCartOpt.isEmpty() && sessionCartOpt.isEmpty()) {
            Cart c = new Cart();
            User u = new User(); u.setId(userId);
            c.setUser(u);
            c.setItems(new HashSet<>());
            c.setSessionId(UUID.randomUUID().toString());
            c.setUpdatedAt(LocalDateTime.now());
            Cart saved = cartRepository.save(c);
            return ServiceResult.created(cartMapper.toResponse(saved));
        }

        // C) Solo carrito de usuario → devolver y asegurar sessionId (sin rotar si ya existe)
        if (userCartOpt.isPresent() && sessionCartOpt.isEmpty()) {
            Cart userCart = userCartOpt.get();
            if (userCart.getSessionId() == null || userCart.getSessionId().isBlank()) {
                userCart.setSessionId(UUID.randomUUID().toString());
                userCart.setUpdatedAt(LocalDateTime.now());
                userCart = cartRepository.save(userCart);
                return ServiceResult.created(cartMapper.toResponse(userCart));
            }
            return ServiceResult.ok(cartMapper.toResponse(userCart));
        }

        // D) Solo carrito de sesión
        if (userCartOpt.isEmpty() && sessionCartOpt.isPresent()) {
            Cart sessionCart = sessionCartOpt.get();

            if (sessionCart.getUser() == null) {
                // ► ADOPCIÓN: fijamos el guest al usuario y (solo aquí) rotamos sessionId
                User u = new User(); u.setId(userId);
                sessionCart.setUser(u);
                sessionCart.setSessionId(UUID.randomUUID().toString());
                sessionCart.setUpdatedAt(LocalDateTime.now());
                Cart saved = cartRepository.save(sessionCart);
                return ServiceResult.ok(cartMapper.toResponse(saved));
            } else {
                // Ya es del usuario (defensivo)
                if (sessionCart.getUser().getId().equals(userId)) {
                    if (sessionCart.getSessionId() == null || sessionCart.getSessionId().isBlank()) {
                        sessionCart.setSessionId(UUID.randomUUID().toString());
                        sessionCart.setUpdatedAt(LocalDateTime.now());
                        sessionCart = cartRepository.save(sessionCart);
                        return ServiceResult.created(cartMapper.toResponse(sessionCart));
                    }
                    return ServiceResult.ok(cartMapper.toResponse(sessionCart));
                }
                // De otro owner (ya cubierto arriba), creamos uno propio por claridad
                Cart c = new Cart();
                User u = new User(); u.setId(userId);
                c.setUser(u);
                c.setItems(new HashSet<>());
                c.setSessionId(UUID.randomUUID().toString());
                c.setUpdatedAt(LocalDateTime.now());
                Cart saved = cartRepository.save(c);
                return ServiceResult.created(cartMapper.toResponse(saved));
            }
        }

        // E) Existen ambos (user + session)
        Cart userCart = userCartOpt.get();
        Cart sessionCart = sessionCartOpt.get();

        if (userCart.getId().equals(sessionCart.getId())) {
            if (userCart.getSessionId() == null || userCart.getSessionId().isBlank()) {
                userCart.setSessionId(UUID.randomUUID().toString());
                userCart.setUpdatedAt(LocalDateTime.now());
                userCart = cartRepository.save(userCart);
                return ServiceResult.created(cartMapper.toResponse(userCart));
            }
            return ServiceResult.ok(cartMapper.toResponse(userCart));
        }

        // session guest o mismo user → merge en userCart (sin rotar si ya hay sessionId)
        if (sessionCart.getUser() == null || sessionCart.getUser().getId().equals(userId)) {
            for (CartItem si : sessionCart.getItems()) {
                var variant = si.getVariant();
                if (variant == null) continue;

                var existingOpt = cartItemRepository.findByCartAndProductAndVariant(userCart, si.getProduct(), variant);
                int stock = safeStock(variant.getStock());
                int baseQty = existingOpt.map(CartItem::getQuantity).orElse(0);
                int mergedQty = Math.min(baseQty + si.getQuantity(), stock);

                if (existingOpt.isEmpty()) {
                    if (mergedQty <= 0) continue;
                    CartItem ni = new CartItem();
                    ni.setCart(userCart);
                    ni.setProduct(si.getProduct());
                    ni.setVariant(variant);
                    ni.setQuantity(mergedQty);
                    ni.setPriceAtAddition(si.getPriceAtAddition());
                    ni.setDiscountedPriceAtAddition(si.getDiscountedPriceAtAddition());
                    userCart.getItems().add(ni);
                } else {
                    if (mergedQty == 0) {
                        cartItemRepository.delete(existingOpt.get());
                        userCart.getItems().remove(existingOpt.get());
                    } else {
                        existingOpt.get().setQuantity(mergedQty);
                    }
                }
            }
            cartItemRepository.deleteAll(sessionCart.getItems());
            sessionCart.getItems().clear();
            cartRepository.delete(sessionCart);

            if (userCart.getSessionId() == null || userCart.getSessionId().isBlank()) {
                userCart.setSessionId(UUID.randomUUID().toString());
                userCart.setUpdatedAt(LocalDateTime.now());
                userCart = cartRepository.save(userCart);
                return ServiceResult.created(cartMapper.toResponse(userCart));
            }
            userCart.setUpdatedAt(LocalDateTime.now());
            Cart saved = cartRepository.save(userCart);
            return ServiceResult.ok(cartMapper.toResponse(saved));
        }

        // De otro user (defensivo) → devolver el del user (sin rotar si ya tiene)
        if (userCart.getSessionId() == null || userCart.getSessionId().isBlank()) {
            userCart.setSessionId(UUID.randomUUID().toString());
            userCart.setUpdatedAt(LocalDateTime.now());
            userCart = cartRepository.save(userCart);
            return ServiceResult.created(cartMapper.toResponse(userCart));
        }
        return ServiceResult.ok(cartMapper.toResponse(userCart));
    }

    /* =================== USER-FIRST OPS =================== */

    @Override
    @Transactional
    public ServiceResult<CartResponse> getOrCreate(Long userId, String sessionId) {
        // 1) Usuario autenticado → por userId
        if (userId != null) {
            var existing = cartRepository.findByUserId(userId);
            if (existing.isPresent()) {
                return ServiceResult.ok(cartMapper.toResponse(existing.get()));
            }
            // Crear nuevo para el user (independiente de cookie)
            Cart c = new Cart();
            User u = new User(); u.setId(userId);
            c.setUser(u);
            c.setItems(new HashSet<>());
            c.setSessionId(UUID.randomUUID().toString());
            c.setUpdatedAt(LocalDateTime.now());
            Cart saved = cartRepository.save(c);
            return ServiceResult.created(cartMapper.toResponse(saved)); // 201 → rota cookie
        }

        // 2) Guest → por sessionId, pero si apunta a carrito de usuario, crear guest nuevo
        if (sessionId != null && !sessionId.isBlank()) {
            var existing = cartRepository.findBySessionId(sessionId);
            if (existing.isPresent()) {
                Cart found = existing.get();
                if (found.getUser() != null) {
                    // cookie apunta a carrito “propiedad de usuario” → crear guest nuevo
                    Cart c = new Cart();
                    c.setSessionId(UUID.randomUUID().toString());
                    c.setItems(new HashSet<>());
                    c.setUpdatedAt(LocalDateTime.now());
                    Cart saved = cartRepository.save(c);
                    return ServiceResult.created(cartMapper.toResponse(saved)); // 201 → rota cookie
                }
                return ServiceResult.ok(cartMapper.toResponse(found));
            }
            // no existe → crear guest con nueva cookie
            Cart c = new Cart();
            c.setSessionId(UUID.randomUUID().toString());
            c.setItems(new HashSet<>());
            c.setUpdatedAt(LocalDateTime.now());
            Cart saved = cartRepository.save(c);
            return ServiceResult.created(cartMapper.toResponse(saved)); // 201 → rota cookie
        }

        // 3) Guest sin cookie → crear guest
        Cart c = new Cart();
        c.setSessionId(UUID.randomUUID().toString());
        c.setItems(new HashSet<>());
        c.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(c);
        return ServiceResult.created(cartMapper.toResponse(saved)); // 201 → rota cookie
    }


    @Override
    @Transactional
    public ServiceResult<CartResponse> addItem(Long userId, String sessionId, AddItemRequest dto) {
        // ⛔ guest con cookie que apunta a carrito de usuario → prohibir
        var guard = forbidIfGuestTouchesUserCart(userId, sessionId);
        if (guard != null) return guard;

        Cart cart = resolveCart(userId, sessionId, true);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        if (dto.getVariantId() == null) {
            throw new NeedsVariantException("Este producto requiere que selecciones una variante", product.getId());
        }

        ProductVariant variant = variantRepository
                .findByIdAndProductId(dto.getVariantId(), product.getId())
                .orElseThrow(() -> new EntityNotFoundException("La variante indicada no existe para este producto"));

        BigDecimal listPrice = variant.getPrice();
        BigDecimal discounted = listPrice;

        Optional<CartItem> existing = cartItemRepository.findByCartAndProductAndVariant(cart, product, variant);
        CartItem item = existing.orElse(null);

        int newQty = (item != null ? item.getQuantity() : 0) + dto.getQuantity();
        int stock = safeStock(variant.getStock());
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
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQty);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<CartResponse> updateQuantity(Long userId, String sessionId, Long itemId, UpdateQtyRequest dto) {
        var guard = forbidIfGuestTouchesUserCart(userId, sessionId);
        if (guard != null) return guard;

        Cart cart = resolveCart(userId, sessionId, false);

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        if (dto.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            ProductVariant v = item.getVariant();
            if (v == null) {
                return ServiceResult.error(HttpStatus.CONFLICT, "Ítem inválido: falta la variante");
            }
            int stock = safeStock(v.getStock());
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
    public ServiceResult<CartResponse> incrementItem(Long userId, String sessionId, Long itemId) {
        var guard = forbidIfGuestTouchesUserCart(userId, sessionId);
        if (guard != null) return guard;

        Cart cart = resolveCart(userId, sessionId, false);

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        ProductVariant v = item.getVariant();
        if (v == null) {
            return ServiceResult.error(HttpStatus.CONFLICT, "Ítem inválido: falta la variante");
        }

        int newQty = item.getQuantity() + 1;
        int stock = safeStock(v.getStock());
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
    public ServiceResult<CartResponse> decrementItem(Long userId, String sessionId, Long itemId) {
        var guard = forbidIfGuestTouchesUserCart(userId, sessionId);
        if (guard != null) return guard;

        Cart cart = resolveCart(userId, sessionId, false);

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        int newQty = Math.max(1, item.getQuantity() - 1);
        item.setQuantity(newQty);

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<CartResponse> removeItem(Long userId, String sessionId, Long itemId) {
        var guard = forbidIfGuestTouchesUserCart(userId, sessionId);
        if (guard != null) return guard;

        Cart cart = resolveCart(userId, sessionId, false);

        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Ítem no encontrado en el carrito"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<CartResponse> clear(Long userId, String sessionId) {
        var guard = forbidIfGuestTouchesUserCart(userId, sessionId);
        if (guard != null) return guard;

        Cart cart = resolveCart(userId, sessionId, false);

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return ServiceResult.ok(cartMapper.toResponse(saved));
    }


    /* =================== HELPER: resolver por user o cookie =================== */
    private Cart resolveCart(Long userId, String sessionId, boolean createIfMissing) {
        if (userId != null) {
            // autenticado → por usuario
            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        if (!createIfMissing) throw new EntityNotFoundException("Carrito no encontrado");
                        Cart c = new Cart();
                        User u = new User(); u.setId(userId);
                        c.setUser(u);
                        c.setItems(new HashSet<>());
                        c.setSessionId(UUID.randomUUID().toString());
                        c.setUpdatedAt(LocalDateTime.now());
                        return cartRepository.save(c);
                    });
        }

        // guest
        if (sessionId != null && !sessionId.isBlank()) {
            var existing = cartRepository.findBySessionId(sessionId);
            if (existing.isPresent()) {
                Cart found = existing.get();
                if (found.getUser() != null) {
                    // cookie apunta a carrito de usuario
                    if (!createIfMissing) {
                        // mutaciones guest → NO tocar carritos de usuario
                        throw new EntityNotFoundException("Carrito no encontrado");
                    }
                    // operaciones tipo GET/add → crear guest nuevo
                    Cart c = new Cart();
                    c.setSessionId(UUID.randomUUID().toString());
                    c.setItems(new HashSet<>());
                    c.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(c);
                }
                return found;
            }
            if (!createIfMissing) throw new EntityNotFoundException("Carrito no encontrado");
            Cart c = new Cart();
            c.setSessionId(UUID.randomUUID().toString());
            c.setItems(new HashSet<>());
            c.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(c);
        }

        if (!createIfMissing) throw new EntityNotFoundException("Carrito no encontrado");

        Cart c = new Cart();
        c.setSessionId(UUID.randomUUID().toString());
        c.setItems(new HashSet<>());
        c.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userHasCart(Long userId) {
        if (userId == null) return false;
        return cartRepository.findByUserId(userId).isPresent();
    }

    private ServiceResult<CartResponse> forbidIfGuestTouchesUserCart(Long userId, String sessionId) {
        if (userId == null && sessionId != null && !sessionId.isBlank()) {
            var existing = cartRepository.findBySessionId(sessionId);
            if (existing.isPresent() && existing.get().getUser() != null) {
                return ServiceResult.error(HttpStatus.FORBIDDEN,
                        "La cookie apunta a un carrito de usuario. Iniciá sesión para operar ese carrito.");
            }
        }
        return null; // OK
    }

    private int safeStock(Integer stock) {
        return stock == null ? 0 : stock;
    }
}
