// src/main/java/com/empresa/ecommerce_backend/advice/ServiceResultAdvice.java
package com.empresa.ecommerce_backend.advice;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ServiceResultAdvice implements ResponseBodyAdvice<Object> {

    /* ---- Sólo interceptamos métodos que devuelvan ServiceResult ---- */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return ServiceResult.class.isAssignableFrom(returnType.getParameterType());
    }

    /* ---- Éxitos: fija el código HTTP y deja el cuerpo tal cual ---- */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        ServiceResult<?> result = (ServiceResult<?>) body;
        response.setStatusCode(result.getStatus());
        return result;
    }

    /* ------------- ERRORES / EXCEPCIONES ------------- */

    /* 409 */
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ServiceResult<Void>> handleConflict(EmailDuplicadoException ex) {
        ServiceResult<Void> res = ServiceResult.error(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    /* 404 */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ServiceResult<Void>> handleNotFound(RecursoNoEncontradoException ex) {
        ServiceResult<Void> res = ServiceResult.error(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    /* 400 – validación Bean Validation (@Valid) */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ServiceResult<Void>> handleBadRequest(Exception ex) {
        ServiceResult<Void> res = ServiceResult.error(HttpStatus.BAD_REQUEST,
                "Datos de entrada inválidos");
        return ResponseEntity.badRequest().body(res);
    }

    /* 500 – cualquier otro RuntimeException */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ServiceResult<Void>> handleGeneric(Throwable ex) {
        // log.error("Error inesperado", ex);
        ServiceResult<Void> res = ServiceResult.error(
                HttpStatus.INTERNAL_SERVER_ERROR, "Error interno");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ServiceResult<Void>> handleEmailSending(EmailSendingException ex) {
        // Puedes usar 502 Bad Gateway si quieres indicar fallo de un “proveedor externo” (SMTP),
        // o 500 Internal Server Error. Aquí uso 502:
        ServiceResult<Void> res = ServiceResult.error(HttpStatus.BAD_GATEWAY, "No se pudo enviar el email de verificación");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(res);
    }


}
