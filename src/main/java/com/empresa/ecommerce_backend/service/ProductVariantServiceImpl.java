package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ProductVariantRequest;
import com.empresa.ecommerce_backend.dto.response.ProductVariantResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.mapper.ProductVariantMapper;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductVariantService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<ProductVariantResponse>> listByProduct(Long productId) {
        ensureProductExists(productId);
        var variants = variantRepository.findAllByProductId(productId)
                .stream().map(mapper::toResponse).toList();
        return ServiceResult.ok(variants);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<ProductVariantResponse> getOne(Long productId, Long variantId) {
        var variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada"));
        return ServiceResult.ok(mapper.toResponse(variant));
    }

    @Override
    @Transactional
    public ServiceResult<ProductVariantResponse> create(Long productId, ProductVariantRequest req) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        if (variantRepository.existsBySku(req.getSku())) {
            return ServiceResult.error(HttpStatus.CONFLICT, "SKU de variante ya existe.");
        }

        // Validación condicional
        validateFulfillment(req);

        var v = mapper.toEntity(req);
        v.setProduct(product);

        // Asegurar defaults si es digital
        if (v.getFulfillmentType() == com.empresa.ecommerce_backend.enums.FulfillmentType.DIGITAL_ON_DEMAND
            || v.getFulfillmentType() == com.empresa.ecommerce_backend.enums.FulfillmentType.DIGITAL_INSTANT) {
            v.setWeightKg(null);
            v.setLengthCm(null);
            v.setWidthCm(null);
            v.setHeightCm(null);
            // Para DIGITAL_INSTANT quizás sí quieras controlar stock, pero para ON_DEMAND no.
            // Dejemos que si viene null, se ponga en 0.
            if (v.getStock() == null) v.setStock(0);
        }

        var saved = variantRepository.save(v);
        return ServiceResult.created(mapper.toResponse(saved));
    }

    private void validateFulfillment(ProductVariantRequest req) {
        // Default a PHYSICAL si viene null
        if (req.getFulfillmentType() == null) {
            req.setFulfillmentType(com.empresa.ecommerce_backend.enums.FulfillmentType.PHYSICAL);
        }

        if (req.getFulfillmentType() == com.empresa.ecommerce_backend.enums.FulfillmentType.PHYSICAL) {
            if (req.getStock() == null) throw new IllegalArgumentException("Stock es requerido para productos físicos");
            if (req.getWeightKg() == null) throw new IllegalArgumentException("Peso es requerido para productos físicos");
            if (req.getLengthCm() == null) throw new IllegalArgumentException("Largo es requerido para productos físicos");
            if (req.getWidthCm() == null) throw new IllegalArgumentException("Ancho es requerido para productos físicos");
            if (req.getHeightCm() == null) throw new IllegalArgumentException("Alto es requerido para productos físicos");
        }
    }

    @Override
    @Transactional
    public ServiceResult<ProductVariantResponse> update(Long productId, Long variantId, ProductVariantRequest req) {
        var variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada"));

        if (req.getSku() != null && !req.getSku().equals(variant.getSku())
                && variantRepository.existsBySku(req.getSku())) {
            return ServiceResult.error(HttpStatus.CONFLICT, "SKU de variante ya existe.");
        }

        mapper.updateEntityFromRequest(req, variant);
        var saved = variantRepository.save(variant);
        return ServiceResult.ok(mapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> delete(Long productId, Long variantId) {
        var variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new EntityNotFoundException("Variante no encontrada"));

        long remaining = variantRepository.countByProductId(productId);
        if (remaining <= 1) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "No puede eliminar la última variante del producto. Debe existir al menos una variante.");
        }

        variantRepository.delete(variant);
        return ServiceResult.noContent();
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Producto no encontrado");
        }
    }
}
