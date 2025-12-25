package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.BankAccountRequest;
import com.empresa.ecommerce_backend.dto.response.BankAccountResponse;
import com.empresa.ecommerce_backend.model.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @org.mapstruct.Mapping(target = "id", ignore = true)
    BankAccount toEntity(BankAccountRequest request);

    BankAccountResponse toResponse(BankAccount entity);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(BankAccountRequest request, @MappingTarget BankAccount entity);
}
