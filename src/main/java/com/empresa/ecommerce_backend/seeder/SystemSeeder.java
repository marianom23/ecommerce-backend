package com.empresa.ecommerce_backend.seeder;

import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.model.Category;
import com.empresa.ecommerce_backend.model.Role;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.model.Brand;
import com.empresa.ecommerce_backend.repository.CategoryRepository;
import com.empresa.ecommerce_backend.repository.RoleRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.repository.BrandRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
// Lo podés ajustar según tus profiles: prod, dev, etc.
@Profile("prod")
public class SystemSeeder {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final PasswordEncoder passwordEncoder;

    // Podés sobreescribir estos valores en application-*.yml
    @Value("${app.admin.email:admin@admin.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @PostConstruct
    public void seedData() {
        seedRoles();
        seedAdmin();
        seedCategories();
        seedBrands();
    }

    /* =================== ROLES =================== */

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));
        }
    }

    /* =================== ADMIN INICIAL =================== */

    private void seedAdmin() {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found."));

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Principal");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setVerified(true);
        admin.setAuthProvider(AuthProvider.LOCAL);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
    }

    /* =================== CATEGORÍAS =================== */

    private void seedCategories() {
        createCategoryIfNotExists("Accesorios");
        createCategoryIfNotExists("Juegos");
        createCategoryIfNotExists("Celulares");
        createCategoryIfNotExists("Consolas");
        createCategoryIfNotExists("PC y Notebooks");
        createCategoryIfNotExists("Retro / Handhelds");
        createCategoryIfNotExists("Audio");
    }

    private void createCategoryIfNotExists(String name) {
        categoryRepository.findByName(name).orElseGet(() -> {
            Category cat = new Category();
            cat.setName(name);
            return categoryRepository.save(cat);
        });
    }

    /* =================== MARCAS =================== */

    private void seedBrands() {
        // Consolas grandes
        createBrandIfNotExists("Nintendo", null);
        createBrandIfNotExists("Sony PlayStation", null);
        createBrandIfNotExists("Microsoft Xbox", null);
        createBrandIfNotExists("Sega", null);
        createBrandIfNotExists("Atari", null);

        // Handhelds chinas / retro
        createBrandIfNotExists("Anbernic", null);
        createBrandIfNotExists("Retroid", null);
        createBrandIfNotExists("Powkiddy", null);
        createBrandIfNotExists("Miyoo", null);
        createBrandIfNotExists("AYN Odin", null);
        createBrandIfNotExists("Trimui", null);
        createBrandIfNotExists("GKD", null);

        // Tech general (si vendés celulares / electrónica)
        createBrandIfNotExists("Apple", null);
        createBrandIfNotExists("Samsung", null);
        createBrandIfNotExists("Xiaomi", null);
        createBrandIfNotExists("Motorola", null);

        // Una genérica
        createBrandIfNotExists("Genérica", null);
    }

    private void createBrandIfNotExists(String name, String logoUrl) {
        brandRepository.findByName(name).orElseGet(() ->
                brandRepository.save(new Brand(null, name, logoUrl, Set.of()))
        );
    }
}
