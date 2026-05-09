package com.dona.spring_rest.feature.company;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CompanyController Tests")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    private Company testCompany;
    private String validCompanyJson;

    @BeforeEach
    void setUp() throws Exception {
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Tech Corp");
        testCompany.setDescription("A technology company with more than 10 characters");
        testCompany.setAddress("123 Tech Street Address");
        testCompany.setEmail("contact@techcorp.com");
        testCompany.setPhone("0123456789");
        testCompany.setWebsite("www.techcorp.com");
        testCompany.setTaxCode("1234567890");
        testCompany.setNumberOfEmployees(100);
        testCompany.setCreatedAt(Instant.now());
        testCompany.setUpdatedAt(Instant.now());

        validCompanyJson = objectMapper.writeValueAsString(testCompany);
    }

    // ==================== GET /api/companies Tests ====================

    @Test
    @DisplayName("getAllCompanies_returns200_withListOfCompanies")
    void getAllCompanies_returns200_withListOfCompanies() throws Exception {
        // Arrange
        when(companyService.getAllCompanies()).thenReturn(Arrays.asList(testCompany));

        // Act & Assert
        mockMvc.perform(get("/api/companies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Tech Corp"));
    }

    @Test
    @DisplayName("getAllCompanies_returns200_withEmptyList_whenNoCompaniesExist")
    void getAllCompanies_returns200_withEmptyList_whenNoCompaniesExist() throws Exception {
        // Arrange
        when(companyService.getAllCompanies()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/companies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== GET /api/companies/{id} Tests ====================

    @Test
    @DisplayName("getCompanyById_returns200_withCompanyData")
    void getCompanyById_returns200_withCompanyData() throws Exception {
        // Arrange
        when(companyService.getCompanyById(1L)).thenReturn(testCompany);

        // Act & Assert
        mockMvc.perform(get("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Tech Corp"))
                .andExpect(jsonPath("$.data.email").value("contact@techcorp.com"));
    }

    @Test
    @DisplayName("getCompanyById_returns404_whenCompanyNotFound")
    void getCompanyById_returns404_whenCompanyNotFound() throws Exception {
        // Arrange
        when(companyService.getCompanyById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Công ty", "id", 999));

        // Act & Assert
        mockMvc.perform(get("/api/companies/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    // ==================== POST /api/companies Tests ====================

    @Test
    @DisplayName("createCompany_returns201_withCreatedCompany")
    void createCompany_returns201_withCreatedCompany() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class))).thenReturn(testCompany);

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Tech Corp"));
    }

    @Test
    @DisplayName("createCompany_returns400_whenValidationFails")
    void createCompany_returns400_whenValidationFails() throws Exception {
        // Arrange - Missing required fields
        String invalidJson = "{\"name\": \"X\", \"email\": \"invalid-email\"}";

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("createCompany_returns409_whenEmailAlreadyExists")
    void createCompany_returns409_whenEmailAlreadyExists() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class)))
                .thenThrow(new DuplicateResourceException("Email công ty đã tồn tại"));

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("createCompany_returns409_whenTaxCodeAlreadyExists")
    void createCompany_returns409_whenTaxCodeAlreadyExists() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class)))
                .thenThrow(new DuplicateResourceException("Mã số thuế đã tồn tại"));

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    // ==================== PUT /api/companies/{id} Tests ====================

    @Test
    @DisplayName("updateCompany_returns200_withUpdatedCompany")
    void updateCompany_returns200_withUpdatedCompany() throws Exception {
        // Arrange
        Company updatedCompany = new Company();
        updatedCompany.setId(1L);
        updatedCompany.setName("Updated Tech Corp");
        updatedCompany.setDescription("Updated description with more details");
        updatedCompany.setAddress("456 New Street");
        updatedCompany.setEmail("newemail@techcorp.com");
        updatedCompany.setPhone("0987654321");
        updatedCompany.setWebsite("www.updated.com");
        updatedCompany.setTaxCode("0987654321");
        updatedCompany.setNumberOfEmployees(150);

        when(companyService.updateCompany(anyLong(), any(Company.class))).thenReturn(updatedCompany);

        String updateJson = objectMapper.writeValueAsString(updatedCompany);

        // Act & Assert
        mockMvc.perform(put("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Updated Tech Corp"));
    }

    @Test
    @DisplayName("updateCompany_returns400_whenValidationFails")
    void updateCompany_returns400_whenValidationFails() throws Exception {
        // Arrange - Invalid data
        String invalidJson = "{\"name\": \"X\", \"email\": \"invalid\"}";

        // Act & Assert
        mockMvc.perform(put("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("updateCompany_returns404_whenCompanyNotFound")
    void updateCompany_returns404_whenCompanyNotFound() throws Exception {
        // Arrange
        when(companyService.updateCompany(anyLong(), any(Company.class)))
                .thenThrow(new ResourceNotFoundException("Công ty", "id", 999));

        // Act & Assert
        mockMvc.perform(put("/api/companies/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("updateCompany_returns409_whenEmailAlreadyExists")
    void updateCompany_returns409_whenEmailAlreadyExists() throws Exception {
        // Arrange
        when(companyService.updateCompany(anyLong(), any(Company.class)))
                .thenThrow(new DuplicateResourceException("Email công ty đã tồn tại"));

        // Act & Assert
        mockMvc.perform(put("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    // ==================== DELETE /api/companies/{id} Tests ====================

    @Test
    @DisplayName("deleteCompany_returns204_whenCompanyDeleted")
    void deleteCompany_returns204_whenCompanyDeleted() throws Exception {
        // Arrange
        doNothing().when(companyService).deleteCompany(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteCompany_returns404_whenCompanyNotFound")
    void deleteCompany_returns404_whenCompanyNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Công ty", "id", 999))
                .when(companyService).deleteCompany(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/companies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }
}
