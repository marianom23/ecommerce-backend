// src/main/java/com/empresa/ecommerce_backend/dto/response/ServiceResult.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ServiceResult<T> {
    private String message;
    private T data;
    private HttpStatus status;

    /* --------- Fábricas de utilidad --------- */
    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(null, data, HttpStatus.OK);
    }
    public static <T> ServiceResult<T> created(T data) {
        return new ServiceResult<>(null, data, HttpStatus.CREATED);
    }
    public static ServiceResult<Void> noContent() {
        return new ServiceResult<>(null, null, HttpStatus.NO_CONTENT);
    }
    public static <T> ServiceResult<T> error(HttpStatus st, String msg) {
        return new ServiceResult<>(msg, null, st);
    }
    public static <T> ServiceResult<T> error(HttpStatus st, String msg, T data) {
        return new ServiceResult<>(msg, data, st);
    }
}
