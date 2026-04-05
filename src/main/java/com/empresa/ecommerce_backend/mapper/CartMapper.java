package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.model.*;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CartMapper {

    @Autowired
    protected com.empresa.ecommerce_backend.repository.DiscountRepository discountRepository;

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal itemsSubtotal = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartTotalsResponse totals = CartTotalsResponse.builder()
                .itemsSubtotal(itemsSubtotal)
                .grandTotal(itemsSubtotal) // si luego sumás envío/impuestos, cámbialo acá
                .build();

        return CartResponse.builder()
                .id(cart.getId())
                .sessionId(cart.getSessionId())
                .updatedAt(cart.getUpdatedAt())
                .items(items)
                .totals(totals)
                .build();
    }

    public CartItemResponse toItemResponse(CartItem ci) {
        Product p = ci.getProduct();
        ProductVariant v = ci.getVariant();

        if (v == null) {
            throw new IllegalStateException("El ítem de carrito debe tener una variante asociada");
            // o si preferís: return null; pero lo mejor es no permitir carrito sin variante
        }

        BigDecimal unitPrice = v.getPrice(); // 👈 siempre desde la variante
        BigDecimal unitDiscounted = computeDiscountedPriceForVariant(v); 
        BigDecimal qty = BigDecimal.valueOf(ci.getQuantity());
        BigDecimal subtotal = unitDiscounted.multiply(qty);

        return CartItemResponse.builder()
                .id(ci.getId())
                .productId(p.getId())
                .variantId(v.getId())
                .name(p.getName())
                .imageUrl(resolvePrimaryImageUrl(p, v))
                .attributesJson(v.getAttributesJson())
                .unitPrice(unitPrice)
                .unitDiscountedPrice(unitDiscounted)
                .priceAtAddition(ci.getPriceAtAddition())
                .discountedPriceAtAddition(ci.getDiscountedPriceAtAddition())
                .quantity(ci.getQuantity())
                .subtotal(subtotal)
                .build();
    }


    public BigDecimal computeDiscountedPriceForVariant(ProductVariant v) {
        if (v == null || v.getPrice() == null) return BigDecimal.ZERO;
        
        Product p = v.getProduct();
        List<com.empresa.ecommerce_backend.model.Discount> globalDiscounts = 
            discountRepository.findActiveBroadDiscounts(java.time.LocalDateTime.now(), p.getProductType());
            
        BigDecimal base = v.getPrice();
        BigDecimal best = base;

        if (p.getDiscounts() != null) {
            for (com.empresa.ecommerce_backend.model.Discount d : p.getDiscounts()) {
                best = applyIfBetter(base, best, d);
            }
        }

        if (globalDiscounts != null) {
            for (com.empresa.ecommerce_backend.model.Discount d : globalDiscounts) {
                best = applyIfBetter(base, best, d);
            }
        }

        return best.max(BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal applyIfBetter(BigDecimal base, BigDecimal currentBest, com.empresa.ecommerce_backend.model.Discount d) {
        BigDecimal pct = d.getPercentage();
        if (pct != null && pct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal off = base.multiply(pct).divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            BigDecimal candidate = base.subtract(off);
            if (candidate.compareTo(currentBest) < 0) return candidate;
        }
        return currentBest;
    }

    /**
     * Devuelve la URL principal de imagen para la línea del carrito:
     * - Si la variante tiene imágenes, toma la primera por "position".
     * - Si no, toma la primera imagen "general" del producto (variant == null).
     * - Si no hay ninguna, retorna null (o usa un placeholder si querés).
     */
    protected String resolvePrimaryImageUrl(Product p, ProductVariant v) {
        // 1) imágenes específicas de la variante
        if (v != null && v.getImages() != null && !v.getImages().isEmpty()) {
            return v.getImages().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(ProductImage::getPosition,
                            Comparator.nullsLast(Integer::compareTo)))
                    .map(ProductImage::getUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        // 2) imágenes generales del producto (variant == null)
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            return p.getImages().stream()
                    .filter(Objects::nonNull)
                    .filter(img -> img.getVariant() == null)
                    .sorted(Comparator.comparing(ProductImage::getPosition,
                            Comparator.nullsLast(Integer::compareTo)))
                    .map(ProductImage::getUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        // 3) sin imágenes → opcional: devolver placeholder
        // return "https://cdn.tuapp.com/placeholders/product.png";
        return null;
    }
}
