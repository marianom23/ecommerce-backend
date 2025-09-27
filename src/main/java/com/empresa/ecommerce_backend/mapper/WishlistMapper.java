// src/main/java/com/empresa/ecommerce_backend/mapper/WishlistMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.dto.response.WishlistResponse;
import com.empresa.ecommerce_backend.model.Wishlist;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ProductMapper.class })
public abstract class WishlistMapper {

    @Autowired
    protected ProductMapper productMapper;

    public WishlistResponse toResponse(Wishlist w) {
        List<ProductResponse> products = w.getProducts().stream()
                .map(productMapper::toResponse) // 👈 reutiliza TU lógica existente
                .toList();

        return WishlistResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .products(products)
                .build();
    }
}
