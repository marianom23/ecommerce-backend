package com.empresa.ecommerce_backend.dto.response;

import com.empresa.ecommerce_backend.enums.AttributeScope;
import com.empresa.ecommerce_backend.enums.ProductType;
import lombok.Data;

import java.util.List;

@Data
public class AttributeTemplateResponse {
    private Long id;
    private String name;
    private AttributeScope scope;
    private ProductType productType;
    private Long categoryId;
    private List<AttributeValueResponse> values;

    @Data
    public static class AttributeValueResponse {
        private Long id;
        private String label;
        private String value;
    }
}
