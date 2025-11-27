package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ReviewRequest;
import com.empresa.ecommerce_backend.dto.response.ReviewResponse;
import com.empresa.ecommerce_backend.model.Review;
import com.empresa.ecommerce_backend.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "rating", source = "dto.rating")
    @Mapping(target = "comment", source = "dto.comment")
    @Mapping(target = "reviewDate", expression = "java(java.time.LocalDateTime.now())")
    Review toEntity(ReviewRequest dto, Long userId);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(getUserFullName(review.getUser()))")
    ReviewResponse toResponse(Review review);

    default String getUserFullName(User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }
}
