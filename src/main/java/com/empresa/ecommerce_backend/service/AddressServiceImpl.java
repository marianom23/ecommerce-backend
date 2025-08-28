// AddressServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.AddressRequest;
import com.empresa.ecommerce_backend.dto.response.AddressResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AddressType;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.AddressMapper;
import com.empresa.ecommerce_backend.model.Address;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.AddressRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.interfaces.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

// AddressServiceImpl.java
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper mapper;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) throw new IllegalArgumentException("No autenticado.");
        try {
            return (Long) auth.getPrincipal().getClass().getMethod("getId").invoke(auth.getPrincipal());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener ID de usuario autenticado");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<AddressResponse>> listForCurrentUser(AddressType type) {
        Long uid = currentUserId();
        var list = addressRepository.findOrderedForUser(uid, type)
                .stream().map(mapper::toResponse).toList();
        return ServiceResult.ok(list);
    }

    @Override
    @Transactional
    public ServiceResult<AddressResponse> createForCurrentUser(AddressRequest dto) {
        Long uid = currentUserId();
        User u = userRepository.findById(uid).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Address entity = mapper.toEntity(dto);
        entity.setUser(u);
        entity.setLastUsedAt(LocalDateTime.now());

        Address saved = addressRepository.save(entity);
        return ServiceResult.created(mapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<AddressResponse> updateForCurrentUser(Long id, AddressRequest dto) {
        Long uid = currentUserId();
        Address existing = addressRepository.findByIdAndUserId(id, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada"));

        mapper.updateFromDto(dto, existing); // MapStruct actualiza in-place
        Address saved = addressRepository.save(existing);
        return ServiceResult.ok(mapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteForCurrentUser(Long id) {
        Long uid = currentUserId();
        Address a = addressRepository.findByIdAndUserId(id, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada"));
        addressRepository.delete(a);
        return ServiceResult.noContent();
    }

    @Override
    @Transactional
    public ServiceResult<AddressResponse> touchUse(Long id) {
        Long uid = currentUserId();
        Address a = addressRepository.findByIdAndUserId(id, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada"));
        a.setLastUsedAt(LocalDateTime.now());
        Address saved = addressRepository.save(a);
        return ServiceResult.ok(mapper.toResponse(saved));
    }
}
