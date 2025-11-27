package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.ReviewRequest;
import com.empresa.ecommerce_backend.dto.response.ReviewResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.security.AuthUser;
import com.empresa.ecommerce_backend.service.interfaces.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "API para gestión de reviews de productos")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear una review", description = "Permite a un usuario autenticado crear una review de un producto")
    public ServiceResult<ReviewResponse> createReview(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody ReviewRequest dto
    ) {
        return reviewService.createReview(user.getId(), dto);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar una review", description = "Permite a un usuario actualizar su propia review")
    public ServiceResult<ReviewResponse> updateReview(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest dto
    ) {
        return reviewService.updateReview(user.getId(), reviewId, dto);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar una review", description = "Permite a un usuario eliminar su propia review")
    public ServiceResult<Void> deleteReview(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long reviewId
    ) {
        return reviewService.deleteReview(user.getId(), reviewId);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Obtener reviews de un producto", description = "Obtiene todas las reviews de un producto específico")
    public ServiceResult<List<ReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId
    ) {
        return reviewService.getReviewsByProduct(productId);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener reviews de un usuario", description = "Obtiene todas las reviews hechas por un usuario")
    public ServiceResult<List<ReviewResponse>> getReviewsByUser(
            @PathVariable Long userId
    ) {
        return reviewService.getReviewsByUser(userId);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener mis reviews", description = "Obtiene todas las reviews del usuario autenticado")
    public ServiceResult<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal AuthUser user
    ) {
        return reviewService.getReviewsByUser(user.getId());
    }

    @GetMapping("/me/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener mi review de un producto", description = "Obtiene la review del usuario autenticado para un producto específico")
    public ServiceResult<ReviewResponse> getMyReviewForProduct(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long productId
    ) {
        return reviewService.getUserReviewForProduct(user.getId(), productId);
    }
}
