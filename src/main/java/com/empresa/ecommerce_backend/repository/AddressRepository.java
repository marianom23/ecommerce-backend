// AddressRepository.java
package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.enums.AddressType;
import com.empresa.ecommerce_backend.model.Address;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("""
        select a from Address a
        where a.user.id = :userId
          and (:type is null or a.type = :type)
        order by
          case when a.lastUsedAt is null then 1 else 0 end asc,
          a.lastUsedAt desc nulls last,
          a.updatedAt desc nulls last,
          a.createdAt desc
        """)
    List<Address> findOrderedForUser(@Param("userId") Long userId, @Param("type") AddressType type);

    @Query("select a from Address a where a.id = :id and a.user.id = :userId")
    Optional<Address> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
