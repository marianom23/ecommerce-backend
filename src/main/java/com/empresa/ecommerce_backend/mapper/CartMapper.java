package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.model.*;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    default CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal itemsSubtotal = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartTotalsResponse totals = CartTotalsResponse.builder()
                .itemsSubtotal(itemsSubtotal)
                .grandTotal(itemsSubtotal)
                .build();

        return CartResponse.builder()
                .id(cart.getId())
                .sessionId(cart.getSessionId())
                .updatedAt(cart.getUpdatedAt())
                .items(items)
                .totals(totals)
                .build();
    }

    default CartItemResponse toItemResponse(CartItem ci) {
        Product p = ci.getProduct();
        ProductVariant v = ci.getVariant();

        BigDecimal unitPrice = (v != null) ? v.getPrice() : p.getPrice();
        BigDecimal unitDiscounted = unitPrice; // MVP sin descuentos
        BigDecimal subtotal = unitDiscounted.multiply(BigDecimal.valueOf(ci.getQuantity()));

        return CartItemResponse.builder()
                .id(ci.getId())
                .productId(p.getId())
                .variantId(v != null ? v.getId() : null)
                .name(p.getName())
                .imageUrl(p.getImageUrl())
                .attributesJson(v != null ? v.getAttributesJson() : null)
                .unitPrice(unitPrice)
                .unitDiscountedPrice(unitDiscounted)
                .priceAtAddition(ci.getPriceAtAddition())
                .discountedPriceAtAddition(ci.getDiscountedPriceAtAddition())
                .quantity(ci.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
