// src/main/java/com/empresa/ecommerce_backend/service/interfaces/BillingProfileService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.BillingProfileRequest;
import com.empresa.ecommerce_backend.dto.response.BillingProfileResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface BillingProfileService {
    ServiceResult<List<BillingProfileResponse>> listForCurrentUser();
    ServiceResult<BillingProfileResponse> createForCurrentUser(BillingProfileRequest dto);
    ServiceResult<BillingProfileResponse> updateForCurrentUser(Long id, BillingProfileRequest dto);
    ServiceResult<Void> deleteForCurrentUser(Long id);
    ServiceResult<BillingProfileResponse> setDefault(Long id); // opcional: marcar default explícito
}
