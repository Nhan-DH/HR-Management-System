package com.dona.spring_rest.feature.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import com.dona.spring_rest.feature.company.Company;
import com.dona.spring_rest.feature.company.CompanyRepository;
import com.dona.spring_rest.feature.role.Role;
import com.dona.spring_rest.feature.role.RoleRepository;
import com.dona.spring_rest.feature.user.dto.CreateUserRequest;
import com.dona.spring_rest.feature.user.dto.UpdateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@SpringBootTest
@DisplayName("UserController Integration Tests")
class UserControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @MockitoBean
        private UserRepository userRepository;

        @MockitoBean
        private CompanyRepository companyRepository;

        @MockitoBean
        private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private UserService userService;

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        private User testUser;
        private Company testCompany;
        private Role testRole;
        private CreateUserRequest validCreateRequest;
        private UpdateUserRequest validUpdateRequest;

        @BeforeEach
        void setUp() throws Exception {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
                objectMapper = new ObjectMapper();

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

                validCreateRequest = new CreateUserRequest(
                                "Tran Thi B",
                                "tran@example.com",
                                "password123",
                                30,
                                "Ha Noi",
                                Gender.FEMALE,
                                1L,
                                Arrays.asList(1L));

                validUpdateRequest = new UpdateUserRequest(
                                "Updated Name",
                                "updated@example.com",
                                26,
                                "Da Nang",
                                Gender.OTHER,
                                1L,
                                Arrays.asList(1L),
                                "newPassword123");
        }

        // ==================== GET /api/v1/users Tests ====================

        @Test
        @DisplayName("GET /api/v1/users returns 200 with user list")
        void testGetAllUsersSuccess() throws Exception {
                when(userRepository.findAll()).thenReturn(Arrays.asList(testUser));

                mockMvc.perform(get("/api/v1/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(200))
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[0].name").value("Nguyen Van A"))
                                .andExpect(jsonPath("$.data[0].email").value("user@example.com"));

                verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("GET /api/v1/users returns 200 with empty list")
        void testGetAllUsersEmpty() throws Exception {
                when(userRepository.findAll()).thenReturn(Arrays.asList());

                mockMvc.perform(get("/api/v1/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(200))
                                .andExpect(jsonPath("$.data.length()").value(0));

                verify(userRepository, times(1)).findAll();
        }

        // ==================== GET /api/v1/users/{id} Tests ====================

        @Test
        @DisplayName("GET /api/v1/users/{id} returns 200 with user")
        void testGetUserByIdSuccess() throws Exception {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

                mockMvc.perform(get("/api/v1/users/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(200))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.name").value("Nguyen Van A"));

                verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("GET /api/v1/users/{id} returns 404 when not found")
        void testGetUserByIdNotFound() throws Exception {
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                mockMvc.perform(get("/api/v1/users/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.statusCode").value(404));

                verify(userRepository, times(1)).findById(999L);
        }

        // ==================== POST /api/v1/users Tests ====================

        @Test
        @DisplayName("POST /api/v1/users returns 201 when user created successfully")
        void testCreateUserSuccess() throws Exception {
                when(userRepository.existsByEmail("tran@example.com")).thenReturn(false);
                when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
                when(roleRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testRole));
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validCreateRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.statusCode").value(201))
                                .andExpect(jsonPath("$.data.id").value(1));

                verify(userRepository, times(1)).existsByEmail("tran@example.com");
        }

        @Test
        @DisplayName("POST /api/v1/users returns 400 when validation fails")
        void testCreateUserBadRequest() throws Exception {
                CreateUserRequest invalidRequest = new CreateUserRequest(
                                "", // Invalid: empty name
                                "invalid-email", // Invalid email
                                "short", // Invalid password too short
                                30,
                                "Ha Noi",
                                Gender.FEMALE,
                                1L,
                                Arrays.asList(1L));

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.statusCode").value(400));

                verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("POST /api/v1/users returns 409 when duplicate email")
        void testCreateUserDuplicateEmail() throws Exception {
                when(userRepository.existsByEmail("tran@example.com")).thenReturn(true);

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validCreateRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.statusCode").value(409));

                verify(userRepository, times(1)).existsByEmail("tran@example.com");
                verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("POST /api/v1/users returns 404 when company not found")
        void testCreateUserCompanyNotFound() throws Exception {
                when(userRepository.existsByEmail("tran@example.com")).thenReturn(false);
                when(companyRepository.findById(1L)).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validCreateRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.statusCode").value(404));

                verify(userRepository, never()).save(any());
        }

        // ==================== PUT /api/v1/users/{id} Tests ====================

        @Test
        @DisplayName("PUT /api/v1/users/{id} returns 200 when updated successfully")
        void testUpdateUserSuccess() throws Exception {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
                when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
                when(roleRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(testRole));
                when(userRepository.save(any(User.class))).thenReturn(testUser);

                mockMvc.perform(put("/api/v1/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusCode").value(200));

                verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("PUT /api/v1/users/{id} returns 400 when validation fails")
        void testUpdateUserBadRequest() throws Exception {
                UpdateUserRequest invalidRequest = new UpdateUserRequest(
                                "", // Invalid: empty name
                                "invalid-email",
                                0,
                                "Da Nang",
                                Gender.OTHER,
                                1L,
                                Arrays.asList(1L),
                                null);

                mockMvc.perform(put("/api/v1/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.statusCode").value(400));

                verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("PUT /api/v1/users/{id} returns 404 when not found")
        void testUpdateUserNotFound() throws Exception {
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                mockMvc.perform(put("/api/v1/users/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.statusCode").value(404));

                verify(userRepository, times(1)).findById(999L);
                verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("PUT /api/v1/users/{id} returns 409 when duplicate email")
        void testUpdateUserDuplicateEmail() throws Exception {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
                when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

                mockMvc.perform(put("/api/v1/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.statusCode").value(409));

                verify(userRepository, times(1)).findById(1L);
                verify(userRepository, never()).save(any());
        }

        // ==================== DELETE /api/v1/users/{id} Tests ====================

        @Test
        @DisplayName("DELETE /api/v1/users/{id} returns 204 when deleted successfully")
        void testDeleteUserSuccess() throws Exception {
                when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

                mockMvc.perform(delete("/api/v1/users/1"))
                                .andExpect(status().isNoContent());

                verify(userRepository, times(1)).findById(1L);
                verify(userRepository, times(1)).delete(testUser);
        }

        @Test
        @DisplayName("DELETE /api/v1/users/{id} returns 404 when not found")
        void testDeleteUserNotFound() throws Exception {
                when(userRepository.findById(999L)).thenReturn(Optional.empty());

                mockMvc.perform(delete("/api/v1/users/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.statusCode").value(404));

                verify(userRepository, times(1)).findById(999L);
                verify(userRepository, never()).delete(any());
        }
}
