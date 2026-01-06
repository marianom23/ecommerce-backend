package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.UserAdminResponse;
import com.empresa.ecommerce_backend.mapper.UserAdminMapper;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.OrderRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.repository.projection.OrderSummaryProjection;
import com.empresa.ecommerce_backend.service.interfaces.AdminUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserAdminMapper userAdminMapper;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<PaginatedResponse<UserAdminResponse>> getUsers(String query, Pageable pageable) {
        Page<User> page;
        if (query != null && !query.isBlank()) {
            page = userRepository.searchUsers(query, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        List<UserAdminResponse> dtos = page.map(userAdminMapper::toAdminResponse).getContent();
        
        return ServiceResult.ok(new PaginatedResponse<>(
                dtos,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<UserAdminResponse> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ServiceResult.ok(userAdminMapper.toAdminResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<PaginatedResponse<OrderSummaryProjection>> getUserOrders(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        
        // Reusamos la proyección que ya existe para listar órdenes
        Page<OrderSummaryProjection> page = orderRepository.findSummariesByUserId(userId, pageable);
        
        return ServiceResult.ok(new PaginatedResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        ));
    }

    @Override
    @Transactional
    public ServiceResult<Void> toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setVerified(!user.isVerified());
        userRepository.save(user);
        return ServiceResult.noContent();
    }
}
