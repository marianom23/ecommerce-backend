package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.BankAccountRequest;
import com.empresa.ecommerce_backend.dto.response.BankAccountResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.BankAccountMapper;
import com.empresa.ecommerce_backend.model.BankAccount;
import com.empresa.ecommerce_backend.repository.BankAccountRepository;
import com.empresa.ecommerce_backend.service.interfaces.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repository;
    private final BankAccountMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<BankAccountResponse>> getAllActive() {
        var list = repository.findByActiveTrue().stream()
                .map(mapper::toResponse)
                .toList();
        return ServiceResult.ok(list);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<BankAccountResponse>> getAllAdmin() {
        var list = repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
        return ServiceResult.ok(list);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<BankAccountResponse> getById(Long id) {
        BankAccount account = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta bancaria no encontrada"));
        return ServiceResult.ok(mapper.toResponse(account));
    }

    @Override
    @Transactional
    public ServiceResult<BankAccountResponse> create(BankAccountRequest request) {
        BankAccount entity = mapper.toEntity(request);
        BankAccount saved = repository.save(entity);
        return ServiceResult.created(mapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<BankAccountResponse> update(Long id, BankAccountRequest request) {
        BankAccount existing = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta bancaria no encontrada"));
        
        mapper.updateEntityFromRequest(request, existing);
        
        return ServiceResult.ok(mapper.toResponse(repository.save(existing)));
    }

    @Override
    @Transactional
    public ServiceResult<Void> delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cuenta bancaria no encontrada");
        }
        repository.deleteById(id);
        return ServiceResult.noContent();
    }

    @Override
    @Transactional
    public ServiceResult<BankAccountResponse> toggleActive(Long id) {
        BankAccount existing = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta bancaria no encontrada"));
        existing.setActive(!existing.isActive());
        return ServiceResult.ok(mapper.toResponse(repository.save(existing)));
    }
}
