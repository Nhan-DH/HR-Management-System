package com.dona.spring_rest.feature.permission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên quyền không được để trống")
    @Size(min = 3, max = 100, message = "Tên quyền phải từ 3 đến 100 ký tự")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Đường dẫn API không được để trống")
    @Size(max = 255, message = "Đường dẫn API không được quá 255 ký tự")
    @Column(nullable = false)
    private String apiPath;

    @NotBlank(message = "Phương thức HTTP không được để trống")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH)$", message = "Phương thức HTTP phải là GET, POST, PUT, DELETE hoặc PATCH")
    @Column(nullable = false)
    @Size(max = 10, message = "Phương thức HTTP không được quá 10 ký tự")
    private String method;

    @NotBlank(message = "Mô-đun không được để trống")
    @Size(min = 2, max = 100, message = "Mô-đun phải từ 2 đến 100 ký tự")
    @Column(nullable = false)
    private String module;

    private Instant createdAt;
    private Instant updatedAt;

    public Permission() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
