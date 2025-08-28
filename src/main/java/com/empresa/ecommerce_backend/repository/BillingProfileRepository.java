// src/main/java/com/empresa/ecommerce_backend/repository/BillingProfileRepository.java
package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.BillingProfile;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillingProfileRepository extends JpaRepository<BillingProfile, Long> {

    @Query("select bp from BillingProfile bp where bp.user.id = :uid order by bp.defaultProfile desc, bp.updatedAt desc nulls last, bp.createdAt desc")
    List<BillingProfile> findAllForUser(@Param("uid") Long userId);

    @Query("select bp from BillingProfile bp where bp.id = :id and bp.user.id = :uid")
    Optional<BillingProfile> findByIdAndUserId(@Param("id") Long id, @Param("uid") Long userId);

    @Modifying
    @Query("update BillingProfile bp set bp.defaultProfile = false where bp.user.id = :uid")
    void clearDefaultForUser(@Param("uid") Long userId);
}
