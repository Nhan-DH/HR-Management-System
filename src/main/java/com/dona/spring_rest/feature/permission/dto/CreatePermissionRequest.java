package com.dona.spring_rest.feature.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank(message = "Tên quyền không được để trống") @Size(min = 3, max = 100, message = "Tên quyền phải từ 3 đến 100 ký tự") String name,

        @NotBlank(message = "Đường dẫn API không được để trống") @Size(max = 255, message = "Đường dẫn API không được quá 255 ký tự") String apiPath,

        @NotBlank(message = "Phương thức HTTP không được để trống") @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH)$", message = "Phương thức HTTP phải là GET, POST, PUT, DELETE hoặc PATCH") String method,

        @NotBlank(message = "Mô-đun không được để trống") @Size(min = 2, max = 100, message = "Mô-đun phải từ 2 đến 100 ký tự") String module) {
}
