// src/main/java/com/empresa/ecommerce_backend/service/PurchaseOrderServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.PurchaseOrderRequest;
import com.empresa.ecommerce_backend.dto.response.PurchaseOrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.PurchaseLotMapper;
import com.empresa.ecommerce_backend.mapper.PurchaseOrderMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.PurchaseLot;
import com.empresa.ecommerce_backend.model.PurchaseOrder;
import com.empresa.ecommerce_backend.model.Supplier;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.PurchaseOrderRepository;
import com.empresa.ecommerce_backend.repository.SupplierRepository;
import com.empresa.ecommerce_backend.service.interfaces.PurchaseOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseLotMapper purchaseLotMapper;

    @Override
    @Transactional
    public ServiceResult<PurchaseOrderResponse> createPurchaseOrder(PurchaseOrderRequest dto) {

        // Validación básica del request
        if (dto.getLots() == null || dto.getLots().isEmpty()) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "La orden debe incluir al menos un lote.");
        }

        // 1) Proveedor
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Proveedor no encontrado con ID: " + dto.getSupplierId()));

        // 2) Orden
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(dto);
        purchaseOrder.setSupplier(supplier);

        // 3) Lotes + relaciones + stock
        List<PurchaseLot> lotEntities = dto.getLots().stream().map(lotDto -> {
            Product product = productRepository.findById(lotDto.getProductId())
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException("Producto no encontrado con ID: " + lotDto.getProductId()));

            PurchaseLot lot = purchaseLotMapper.toEntity(lotDto);
            lot.setProduct(product);
            lot.setPurchaseOrder(purchaseOrder);

            // Actualizar stock (JPA hará dirty checking al commit)
            product.setStock(product.getStock() + lot.getQuantity());

            return lot;
        }).toList();

        purchaseOrder.getLots().addAll(lotEntities);

        // 4) Guardar y responder
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        PurchaseOrderResponse response = purchaseOrderMapper.toResponse(saved);

        // 201 Created
        return ServiceResult.created(response);
    }
}
