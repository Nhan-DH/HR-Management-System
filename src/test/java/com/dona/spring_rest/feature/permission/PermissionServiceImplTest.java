package com.dona.spring_rest.feature.permission;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setId(1L);
        testPermission.setName("CREATE_USER");
        testPermission.setApiPath("/api/v1/users");
        testPermission.setMethod("POST");
        testPermission.setModule("USER");
        testPermission.setCreatedAt(Instant.now());
        testPermission.setUpdatedAt(Instant.now());
    }

    // ==================== Get All Permissions Tests ====================

    @Test
    @DisplayName("getAllPermissions should return list of permissions")
    void testGetAllPermissionsSuccess() {
        // Arrange
        when(permissionRepository.findAll()).thenReturn(Arrays.asList(testPermission));

        // Act
        var result = permissionService.getAllPermissions();

        // Assert
        assertEquals(1, result.size());
        assertEquals("CREATE_USER", result.get(0).getName());
        verify(permissionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllPermissions should return empty list when no permissions exist")
    void testGetAllPermissionsEmpty() {
        // Arrange
        when(permissionRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        var result = permissionService.getAllPermissions();

        // Assert
        assertEquals(0, result.size());
        verify(permissionRepository, times(1)).findAll();
    }

    // ==================== Get Permission By ID Tests ====================

    @Test
    @DisplayName("getPermissionById should return permission when found")
    void testGetPermissionByIdSuccess() {
        // Arrange
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));

        // Act
        Permission result = permissionService.getPermissionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CREATE_USER", result.getName());
        verify(permissionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getPermissionById should throw ResourceNotFoundException when not found")
    void testGetPermissionByIdNotFound() {
        // Arrange
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> permissionService.getPermissionById(999L));
        verify(permissionRepository, times(1)).findById(999L);
    }

    // ==================== Create Permission Tests ====================

    @Test
    @DisplayName("createPermission should create permission when valid")
    void testCreatePermissionSuccess() {
        // Arrange
        when(permissionRepository.existsByName("CREATE_USER")).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "POST")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        // Act
        Permission result = permissionService.createPermission(testPermission);

        // Assert
        assertNotNull(result);
        assertEquals("CREATE_USER", result.getName());
        verify(permissionRepository, times(1)).existsByName("CREATE_USER");
        verify(permissionRepository, times(1)).existsByApiPathAndMethod("/api/v1/users", "POST");
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    @DisplayName("createPermission should throw DuplicateResourceException when name exists")
    void testCreatePermissionDuplicateName() {
        // Arrange
        when(permissionRepository.existsByName("CREATE_USER")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> permissionService.createPermission(testPermission));
        verify(permissionRepository, times(1)).existsByName("CREATE_USER");
        verify(permissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPermission should throw DuplicateResourceException when apiPath+method exists")
    void testCreatePermissionDuplicateApiPathMethod() {
        // Arrange
        when(permissionRepository.existsByName("CREATE_USER")).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "POST")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> permissionService.createPermission(testPermission));
        verify(permissionRepository, times(1)).existsByName("CREATE_USER");
        verify(permissionRepository, times(1)).existsByApiPathAndMethod("/api/v1/users", "POST");
        verify(permissionRepository, never()).save(any());
    }

    // ==================== Update Permission Tests ====================

    @Test
    @DisplayName("updatePermission should update permission when valid")
    void testUpdatePermissionSuccess() {
        // Arrange
        Permission updateData = new Permission();
        updateData.setName("UPDATE_USER");
        updateData.setApiPath("/api/v1/users");
        updateData.setMethod("PUT");
        updateData.setModule("USER");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));
        when(permissionRepository.existsByName("UPDATE_USER")).thenReturn(false);
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "PUT")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        // Act
        Permission result = permissionService.updatePermission(1L, updateData);

        // Assert
        assertNotNull(result);
        verify(permissionRepository, times(1)).findById(1L);
        verify(permissionRepository, times(1)).existsByName("UPDATE_USER");
        verify(permissionRepository, times(1)).existsByApiPathAndMethod("/api/v1/users", "PUT");
        verify(permissionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("updatePermission should throw ResourceNotFoundException when not found")
    void testUpdatePermissionNotFound() {
        // Arrange
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> permissionService.updatePermission(999L, testPermission));
        verify(permissionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("updatePermission should throw DuplicateResourceException for duplicate name")
    void testUpdatePermissionDuplicateName() {
        // Arrange
        Permission updateData = new Permission();
        updateData.setName("UPDATE_USER");
        updateData.setApiPath("/api/v1/users");
        updateData.setMethod("POST");
        updateData.setModule("USER");

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));
        when(permissionRepository.existsByName("UPDATE_USER")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
            () -> permissionService.updatePermission(1L, updateData));
        verify(permissionRepository, times(1)).findById(1L);
        verify(permissionRepository, never()).save(any());
    }

    // ==================== Delete Permission Tests ====================

    @Test
    @DisplayName("deletePermission should delete permission when found")
    void testDeletePermissionSuccess() {
        // Arrange
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));

        // Act
        permissionService.deletePermission(1L);

        // Assert
        verify(permissionRepository, times(1)).findById(1L);
        verify(permissionRepository, times(1)).delete(testPermission);
    }

    @Test
    @DisplayName("deletePermission should throw ResourceNotFoundException when not found")
    void testDeletePermissionNotFound() {
        // Arrange
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> permissionService.deletePermission(999L));
        verify(permissionRepository, times(1)).findById(999L);
        verify(permissionRepository, never()).delete(any());
    }

    // ==================== Helper Method Tests ====================

    @Test
    @DisplayName("existsByName should return true when name exists")
    void testExistsByNameTrue() {
        // Arrange
        when(permissionRepository.existsByName("CREATE_USER")).thenReturn(true);

        // Act
        boolean result = permissionService.existsByName("CREATE_USER");

        // Assert
        assertTrue(result);
        verify(permissionRepository, times(1)).existsByName("CREATE_USER");
    }

    @Test
    @DisplayName("existsByName should return false when name doesn't exist")
    void testExistsByNameFalse() {
        // Arrange
        when(permissionRepository.existsByName("DELETE_USER")).thenReturn(false);

        // Act
        boolean result = permissionService.existsByName("DELETE_USER");

        // Assert
        assertFalse(result);
        verify(permissionRepository, times(1)).existsByName("DELETE_USER");
    }

    @Test
    @DisplayName("existsByApiPathAndMethod should return true when exists")
    void testExistsByApiPathAndMethodTrue() {
        // Arrange
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/users", "POST")).thenReturn(true);

        // Act
        boolean result = permissionService.existsByApiPathAndMethod("/api/v1/users", "POST");

        // Assert
        assertTrue(result);
        verify(permissionRepository, times(1)).existsByApiPathAndMethod("/api/v1/users", "POST");
    }

    @Test
    @DisplayName("existsByApiPathAndMethod should return false when doesn't exist")
    void testExistsByApiPathAndMethodFalse() {
        // Arrange
        when(permissionRepository.existsByApiPathAndMethod("/api/v1/roles", "GET")).thenReturn(false);

        // Act
        boolean result = permissionService.existsByApiPathAndMethod("/api/v1/roles", "GET");

        // Assert
        assertFalse(result);
        verify(permissionRepository, times(1)).existsByApiPathAndMethod("/api/v1/roles", "GET");
    }
}
