// src/main/java/com/empresa/ecommerce_backend/dto/request/wishlist/AddProductToWishlistRequest.java
package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AddProductToWishlistRequest {
    @NotNull
    private Long productId;
}
