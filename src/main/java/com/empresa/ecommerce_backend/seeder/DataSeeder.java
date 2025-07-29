package com.empresa.ecommerce_backend.seeder;

import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupplierRepository supplierRepository;

    @PostConstruct
    public void seedData() {
        seedRoles();
        seedAdmin();
        seedBrands();
        seedCategories();
        seedSuppliers();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    private void seedAdmin() {
        String adminEmail = "admin@admin.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found."));

            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("Root");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setVerified(true);
            admin.setAuthProvider(AuthProvider.LOCAL);
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
        }
    }

    private void seedBrands() {
        createBrandIfNotExists("Genérica", "https://example.com/logo-generica.png");
        createBrandIfNotExists("Nike", "https://example.com/logo-nike.png");
        createBrandIfNotExists("Adidas", "https://example.com/logo-adidas.png");
        createBrandIfNotExists("Samsung", "https://example.com/logo-samsung.png");
        createBrandIfNotExists("Apple", "https://example.com/logo-apple.png");
    }

    private void createBrandIfNotExists(String name, String logoUrl) {
        brandRepository.findByName(name).orElseGet(() ->
                brandRepository.save(new Brand(null, name, logoUrl, Set.of()))
        );
    }

    private void seedCategories() {
        createCategoryIfNotExists("Electrónica");
        createCategoryIfNotExists("Ropa");
        createCategoryIfNotExists("Calzado");
        createCategoryIfNotExists("Hogar");
        createCategoryIfNotExists("Accesorios");
    }

    private void createCategoryIfNotExists(String name) {
        categoryRepository.findByName(name).orElseGet(() -> {
            Category cat = new Category();
            cat.setName(name);
            return categoryRepository.save(cat);
        });
    }

    private void seedSuppliers() {
        createSupplierIfNotExists("Distribuidora ABC", "ventas@abc.com", "123456789", "Av. Siempre Viva 123");
        createSupplierIfNotExists("Proveedor XYZ", null, null, null);
    }

    private void createSupplierIfNotExists(String name, String email, String phone, String address) {
        supplierRepository.findByName(name).orElseGet(() -> {
            Supplier supplier = new Supplier();
            supplier.setName(name);
            supplier.setEmail(email);
            supplier.setPhone(phone);
            supplier.setAddress(address);
            return supplierRepository.save(supplier);
        });
    }

}
