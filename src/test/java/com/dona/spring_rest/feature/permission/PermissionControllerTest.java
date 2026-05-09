package com.dona.spring_rest.feature.permission;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import com.dona.spring_rest.feature.permission.dto.CreatePermissionRequest;
import com.dona.spring_rest.feature.permission.dto.PermissionResponse;
import com.dona.spring_rest.feature.permission.dto.UpdatePermissionRequest;
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
@DisplayName("PermissionController Integration Tests")
class PermissionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PermissionRepository permissionRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PermissionService permissionService;

    private Permission testPermission;
    private CreatePermissionRequest validRequest;
    private UpdatePermissionRequest validUpdateRequest;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Create a real service with mocked repository
        permissionService = new PermissionServiceImpl(permissionRepository);

        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setName("CREATE_USER");
        testPermission.setApiPath("/api/v1/users");
        testPermission.setMethod("POST");
        testPermission.setModule("USER");
        testPermission.setCreatedAt(Instant.now());
        testPermission.setUpdatedAt(Instant.now());

        validRequest = new CreatePermissionRequest(
            "CREATE_USER",
            "/api/v1/users",
            "POST",
            "USER"
        );

        validUpdateRequest = new UpdatePermissionRequest(
            "UPDATE_USER",
            "/api/v1/users",
            "PUT",
            "USER"
        );
    }

    // ==================== GET /api/v1/permissions Tests ====================

    @Test
    @DisplayName("GET /api/v1/permissions returns 200 with permission list")
    void testGetAllPermissionsSuccess() throws Exception {
        // Arrange
        when(permissionRepository.findAll()).thenReturn(Arrays.asList(testPermission));

        // Act & Assert
        mockMvc.perform(get("/api/v1/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].name").value("CREATE_USER"));

        verify(permissionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/permissions returns 200 with empty list")
    void testGetAllPermissionsEmpty() throws Exception {
        // Arrange
        when(permissionRepository.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.length()").value(0));

        verify(permissionRepository, times(1)).findAll();
    }

    // ==================== GET /api/v1/permissions/{id} Tests ====================

    @Test
    @DisplayName("GET /api/v1/permissions/{id} returns 200 with permission")
    void testGetPermissionByIdSuccess() throws Exception {
        // Arrange
        when(permissionRepository.findById(1L)).thenReturn(java.util.Optional.of(testPermission));

        // Act & Assert
        mockMvc.perform(get("/api/v1/permissions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("CREATE_USER"));

        verify(permissionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/permissions/{id} returns 404 when not found")
    void testGetPermissionByIdNotFound() throws Exception {
        // Arrange
        when(permissionRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/permissions/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.statusCode").value(404));

        verify(permissionRepository, times(1)).findById(999L);
    }

    // ==================== POST /api/v1/permissions Tests ====================

    @Test
    @DisplayName("POST /api/v1/permissions returns 201 when permission created successfully")
    void testCreatePermissionSuccess() throws Exception {
        // Arrange
        when(permissionRepository.existsByName("CREATE_USER")).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "POST")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        // Act & Assert
        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.statusCode").value(201))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.name").value("CREATE_USER"));

        verify(permissionRepository, times(1)).existsByName("CREATE_USER");
        verify(permissionRepository, times(1)).existsByApiPathAndMethod("/api/v1/users", "POST");
        verify(permissionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("POST /api/v1/permissions returns 400 when validation fails")
    void testCreatePermissionBadRequest() throws Exception {
        // Arrange
        CreatePermissionRequest invalidRequest = new CreatePermissionRequest(
            "",  // Invalid: empty name
            "/api/v1/users",
            "POST",
            "USER"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.statusCode").value(400));

        verify(permissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/v1/permissions returns 409 when duplicate name")
    void testCreatePermissionDuplicateName() throws Exception {
        // Arrange
        when(permissionRepository.existsByName("CREATE_USER")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.statusCode").value(409));

        verify(permissionRepository, times(1)).existsByName("CREATE_USER");
        verify(permissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/v1/permissions returns 409 when duplicate apiPath+method")
    void testCreatePermissionDuplicateApiPathMethod() throws Exception {
        // Arrange
        when(permissionRepository.existsByName("CREATE_USER")).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "POST")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.statusCode").value(409));

        verify(permissionRepository, times(1)).existsByName("CREATE_USER");
        verify(permissionRepository, times(1)).existsByApiPathAndMethod("/api/v1/users", "POST");
        verify(permissionRepository, never()).save(any());
    }

    // ==================== PUT /api/v1/permissions/{id} Tests ====================

    @Test
    @DisplayName("PUT /api/v1/permissions/{id} returns 200 when updated successfully")
    void testUpdatePermissionSuccess() throws Exception {
        // Arrange
        Permission updatedPermission = new Permission();
        updatedPermission.setId(1L);
        updatedPermission.setName("UPDATE_USER");
        updatedPermission.setApiPath("/api/v1/users");
        updatedPermission.setMethod("PUT");
        updatedPermission.setModule("USER");

        when(permissionRepository.findById(1L)).thenReturn(java.util.Optional.of(testPermission));
        when(permissionRepository.existsByName("UPDATE_USER")).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "PUT")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(updatedPermission);

        // Act & Assert
        mockMvc.perform(put("/api/v1/permissions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.name").value("UPDATE_USER"));

        verify(permissionRepository, times(1)).findById(1L);
        verify(permissionRepository, times(1)).existsByName("UPDATE_USER");
    }

    @Test
    @DisplayName("PUT /api/v1/permissions/{id} returns 400 when validation fails")
    void testUpdatePermissionBadRequest() throws Exception {
        // Arrange
        UpdatePermissionRequest invalidRequest = new UpdatePermissionRequest(
            "",  // Invalid: empty name
            "/api/v1/users",
            "PUT",
            "USER"
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/permissions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.statusCode").value(400));

        verify(permissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/v1/permissions/{id} returns 404 when not found")
    void testUpdatePermissionNotFound() throws Exception {
        // Arrange
        when(permissionRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/v1/permissions/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.statusCode").value(404));

        verify(permissionRepository, times(1)).findById(999L);
        verify(permissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /api/v1/permissions/{id} returns 409 when duplicate name")
    void testUpdatePermissionDuplicateName() throws Exception {
        // Arrange
        when(permissionRepository.findById(1L)).thenReturn(java.util.Optional.of(testPermission));
        when(permissionRepository.existsByName("UPDATE_USER")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(put("/api/v1/permissions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.statusCode").value(409));

        verify(permissionRepository, times(1)).findById(1L);
        verify(permissionRepository, never()).save(any());
    }

    // ==================== DELETE /api/v1/permissions/{id} Tests ====================

    @Test
    @DisplayName("DELETE /api/v1/permissions/{id} returns 204 when deleted successfully")
    void testDeletePermissionSuccess() throws Exception {
        // Arrange
        when(permissionRepository.findById(1L)).thenReturn(java.util.Optional.of(testPermission));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/permissions/1"))
            .andExpect(status().isNoContent());

        verify(permissionRepository, times(1)).findById(1L);
        verify(permissionRepository, times(1)).delete(testPermission);
    }

    @Test
    @DisplayName("DELETE /api/v1/permissions/{id} returns 404 when not found")
    void testDeletePermissionNotFound() throws Exception {
        // Arrange
        when(permissionRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/permissions/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.statusCode").value(404));

        verify(permissionRepository, times(1)).findById(999L);
        verify(permissionRepository, never()).delete(any());
    }
}
