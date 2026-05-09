package com.dona.spring_rest.feature.user.dto;

import com.dona.spring_rest.feature.user.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateUserRequest(
        @NotBlank(message = "Tên người dùng không được để trống") @Size(min = 2, max = 100, message = "Tên người dùng phải từ 2 đến 100 ký tự") String name,

        @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") String email,

        Integer age,

        String address,

        Gender gender,

        Long companyId,

        List<Long> roleIds,

        String password) {
}
