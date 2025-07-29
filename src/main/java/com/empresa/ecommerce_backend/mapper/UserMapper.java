package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Mapear de DTO a entidad
    User toEntity(RegisterUserRequest request);

    // Respuesta luego del registro
    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    RegisterUserResponse toRegisterResponse(User user);

    // Mapeo de user + token a login response
    @Mapping(source = "token", target = "token")
    @Mapping(source = "user.email", target = "email")
    @Mapping(expression = "java(user.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(\"\"))", target = "role")
    LoginResponse toLoginResponse(User user, String token);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName", defaultValue = "Usuario")
    @Mapping(target = "lastName", source = "lastName", defaultValue = "OAuth")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "verified", constant = "true")
    @Mapping(target = "authProvider", expression = "java(mapProvider(dto.getProvider()))")
    @Mapping(target = "roles", ignore = true) // Se setea aparte
    User fromOAuthDto(OAuthCallbackRequest dto);

    default AuthProvider mapProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "azure-ad" -> AuthProvider.AZURE_AD;
            default -> AuthProvider.LOCAL;
        };
    }
}
