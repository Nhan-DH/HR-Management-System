package com.dona.spring_rest.feature.role;

import com.dona.spring_rest.dto.ApiResponse;
import com.dona.spring_rest.feature.role.dto.CreateRoleRequest;
import com.dona.spring_rest.feature.role.dto.RoleResponse;
import com.dona.spring_rest.feature.role.dto.UpdateRoleRequest;
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
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles()
                .stream()
                .map(RoleResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Danh sách vai trò được lấy thành công", roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        RoleResponse response = RoleResponse.fromEntity(role);

        return ResponseEntity.ok(ApiResponse.success("Vai trò được lấy thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {

        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());

        Role createdRole = roleService.createRole(role);
        RoleResponse response = RoleResponse.fromEntity(createdRole);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Vai trò được tạo thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {

        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());

        Role updatedRole = roleService.updateRole(id, role);
        RoleResponse response = RoleResponse.fromEntity(updatedRole);

        return ResponseEntity.ok(ApiResponse.success("Vai trò được cập nhật thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);

        return ResponseEntity.noContent().build();
    }
}
