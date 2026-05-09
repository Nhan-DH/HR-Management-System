package com.dona.spring_rest.feature.permission;

import com.dona.spring_rest.dto.ApiResponse;
import com.dona.spring_rest.feature.permission.dto.CreatePermissionRequest;
import com.dona.spring_rest.feature.permission.dto.PermissionResponse;
import com.dona.spring_rest.feature.permission.dto.UpdatePermissionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions()
                .stream()
                .map(PermissionResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Danh sách quyền được lấy thành công", permissions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        Permission permission = permissionService.getPermissionById(id);
        PermissionResponse response = PermissionResponse.fromEntity(permission);

        return ResponseEntity.ok(ApiResponse.success("Quyền được lấy thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {

        Permission permission = new Permission();
        permission.setName(request.name());
        permission.setApiPath(request.apiPath());
        permission.setMethod(request.method());
        permission.setModule(request.module());

        Permission createdPermission = permissionService.createPermission(permission);
        PermissionResponse response = PermissionResponse.fromEntity(createdPermission);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Quyền được tạo thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionRequest request) {

        Permission permission = new Permission();
        permission.setName(request.name());
        permission.setApiPath(request.apiPath());
        permission.setMethod(request.method());
        permission.setModule(request.module());

        Permission updatedPermission = permissionService.updatePermission(id, permission);
        PermissionResponse response = PermissionResponse.fromEntity(updatedPermission);

        return ResponseEntity.ok(ApiResponse.success("Quyền được cập nhật thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);

        return ResponseEntity.noContent().build();
    }
}
