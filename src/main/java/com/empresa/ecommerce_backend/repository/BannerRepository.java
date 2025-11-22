// src/main/java/com/empresa/ecommerce_backend/repository/BannerRepository.java
package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.Banner;
import com.empresa.ecommerce_backend.enums.BannerPlacement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByPlacementAndActiveTrueOrderBySortOrderAsc(BannerPlacement placement);

    List<Banner> findByActiveTrueOrderByPlacementAscSortOrderAsc();
}
