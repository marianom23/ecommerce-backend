// src/main/java/com/empresa/ecommerce_backend/controller/MercadoPagoWebhookController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {
    private final PaymentService paymentService;

    @PostMapping("/mercadopago")
    public ResponseEntity<String> handle(@RequestBody(required = false) Map<String,Object> body,
                                         @RequestParam(required = false) Map<String,String> query) {
        Map<String,Object> payload = body != null ? new HashMap<>(body) : new HashMap<>();
        if (query != null) payload.putAll(query); // MP a veces manda topic & id por query
        paymentService.handleGatewayWebhook("MERCADO_PAGO", payload);
        return ResponseEntity.ok("ok");
    }
}
