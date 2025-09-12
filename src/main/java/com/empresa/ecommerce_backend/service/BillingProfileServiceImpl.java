// src/main/java/com/empresa/ecommerce_backend/service/BillingProfileServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.BillingProfileRequest;
import com.empresa.ecommerce_backend.dto.response.BillingProfileResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.BillingProfileMapper;
import com.empresa.ecommerce_backend.model.Address;
import com.empresa.ecommerce_backend.model.BillingProfile;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.AddressRepository;
import com.empresa.ecommerce_backend.repository.BillingProfileRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.interfaces.BillingProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingProfileServiceImpl implements BillingProfileService {

    private final BillingProfileRepository repo;
    private final AddressRepository addressRepo;
    private final UserRepository userRepo;
    private final BillingProfileMapper mapper;

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
    public ServiceResult<List<BillingProfileResponse>> listForCurrentUser() {
        Long uid = currentUserId();
        var list = repo.findAllForUser(uid).stream().map(mapper::toResponse).toList();
        return ServiceResult.ok(list);
    }

    @Override
    @Transactional
    public ServiceResult<BillingProfileResponse> createForCurrentUser(BillingProfileRequest dto) {
        Long uid = currentUserId();
        User user = userRepo.findById(uid).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Address addr = addressRepo.findById(dto.getBillingAddressId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección de facturación no encontrada"));

        // (opcional) validar que addr.user.id == uid y addr.type == BILLING

        BillingProfile entity = mapper.toEntity(dto);
        entity.setUser(user);
        entity.setBillingAddress(addr);

        boolean wantDefault = Boolean.TRUE.equals(dto.getIsDefault());
        if (wantDefault) {
            repo.clearDefaultForUser(uid);
            entity.setDefaultProfile(true);
        }

        BillingProfile saved = repo.save(entity);
        return ServiceResult.created(mapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<BillingProfileResponse> updateForCurrentUser(Long id, BillingProfileRequest dto) {
        Long uid = currentUserId();
        BillingProfile existing = repo.findByIdAndUserId(id, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de facturación no encontrado"));

        mapper.updateFromDto(dto, existing);

        // ¿cambió la dirección?
        if (dto.getBillingAddressId() != null) {
            Address addr = addressRepo.findById(dto.getBillingAddressId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Dirección de facturación no encontrada"));
            // (opcional) validar dueño y tipo
            existing.setBillingAddress(addr);
        }

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            repo.clearDefaultForUser(uid);
            existing.setDefaultProfile(true);
        }

        BillingProfile saved = repo.save(existing);
        return ServiceResult.ok(mapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteForCurrentUser(Long id) {
        Long uid = currentUserId();
        BillingProfile bp = repo.findByIdAndUserId(id, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de facturación no encontrado"));
        repo.delete(bp);
        return ServiceResult.noContent();
    }

    @Override
    @Transactional
    public ServiceResult<BillingProfileResponse> setDefault(Long id) {
        Long uid = currentUserId();
        BillingProfile bp = repo.findByIdAndUserId(id, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de facturación no encontrado"));
        repo.clearDefaultForUser(uid);
        bp.setDefaultProfile(true);
        BillingProfile saved = repo.save(bp);
        return ServiceResult.ok(mapper.toResponse(saved));
    }
}
