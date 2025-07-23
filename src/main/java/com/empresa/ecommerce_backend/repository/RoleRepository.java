// src/main/java/com/empresa/ecommerce_backend/repository/RoleRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.model.Role;

public interface RoleRepository extends BaseRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findWithPermissionsByName(RoleName name);
}
