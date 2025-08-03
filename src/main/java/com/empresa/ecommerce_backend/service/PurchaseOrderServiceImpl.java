// src/main/java/com/empresa/ecommerce_backend/service/PurchaseOrderServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.PurchaseOrderRequest;
import com.empresa.ecommerce_backend.dto.response.PurchaseOrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.StockTrackingMode;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.PurchaseLotMapper;
import com.empresa.ecommerce_backend.mapper.PurchaseOrderMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.model.PurchaseLot;
import com.empresa.ecommerce_backend.model.PurchaseOrder;
import com.empresa.ecommerce_backend.model.Supplier;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository; // <-- NUEVO
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
    private final ProductVariantRepository productVariantRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseLotMapper purchaseLotMapper;

    @Override
    @Transactional
    public ServiceResult<PurchaseOrderResponse> createPurchaseOrder(PurchaseOrderRequest dto) {

        if (dto.getLots() == null || dto.getLots().isEmpty()) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "La orden debe incluir al menos un lote.");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con ID: " + dto.getSupplierId()));

        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(dto);
        purchaseOrder.setSupplier(supplier);

        List<PurchaseLot> lotEntities = dto.getLots().stream().map(lotDto -> {

            ProductVariant variant = null;
            if (lotDto.getProductVariantId() != null) {
                variant = productVariantRepository.findById(lotDto.getProductVariantId())
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Variante no encontrada con ID: " + lotDto.getProductVariantId()));
            }

            Product product;
            if (variant != null) {
                product = variant.getProduct();
                if (lotDto.getProductId() != null && !product.getId().equals(lotDto.getProductId())) {
                    throw new IllegalArgumentException("productId no coincide con el producto de la variante " + variant.getId());
                }
            } else {
                if (lotDto.getProductId() == null) {
                    throw new IllegalArgumentException("Debe enviar productId o productVariantId en cada lote.");
                }
                product = productRepository.findById(lotDto.getProductId())
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Producto no encontrado con ID: " + lotDto.getProductId()));
            }

            // ---- Validación según modo ----
            if (product.getStockTrackingMode() == StockTrackingMode.VARIANT) {
                if (variant == null) {
                    throw new IllegalArgumentException("El producto " + product.getId() + " opera por variantes. Debe indicar productVariantId.");
                }
            } else { // SIMPLE
                if (variant != null) {
                    throw new IllegalArgumentException("El producto " + product.getId() + " es SIMPLE. No debe indicar productVariantId.");
                }
            }

            // Mapear lote y setear relaciones
            PurchaseLot lot = purchaseLotMapper.toEntity(lotDto);
            lot.setPurchaseOrder(purchaseOrder);
            lot.setProduct(product);
            lot.setProductVariant(variant);

            // ---- Stock ----
            if (variant != null) {
                // Modo VARIANT (o al menos vino variante): solo variante
                variant.setStock(variant.getStock() + lot.getQuantity());
            } else {
                // Modo SIMPLE: solo producto
                product.setStock(product.getStock() + lot.getQuantity());
            }

            return lot;
        }).toList();

        purchaseOrder.getLots().addAll(lotEntities);

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        PurchaseOrderResponse response = purchaseOrderMapper.toResponse(saved);
        return ServiceResult.created(response);
    }
}

