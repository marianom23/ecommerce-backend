// src/main/java/com/empresa/ecommerce_backend/exception/BusinessConflictException.java
package com.empresa.ecommerce_backend.exception;

public class BusinessConflictException extends RuntimeException {
  private final String code;
  public BusinessConflictException(String code, String message) {
    super(message);
    this.code = code;
  }
  public String getCode() { return code; }
}
