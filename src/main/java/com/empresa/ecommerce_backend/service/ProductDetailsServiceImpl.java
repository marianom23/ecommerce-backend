package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.ProductDetailsResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.ProductDetailsMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductDetailsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailsServiceImpl implements ProductDetailsService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductDetailsMapper productDetailsMapper;

    @Override
    public ServiceResult<ProductDetailsResponse> getDetails(Long productId) {
        Product product = productRepository.findWithDetailsById(productId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        List<ProductVariant> variants =
                productVariantRepository.findAllByProductIdOrderByIdAsc(productId);

        ProductDetailsResponse dto = productDetailsMapper.toDetails(product, variants);
        return ServiceResult.ok(dto);
    }
}
