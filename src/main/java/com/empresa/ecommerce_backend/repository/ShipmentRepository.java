// repository/ShipmentRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.enums.ShipmentStatus;
import com.empresa.ecommerce_backend.model.Shipment;

public interface ShipmentRepository extends BaseRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByOrder_Id(Long orderId);

    List<Shipment> findByStatus(ShipmentStatus status);
}
