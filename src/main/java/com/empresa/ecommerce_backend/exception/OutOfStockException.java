// src/main/java/com/empresa/ecommerce_backend/exception/OutOfStockException.java
package com.empresa.ecommerce_backend.exception;

public class OutOfStockException extends BusinessConflictException {
  public OutOfStockException(String message) {
    super("OUT_OF_STOCK", message);
  }
}
