package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPaginatedRequest {

    @Builder.Default
    @Min(1)
    private int page = 1;

    @Builder.Default
    @Min(1)
    @Max(100)
    private int limit = 12;

    private Boolean inStockOnly = true;

    @Builder.Default
    private String sort = "latest";

    private String q;
}