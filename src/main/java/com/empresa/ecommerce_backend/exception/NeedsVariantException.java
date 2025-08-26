// src/main/java/com/empresa/ecommerce_backend/exception/NeedsVariantException.java
package com.empresa.ecommerce_backend.exception;

public class NeedsVariantException extends RuntimeException {
    private final Long productId;

    public NeedsVariantException(String message, Long productId) {
        super(message);
        this.productId = productId;
    }
    public Long getProductId() { return productId; }
}
