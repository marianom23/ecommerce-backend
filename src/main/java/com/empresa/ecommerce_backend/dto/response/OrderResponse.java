// src/main/java/com/empresa/ecommerce_backend/dto/response/OrderResponse.java
package com.empresa.ecommerce_backend.dto.response;

import com.empresa.ecommerce_backend.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus status;

    // Totales (snapshots)
    private BigDecimal subTotal;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal discountTotal;
    private BigDecimal totalAmount;

    // Shipping snapshot (campos planos)
    private String shippingStreet;
    private String shippingStreetNumber;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;
    private String shippingApartmentNumber;
    private String shippingFloor;
    private String shippingRecipientName;
    private String shippingPhone;

    // Billing snapshot (campos planos)
    private String billingFullName;
    private String billingDocumentType;
    private String billingDocumentNumber;
    private String billingTaxCondition;
    private String billingBusinessName;
    private String billingEmailForInvoices;
    private String billingPhone;
    private String billingStreet;
    private String billingStreetNumber;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;
    private String billingApartmentNumber;
    private String billingFloor;
    private String chosenPaymentMethod;   // CARD, MERCADO_PAGO, etc.
    private PaymentSummaryResponse payment; // puede venir null

    private List<OrderItemResponse> items;
}
