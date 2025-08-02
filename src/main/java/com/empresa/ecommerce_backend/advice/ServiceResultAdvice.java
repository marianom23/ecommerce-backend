// src/main/java/com/empresa/ecommerce_backend/advice/ServiceResultAdvice.java
package com.empresa.ecommerce_backend.advice;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.EmailSendingException;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ServiceResultAdvice implements ResponseBodyAdvice<Object> {

    /* ---- Interceptamos SIEMPRE y decidimos en runtime ---- */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /* ---- Éxitos: si el body es ServiceResult, fijamos el status HTTP ---- */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body instanceof ServiceResult<?> sr) {
            // Respeta el status que trae el ServiceResult (ok, created, etc.)
            if (sr.getStatus() != null) {
                response.setStatusCode(sr.getStatus());
            }
            return sr;
        }
        // Si no es ServiceResult, lo dejamos pasar (ej. endpoints de Swagger)
        return body;
    }

    /* ---------------------- HANDLERS DE EXCEPCIÓN ---------------------- */

    /* 404 */
    @ExceptionHandler({EntityNotFoundException.class, RecursoNoEncontradoException.class})
    public ResponseEntity<ServiceResult<Void>> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /* 409 - conflicto (duplicados/índices únicos) */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ServiceResult<Void>> handleConflict(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "Conflicto de datos (duplicado o restricción).");
    }

    /* 400 - validaciones de Bean Validation (@Valid en body o params) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceResult<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (msg.isBlank()) msg = "Datos de entrada inválidos.";
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ServiceResult<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        if (msg.isBlank()) msg = "Datos de entrada inválidos.";
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ServiceResult<Void>> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage() != null ? ex.getMessage() : "Solicitud inválida.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ServiceResult<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "JSON mal formado o tipos de datos inválidos.");
    }

    /* 401 - autenticación */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ServiceResult<Void>> handleAuth(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "No autenticado o token inválido.");
    }

    /* 403 - autorización */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ServiceResult<Void>> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "No tenés permisos para esta operación.");
    }

    /* 404 - no handler (si tenés habilitado throw-exception-if-no-handler-found) */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ServiceResult<Void>> handleNoHandler(NoHandlerFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Endpoint no encontrado.");
    }

    /* 502 - email falló (proveedor externo) */
    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ServiceResult<Void>> handleEmailSending(EmailSendingException ex) {
        return build(HttpStatus.BAD_GATEWAY, "No se pudo enviar el email de verificación.");
    }

    /* 500 - catch-all */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ServiceResult<Void>> handleGeneric(Throwable ex) {
        // log.error("Error inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno.");
    }

    /* ---------------------- Helper ---------------------- */
    private ResponseEntity<ServiceResult<Void>> build(HttpStatus status, String message) {
        ServiceResult<Void> res = ServiceResult.error(status, message);
        return ResponseEntity.status(status).body(res);
    }
}
