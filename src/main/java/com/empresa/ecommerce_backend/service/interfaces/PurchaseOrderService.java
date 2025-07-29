package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.PurchaseOrderRequest;
import com.empresa.ecommerce_backend.dto.response.PurchaseOrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;


public interface PurchaseOrderService {
    ServiceResult<PurchaseOrderResponse> createPurchaseOrder(PurchaseOrderRequest dto);
}
