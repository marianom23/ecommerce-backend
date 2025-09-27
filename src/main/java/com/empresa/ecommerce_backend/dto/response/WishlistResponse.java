// src/main/java/com/empresa/ecommerce_backend/dto/response/wishlist/WishlistResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder @AllArgsConstructor
public class WishlistResponse {
    private final Long id;
    private final String name;
    private final List<ProductResponse> products;
}