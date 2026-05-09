package com.dona.spring_rest.feature.role;

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
@DisplayName("RoleService Unit Tests")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ADMIN");
        testRole.setDescription("Administrator role with full permissions");
        testRole.setCreatedAt(Instant.now());
        testRole.setUpdatedAt(Instant.now());
    }

    // ==================== Get All Roles Tests ====================

    @Test
    @DisplayName("getAllRoles should return list of roles")
    void testGetAllRolesSuccess() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Arrays.asList(testRole));

        // Act
        var result = roleService.getAllRoles();

        // Assert
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllRoles should return empty list when no roles exist")
    void testGetAllRolesEmpty() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        var result = roleService.getAllRoles();

        // Assert
        assertEquals(0, result.size());
        verify(roleRepository, times(1)).findAll();
    }

    // ==================== Get Role By ID Tests ====================

    @Test
    @DisplayName("getRoleById should return role when found")
    void testGetRoleByIdSuccess() {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        // Act
        Role result = roleService.getRoleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ADMIN", result.getName());
        verify(roleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getRoleById should throw ResourceNotFoundException when not found")
    void testGetRoleByIdNotFound() {
        // Arrange
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById(999L));
        verify(roleRepository, times(1)).findById(999L);
    }

    // ==================== Create Role Tests ====================

    @Test
    @DisplayName("createRole should create role when valid")
    void testCreateRoleSuccess() {
        // Arrange
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act
        Role result = roleService.createRole(testRole);

        // Assert
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        verify(roleRepository, times(1)).existsByName("ADMIN");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("createRole should throw DuplicateResourceException when name exists")
    void testCreateRoleDuplicateName() {
        // Arrange
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> roleService.createRole(testRole));
        verify(roleRepository, times(1)).existsByName("ADMIN");
        verify(roleRepository, never()).save(any());
    }

    // ==================== Update Role Tests ====================

    @Test
    @DisplayName("updateRole should update role when valid")
    void testUpdateRoleSuccess() {
        // Arrange
        Role updateData = new Role();
        updateData.setName("USER");
        updateData.setDescription("Regular user role");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName("USER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // Act
        Role result = roleService.updateRole(1L, updateData);

        // Assert
        assertNotNull(result);
        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).existsByName("USER");
        verify(roleRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("updateRole should throw ResourceNotFoundException when not found")
    void testUpdateRoleNotFound() {
        // Arrange
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> roleService.updateRole(999L, testRole));
        verify(roleRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("updateRole should throw DuplicateResourceException for duplicate name")
    void testUpdateRoleDuplicateName() {
        // Arrange
        Role updateData = new Role();
        updateData.setName("USER");
        updateData.setDescription("Regular user role");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName("USER")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> roleService.updateRole(1L, updateData));
        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, never()).save(any());
    }

    // ==================== Delete Role Tests ====================

    @Test
    @DisplayName("deleteRole should delete role when found")
    void testDeleteRoleSuccess() {
        // Arrange
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        // Act
        roleService.deleteRole(1L);

        // Assert
        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).delete(testRole);
    }

    @Test
    @DisplayName("deleteRole should throw ResourceNotFoundException when not found")
    void testDeleteRoleNotFound() {
        // Arrange
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole(999L));
        verify(roleRepository, times(1)).findById(999L);
        verify(roleRepository, never()).delete(any());
    }

    // ==================== Helper Method Tests ====================

    @Test
    @DisplayName("existsByName should return true when name exists")
    void testExistsByNameTrue() {
        // Arrange
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // Act
        boolean result = roleService.existsByName("ADMIN");

        // Assert
        assertTrue(result);
        verify(roleRepository, times(1)).existsByName("ADMIN");
    }

    @Test
    @DisplayName("existsByName should return false when name doesn't exist")
    void testExistsByNameFalse() {
        // Arrange
        when(roleRepository.existsByName("SUPER_ADMIN")).thenReturn(false);

        // Act
        boolean result = roleService.existsByName("SUPER_ADMIN");

        // Assert
        assertFalse(result);
        verify(roleRepository, times(1)).existsByName("SUPER_ADMIN");
    }
}
