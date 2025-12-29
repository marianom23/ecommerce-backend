package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductImage;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.repository.ProductImageRepository;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductImageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductImageRepository imageRepo;

    @Transactional
    @Override
    public ServiceResult<Void> addProductImages(Long productId, List<String> urls) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        int nextPos = imageRepo.findMaxPosition(productId, null).orElse(0);

        for (String url : urls) {
            ProductImage img = new ProductImage();
            img.setProduct(p);
            img.setVariant(null);     // imagen general del producto
            img.setUrl(url);
            img.setPosition(++nextPos);
            imageRepo.save(img);
        }
        return ServiceResult.noContent(); // 204 (tu Advice lo setea)
    }

    @Transactional
    @Override
    public ServiceResult<Void> addVariantImages(Long productId, Long variantId, List<String> urls) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        ProductVariant v = variantRepo.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));

        if (!Objects.equals(v.getProduct().getId(), productId)) {
            throw new IllegalArgumentException("Variant does not belong to product");
        }

        int nextPos = imageRepo.findMaxPosition(productId, variantId).orElse(0);

        for (String url : urls) {
            ProductImage img = new ProductImage();
            img.setProduct(p);
            img.setVariant(v);        // imagen de la variante
            img.setUrl(url);
            img.setPosition(++nextPos);
            imageRepo.save(img);
        }
        return ServiceResult.noContent();
    }

    @Transactional
    @Override
    public ServiceResult<Void> deleteImage(Long productId, Long imageId) {
        ProductImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found"));

        if (!Objects.equals(img.getProduct().getId(), productId)) {
            throw new IllegalArgumentException("Image does not belong to product");
        }

        imageRepo.delete(img);
        return ServiceResult.noContent();
    }

    @Transactional
    @Override
    public ServiceResult<Void> reorderImages(Long productId, List<Long> imageIdsInOrder) {
        int pos = 1;
        for (Long id : imageIdsInOrder) {
            ProductImage img = imageRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Image not found"));

            if (!Objects.equals(img.getProduct().getId(), productId)) {
                throw new IllegalArgumentException("Image does not belong to product");
            }

            img.setPosition(pos++);
            // al estar en contexto de persistencia, con @Transactional no hace falta save explícito
        }
        return ServiceResult.noContent();
    }

    @Transactional
    @Override
    public ServiceResult<Void> reorderVariantImages(Long productId, Long variantId, List<Long> imageIdsInOrder) {
        // Verificar que la variante existe y pertenece al producto
        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Variant not found"));
        
        if (!Objects.equals(variant.getProduct().getId(), productId)) {
            throw new IllegalArgumentException("Variant does not belong to product");
        }

        int pos = 1;
        for (Long id : imageIdsInOrder) {
            ProductImage img = imageRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Image not found"));

            // Verificar que la imagen pertenece a esta variante
            if (!Objects.equals(img.getProduct().getId(), productId)) {
                throw new IllegalArgumentException("Image does not belong to product");
            }
            if (img.getVariant() == null || !Objects.equals(img.getVariant().getId(), variantId)) {
                throw new IllegalArgumentException("Image does not belong to variant");
            }

            img.setPosition(pos++);
        }
        return ServiceResult.noContent();
    }
}
