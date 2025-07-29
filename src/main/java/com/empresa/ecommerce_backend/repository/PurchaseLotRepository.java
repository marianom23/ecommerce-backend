package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.PurchaseLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseLotRepository extends JpaRepository<PurchaseLot, Long> {

    List<PurchaseLot> findByProduct_IdOrderByIdDesc(Long productId);

    List<PurchaseLot> findByPurchaseOrder_Id(Long purchaseOrderId);

    // Buscar los lotes disponibles para un producto (si usás control de stock)
    List<PurchaseLot> findByProduct_IdAndQuantityGreaterThanOrderByIdAsc(Long productId, Integer minQty);
}
