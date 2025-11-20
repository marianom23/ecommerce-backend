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

    // 👇 NUEVO: helper para saber si fue exitoso
    public boolean isSuccess() {
        return status != null && status.is2xxSuccessful();
    }

    // 👇 NUEVO: alias para data, para que el código que usa getBody() compile
    public T getBody() {
        return data;
    }
}
