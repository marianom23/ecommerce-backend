// src/main/java/com/empresa/ecommerce_backend/service/impl/WishlistServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.WishlistResponse;
import com.empresa.ecommerce_backend.mapper.WishlistMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.model.Wishlist;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.WishlistRepository;
import com.empresa.ecommerce_backend.service.interfaces.WishlistService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final WishlistMapper wishlistMapper;

    /* ============ GET/CREATE única por usuario ============ */
    @Override
    @Transactional
    public ServiceResult<WishlistResponse> getOrCreateForUser(Long userId) {
        if (userId == null) {
            return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        Wishlist w = wishlistRepository.findByUserId(userId).orElseGet(() -> {
            Wishlist nw = new Wishlist();
            User u = new User(); u.setId(userId);
            nw.setUser(u);
            nw.setName("Mi lista"); // fijo/simple, no renombrable
            return wishlistRepository.save(nw);
        });

        return ServiceResult.ok(wishlistMapper.toResponse(w));
    }

    /* ============ ADD (sin cantidades) ============ */
    @Override
    @Transactional
    public ServiceResult<WishlistResponse> addProduct(Long userId, Long productId) {
        Wishlist w = wishlistRepository.lockByUserId(userId)
                .orElseGet(() -> {
                    Wishlist nw = new Wishlist();
                    User u = new User(); u.setId(userId);
                    nw.setUser(u);
                    nw.setName("Mi lista");
                    return wishlistRepository.save(nw);
                });

        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        w.getProducts().add(p);
        Wishlist saved = wishlistRepository.save(w);
        return ServiceResult.ok(wishlistMapper.toResponse(saved));
    }

    /* ============ REMOVE ============ */
    @Override
    @Transactional
    public ServiceResult<WishlistResponse> removeProduct(Long userId, Long productId) {
        Wishlist w = wishlistRepository.lockByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist no encontrada"));

        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        w.getProducts().remove(p);
        Wishlist saved = wishlistRepository.save(w);
        return ServiceResult.ok(wishlistMapper.toResponse(saved));
    }

    /* ============ TOGGLE ============ */
    @Override
    @Transactional
    public ServiceResult<WishlistResponse> toggleProduct(Long userId, Long productId) {
        Wishlist w = wishlistRepository.lockByUserId(userId)
                .orElseGet(() -> {
                    Wishlist nw = new Wishlist();
                    User u = new User(); u.setId(userId);
                    nw.setUser(u);
                    nw.setName("Mi lista");
                    return wishlistRepository.save(nw);
                });

        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        if (w.getProducts().contains(p)) {
            w.getProducts().remove(p);
        } else {
            w.getProducts().add(p);
        }

        Wishlist saved = wishlistRepository.save(w);
        return ServiceResult.ok(wishlistMapper.toResponse(saved));
    }

    /* ============ CLEAR (opcional) ============ */
    @Override
    @Transactional
    public ServiceResult<WishlistResponse> clear(Long userId) {
        Wishlist w = wishlistRepository.lockByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist no encontrada"));

        w.getProducts().clear();
        Wishlist saved = wishlistRepository.save(w);
        return ServiceResult.ok(wishlistMapper.toResponse(saved));
    }
}
