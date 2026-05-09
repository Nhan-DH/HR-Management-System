package com.dona.spring_rest.feature.company;

import org.springframework.stereotype.Service;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", id));
    }

    @Override
    public Company createCompany(Company company) {
        if (companyRepository.existsByEmail(company.getEmail())) {
            throw new DuplicateResourceException("Email công ty đã tồn tại");
        }

        if (companyRepository.existsByTaxCode(company.getTaxCode())) {
            throw new DuplicateResourceException("Mã số thuế đã tồn tại");
        }

        if (companyRepository.existsByName(company.getName())) {
            throw new DuplicateResourceException("Tên công ty đã tồn tại");
        }

        company.setCreatedAt(Instant.now());
        company.setUpdatedAt(Instant.now());

        return companyRepository.save(company);
    }

    @Override
    public Company updateCompany(Long id, Company companyDetails) {
        Company company = getCompanyById(id);

        // Check for duplicate email if email is being updated
        if (!company.getEmail().equals(companyDetails.getEmail()) &&
                companyRepository.existsByEmail(companyDetails.getEmail())) {
            throw new DuplicateResourceException("Email công ty đã tồn tại");
        }

        // Check for duplicate tax code if tax code is being updated
        if (!company.getTaxCode().equals(companyDetails.getTaxCode()) &&
                companyRepository.existsByTaxCode(companyDetails.getTaxCode())) {
            throw new DuplicateResourceException("Mã số thuế đã tồn tại");
        }

        // Check for duplicate name if name is being updated
        if (!company.getName().equals(companyDetails.getName()) &&
                companyRepository.existsByName(companyDetails.getName())) {
            throw new DuplicateResourceException("Tên công ty đã tồn tại");
        }

        company.setName(companyDetails.getName());
        company.setDescription(companyDetails.getDescription());
        company.setAddress(companyDetails.getAddress());
        company.setEmail(companyDetails.getEmail());
        company.setPhone(companyDetails.getPhone());
        company.setWebsite(companyDetails.getWebsite());
        company.setTaxCode(companyDetails.getTaxCode());
        company.setNumberOfEmployees(companyDetails.getNumberOfEmployees());
        company.setLogo(companyDetails.getLogo());
        company.setUpdatedAt(Instant.now());

        return companyRepository.save(company);
    }

    @Override
    public void deleteCompany(Long id) {
        Company company = getCompanyById(id);
        companyRepository.delete(company);
    }

    @Override
    public boolean existsByEmail(String email) {
        return companyRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByTaxCode(String taxCode) {
        return companyRepository.existsByTaxCode(taxCode);
    }
}
