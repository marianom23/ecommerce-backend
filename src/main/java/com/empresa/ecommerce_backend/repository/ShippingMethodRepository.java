// repository/ShippingMethodRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;

import com.empresa.ecommerce_backend.model.ShippingMethod;

public interface ShippingMethodRepository extends BaseRepository<ShippingMethod, Long> {

    List<ShippingMethod> findByActiveTrueOrderByCostAsc();
}
