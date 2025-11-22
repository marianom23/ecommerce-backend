// src/main/java/com/empresa/ecommerce_backend/mapper/BannerMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.BannerRequest;
import com.empresa.ecommerce_backend.dto.response.BannerResponse;
import com.empresa.ecommerce_backend.model.Banner;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BannerMapper {

    BannerResponse toResponse(Banner banner);

    List<BannerResponse> toResponseList(List<Banner> banners);

    @Mapping(target = "id", ignore = true)
    Banner toEntity(BannerRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(BannerRequest request, @MappingTarget Banner banner);
}
