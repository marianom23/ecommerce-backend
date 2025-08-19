package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface ProductImageService {

    // Imágenes generales del producto
    ServiceResult<Void> addProductImages(Long productId, List<String> urls);

    // Imágenes de una variante
    ServiceResult<Void> addVariantImages(Long productId, Long variantId, List<String> urls);

    // Borrar una imagen
    ServiceResult<Void> deleteImage(Long productId, Long imageId);

    // Reordenar imágenes
    ServiceResult<Void> reorderImages(Long productId, List<Long> imageIdsInOrder);
}
