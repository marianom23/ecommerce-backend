// src/main/java/com/empresa/ecommerce_backend/exception/VariantRequiredException.java
package com.empresa.ecommerce_backend.exception;

public class VariantRequiredException extends BusinessConflictException {
  public VariantRequiredException(String message) {
    super("VARIANT_REQUIRED", message);
  }
}
