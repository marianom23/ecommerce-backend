// src/main/java/com/empresa/ecommerce_backend/service/interfaces/BannerService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.BannerRequest;
import com.empresa.ecommerce_backend.dto.response.BannerResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.BannerPlacement;

import java.util.List;

public interface BannerService {

    ServiceResult<List<BannerResponse>> getActiveBanners(BannerPlacement placement);

    ServiceResult<BannerResponse> create(BannerRequest request);

    ServiceResult<BannerResponse> update(Long id, BannerRequest request);

    ServiceResult<Void> delete(Long id);

    ServiceResult<BannerResponse> getById(Long id);
}
