package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.Supplier;

import java.util.Optional;

public interface SupplierRepository extends BaseRepository<Supplier, Long> {

    Optional<Supplier> findByName(String name);

    boolean existsByName(String name);
}
