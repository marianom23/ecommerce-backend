package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.AttributeTemplateRequest;
import com.empresa.ecommerce_backend.dto.response.AttributeTemplateResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AttributeScope;
import com.empresa.ecommerce_backend.enums.ProductType;

import java.util.List;

public interface AttributeTemplateService {
    ServiceResult<AttributeTemplateResponse> createTemplate(AttributeTemplateRequest request);
    ServiceResult<AttributeTemplateResponse> updateTemplate(Long id, AttributeTemplateRequest request);
    ServiceResult<Void> deleteTemplate(Long id);
    ServiceResult<List<AttributeTemplateResponse>> getAllTemplates();
    ServiceResult<List<AttributeTemplateResponse>> getApplicableTemplates(AttributeScope scope, ProductType type, Long categoryId);
}
