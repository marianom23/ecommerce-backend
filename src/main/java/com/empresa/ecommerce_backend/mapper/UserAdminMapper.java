package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.response.UserAdminResponse;
import com.empresa.ecommerce_backend.model.Role;
import com.empresa.ecommerce_backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserAdminMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    @Mapping(target = "orderCount", expression = "java(user.getOrders() != null ? user.getOrders().size() : 0)")
    UserAdminResponse toAdminResponse(User user);

    @Named("mapRoles")
    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}
