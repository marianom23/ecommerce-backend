package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class MercadoPagoWebhookController {
    private final PaymentService paymentService;

    @PostMapping("/mercadopago")
    public ResponseEntity<String> handle(@RequestBody Map<String,Object> payload,
                                         @RequestHeader Map<String,String> headers) {
        paymentService.handleGatewayWebhook("MERCADO_PAGO", payload);
        return ResponseEntity.ok("ok");
    }
}
