package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findBySupplierContainingIgnoreCase(String supplier);

    List<PurchaseOrder> findByPurchaseDateBetween(LocalDate from, LocalDate to);
}
