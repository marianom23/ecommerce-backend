package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateProfileRequest;
import com.empresa.ecommerce_backend.dto.request.ChangePasswordRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.UserMeResponse;
import org.springframework.security.core.Authentication;

public interface UserService {

    ServiceResult<RegisterUserResponse> registerUser(RegisterUserRequest dto);

    ServiceResult<Void> verifyEmail(String token);

    ServiceResult<LoginResponse> login(LoginRequest request, String ip);

    ServiceResult<UserMeResponse> getProfile(Authentication authentication);

    ServiceResult<UserMeResponse> updateProfile(Long userId, UpdateProfileRequest dto);

    ServiceResult<Void> changePassword(Long userId, ChangePasswordRequest dto);
}