package com.empresa.ecommerce_backend.seeder;

import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.model.Role;
import com.empresa.ecommerce_backend.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            createRoleIfNotExists(roleName);
        }
    }

    private void createRoleIfNotExists(RoleName name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(new Role(name));
        }
    }
}
