package com.dona.spring_rest.feature.role.dto;

import com.dona.spring_rest.feature.role.Role;

import java.time.Instant;

public record RoleResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt) {
    public static RoleResponse fromEntity(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }
}
