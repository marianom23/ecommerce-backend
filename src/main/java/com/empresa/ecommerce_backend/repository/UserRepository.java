// src/main/java/com/empresa/ecommerce_backend/repository/UserRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByOauthId(String oauthId);

    boolean existsByEmail(String email);

    // Para traer roles en la misma query (evitas LazyInitialization/N+1)
    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByEmail(String email);

    long countByRoles_Name(String roleName);

    // Búsqueda por nombre o email
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<User> searchUsers(@Param("query") String query, org.springframework.data.domain.Pageable pageable);
}

