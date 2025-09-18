// dto/response/CategoryFacetResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CategoryFacetResponse {
    private Long id;
    private String name;
    private Long count;
}
