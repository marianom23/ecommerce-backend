// src/main/java/com/empresa/ecommerce_backend/repository/AddressRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.enums.AddressType;
import com.empresa.ecommerce_backend.model.Address;

public interface AddressRepository extends BaseRepository<Address, Long> {

    List<Address> findByUser_Id(Long userId);

    Optional<Address> findByIdAndUser_Id(Long id, Long userId);

    List<Address> findByUser_IdAndType(Long userId, AddressType type);

    boolean existsByIdAndUser_Id(Long id, Long userId);
}
