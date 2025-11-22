// src/main/java/com/empresa/ecommerce_backend/service/BannerServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.BannerRequest;
import com.empresa.ecommerce_backend.dto.response.BannerResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.mapper.BannerMapper;
import com.empresa.ecommerce_backend.model.Banner;
import com.empresa.ecommerce_backend.enums.BannerPlacement;
import com.empresa.ecommerce_backend.repository.BannerRepository;
import com.empresa.ecommerce_backend.service.interfaces.BannerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<BannerResponse>> getActiveBanners(BannerPlacement placement) {

        List<Banner> banners = (placement == null)
                ? bannerRepository.findByActiveTrueOrderByPlacementAscSortOrderAsc()
                : bannerRepository.findByPlacementAndActiveTrueOrderBySortOrderAsc(placement);

        return ServiceResult.ok(bannerMapper.toResponseList(banners));
    }

    @Override
    @Transactional
    public ServiceResult<BannerResponse> create(BannerRequest request) {
        Banner banner = bannerMapper.toEntity(request);

        // Si active viene null, dejar true por defecto
        if (request.getActive() != null) {
            banner.setActive(request.getActive());
        }

        Banner saved = bannerRepository.save(banner);
        return ServiceResult.created(bannerMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<BannerResponse> update(Long id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Banner no encontrado"));

        bannerMapper.updateFromRequest(request, banner);

        if (request.getActive() != null) {
            banner.setActive(request.getActive());
        }

        Banner saved = bannerRepository.save(banner);
        return ServiceResult.ok(bannerMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> delete(Long id) {
        if (!bannerRepository.existsById(id)) {
            return ServiceResult.error(HttpStatus.NOT_FOUND, "Banner no encontrado");
        }
        bannerRepository.deleteById(id);
        return ServiceResult.ok(null);
    }
}
