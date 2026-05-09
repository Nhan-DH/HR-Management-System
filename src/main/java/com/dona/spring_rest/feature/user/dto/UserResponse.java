package com.dona.spring_rest.feature.user.dto;

import com.dona.spring_rest.feature.user.Gender;
import com.dona.spring_rest.feature.user.User;

import java.time.Instant;
import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        Integer age,
        String address,
        Gender gender,
        String avatar,
        CompanyBasicResponse company,
        List<RoleBasicResponse> roles,
        Instant createdAt,
        Instant updatedAt) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getAddress(),
                user.getGender(),
                user.getAvatar(),
                CompanyBasicResponse.fromEntity(user.getCompany()),
                user.getRoles() != null ? user.getRoles().stream().map(RoleBasicResponse::fromEntity).toList()
                        : List.of(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
