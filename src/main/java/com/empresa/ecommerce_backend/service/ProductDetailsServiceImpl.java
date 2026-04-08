package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.ProductDetailsResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.ProductDetailsMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailsServiceImpl implements ProductDetailsService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductDetailsMapper productDetailsMapper;
    private final com.empresa.ecommerce_backend.repository.ReviewRepository reviewRepository;
    private final com.empresa.ecommerce_backend.repository.SystemSettingRepository systemSettingRepository;
    private final com.empresa.ecommerce_backend.repository.DiscountRepository discountRepository;

    private static final String TRANSFER_KEY = "TRANSFER_DISCOUNT_PCT";
    private static final java.math.BigDecimal DEFAULT_TRANSFER = new java.math.BigDecimal("10.00");

    @Override
    public ServiceResult<ProductDetailsResponse> getDetails(Long productId) {
        Product product = productRepository.findWithDetailsById(productId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        if (Boolean.FALSE.equals(product.getIsVisible())) {
            throw new RecursoNoEncontradoException("Producto no encontrado");
        }

        List<ProductVariant> variants =
                productVariantRepository.findAllByProductIdOrderByIdAsc(productId);

        ProductDetailsResponse dto = productDetailsMapper.toDetails(product, variants, 
                getActiveBroadDiscounts(product.getProductType()), getTransferDiscount());

        // Enriquecer con estadísticas de reviews
        Double avgRating = reviewRepository.averageRatingByProduct(productId);
        Long totalReviews = reviewRepository.countByProduct_Id(productId);
        dto.setAverageRating(avgRating);
        dto.setTotalReviews(totalReviews);

        // --- Relaciones DLC ---
        // Si ES un DLC: incluir referencia al juego padre
        if (product.getParentGame() != null) {
            dto.setParentGameId(product.getParentGame().getId());
            dto.setParentGameName(product.getParentGame().getName());
        }

        dto.setSpecificationsJson(product.getSpecificationsJson());

        // Si ES un JUEGO: incluir lista de DLCs anidados
        List<Product> dlcProducts = productRepository.findByParentGame_Id(productId);
        if (dlcProducts != null && !dlcProducts.isEmpty()) {
            List<ProductDetailsResponse.DlcSummaryDto> dlcSummaries = dlcProducts.stream().map(dlc -> {
                ProductDetailsResponse.DlcSummaryDto s = new ProductDetailsResponse.DlcSummaryDto();
                s.setId(dlc.getId());
                s.setTitle(dlc.getName());

                // Precio mínimo del DLC
                if (dlc.getVariants() != null && !dlc.getVariants().isEmpty()) {
                    s.setPrice(dlc.getVariants().stream()
                            .map(v -> v.getPrice())
                            .filter(p -> p != null)
                            .min(java.math.BigDecimal::compareTo)
                            .orElse(null));
                }

                // Imagen del DLC
                String img = null;
                if (dlc.getImages() != null && !dlc.getImages().isEmpty()) {
                    img = dlc.getImages().stream()
                            .sorted(java.util.Comparator.comparing(
                                    com.empresa.ecommerce_backend.model.ProductImage::getPosition,
                                    java.util.Comparator.nullsLast(Integer::compareTo)))
                            .findFirst()
                            .map(com.empresa.ecommerce_backend.model.ProductImage::getUrl)
                            .orElse(null);
                }
                s.setImageUrl(img);
                return s;
            }).collect(Collectors.toList());
            dto.setDlcs(dlcSummaries);
        }

        return ServiceResult.ok(dto);
    }

    private java.math.BigDecimal getTransferDiscount() {
        return systemSettingRepository.findByKey(TRANSFER_KEY)
                .map(s -> {
                    try { return new java.math.BigDecimal(s.getValue()); }
                    catch (Exception e) { return DEFAULT_TRANSFER; }
                })
                .orElse(DEFAULT_TRANSFER);
    }

    private List<com.empresa.ecommerce_backend.model.Discount> getActiveBroadDiscounts(com.empresa.ecommerce_backend.enums.ProductType type) {
        return discountRepository.findActiveBroadDiscounts(java.time.LocalDateTime.now(), type);
    }
}
