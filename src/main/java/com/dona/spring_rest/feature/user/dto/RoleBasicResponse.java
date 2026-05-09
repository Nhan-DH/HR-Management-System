package com.dona.spring_rest.feature.user.dto;

import com.dona.spring_rest.feature.role.Role;

public record RoleBasicResponse(
        Long id,
        String name) {
    public static RoleBasicResponse fromEntity(Role role) {
        return new RoleBasicResponse(role.getId(), role.getName());
    }
}
