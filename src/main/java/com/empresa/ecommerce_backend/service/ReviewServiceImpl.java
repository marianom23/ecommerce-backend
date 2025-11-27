package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ReviewRequest;
import com.empresa.ecommerce_backend.dto.response.ReviewResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.ReviewMapper;
import com.empresa.ecommerce_backend.model.Review;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ReviewRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.interfaces.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ServiceResult<ReviewResponse> createReview(Long userId, ReviewRequest dto) {
        // Verificar que el producto existe
        var product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        // Verificar que el usuario existe
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Verificar que el usuario no haya hecho ya una review de este producto
        if (reviewRepository.existsByUser_IdAndProduct_Id(userId, dto.getProductId())) {
            return ServiceResult.error(
                    HttpStatus.CONFLICT,
                    "Ya has hecho una review de este producto. Puedes editarla si lo deseas."
            );
        }

        // Crear la review
        var review = reviewMapper.toEntity(dto, userId);
        review.setUser(user);
        review.setProduct(product);
        
        var saved = reviewRepository.save(review);
        
        return ServiceResult.created(reviewMapper.toResponse(saved));
    }


    @Override
    @Transactional
    public ServiceResult<ReviewResponse> updateReview(Long userId, Long reviewId, ReviewRequest dto) {
        // Buscar la review
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Review no encontrada"));

        // Verificar que la review pertenece al usuario
        if (!review.getUser().getId().equals(userId)) {
            return ServiceResult.error(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para editar esta review"
            );
        }

        // Actualizar los campos
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setReviewDate(LocalDateTime.now());

        var saved = reviewRepository.save(review);
        return ServiceResult.ok(reviewMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteReview(Long userId, Long reviewId) {
        // Buscar la review
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Review no encontrada"));

        // Verificar que la review pertenece al usuario
        if (!review.getUser().getId().equals(userId)) {
            return ServiceResult.error(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para eliminar esta review"
            );
        }

        reviewRepository.delete(review);
        return ServiceResult.noContent();
    }

    @Override
    public ServiceResult<List<ReviewResponse>> getReviewsByProduct(Long productId) {
        // Verificar que el producto existe
        if (!productRepository.existsById(productId)) {
            throw new RecursoNoEncontradoException("Producto no encontrado");
        }

        var reviews = reviewRepository.findByProduct_IdOrderByReviewDateDesc(productId);
        var responses = reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();
        
        return ServiceResult.ok(responses);
    }

    @Override
    public ServiceResult<List<ReviewResponse>> getReviewsByUser(Long userId) {
        // Verificar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }

        var reviews = reviewRepository.findByUser_IdOrderByReviewDateDesc(userId);
        var responses = reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();
        
        return ServiceResult.ok(responses);
    }

    @Override
    public ServiceResult<ReviewResponse> getUserReviewForProduct(Long userId, Long productId) {
        var review = reviewRepository.findByUser_IdAndProduct_Id(userId, productId);
        
        return review.map(r -> ServiceResult.ok(reviewMapper.toResponse(r)))
                .orElse(ServiceResult.ok(null));
    }
}
