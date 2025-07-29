package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull
    private Long supplierId;  // 👈 nuevo: ID del proveedor

    @NotNull
    private LocalDate purchaseDate;  // 👈 nuevo: fecha de compra

    @NotNull
    private List<PurchaseLotRequest> lots;  // 👈 cada lote con producto, cantidad, etc.

}
