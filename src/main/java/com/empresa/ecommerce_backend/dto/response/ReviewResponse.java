package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
}
