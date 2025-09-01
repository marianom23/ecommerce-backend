// src/main/java/com/empresa/ecommerce_backend/mapper/OrderMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.response.OrderItemResponse;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.PaymentSummaryResponse;
import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.OrderItem;
import com.empresa.ecommerce_backend.model.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    // ---------- Order -> OrderResponse ----------
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

            // items se mapea con method reference
            @Mapping(target = "items", expression = "java(toItemResponses(order.getItems()))")
    })
    OrderResponse toResponse(Order order);

    // ---------- OrderItem -> OrderItemResponse ----------
    @Mappings({
            @Mapping(target = "productId", source = "variant.product.id"),
            @Mapping(target = "variantId", source = "variant.id"),
            @Mapping(target = "attributesJson", source = "attributesJson")
    })
    OrderItemResponse toItemResponse(OrderItem item);


    default PaymentSummaryResponse toPaymentSummary(Payment payment) {
        if (payment == null) return null;

        PaymentSummaryResponse dto = new PaymentSummaryResponse();
        dto.setMethod(payment.getMethod().name());
        dto.setStatus(payment.getStatus().name());
        dto.setAmount(payment.getAmount());

        // 👇 extraer init_point del JSON que guardaste en providerMetadata
        String meta = payment.getProviderMetadata();
        if (meta != null && !meta.isBlank()) {
            try {
                var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(meta);
                String initPoint = node.path("init_point").asText(null);
                dto.setRedirectUrl(initPoint);
            } catch (Exception ignored) {
                // si el JSON no parsea, no rompemos el mapeo
            }
        }

        return dto;
    }


    default List<OrderItemResponse> toItemResponses(java.util.List<OrderItem> items) {
        return items.stream().map(this::toItemResponse).toList();
    }

}
