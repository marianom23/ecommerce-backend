// src/main/java/com/empresa/ecommerce_backend/repository/AdminUserRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.AdminUser;

public interface AdminUserRepository extends BaseRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<AdminUser> findWithRolesByUsername(String username);
}
