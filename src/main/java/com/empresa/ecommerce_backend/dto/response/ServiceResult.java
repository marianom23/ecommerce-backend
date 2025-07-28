// src/main/java/com/empresa/ecommerce_backend/dto/response/ServiceResult.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceResult<T> {
    private boolean success;
    private String message;
    private T data;

    // ✅ Método estático para respuesta exitosa
    public static <T> ServiceResult<T> success(T data) {
        return new ServiceResult<>(true, null, data);
    }

    // ✅ Método estático para respuesta con error
    public static <T> ServiceResult<T> failure(String message) {
        return new ServiceResult<>(false, message, null);
    }
}
