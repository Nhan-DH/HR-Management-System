package com.dona.spring_rest.feature.user;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import com.dona.spring_rest.feature.company.Company;
import com.dona.spring_rest.feature.company.CompanyRepository;
import com.dona.spring_rest.feature.role.Role;
import com.dona.spring_rest.feature.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Company testCompany;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Tech Corp");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ADMIN");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Nguyen Van A");
        testUser.setEmail("user@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setAge(25);
        testUser.setAddress("Ho Chi Minh City");
        testUser.setGender(Gender.MALE);
        testUser.setCompany(testCompany);
        testUser.setRoles(Arrays.asList(testRole));
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
    }

    // ==================== Get All Users Tests ====================

    @Test
    @DisplayName("getAllUsers should return list of users")
    void testGetAllUsersSuccess() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

        var result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("Nguyen Van A", result.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers should return empty list when no users exist")
    void testGetAllUsersEmpty() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        var result = userService.getAllUsers();

        assertEquals(0, result.size());
        verify(userRepository, times(1)).findAll();
    }

    // ==================== Get User By ID Tests ====================

    @Test
    @DisplayName("getUserById should return user when found")
    void testGetUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Nguyen Van A", result.getName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUserById should throw ResourceNotFoundException when not found")
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    // ==================== Create User Tests ====================

    @Test
    @DisplayName("createUser should create user with company and roles")
    void testCreateUserSuccess() {
        User newUser = new User();
        newUser.setName("Tran Thi B");
        newUser.setEmail("tran@example.com");
        newUser.setPassword("password123");
        newUser.setAge(30);
        newUser.setGender(Gender.FEMALE);
        newUser.setCompany(testCompany);

        when(userRepository.existsByEmail("tran@example.com")).thenReturn(false);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(roleRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testRole));

        User result = userService.createUser(newUser, Arrays.asList(1L));

        assertNotNull(result);
        assertEquals("Nguyen Van A", result.getName());
        verify(userRepository, times(1)).existsByEmail("tran@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser should throw DuplicateResourceException for duplicate email")
    void testCreateUserDuplicateEmail() {
        User newUser = new User();
        newUser.setEmail("user@example.com");
        newUser.setPassword("password123");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(newUser, null));
        verify(userRepository, times(1)).existsByEmail("user@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser should throw ResourceNotFoundException for invalid company")
    void testCreateUserInvalidCompany() {
        User newUser = new User();
        newUser.setEmail("tran@example.com");
        newUser.setPassword("password123");
        Company invalidCompany = new Company();
        invalidCompany.setId(999L);
        newUser.setCompany(invalidCompany);

        when(userRepository.existsByEmail("tran@example.com")).thenReturn(false);
        when(companyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.createUser(newUser, null));
        verify(userRepository, never()).save(any());
    }

    // ==================== Update User Tests ====================

    @Test
    @DisplayName("updateUser should update user successfully")
    void testUpdateUserSuccess() {
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setEmail("updated@example.com");
        updateData.setAge(26);
        updateData.setGender(Gender.FEMALE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(roleRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testRole));

        User result = userService.updateUser(1L, updateData, Arrays.asList(1L));

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("updated@example.com");
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("updateUser should hash new password only if provided")
    void testUpdateUserPasswordUpdate() {
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setEmail("user@example.com");
        updateData.setPassword("newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(1L, updateData, null);

        verify(passwordEncoder, times(1)).encode("newPassword123");
    }

    @Test
    @DisplayName("updateUser should not hash password if null or blank")
    void testUpdateUserNoPasswordChange() {
        User updateData = new User();
        updateData.setName("Updated Name");
        updateData.setEmail("user@example.com");
        updateData.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUser(1L, updateData, null);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("updateUser should throw ResourceNotFoundException when not found")
    void testUpdateUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(999L, testUser, null));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("updateUser should throw DuplicateResourceException for duplicate email")
    void testUpdateUserDuplicateEmail() {
        User updateData = new User();
        updateData.setEmail("other@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.updateUser(1L, updateData, null));
        verify(userRepository, never()).save(any());
    }

    // ==================== Delete User Tests ====================

    @Test
    @DisplayName("deleteUser should delete user when found")
    void testDeleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("deleteUser should throw ResourceNotFoundException when not found")
    void testDeleteUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).delete(any());
    }

    // ==================== Helper Method Tests ====================

    @Test
    @DisplayName("existsByEmail should return true when email exists")
    void testExistsByEmailTrue() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("user@example.com");

        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail("user@example.com");
    }

    @Test
    @DisplayName("existsByEmail should return false when email doesn't exist")
    void testExistsByEmailFalse() {
        when(userRepository.existsByEmail("notfound@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("notfound@example.com");

        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("getUserByEmail should return user when found")
    void testGetUserByEmailSuccess() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail("user@example.com");

        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    @DisplayName("getUserByEmail should throw ResourceNotFoundException when not found")
    void testGetUserByEmailNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("notfound@example.com"));
        verify(userRepository, times(1)).findByEmail("notfound@example.com");
    }
}
