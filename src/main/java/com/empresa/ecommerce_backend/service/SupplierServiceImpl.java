package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.SupplierRequest;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.SupplierResponse;
import com.empresa.ecommerce_backend.mapper.SupplierMapper;
import com.empresa.ecommerce_backend.model.Supplier;
import com.empresa.ecommerce_backend.repository.SupplierRepository;
import com.empresa.ecommerce_backend.service.interfaces.SupplierService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<SupplierResponse>> getAll() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return ServiceResult.ok(supplierMapper.toResponseList(suppliers));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<SupplierResponse> getById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));
        return ServiceResult.ok(supplierMapper.toResponse(supplier));
    }

    @Override
    @Transactional
    public ServiceResult<SupplierResponse> create(SupplierRequest request) {
        if (supplierRepository.existsByName(request.getName())) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Ya existe un proveedor con ese nombre");
        }
        Supplier supplier = supplierMapper.toEntity(request);
        Supplier saved = supplierRepository.save(supplier);
        return ServiceResult.created(supplierMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<SupplierResponse> update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado"));

        // Si cambia el nombre, verificar que no exista otro
        if (request.getName() != null && !request.getName().equals(supplier.getName())) {
            if (supplierRepository.existsByName(request.getName())) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST, "Ya existe un proveedor con ese nombre");
            }
        }

        supplierMapper.updateFromRequest(request, supplier);
        Supplier saved = supplierRepository.save(supplier);
        return ServiceResult.ok(supplierMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            return ServiceResult.error(HttpStatus.NOT_FOUND, "Proveedor no encontrado");
        }
        // Validar si tiene productos o órdenes asociadas si fuera necesario, 
        // pero por ahora solo borramos (o dejamos que la FK falle si hay restricciones)
        try {
            supplierRepository.deleteById(id);
            return ServiceResult.ok(null);
        } catch (Exception e) {
            return ServiceResult.error(HttpStatus.CONFLICT, "No se puede eliminar el proveedor porque tiene datos asociados");
        }
    }
}
