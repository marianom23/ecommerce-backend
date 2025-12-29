package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.BankAccountRequest;
import com.empresa.ecommerce_backend.dto.response.BankAccountResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface BankAccountService {
    ServiceResult<List<BankAccountResponse>> getAllActive();
    ServiceResult<List<BankAccountResponse>> getAllAdmin(); // includes inactive
    ServiceResult<BankAccountResponse> getById(Long id);
    ServiceResult<BankAccountResponse> create(BankAccountRequest request);
    ServiceResult<BankAccountResponse> update(Long id, BankAccountRequest request);
    ServiceResult<Void> delete(Long id);
    ServiceResult<BankAccountResponse> toggleActive(Long id);
}
