package com.empresa.ecommerce_backend.dto.request;

import com.empresa.ecommerce_backend.enums.AttributeScope;
import com.empresa.ecommerce_backend.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AttributeTemplateRequest {
    @NotBlank
    private String name;
    
    @NotNull
    private AttributeScope scope;
    
    private ProductType productType;
    private Long categoryId;
    
    private List<AttributeValueRequest> values;

    @Data
    public static class AttributeValueRequest {
        private Long id; // Para updates
        @NotBlank
        private String label;
        @NotBlank
        private String value;
    }
}
