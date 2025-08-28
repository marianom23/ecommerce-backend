// AddressService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.AddressRequest;
import com.empresa.ecommerce_backend.dto.response.AddressResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AddressType;


import java.util.List;

// AddressService.java
public interface AddressService {
    ServiceResult<List<AddressResponse>> listForCurrentUser(AddressType type);
    ServiceResult<AddressResponse> createForCurrentUser(AddressRequest dto);
    ServiceResult<AddressResponse> updateForCurrentUser(Long id, AddressRequest dto);
    ServiceResult<Void> deleteForCurrentUser(Long id);
    ServiceResult<AddressResponse> touchUse(Long id);
}
