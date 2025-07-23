// src/main/java/com/empresa/ecommerce_backend/repository/PermissionRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Permission;

public interface PermissionRepository extends BaseRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = "roles")
    Optional<Permission> findWithRolesByName(String name);
}
