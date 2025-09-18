// dto/response/ProductFacetsResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ProductFacetsResponse {
    private List<CategoryFacetResponse> categoryFacets;
    private List<BrandFacetResponse> brandFacets;
    private PriceRangeResponse priceRange;
}
