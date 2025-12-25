package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.SupplierRequest;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {

    ServiceResult<List<SupplierResponse>> getAll();

    ServiceResult<SupplierResponse> getById(Long id);

    ServiceResult<SupplierResponse> create(SupplierRequest request);

    ServiceResult<SupplierResponse> update(Long id, SupplierRequest request);

    ServiceResult<Void> delete(Long id);
}
