// src/main/java/com/empresa/ecommerce_backend/mapper/OrderMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.projection.OrderSummaryProjection;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    // ---------- Order -> OrderResponse (ya lo tenías) ----------
    @Mappings({
            @Mapping(target = "shippingStreet", source = "shippingAddress.street"),
            @Mapping(target = "shippingStreetNumber", source = "shippingAddress.streetNumber"),
            @Mapping(target = "shippingCity", source = "shippingAddress.city"),
            @Mapping(target = "shippingState", source = "shippingAddress.state"),
            @Mapping(target = "shippingPostalCode", source = "shippingAddress.postalCode"),
            @Mapping(target = "shippingCountry", source = "shippingAddress.country"),
            @Mapping(target = "shippingApartmentNumber", source = "shippingAddress.apartmentNumber"),
            @Mapping(target = "shippingFloor", source = "shippingAddress.floor"),
            @Mapping(target = "shippingRecipientName", source = "shippingAddress.recipientName"),
            @Mapping(target = "shippingPhone", source = "shippingAddress.phone"),

            @Mapping(target = "billingFullName", source = "billingInfo.fullName"),
            @Mapping(target = "billingDocumentType", source = "billingInfo.documentType"),
            @Mapping(target = "billingDocumentNumber", source = "billingInfo.documentNumber"),
            @Mapping(target = "billingTaxCondition", source = "billingInfo.taxCondition"),
            @Mapping(target = "billingBusinessName", source = "billingInfo.businessName"),
            @Mapping(target = "billingEmailForInvoices", source = "billingInfo.emailForInvoices"),
            @Mapping(target = "billingPhone", source = "billingInfo.phone"),
            @Mapping(target = "billingStreet", source = "billingInfo.street"),
            @Mapping(target = "billingStreetNumber", source = "billingInfo.streetNumber"),
            @Mapping(target = "billingCity", source = "billingInfo.city"),
            @Mapping(target = "billingState", source = "billingInfo.state"),
            @Mapping(target = "billingPostalCode", source = "billingInfo.postalCode"),
            @Mapping(target = "billingCountry", source = "billingInfo.country"),
            @Mapping(target = "billingApartmentNumber", source = "billingInfo.apartmentNumber"),
            @Mapping(target = "billingFloor", source = "billingInfo.floor"),

            @Mapping(target = "chosenPaymentMethod",
                    expression = "java(order.getChosenPaymentMethod() != null ? order.getChosenPaymentMethod().name() : null)"),
            @Mapping(target = "payment", expression = "java(toPaymentSummary(order.getPayment()))"),

            @Mapping(target = "items", expression = "java(toItemResponses(order.getItems()))")
    })
    OrderResponse toResponse(Order order);

    // ---------- NUEVO: Projection -> OrderSummaryResponse ----------
    default OrderSummaryResponse toSummary(OrderSummaryProjection p) {
        if (p == null) return null;
        return OrderSummaryResponse.builder()
                .id(p.getId())
                .orderNumber(p.getOrderNumber())
                .orderDate(p.getOrderDate())
                .status(p.getStatus())
                .totalAmount(p.getTotalAmount())
                .itemCount(p.getItemCount())
                .firstItemThumb(p.getFirstItemThumb())
                .build();
    }

    // ---------- Items ----------
    @Mappings({
            @Mapping(target = "productId", source = "variant.product.id"),
            @Mapping(target = "variantId", source = "variant.id"),
            @Mapping(target = "attributesJson", source = "attributesJson")
    })
    OrderItemResponse toItemResponse(OrderItem item);

    default List<OrderItemResponse> toItemResponses(java.util.List<OrderItem> items) {
        return items.stream().map(this::toItemResponse).toList();
    }

    // ---------- Payment ----------
    default PaymentSummaryResponse toPaymentSummary(Payment payment) {
        if (payment == null) return null;

        PaymentSummaryResponse dto = new PaymentSummaryResponse();
        dto.setMethod(payment.getMethod().name());
        dto.setStatus(payment.getStatus().name());
        dto.setAmount(payment.getAmount());

        String meta = payment.getProviderMetadata();
        if (meta != null && !meta.isBlank()) {
            try {
                var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(meta);
                String initPoint = node.path("init_point").asText(null);
                dto.setRedirectUrl(initPoint);
            } catch (Exception ignored) { }
        }
        return dto;
    }
}
