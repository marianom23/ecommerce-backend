// dto/response/BrandFacetResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class BrandFacetResponse {
    private Long id;
    private String name;
    private Long count;
}
