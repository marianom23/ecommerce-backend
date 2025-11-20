package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.UserMeResponse;   // 👈 NUEVO
import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.model.User;
import org.mapstruct.*;
import java.util.stream.Collectors;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface UserMapper {
    /* ========= Register ========= */

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "firstName", source = "firstName"),
            @Mapping(target = "lastName",  source = "lastName"),
            @Mapping(target = "email",     source = "email"),
            @Mapping(target = "password",  source = "password"),
            @Mapping(target = "verified",  constant = "false"),
            // 🔧 usa constant para evitar import en impl
            @Mapping(target = "authProvider", constant = "LOCAL")
    })
    User toEntity(RegisterUserRequest request);

    @Mappings({
            @Mapping(source = "id",    target = "id"),
            @Mapping(source = "email", target = "email")
    })
    RegisterUserResponse toRegisterResponse(User user);

    /* ========= Login ========= */

    @Mappings({
            @Mapping(source = "token",      target = "token"),
            @Mapping(source = "expiresAt",  target = "expiresAt"),
            @Mapping(target = "tokenType",  constant = "Bearer"),

            @Mapping(source = "user.id",         target = "id"),
            @Mapping(source = "user.email",      target = "email"),
            @Mapping(source = "user.firstName",  target = "firstName"),
            @Mapping(source = "user.lastName",   target = "lastName"),
            @Mapping(source = "user.verified",   target = "verified"),
            @Mapping(
                    target = "provider",
                    expression = "java(user.getAuthProvider()!=null ? user.getAuthProvider().name() : \"LOCAL\")"
            ),
            @Mapping(
                    target = "roles",
                    expression = "java(user.getRoles().stream().map(r -> r.getName().name()).toList())"
            ),
            @Mapping(target = "fullName", ignore = true)
    })
    LoginResponse toLoginResponse(User user, String token, Instant expiresAt);

    @AfterMapping
    default void fillFullName(@MappingTarget LoginResponse target, User user) {
        String fn = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String ln = user.getLastName() == null ? "" : user.getLastName().trim();
        target.setFullName((fn + " " + ln).trim());
    }

    /* ========= OAuth ========= */

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "email",        source = "email"),
            @Mapping(target = "firstName",    source = "firstName", defaultValue = "Usuario"),
            @Mapping(target = "lastName",     source = "lastName",  defaultValue = "OAuth"),
            @Mapping(target = "password",     ignore = true),
            @Mapping(target = "verified",     constant = "true"),
            @Mapping(target = "authProvider", expression = "java(mapProvider(dto.getProvider()))")
    })
    User fromOAuthDto(OAuthCallbackRequest dto);

    default AuthProvider mapProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "google"   -> AuthProvider.GOOGLE;
            case "azure-ad" -> AuthProvider.AZURE_AD;
            default         -> AuthProvider.LOCAL;
        };
    }

    /* ========= /b/me ========= */

    @Mappings({
            @Mapping(source = "id",        target = "id"),
            @Mapping(source = "email",     target = "email"),
            @Mapping(source = "firstName", target = "firstName"),
            @Mapping(source = "lastName",  target = "lastName"),
            @Mapping(source = "verified",  target = "verified"),
            @Mapping(
                    target = "roles",
                    expression = "java(user.getRoles().stream()" +
                            ".map(r -> r.getName().name())" +
                            ".collect(java.util.stream.Collectors.toSet()))"
            )
    })
    UserMeResponse toMeResponse(User user);

}
