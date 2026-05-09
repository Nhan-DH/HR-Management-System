package com.dona.spring_rest.feature.company;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
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
        testCompany.setLogo("logo_url");
        testCompany.setCreatedAt(Instant.now());
        testCompany.setUpdatedAt(Instant.now());

        CompanyRequest request = new CompanyRequest();
        request.setName("Tech Corp");
        request.setDescription("A technology company with more than 10 characters");
        request.setAddress("123 Tech Street Address");
        request.setEmail("contact@techcorp.com");
        request.setPhone("0123456789");
        request.setWebsite("www.techcorp.com");
        request.setTaxCode("1234567890");
        request.setNumberOfEmployees(100);
        request.setLogo("logo_url");

        validCompanyJson = objectMapper.writeValueAsString(request);
    }

    // ==================== GET /api/companies Tests ====================

    @Test
    @DisplayName("getAllCompanies_returns200_withCompanyList")
    void getAllCompanies_returns200_withCompanyList() throws Exception {
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
    @DisplayName("getAllCompanies_returns200_withEmptyList")
    void getAllCompanies_returns200_withEmptyList() throws Exception {
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
                .andExpect(jsonPath("$.data.name").value("Tech Corp"));

        verify(companyService, times(1)).getCompanyById(1L);
    }

    @Test
    @DisplayName("getCompanyById_returns404_whenCompanyNotFound")
    void getCompanyById_returns404_whenCompanyNotFound() throws Exception {
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
    @DisplayName("createCompany_returns201_whenValidDataProvided")
    void createCompany_returns201_whenValidDataProvided() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class))).thenReturn(testCompany);

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.name").value("Tech Corp"));

        verify(companyService, times(1)).createCompany(any(Company.class));
    }

    @Test
    @DisplayName("createCompany_returns400_whenInvalidDataProvided")
    void createCompany_returns400_whenInvalidDataProvided() throws Exception {
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
    @DisplayName("createCompany_returns409_whenEmailAlreadyExists")
    void createCompany_returns409_whenEmailAlreadyExists() throws Exception {
        // Arrange
        when(companyService.createCompany(any(Company.class)))
                .thenThrow(new DuplicateResourceException("Công ty", "email", "contact@techcorp.com"));

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
                .thenThrow(new DuplicateResourceException("Công ty", "taxCode", "1234567890"));

        // Act & Assert
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    // ==================== PUT /api/companies/{id} Tests ====================

    @Test
    @DisplayName("updateCompany_returns200_whenValidDataProvided")
    void updateCompany_returns200_whenValidDataProvided() throws Exception {
        // Arrange
        when(companyService.updateCompany(anyLong(), any(Company.class))).thenReturn(testCompany);

        // Act & Assert
        mockMvc.perform(put("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCompanyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.name").value("Tech Corp"));

        verify(companyService, times(1)).updateCompany(anyLong(), any(Company.class));
    }

    @Test
    @DisplayName("updateCompany_returns400_whenInvalidDataProvided")
    void updateCompany_returns400_whenInvalidDataProvided() throws Exception {
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
    @DisplayName("updateCompany_returns404_whenCompanyNotFound")
    void updateCompany_returns404_whenCompanyNotFound() throws Exception {
        // Arrange
        when(companyService.updateCompany(anyLong(), any(Company.class)))
                .thenThrow(new ResourceNotFoundException("Công ty", "id", 999L));

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
                .thenThrow(new DuplicateResourceException("Công ty", "email", "contact@techcorp.com"));

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
        doNothing().when(companyService).deleteCompany(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/companies/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(companyService, times(1)).deleteCompany(1L);
    }

    @Test
    @DisplayName("deleteCompany_returns404_whenCompanyNotFound")
    void deleteCompany_returns404_whenCompanyNotFound() throws Exception {
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
