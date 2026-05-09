package com.dona.spring_rest.feature.permission.dto;

import com.dona.spring_rest.feature.permission.Permission;

import java.time.Instant;

public record PermissionResponse(
        Long id,
        String name,
        String apiPath,
        String method,
        String module,
        Instant createdAt,
        Instant updatedAt) {
    public static PermissionResponse fromEntity(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getCreatedAt(),
                permission.getUpdatedAt());
    }
}
