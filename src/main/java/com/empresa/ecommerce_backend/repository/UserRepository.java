// src/main/java/com/empresa/ecommerce_backend/repository/UserRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.User;

public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Para traer roles en la misma query (evitas LazyInitialization/N+1)
    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByEmail(String email);
}

