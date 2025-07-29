package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.PurchaseLotRequest;
import com.empresa.ecommerce_backend.dto.request.PurchaseOrderRequest;
import com.empresa.ecommerce_backend.dto.response.PurchaseOrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
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
        // 1. Buscar proveedor
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + dto.getSupplierId()));

        // 2. Mapear orden desde DTO
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(dto);
        purchaseOrder.setSupplier(supplier);

        // 3. Mapear lotes y configurar relaciones
        List<PurchaseLot> lotEntities = dto.getLots().stream().map(lotDto -> {
            Product product = productRepository.findById(lotDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + lotDto.getProductId()));

            PurchaseLot lot = purchaseLotMapper.toEntity(lotDto);
            lot.setProduct(product);
            lot.setPurchaseOrder(purchaseOrder);

            // actualizar stock
            product.setStock(product.getStock() + lot.getQuantity());

            return lot;
        }).toList();

        // 4. Agregar lotes a la orden
        purchaseOrder.getLots().addAll(lotEntities);

        // 5. Guardar y retornar
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        PurchaseOrderResponse response = purchaseOrderMapper.toResponse(saved);
        return ServiceResult.success(response);
    }
}
