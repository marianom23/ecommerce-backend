// src/main/java/com/empresa/ecommerce_backend/repository/LoginAttemptRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.LoginAttempt;

public interface LoginAttemptRepository extends BaseRepository<LoginAttempt, Long> {

    List<LoginAttempt> findTop5ByUser_IdOrderByAttemptAtDesc(Long userId);

    long countByUser_IdAndSuccessFalseAndAttemptAtAfter(Long userId, LocalDateTime since);

    Optional<LoginAttempt> findFirstByIpAddressOrderByAttemptAtDesc(String ipAddress);

    @EntityGraph(attributePaths = "user")
    List<LoginAttempt> findAllByUser_IdOrderByAttemptAtDesc(Long userId);
}
