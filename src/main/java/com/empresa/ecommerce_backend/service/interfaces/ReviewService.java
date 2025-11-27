package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.ReviewRequest;
import com.empresa.ecommerce_backend.dto.response.ReviewResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface ReviewService {
    
    /**
     * Crear una review para un producto
     * @param userId ID del usuario autenticado
     * @param dto Datos de la review
     * @return ServiceResult con la review creada
     */
    ServiceResult<ReviewResponse> createReview(Long userId, ReviewRequest dto);
    
    /**
     * Actualizar una review existente
     * @param userId ID del usuario autenticado
     * @param reviewId ID de la review a actualizar
     * @param dto Nuevos datos de la review
     * @return ServiceResult con la review actualizada
     */
    ServiceResult<ReviewResponse> updateReview(Long userId, Long reviewId, ReviewRequest dto);
    
    /**
     * Eliminar una review
     * @param userId ID del usuario autenticado
     * @param reviewId ID de la review a eliminar
     * @return ServiceResult vacío
     */
    ServiceResult<Void> deleteReview(Long userId, Long reviewId);
    
    /**
     * Obtener todas las reviews de un producto
     * @param productId ID del producto
     * @return ServiceResult con lista de reviews
     */
    ServiceResult<List<ReviewResponse>> getReviewsByProduct(Long productId);
    
    /**
     * Obtener todas las reviews de un usuario
     * @param userId ID del usuario
     * @return ServiceResult con lista de reviews
     */
    ServiceResult<List<ReviewResponse>> getReviewsByUser(Long userId);
    
    /**
     * Obtener la review de un usuario para un producto específico
     * @param userId ID del usuario
     * @param productId ID del producto
     * @return ServiceResult con la review o vacío si no existe
     */
    ServiceResult<ReviewResponse> getUserReviewForProduct(Long userId, Long productId);

    /**
     * Obtener las mejores reviews (4 o 5 estrellas)
     * @param limit Cantidad máxima de reviews a devolver
     * @return ServiceResult con lista de reviews
     */
    ServiceResult<List<ReviewResponse>> getBestReviews(int limit);
}
