package com.dona.spring_rest.feature.company;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyService Unit Tests")
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company testCompany;

    @BeforeEach
    void setUp() {
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
    }

    // ==================== GetAllCompanies Tests ====================

    @Test
    @DisplayName("getAllCompanies returns all companies when they exist")
    void testGetAllCompaniesSuccess() {
        // Arrange
        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Another Corp");
        company2.setEmail("another@corp.com");

        when(companyRepository.findAll()).thenReturn(Arrays.asList(testCompany, company2));

        // Act
        List<Company> result = companyService.getAllCompanies();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Corp", result.get(0).getName());
        verify(companyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllCompanies returns empty list when no companies exist")
    void testGetAllCompaniesEmpty() {
        // Arrange
        when(companyRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Company> result = companyService.getAllCompanies();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(companyRepository, times(1)).findAll();
    }

    // ==================== GetCompanyById Tests ====================

    @Test
    @DisplayName("getCompanyById returns company when it exists")
    void testGetCompanyByIdSuccess() {
        // Arrange
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

        // Act
        Company result = companyService.getCompanyById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Tech Corp", result.getName());
        verify(companyRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getCompanyById throws ResourceNotFoundException when company not found")
    void testGetCompanyByIdNotFound() {
        // Arrange
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            companyService.getCompanyById(999L);
        });
        verify(companyRepository, times(1)).findById(999L);
    }

    // ==================== CreateCompany Tests ====================

    @Test
    @DisplayName("createCompany successfully creates a company with valid data")
    void testCreateCompanySuccess() {
        // Arrange
        when(companyRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyRepository.existsByTaxCode(anyString())).thenReturn(false);
        when(companyRepository.existsByName(anyString())).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

        // Act
        Company result = companyService.createCompany(testCompany);

        // Assert
        assertNotNull(result);
        assertEquals("Tech Corp", result.getName());
        assertEquals("contact@techcorp.com", result.getEmail());
        verify(companyRepository, times(1)).existsByEmail("contact@techcorp.com");
        verify(companyRepository, times(1)).existsByTaxCode("1234567890");
        verify(companyRepository, times(1)).existsByName("Tech Corp");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("createCompany throws DuplicateResourceException when email already exists")
    void testCreateCompanyDuplicateEmail() {
        // Arrange
        when(companyRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            companyService.createCompany(testCompany);
        });
        verify(companyRepository, times(1)).existsByEmail("contact@techcorp.com");
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("createCompany throws DuplicateResourceException when tax code already exists")
    void testCreateCompanyDuplicateTaxCode() {
        // Arrange
        when(companyRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyRepository.existsByTaxCode(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            companyService.createCompany(testCompany);
        });
        verify(companyRepository, times(1)).existsByEmail("contact@techcorp.com");
        verify(companyRepository, times(1)).existsByTaxCode("1234567890");
        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("createCompany throws DuplicateResourceException when name already exists")
    void testCreateCompanyDuplicateName() {
        // Arrange
        when(companyRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyRepository.existsByTaxCode(anyString())).thenReturn(false);
        when(companyRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            companyService.createCompany(testCompany);
        });
        verify(companyRepository, times(1)).existsByEmail("contact@techcorp.com");
        verify(companyRepository, times(1)).existsByTaxCode("1234567890");
        verify(companyRepository, times(1)).existsByName("Tech Corp");
        verify(companyRepository, never()).save(any(Company.class));
    }

    // ==================== UpdateCompany Tests ====================

    @Test
    @DisplayName("updateCompany successfully updates a company with valid data")
    void testUpdateCompanySuccess() {
        // Arrange
        Company updatedData = new Company();
        updatedData.setName("New Tech Corp");
        updatedData.setDescription("Updated description with enough characters");
        updatedData.setAddress("456 New Street");
        updatedData.setEmail("newemail@techcorp.com");
        updatedData.setPhone("0987654321");
        updatedData.setWebsite("www.newtechcorp.com");
        updatedData.setTaxCode("0987654321");
        updatedData.setNumberOfEmployees(200);
        updatedData.setLogo("new_logo_url");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyRepository.existsByTaxCode(anyString())).thenReturn(false);
        when(companyRepository.existsByName(anyString())).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

        // Act
        Company result = companyService.updateCompany(1L, updatedData);

        // Assert
        assertNotNull(result);
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("updateCompany throws ResourceNotFoundException when company not found")
    void testUpdateCompanyNotFound() {
        // Arrange
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        Company updatedData = new Company();
        updatedData.setName("New Name");
        updatedData.setEmail("new@email.com");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            companyService.updateCompany(999L, updatedData);
        });
        verify(companyRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("updateCompany throws DuplicateResourceException when email already exists")
    void testUpdateCompanyDuplicateEmail() {
        // Arrange
        Company updatedData = new Company();
        updatedData.setEmail("other@email.com");
        updatedData.setName("Tech Corp");
        updatedData.setTaxCode("1234567890");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            companyService.updateCompany(1L, updatedData);
        });
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, never()).save(any(Company.class));
    }

    // ==================== DeleteCompany Tests ====================

    @Test
    @DisplayName("deleteCompany successfully deletes a company")
    void testDeleteCompanySuccess() {
        // Arrange
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        doNothing().when(companyRepository).delete(any(Company.class));

        // Act
        companyService.deleteCompany(1L);

        // Assert
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).delete(any(Company.class));
    }

    @Test
    @DisplayName("deleteCompany throws ResourceNotFoundException when company not found")
    void testDeleteCompanyNotFound() {
        // Arrange
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            companyService.deleteCompany(999L);
        });
        verify(companyRepository, times(1)).findById(999L);
        verify(companyRepository, never()).delete(any(Company.class));
    }

    // ==================== Helper Methods Tests ====================

    @Test
    @DisplayName("existsByEmail returns true when email exists")
    void testExistsByEmailTrue() {
        // Arrange
        when(companyRepository.existsByEmail("test@email.com")).thenReturn(true);

        // Act
        boolean result = companyService.existsByEmail("test@email.com");

        // Assert
        assertTrue(result);
        verify(companyRepository, times(1)).existsByEmail("test@email.com");
    }

    @Test
    @DisplayName("existsByTaxCode returns true when tax code exists")
    void testExistsByTaxCodeTrue() {
        // Arrange
        when(companyRepository.existsByTaxCode("1234567890")).thenReturn(true);

        // Act
        boolean result = companyService.existsByTaxCode("1234567890");

        // Assert
        assertTrue(result);
        verify(companyRepository, times(1)).existsByTaxCode("1234567890");
    }
}
