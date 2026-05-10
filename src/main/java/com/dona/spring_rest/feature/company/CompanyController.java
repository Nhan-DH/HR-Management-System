package com.dona.spring_rest.feature.company;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dona.spring_rest.dto.ApiResponse;
import com.dona.spring_rest.feature.company.dto.CompanyRequest;
import com.dona.spring_rest.feature.company.dto.CompanyResponse;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        List<CompanyResponse> responses = companies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(company)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CompanyRequest request) {
        Company company = mapToEntity(request);
        Company createdCompany = companyService.createCompany(company);
        return ResponseEntity.created(URI.create("/api/companies/" + createdCompany.getId()))
                .body(ApiResponse.success(mapToResponse(createdCompany)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        Company companyDetails = mapToEntity(request);
        Company updatedCompany = companyService.updateCompany(id, companyDetails);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(updatedCompany)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null));
    }

    private CompanyResponse mapToResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getAddress(),
                company.getEmail(),
                company.getPhone(),
                company.getWebsite(),
                company.getTaxCode(),
                company.getNumberOfEmployees(),
                company.getLogo(),
                company.getCreatedAt(),
                company.getUpdatedAt());
    }

    private Company mapToEntity(CompanyRequest request) {
        Company company = new Company();
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setAddress(request.getAddress());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setWebsite(request.getWebsite());
        company.setTaxCode(request.getTaxCode());
        company.setNumberOfEmployees(request.getNumberOfEmployees());
        company.setLogo(request.getLogo());
        return company;
    }
}
