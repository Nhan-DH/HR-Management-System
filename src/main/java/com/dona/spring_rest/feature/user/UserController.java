package com.dona.spring_rest.feature.user;

import com.dona.spring_rest.dto.ApiResponse;
import com.dona.spring_rest.feature.company.Company;
import com.dona.spring_rest.feature.user.dto.CreateUserRequest;
import com.dona.spring_rest.feature.user.dto.UpdateUserRequest;
import com.dona.spring_rest.feature.user.dto.UserResponse;
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
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Danh sách người dùng được lấy thành công", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserResponse response = UserResponse.fromEntity(user);

        return ResponseEntity.ok(ApiResponse.success("Người dùng được lấy thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setAge(request.age());
        user.setAddress(request.address());
        user.setGender(request.gender());

        if (request.companyId() != null) {
            Company company = new Company();
            company.setId(request.companyId());
            user.setCompany(company);
        }

        User createdUser = userService.createUser(user, request.roleIds());
        UserResponse response = UserResponse.fromEntity(createdUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Người dùng được tạo thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setAge(request.age());
        user.setAddress(request.address());
        user.setGender(request.gender());
        user.setPassword(request.password());

        if (request.companyId() != null) {
            Company company = new Company();
            company.setId(request.companyId());
            user.setCompany(company);
        }

        User updatedUser = userService.updateUser(id, user, request.roleIds());
        UserResponse response = UserResponse.fromEntity(updatedUser);

        return ResponseEntity.ok(ApiResponse.success("Người dùng được cập nhật thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
