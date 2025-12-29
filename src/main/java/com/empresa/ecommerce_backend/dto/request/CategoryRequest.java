package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank
    private String name;
    
    private String imageUrl;
    private Long parentId;
}
