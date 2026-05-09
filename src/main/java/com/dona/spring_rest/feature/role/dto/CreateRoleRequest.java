package com.dona.spring_rest.feature.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank(message = "Tên vai trò không được để trống") @Size(min = 2, max = 100, message = "Tên vai trò phải từ 2 đến 100 ký tự") String name,

        @NotBlank(message = "Mô tả không được để trống") @Size(min = 5, max = 255, message = "Mô tả phải từ 5 đến 255 ký tự") String description) {
}
