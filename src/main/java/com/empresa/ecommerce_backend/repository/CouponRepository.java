// repository/CouponRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import com.empresa.ecommerce_backend.model.Coupon;

public interface CouponRepository extends BaseRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
    List<Coupon> findByActiveTrueAndStartAtBeforeAndEndAtAfter(LocalDateTime now1, LocalDateTime now2);
}
