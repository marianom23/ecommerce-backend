package com.empresa.ecommerce_backend.seeder;

import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedData() {
        seedRoles();
        seedAdmin();
        seedBrands();
        seedCategories();
        seedSuppliers();
        seedProductsAndVariants(); // 👈 ahora crea productos + variantes
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

    /**
     * Crea 50 productos SIN price/stock y genera variantes:
     * - Siempre crea una variante DEFAULT con price y stock.
     * - Para la mitad de los productos, agrega una 2da variante para probar el modal del front.
     */
    private void seedProductsAndVariants() {
        if (productRepository.count() > 0) return;

        // categ/brand de ejemplo (ajustá si querés algo más aleatorio)
        Category defaultCategory = categoryRepository.findById(1L).orElseThrow();
        Brand defaultBrand = brandRepository.findById(1L).orElseThrow();

        for (int i = 1; i <= 50; i++) {
            Product product = new Product();
            product.setName("Producto de prueba " + i);
            product.setDescription("Descripción del producto " + i);
            product.setSku("SKU-" + String.format("%03d", i));

            // medidas/logística (BigDecimal en entidad)
            product.setWeight(BigDecimal.valueOf(1.25));
            product.setLength(BigDecimal.valueOf(25.0));
            product.setWidth(BigDecimal.valueOf(15.0));
            product.setHeight(BigDecimal.valueOf(10.0));

            product.setCategory(defaultCategory);
            product.setBrand(defaultBrand);

            // Imagen general del producto (variant == null)
            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl("https://picsum.photos/seed/product-" + i + "/1200/800");
            img.setAltText("Imagen del producto " + i);
            img.setPosition(1);

            // Asegurate que Product.images tenga cascade PERSIST o inicialización
            product.getImages().add(img);

            // Guarda el producto primero
            Product savedProduct = productRepository.save(product);

            // Variante por defecto
            ProductVariant v1 = new ProductVariant();
            v1.setProduct(savedProduct);
            v1.setSku(savedProduct.getSku() + "-DEFAULT");
            v1.setPrice(BigDecimal.valueOf(100.0 + i)); // precio base de ejemplo
            v1.setStock((i % 3 == 0) ? 0 : 10);         // algunos sin stock para probar filtros
            v1.setAttributesJson("{}");
            productVariantRepository.save(v1);

            // Para la mitad de los productos, agregamos una segunda variante
            if (i % 2 == 0) {
                ProductVariant v2 = new ProductVariant();
                v2.setProduct(savedProduct);
                v2.setSku(savedProduct.getSku() + "-ALT");
                v2.setPrice(BigDecimal.valueOf(110.0 + i)); // un poco más cara
                v2.setStock(5);
                v2.setAttributesJson("{\"color\":\"Rojo\",\"talle\":\"M\"}");
                productVariantRepository.save(v2);
            }
        }
    }
}
