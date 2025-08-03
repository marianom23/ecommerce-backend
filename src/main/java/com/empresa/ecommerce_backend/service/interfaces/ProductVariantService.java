package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.ProductVariantRequest;
import com.empresa.ecommerce_backend.dto.response.ProductVariantResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface ProductVariantService {
    ServiceResult<List<ProductVariantResponse>> listByProduct(Long productId);
    ServiceResult<ProductVariantResponse> getOne(Long productId, Long variantId);
    ServiceResult<ProductVariantResponse> create(Long productId, ProductVariantRequest req);
    ServiceResult<ProductVariantResponse> update(Long productId, Long variantId, ProductVariantRequest req);
    ServiceResult<Void> delete(Long productId, Long variantId);
}