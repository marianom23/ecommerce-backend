// dto/request/ConfirmOrderRequest.java
package com.empresa.ecommerce_backend.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConfirmOrderRequest {
    // opcionales (para integraciones tipo MercadoPago, Stripe, etc.)
    private String successUrl;
    private String failureUrl;
    private String pendingUrl;
    private String callbackUrl; // webhook
}
