// repository/BannerRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.empresa.ecommerce_backend.model.Banner;

public interface BannerRepository extends BaseRepository<Banner, Long> {
    List<Banner> findByActiveTrueOrderByPositionAsc();
    List<Banner> findByStartAtBeforeAndEndAtAfter(LocalDateTime now1, LocalDateTime now2);
}
