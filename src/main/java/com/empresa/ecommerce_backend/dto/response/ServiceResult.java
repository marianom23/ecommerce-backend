// src/main/java/com/empresa/ecommerce_backend/dto/response/ServiceResult.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@AllArgsConstructor
public class ServiceResult<T> {
    private boolean success;
    private String message;
    private T data;
}
