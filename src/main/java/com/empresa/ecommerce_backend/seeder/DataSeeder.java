package com.empresa.ecommerce_backend.seeder;

import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.enums.FulfillmentType;
import com.empresa.ecommerce_backend.enums.BannerPlacement;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;


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
    private final BannerRepository bannerRepository;

    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedData() {
        seedRoles();
        seedAdmin();
        seedBrands();
        seedCategories();
        seedSuppliers();
        seedBanners();                 // Banners demo
        seedProductsAndVariants();     // Productos físicos demo
        seedDigitalOnDemandProducts(); // Productos digitales demo
    }

    /* =================== ROLES / ADMIN =================== */

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

    /* =================== MARCAS / CATEGORÍAS / PROVEEDORES =================== */

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

    /* =================== BANNERS HOME (TECNOLOGÍA, FOTOS REALES) =================== */

    private void seedBanners() {
        if (bannerRepository.count() > 0) {
            return;
        }

        // HERO principal izquierda — Auriculares Sony
        Banner heroMain = new Banner();
        heroMain.setPlacement(BannerPlacement.HOME_HERO_MAIN);
        heroMain.setTitle("Sony WH-1000XM5");
        heroMain.setSubtitle("Noise Cancelling Headphones");
        heroMain.setDescription("Los mejores auriculares con cancelación activa para una inmersión total en tu música.");
        heroMain.setImageUrl("https://images.unsplash.com/photo-1580894908361-967232db2e56?auto=format&fit=crop&w=1600&q=80");
        heroMain.setCtaText("Comprar ahora");
        heroMain.setCtaUrl("/products/sony-wh-1000xm5");
        heroMain.setPrice(BigDecimal.valueOf(399.99));
        heroMain.setOldPrice(BigDecimal.valueOf(499.99));
        heroMain.setDiscountPercent(20);
        heroMain.setSortOrder(1);
        heroMain.setActive(true);
        bannerRepository.save(heroMain);

        // Tarjeta lateral arriba — iPhone
        Banner heroSideTop = new Banner();
        heroSideTop.setPlacement(BannerPlacement.HOME_HERO_SIDE_TOP);
        heroSideTop.setTitle("iPhone 15 Pro");
        heroSideTop.setSubtitle("Titanium Performance");
        heroSideTop.setDescription("El smartphone más avanzado, con el mejor rendimiento y cámara profesional.");
        heroSideTop.setImageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=800&q=80");
        heroSideTop.setCtaText("Ver más");
        heroSideTop.setCtaUrl("/products/iphone-15-pro");
        heroSideTop.setPrice(BigDecimal.valueOf(1199.99));
        heroSideTop.setOldPrice(BigDecimal.valueOf(1299.99));
        heroSideTop.setDiscountPercent(8);
        heroSideTop.setSortOrder(1);
        heroSideTop.setActive(true);
        bannerRepository.save(heroSideTop);

        // Tarjeta lateral abajo — Headset gaming
        Banner heroSideBottom = new Banner();
        heroSideBottom.setPlacement(BannerPlacement.HOME_HERO_SIDE_BOTTOM);
        heroSideBottom.setTitle("Headset Gaming Pro");
        heroSideBottom.setSubtitle("7.1 Surround");
        heroSideBottom.setDescription("Sonido envolvente, micrófono con cancelación de ruido y diseño ergonómico.");
        heroSideBottom.setImageUrl("https://images.unsplash.com/photo-1614680376573-df3480f0c6ff?auto=format&fit=crop&w=800&q=80");
        heroSideBottom.setCtaText("Descubrir");
        heroSideBottom.setCtaUrl("/products/gaming-headset-pro");
        heroSideBottom.setPrice(BigDecimal.valueOf(129.00));
        heroSideBottom.setOldPrice(BigDecimal.valueOf(179.00));
        heroSideBottom.setDiscountPercent(28);
        heroSideBottom.setSortOrder(1);
        heroSideBottom.setActive(true);
        bannerRepository.save(heroSideBottom);

        // Banner promo top — MacBook / setup
        Banner promoTop = new Banner();
        promoTop.setPlacement(BannerPlacement.HOME_PROMO_TOP);
        promoTop.setTitle("MacBook Pro M3");
        promoTop.setSubtitle("Potencia para profesionales");
        promoTop.setDescription("Ideal para desarrollo, edición de video y diseño 3D.");
        promoTop.setImageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1600&q=80");
        promoTop.setCtaText("Ver MacBooks");
        promoTop.setCtaUrl("/category/macbook");
        promoTop.setPrice(BigDecimal.valueOf(2399.00));
        promoTop.setOldPrice(BigDecimal.valueOf(2599.00));
        promoTop.setDiscountPercent(8);
        promoTop.setSortOrder(1);
        promoTop.setActive(true);
        bannerRepository.save(promoTop);

        // Promo bottom left — Smartwatch
        Banner promoBottomLeft = new Banner();
        promoBottomLeft.setPlacement(BannerPlacement.HOME_PROMO_BOTTOM_LEFT);
        promoBottomLeft.setTitle("Galaxy Watch 6");
        promoBottomLeft.setSubtitle("Tu salud en tu muñeca");
        promoBottomLeft.setDescription("Seguimiento avanzado de actividad, sueño y ritmo cardíaco.");
        promoBottomLeft.setImageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80");
        promoBottomLeft.setCtaText("Ver smartwatches");
        promoBottomLeft.setCtaUrl("/category/smartwatch");
        promoBottomLeft.setPrice(BigDecimal.valueOf(299.00));
        promoBottomLeft.setOldPrice(BigDecimal.valueOf(349.00));
        promoBottomLeft.setDiscountPercent(15);
        promoBottomLeft.setSortOrder(1);
        promoBottomLeft.setActive(true);
        bannerRepository.save(promoBottomLeft);

        // Promo bottom right — Parlante JBL
        Banner promoBottomRight = new Banner();
        promoBottomRight.setPlacement(BannerPlacement.HOME_PROMO_BOTTOM_RIGHT);
        promoBottomRight.setTitle("JBL Charge 5");
        promoBottomRight.setSubtitle("Sonido donde vayas");
        promoBottomRight.setDescription("Batería de hasta 20 horas, resistente al agua y con graves potentes.");
        promoBottomRight.setImageUrl("https://images.unsplash.com/photo-1552914953-938eef1e54b6?auto=format&fit=crop&w=800&q=80");
        promoBottomRight.setCtaText("Comprar");
        promoBottomRight.setCtaUrl("/products/jbl-charge-5");
        promoBottomRight.setPrice(BigDecimal.valueOf(149.99));
        promoBottomRight.setOldPrice(BigDecimal.valueOf(199.99));
        promoBottomRight.setDiscountPercent(25);
        promoBottomRight.setSortOrder(1);
        promoBottomRight.setActive(true);
        bannerRepository.save(promoBottomRight);

        // Banner con countdown — Mega oferta 48hs
        Banner countdown = new Banner();
        countdown.setPlacement(BannerPlacement.HOME_COUNTDOWN);
        countdown.setTitle("Mega Oferta 48hs");
        countdown.setSubtitle("Tecnología seleccionada");
        countdown.setDescription("Descuentos especiales en notebooks, monitores y accesorios.");
        countdown.setImageUrl("https://images.unsplash.com/photo-1587825140708-dfaf72ae4b02?auto=format&fit=crop&w=1600&q=80");
        countdown.setCtaText("Ver ofertas");
        countdown.setCtaUrl("/offers/flash");
        countdown.setPrice(null);
        countdown.setOldPrice(null);
        countdown.setDiscountPercent(null);
        countdown.setCountdownUntil(LocalDateTime.now().plusHours(48));
        countdown.setSortOrder(1);
        countdown.setActive(true);
        bannerRepository.save(countdown);
    }

    /* =================== PRODUCTOS DIGITALES =================== */

    private void seedDigitalOnDemandProducts() {
        final String skuPrefix = "DIG-ONDEM-";

        Category defaultCategory = categoryRepository.findById(1L).orElseThrow();
        Brand defaultBrand = brandRepository.findById(1L).orElseThrow();

        for (int i = 1; i <= 10; i++) {
            Product product = new Product();
            product.setName("Licencia Software Pro " + i + " (digital)");
            product.setDescription("Clave digital bajo demanda. Recibirás tu key por email dentro de 1–12 horas.");
            product.setSku("SKU-DIG-" + String.format("%03d", i));
            product.setCategory(defaultCategory);
            product.setBrand(defaultBrand);

            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl("https://picsum.photos/seed/digital-" + i + "/1200/800");
            img.setAltText("Producto digital " + i);
            img.setPosition(1);
            product.getImages().add(img);

            Product saved = productRepository.save(product);

            ProductVariant v = new ProductVariant();
            v.setProduct(saved);
            v.setSku(skuPrefix + String.format("%03d", i));
            v.setPrice(BigDecimal.valueOf(1500 + (i * 10)));
            v.setStock(0);
            v.setFulfillmentType(FulfillmentType.DIGITAL_ON_DEMAND);
            v.setLeadTimeMinHours(1);
            v.setLeadTimeMaxHours(12);
            v.setAttributesJson("{\"plataforma\":\"PC\",\"region\":\"Global\"}");

            v.setWeightKg(null);
            v.setLengthCm(null);
            v.setWidthCm(null);
            v.setHeightCm(null);

            productVariantRepository.save(v);
        }
    }

    /* =================== PRODUCTOS FÍSICOS DEMO =================== */

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
            product.setSku("SKU-" + String.format("%03d", i));
            product.setCategory(defaultCategory);
            product.setBrand(defaultBrand);

            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl("https://picsum.photos/seed/product-" + i + "/1200/800");
            img.setAltText("Imagen del producto " + i);
            img.setPosition(1);
            product.getImages().add(img);

            Product savedProduct = productRepository.save(product);

            ProductVariant v1 = new ProductVariant();
            v1.setProduct(savedProduct);
            v1.setSku(savedProduct.getSku() + "-DEFAULT");
            v1.setPrice(BigDecimal.valueOf(100.0 + i));
            v1.setStock((i % 3 == 0) ? 0 : 10);
            v1.setAttributesJson("{}");
            v1.setWeightKg(BigDecimal.valueOf(1.25));
            v1.setLengthCm(BigDecimal.valueOf(25.0));
            v1.setWidthCm(BigDecimal.valueOf(15.0));
            v1.setHeightCm(BigDecimal.valueOf(10.0));
            productVariantRepository.save(v1);

            if (i % 2 == 0) {
                ProductVariant v2 = new ProductVariant();
                v2.setProduct(savedProduct);
                v2.setSku(savedProduct.getSku() + "-ALT");
                v2.setPrice(BigDecimal.valueOf(110.0 + i));
                v2.setStock(5);
                v2.setAttributesJson("{\"color\":\"Rojo\",\"talle\":\"M\"}");
                v2.setWeightKg(BigDecimal.valueOf(1.40));
                v2.setLengthCm(BigDecimal.valueOf(27.0));
                v2.setWidthCm(BigDecimal.valueOf(17.0));
                v2.setHeightCm(BigDecimal.valueOf(12.0));
                productVariantRepository.save(v2);
            }
        }
    }
}

