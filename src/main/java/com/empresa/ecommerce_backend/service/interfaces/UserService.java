package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

public interface UserService {

    ServiceResult<RegisterUserResponse> registerUser(RegisterUserRequest dto);

    ServiceResult<Void> verifyEmail(String token);

    ServiceResult<LoginResponse> login(LoginRequest request, String ip);


}