package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.ProductMapper;
import com.empresa.ecommerce_backend.mapper.ProductPageMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.Category;
import com.empresa.ecommerce_backend.model.Brand;
import com.empresa.ecommerce_backend.repository.BrandRepository;
import com.empresa.ecommerce_backend.repository.CategoryRepository;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.repository.spec.ProductSpecs;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.Specification.allOf;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductPageMapper productPageMapper;
    private final ProductVariantRepository productVariantRepository;
    private final com.empresa.ecommerce_backend.repository.ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ServiceResult<ProductResponse> createProduct(ProductRequest dto) {
        if (dto.getSku() != null && productRepository.existsBySku(dto.getSku())) {
            return ServiceResult.error(HttpStatus.CONFLICT, "Ya existe un producto con ese SKU base.");
        }
        var entity = productMapper.toEntity(dto);
        var saved = productRepository.save(entity);
        var response = productMapper.toResponse(saved);
        enrichWithReviewStats(response, saved.getId());
        return ServiceResult.created(response);
    }

    @Override
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        var list = productRepository.findAll()
                .stream()
                .map(p -> {
                    var response = productMapper.toResponse(p);
                    enrichWithReviewStats(response, p.getId());
                    return response;
                })
                .toList();
        return ServiceResult.ok(list);
    }

    @Override
    public ServiceResult<ProductResponse> getProductById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        var response = productMapper.toResponse(product);
        enrichWithReviewStats(response, id);
        return ServiceResult.ok(response);
    }

    @Override
    public ServiceResult<PaginatedResponse<ProductResponse>> getAllProductsPaged(ProductPaginatedRequest params) {
        Pageable pageable = productPageMapper.toPageable(params);

        // normalizar búsqueda
        final String q = (params.getQ() == null || params.getQ().isBlank())
                ? null
                : "%" + params.getQ().toLowerCase(java.util.Locale.ROOT) + "%";

        // detectar sorts especiales
        String sort = params.getSort();
        boolean sortBestSellingWeek = "bestSellingWeek".equalsIgnoreCase(sort);
        boolean sortBestSellingSince = "bestSellingSince".equalsIgnoreCase(sort) ||
                ("bestSelling".equalsIgnoreCase(sort) && params.getSinceDays() != null);

        Page<Product> page;

        if (sortBestSellingWeek || sortBestSellingSince) {
            int days = sortBestSellingWeek
                    ? 7
                    : Math.max(1, params.getSinceDays()); // evita 0 o negativos
            LocalDateTime since = LocalDateTime.now().minusDays(days);

            page = productRepository.findBestSellingSince(
                    since,
                    params.getCategoryId(),
                    params.getBrandId(),
                    q,
                    Boolean.TRUE.equals(params.getInStockOnly()),
                    pageable
            );
        } else {
            // camino actual con Specifications
            List<Specification<Product>> parts = new ArrayList<>();
            if (Boolean.TRUE.equals(params.getInStockOnly())) parts.add(ProductSpecs.inStockOnly(true));
            if (params.getCategoryId() != null) parts.add(ProductSpecs.hasCategory(params.getCategoryId()));
            if (params.getBrandId() != null) parts.add(ProductSpecs.hasBrand(params.getBrandId()));
            if (q != null) parts.add(ProductSpecs.nameContains(params.getQ()));
            if (params.getMinPrice() != null || params.getMaxPrice() != null)
                parts.add(ProductSpecs.priceBetween(params.getMinPrice(), params.getMaxPrice()));
            if (params.getColors() != null && !params.getColors().isEmpty())
                parts.add(ProductSpecs.colorsIn(params.getColors()));
            if (params.getSizes() != null && !params.getSizes().isEmpty())
                parts.add(ProductSpecs.sizesIn(params.getSizes()));
            if (params.getTags() != null && !params.getTags().isEmpty())
                parts.add(ProductSpecs.tagsIn(params.getTags()));

            Specification<Product> spec = parts.isEmpty() ? Specification.allOf() : Specification.allOf(parts);
            page = productRepository.findAll(spec, pageable);
        }

        Page<ProductResponse> mapped = page.map(p -> {
            var response = productMapper.toResponse(p);
            enrichWithReviewStats(response, p.getId());
            return response;
        });
        var response = productPageMapper.toPaginatedResponse(mapped, params);
        return ServiceResult.ok(response);
    }


    @Override
    public ServiceResult<ProductFacetsResponse> getProductFacets(ProductPaginatedRequest params) {

        final String q = (params.getQ() == null || params.getQ().isBlank())
                ? null
                : params.getQ().toLowerCase(java.util.Locale.ROOT);
        final String namePattern = (q == null) ? null : "%" + q + "%";

        final Boolean inStockOnly = params.getInStockOnly();
        final BigDecimal minPrice = params.getMinPrice();
        final BigDecimal maxPrice = params.getMaxPrice();

        var categoryFacets = categoryRepository.findFacetsWithCounts(namePattern, inStockOnly, minPrice, maxPrice);
        var brandFacets    = brandRepository.findFacetsWithCounts(namePattern, inStockOnly, minPrice, maxPrice);
        var pr             = productRepository.findPriceRange(namePattern, inStockOnly, minPrice, maxPrice);

        var dto = new ProductFacetsResponse();
        dto.setCategoryFacets(categoryFacets);
        dto.setBrandFacets(brandFacets);
        dto.setPriceRange(new PriceRangeResponse(
                (pr != null) ? pr.getMinPrice() : null,
                (pr != null) ? pr.getMaxPrice() : null
        ));

        return ServiceResult.ok(dto);
    }


    private Sort resolveSort(String sortKey) {
        if (sortKey == null) sortKey = "latest";
        return switch (sortKey) {
            case "latest"      -> Sort.by(Sort.Direction.DESC, "id");
            case "bestSelling" -> Sort.by(Sort.Direction.DESC, "soldCount").and(Sort.by(Sort.Direction.DESC, "id"));
            case "id"          -> Sort.by(Sort.Direction.ASC, "id");
            default            -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    /**
     * Enriquece un ProductResponse con estadísticas de reviews
     */
    private void enrichWithReviewStats(ProductResponse response, Long productId) {
        Double avgRating = reviewRepository.averageRatingByProduct(productId);
        Long totalReviews = reviewRepository.countByProduct_Id(productId);
        
        response.setAverageRating(avgRating);
        response.setTotalReviews(totalReviews);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<PageResponse<ProductBackofficeResponse>> listForBackoffice(Pageable pageable, String searchQuery) {
        Page<Product> page;
        
        if (searchQuery != null && !searchQuery.isBlank()) {
            String queryPattern = "%" + searchQuery.toLowerCase().trim() + "%";
            Specification<Product> spec = ProductSpecs.nameContains(searchQuery);
            page = productRepository.findAll(spec, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }
        
        Page<ProductBackofficeResponse> mapped = page.map(p -> {
            ProductBackofficeResponse dto = new ProductBackofficeResponse();
            dto.setId(p.getId());
            dto.setName(p.getName());
            
            // Thumbnail: primera imagen del producto o de la primera variante
            String thumbnail = null;
            if (p.getImages() != null && !p.getImages().isEmpty()) {
                thumbnail = p.getImages().stream()
                        .sorted((img1, img2) -> {
                            Integer pos1 = img1.getPosition() != null ? img1.getPosition() : Integer.MAX_VALUE;
                            Integer pos2 = img2.getPosition() != null ? img2.getPosition() : Integer.MAX_VALUE;
                            return pos1.compareTo(pos2);
                        })
                        .findFirst()
                        .map(img -> img.getUrl())
                        .orElse(null);
            }
            if (thumbnail == null && p.getVariants() != null && !p.getVariants().isEmpty()) {
                thumbnail = p.getVariants().stream()
                        .filter(v -> v.getImages() != null && !v.getImages().isEmpty())
                        .flatMap(v -> v.getImages().stream())
                        .sorted((img1, img2) -> {
                            Integer pos1 = img1.getPosition() != null ? img1.getPosition() : Integer.MAX_VALUE;
                            Integer pos2 = img2.getPosition() != null ? img2.getPosition() : Integer.MAX_VALUE;
                            return pos1.compareTo(pos2);
                        })
                        .findFirst()
                        .map(img -> img.getUrl())
                        .orElse(null);
            }
            dto.setThumbnail(thumbnail);
            
            // Category y Brand (nombres, no IDs)
            dto.setCategoryName(p.getCategory() != null ? p.getCategory().getName() : null);
            dto.setBrandName(p.getBrand() != null ? p.getBrand().getName() : null);
            
            // Precio representativo (más barato)
            BigDecimal price = null;
            if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                price = p.getVariants().stream()
                        .map(v -> v.getPrice())
                        .filter(pr -> pr != null)
                        .min(BigDecimal::compareTo)
                        .orElse(null);
            }
            dto.setPrice(price);
            
            // Stock total: considerar tipo de fulfillment
            // Si tiene variantes digitales on-demand: stock ilimitado (-1)
            // Si tiene variantes físicas o digitales instant: sumar stock real
            Integer totalStock = null;
            if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                boolean hasOnlyDigitalOnDemand = p.getVariants().stream()
                        .allMatch(v -> v.getFulfillmentType() == com.empresa.ecommerce_backend.enums.FulfillmentType.DIGITAL_ON_DEMAND);
                
                if (hasOnlyDigitalOnDemand) {
                    // Productos 100% digitales on-demand: always available
                    totalStock = -1; // -1 indica stock ilimitado
                } else {
                    // Productos físicos o digitales instant: sumar stock
                    totalStock = p.getVariants().stream()
                            .filter(v -> v.getStock() != null)
                            .mapToInt(v -> v.getStock())
                            .sum();
                }
            }
            dto.setTotalStock(totalStock);
            
            // Cantidad de variantes
            dto.setVariantCount(p.getVariants() != null ? p.getVariants().size() : 0);
            
            return dto;
        });
        
        return ServiceResult.ok(PageResponse.of(mapped));
    }

    @Override
    @Transactional
    public ServiceResult<ProductResponse> updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        // Actualizar campos básicos
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        
        // Actualizar categoría si viene
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
        
        // Actualizar marca si viene
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada"));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        Product saved = productRepository.save(product);
        return ServiceResult.ok(productMapper.toResponse(saved));
    }
}
