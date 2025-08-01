// src/main/java/com/empresa/ecommerce_backend/exception/RecursoNoEncontradoException.java
package com.empresa.ecommerce_backend.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}