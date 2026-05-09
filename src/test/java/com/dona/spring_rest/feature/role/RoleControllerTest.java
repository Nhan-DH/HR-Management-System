package com.dona.spring_rest.feature.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import com.dona.spring_rest.feature.role.dto.CreateRoleRequest;
import com.dona.spring_rest.feature.role.dto.RoleResponse;
import com.dona.spring_rest.feature.role.dto.UpdateRoleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Arrays;

@SpringBootTest
@DisplayName("RoleController Integration Tests")
class RoleControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RoleRepository roleRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RoleService roleService;

    private Role testRole;
    private CreateRoleRequest validRequest;
    private UpdateRoleRequest validUpdateRequest;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        // Create a real service with mocked repository
        roleService = new RoleServiceImpl(roleRepository);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ADMIN");
        testRole.setDescription("Administrator role with full permissions");
        testRole.setCreatedAt(Instant.now());
        testRole.setUpdatedAt(Instant.now());

        validRequest = new CreateRoleRequest(
                "ADMIN",
                "Administrator role with full permissions");

        validUpdateRequest = new UpdateRoleRequest(
                "MANAGER",
                "Manager role with limited permissions");
    }

    // ==================== GET /api/v1/roles Tests ====================

    @Test
    @DisplayName("GET /api/v1/roles returns 200 with role list")
    void testGetAllRolesSuccess() throws Exception {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Arrays.asList(testRole));

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("ADMIN"));

        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/roles returns 200 with empty list")
    void testGetAllRolesEmpty() throws Exception {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(roleRepository, times(1)).findAll();
    }

    // ==================== GET /api/v1/roles/{id} Tests ====================

    @Test
    @DisplayName("GET /api/v1/roles/{id} returns 200 with role")
    void testGetRoleByIdSuccess() throws Exception {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(java.util.Optional.of(testRole));

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("ADMIN"));

        verify(roleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/roles/{id} returns 404 when not found")
    void testGetRoleByIdNotFound() throws Exception {
        // Arrange
        when(roleRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/roles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));

        verify(roleRepository, times(1)).findById(999L);
    }

    // ==================== POST /api/v1/roles Tests ====================

    @Test
    @DisplayName("POST /api/v1/roles returns 201 when role created successfully")
    void testCreateRoleSuccess() throws Exception {
        // Arrange
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act & Assert
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("ADMIN"));

        verify(roleRepository, times(1)).existsByName("ADMIN");
        verify(roleRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("POST /api/v1/roles returns 400 when validation fails")
    void testCreateRoleBadRequest() throws Exception {
        // Arrange
        CreateRoleRequest invalidRequest = new CreateRoleRequest(
                "", // Invalid: empty name
                "Administrator role");

        // Act & Assert
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/v1/roles returns 409 when duplicate name")
    void testCreateRoleDuplicateName() throws Exception {
        // Arrange
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));

        verify(roleRepository, times(1)).existsByName("ADMIN");
        verify(roleRepository, never()).save(any());
    }

    // ==================== PUT /api/v1/roles/{id} Tests ====================

    @Test
    @DisplayName("PUT /api/v1/roles/{id} returns 200 when updated successfully")
    void testUpdateRoleSuccess() throws Exception {
        // Arrange
        Role updatedRole = new Role();
        updatedRole.setId(1L);
        updatedRole.setName("MANAGER");
        updatedRole.setDescription("Manager role with limited permissions");

        when(roleRepository.findById(1L)).thenReturn(java.util.Optional.of(testRole));
        when(roleRepository.existsByName("MANAGER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(updatedRole);

        // Act & Assert
        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.name").value("MANAGER"));

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).existsByName("MANAGER");
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{id} returns 400 when validation fails")
    void testUpdateRoleBadRequest() throws Exception {
        // Arrange
        UpdateRoleRequest invalidRequest = new UpdateRoleRequest(
                "", // Invalid: empty name
                "Manager role");

        // Act & Assert
        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{id} returns 404 when not found")
    void testUpdateRoleNotFound() throws Exception {
        // Arrange
        when(roleRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/v1/roles/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));

        verify(roleRepository, times(1)).findById(999L);
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/v1/roles/{id} returns 409 when duplicate name")
    void testUpdateRoleDuplicateName() throws Exception {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(java.util.Optional.of(testRole));
        when(roleRepository.existsByName("MANAGER")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, never()).save(any());
    }

    // ==================== DELETE /api/v1/roles/{id} Tests ====================

    @Test
    @DisplayName("DELETE /api/v1/roles/{id} returns 204 when deleted successfully")
    void testDeleteRoleSuccess() throws Exception {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(java.util.Optional.of(testRole));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/roles/1"))
                .andExpect(status().isNoContent());

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).delete(testRole);
    }

    @Test
    @DisplayName("DELETE /api/v1/roles/{id} returns 404 when not found")
    void testDeleteRoleNotFound() throws Exception {
        // Arrange
        when(roleRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/roles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));

        verify(roleRepository, times(1)).findById(999L);
        verify(roleRepository, never()).delete(any());
    }
}
