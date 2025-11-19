package com.empresa.ecommerce_backend.seeder;

import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import com.empresa.ecommerce_backend.enums.FulfillmentType;

@Component
@RequiredArgsConstructor
@Profile("dev")
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
        seedProductsAndVariants();       // físicos demo
        seedDigitalOnDemandProducts();   // digitales on demand (marcados con "(digital)")
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

    private void seedDigitalOnDemandProducts() {
        // Evitá duplicar si ya corriste el seeder antes (chequeo rápido por SKU prefix)
        final String skuPrefix = "DIG-ONDEM-";

        Category defaultCategory = categoryRepository.findById(1L).orElseThrow();
        Brand defaultBrand = brandRepository.findById(1L).orElseThrow();

        for (int i = 1; i <= 10; i++) {
            Product product = new Product();
            product.setName("Licencia Software Pro " + i + " (digital)");
            product.setDescription("Clave digital bajo demanda. Recibirás tu key por email dentro de 1–12 horas.");
            product.setSku("SKU-DIG-" + String.format("%03d", i)); // SKU base opcional
            product.setCategory(defaultCategory);
            product.setBrand(defaultBrand);

            // Imagen genérica
            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl("https://picsum.photos/seed/digital-" + i + "/1200/800");
            img.setAltText("Producto digital " + i);
            img.setPosition(1);
            product.getImages().add(img);

            Product saved = productRepository.save(product);

            // Variante DIGITAL_ON_DEMAND (stock ignorado; dimensiones/peso nulo)
            ProductVariant v = new ProductVariant();
            v.setProduct(saved);
            v.setSku(skuPrefix + String.format("%03d", i));
            v.setPrice(BigDecimal.valueOf(1500 + (i * 10)));  // precio demo
            v.setStock(0);                                     // no aplica, pero lo dejamos en 0
            v.setFulfillmentType(FulfillmentType.DIGITAL_ON_DEMAND);
            v.setLeadTimeMinHours(1);
            v.setLeadTimeMaxHours(12);
            v.setAttributesJson("{\"plataforma\":\"PC\",\"region\":\"Global\"}");

            // Para digitales: logística no aplica → dejá null
            v.setWeightKg(null);
            v.setLengthCm(null);
            v.setWidthCm(null);
            v.setHeightCm(null);

            productVariantRepository.save(v);
        }
    }


    /**
     * Crea 50 productos (solo catálogo) y genera variantes:
     * - Variante DEFAULT con price/stock y logística.
     * - Para la mitad de los productos agrega una 2da variante (ALT) para pruebas.
     */
    private void seedProductsAndVariants() {
        if (productRepository.count() > 0) return;

        Category defaultCategory = categoryRepository.findById(1L).orElseThrow();
        Brand defaultBrand = brandRepository.findById(1L).orElseThrow();

        for (int i = 1; i <= 50; i++) {
            Product product = new Product();
            product.setName("Producto de prueba " + i);
            product.setDescription("Descripción del producto " + i);
            product.setSku("SKU-" + String.format("%03d", i)); // SKU base opcional
            product.setCategory(defaultCategory);
            product.setBrand(defaultBrand);

            // Imagen general del producto
            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl("https://picsum.photos/seed/product-" + i + "/1200/800");
            img.setAltText("Imagen del producto " + i);
            img.setPosition(1);
            product.getImages().add(img);

            // Guarda el producto primero (cascade para imágenes)
            Product savedProduct = productRepository.save(product);

            // ---------- Variante por defecto ----------
            ProductVariant v1 = new ProductVariant();
            v1.setProduct(savedProduct);
            v1.setSku(savedProduct.getSku() + "-DEFAULT");
            v1.setPrice(BigDecimal.valueOf(100.0 + i));           // precio demo
            v1.setStock((i % 3 == 0) ? 0 : 10);                   // algunos sin stock
            v1.setAttributesJson("{}");
            // logística en la VARIANTE
            v1.setWeightKg(BigDecimal.valueOf(1.25));
            v1.setLengthCm(BigDecimal.valueOf(25.0));
            v1.setWidthCm(BigDecimal.valueOf(15.0));
            v1.setHeightCm(BigDecimal.valueOf(10.0));
            productVariantRepository.save(v1);

            // ---------- Segunda variante (para la mitad) ----------
            if (i % 2 == 0) {
                ProductVariant v2 = new ProductVariant();
                v2.setProduct(savedProduct);
                v2.setSku(savedProduct.getSku() + "-ALT");
                v2.setPrice(BigDecimal.valueOf(110.0 + i));
                v2.setStock(5);
                v2.setAttributesJson("{\"color\":\"Rojo\",\"talle\":\"M\"}");
                // logística distinta para probar
                v2.setWeightKg(BigDecimal.valueOf(1.40));
                v2.setLengthCm(BigDecimal.valueOf(27.0));
                v2.setWidthCm(BigDecimal.valueOf(17.0));
                v2.setHeightCm(BigDecimal.valueOf(12.0));
                productVariantRepository.save(v2);
            }
        }
    }
}
