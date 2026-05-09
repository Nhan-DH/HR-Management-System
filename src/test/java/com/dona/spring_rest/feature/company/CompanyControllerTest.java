package com.dona.spring_rest.feature.company;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import com.dona.spring_rest.feature.company.dto.CompanyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Arrays;

@SpringBootTest
@DisplayName("CompanyController Integration Tests")
class CompanyControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyService companyService;

    private Company testCompany;
    private CompanyRequest validRequest;

    @Configuration
    static class TestConfig {
        @Bean
        public CompanyService mockCompanyService() {
            return Mockito.mock(CompanyService.class);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
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
        testCompany.setLogo("logo_url");
        testCompany.setCreatedAt(Instant.now());
        testCompany.setUpdatedAt(Instant.now());

        validRequest = new CompanyRequest();
        validRequest.setName("Tech Corp");
        validRequest.setDescription("A technology company with more than 10 characters");
        validRequest.setAddress("123 Tech Street Address");
        validRequest.setEmail("contact@techcorp.com");
        validRequest.setPhone("0123456789");
        validRequest.setWebsite("www.techcorp.com");
        validRequest.setTaxCode("1234567890");
        validRequest.setNumberOfEmployees(100);
        validRequest.setLogo("logo_url");
    }

    // ==================== GET /api/companies Tests ====================

    @Test
    @DisplayName("GET /api/companies returns 200 with company list")
    void testGetAllCompaniesSuccess() throws Exception {
        // Arrange
        when(companyService.getAllCompanies()).thenReturn(Arrays.asList(testCompany));

        // Act & Assert
        mockMvc.perform(get("/api/companies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @DisplayName("GET /api/companies returns 200 with empty list when no companies exist")
    void testGetAllCompaniesEmpty() throws Exception {
        // Arrange
        when(companyService.getAllCompanies()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/companies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));

        verify(companyService, times(1)).getAllCompanies();
    }

    // ==================== GET /api/companies/{id} Tests ====================

    @Test
    @DisplayName("GET /api/companies/{id} returns 200 with company data")
    void testGetCompanyByIdSuccess() throws Exception {
        // Arrange
        when(companyService.getCompanyById(1L)).thenReturn(testCompany);

        // Act & Assert
        mockMvc.perform(get("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Tech Corp"));

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("GET /api/companies/{id} returns 404 when company not found")
    void testGetCompanyByIdNotFound() throws Exception {
        // Arrange
        when(companyService.getCompanyById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Công ty", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/companies/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));

        verify(companyService, times(1)).getCompanyById(999L);
    }

    // ==================== POST /api/companies Tests ====================

    @Test
    @DisplayName("POST /api/companies returns 201 when company is created successfully")
    void testCreateCompanySuccess() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class))).thenReturn(testCompany);

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.name").value("Tech Corp"));

        verify(companyService, times(1)).createCompany(any(Company.class));
    }

    @Test
    @DisplayName("POST /api/companies returns 400 when invalid data is provided")
    void testCreateCompanyBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("POST /api/companies returns 409 when email already exists")
    void testCreateCompanyDuplicateEmail() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class)))
                .thenThrow(new DuplicateResourceException("Công ty", "email", "contact@techcorp.com"));

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("POST /api/companies returns 409 when tax code already exists")
    void testCreateCompanyDuplicateTaxCode() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class)))
                .thenThrow(new DuplicateResourceException("Công ty", "taxCode", "1234567890"));

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    @DisplayName("POST /api/companies returns 409 when company name already exists")
    void testCreateCompanyDuplicateName() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class)))
                .thenThrow(new DuplicateResourceException("Công ty", "name", "Tech Corp"));

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    // ==================== PUT /api/companies/{id} Tests ====================

    @Test
    @DisplayName("PUT /api/companies/{id} returns 200 when company is updated successfully")
    void testUpdateCompanySuccess() throws Exception {
        // Arrange
        when(companyService.updateCompany(anyLong(), any(Company.class))).thenReturn(testCompany);

        // Act & Assert
        mockMvc.perform(put("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.name").value("Tech Corp"));

        verify(companyService, times(1)).updateCompany(anyLong(), any(Company.class));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} returns 400 when invalid data is provided")
    void testUpdateCompanyBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{}";

        // Act & Assert
        mockMvc.perform(put("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} returns 404 when company not found")
    void testUpdateCompanyNotFound() throws Exception {
        // Arrange
        when(companyService.updateCompany(anyLong(), any(Company.class)))
                .thenThrow(new ResourceNotFoundException("Công ty", "id", 999L));

        // Act & Assert
        mockMvc.perform(put("/api/companies/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("PUT /api/companies/{id} returns 409 when email already exists")
    void testUpdateCompanyDuplicateEmail() throws Exception {
        // Arrange
        when(companyService.updateCompany(anyLong(), any(Company.class)))
                .thenThrow(new DuplicateResourceException("Công ty", "email", "contact@techcorp.com"));

        // Act & Assert
        mockMvc.perform(put("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    // ==================== DELETE /api/companies/{id} Tests ====================

    @Test
    @DisplayName("DELETE /api/companies/{id} returns 204 when company is deleted successfully")
    void testDeleteCompanySuccess() throws Exception {
        // Arrange
        doNothing().when(companyService).deleteCompany(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(companyService, times(1)).deleteCompany(1L);
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} returns 404 when company not found")
    void testDeleteCompanyNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Công ty", "id", 999L))
                .when(companyService).deleteCompany(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/companies/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));

        verify(companyService, times(1)).deleteCompany(999L);
    }
}
