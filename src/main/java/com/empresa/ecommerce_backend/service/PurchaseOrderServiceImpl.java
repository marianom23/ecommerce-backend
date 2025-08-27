// src/main/java/com/empresa/ecommerce_backend/service/PurchaseOrderServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.PurchaseOrderRequest;
import com.empresa.ecommerce_backend.dto.response.PurchaseOrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.PurchaseLotMapper;
import com.empresa.ecommerce_backend.mapper.PurchaseOrderMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.model.PurchaseLot;
import com.empresa.ecommerce_backend.model.PurchaseOrder;
import com.empresa.ecommerce_backend.model.Supplier;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
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
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Proveedor no encontrado con ID: " + dto.getSupplierId()));

        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(dto);
        purchaseOrder.setSupplier(supplier);

        List<PurchaseLot> lotEntities = dto.getLots().stream().map(lotDto -> {

            // ----- Modelo variante-only: productVariantId ES OBLIGATORIO -----
            if (lotDto.getProductVariantId() == null) {
                throw new IllegalArgumentException(
                        "Cada lote debe indicar productVariantId (modelo variante-only).");
            }

            // Traer variante y su producto
            ProductVariant variant = productVariantRepository.findById(lotDto.getProductVariantId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Variante no encontrada con ID: " + lotDto.getProductVariantId()));

            Product product = variant.getProduct();
            if (product == null) {
                throw new IllegalStateException(
                        "La variante " + variant.getId() + " no está asociada a ningún producto.");
            }

            // Si llega productId, validar consistencia
            if (lotDto.getProductId() != null && !product.getId().equals(lotDto.getProductId())) {
                throw new IllegalArgumentException(
                        "productId (" + lotDto.getProductId() + ") no coincide con el producto de la variante " + variant.getId());
            }

            // (Opcional) Si quieres validar que el productId exista cuando viene, puedes cargarlo:
            // productRepository.findById(lotDto.getProductId())...

            // Mapear lote y setear relaciones
            PurchaseLot lot = purchaseLotMapper.toEntity(lotDto);
            lot.setPurchaseOrder(purchaseOrder);
            lot.setProduct(product);
            lot.setProductVariant(variant);

            // ----- Ajuste de stock SOLO en la variante -----
            Integer current = variant.getStock() == null ? 0 : variant.getStock();
            Integer qty = lot.getQuantity() == null ? 0 : lot.getQuantity();
            variant.setStock(current + qty);

            return lot;
        }).toList();

        purchaseOrder.getLots().addAll(lotEntities);

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        PurchaseOrderResponse response = purchaseOrderMapper.toResponse(saved);
        return ServiceResult.created(response);
    }
}
