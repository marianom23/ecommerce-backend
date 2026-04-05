package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ConsoleFacetResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private Long count;
}
