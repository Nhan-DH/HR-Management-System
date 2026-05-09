package com.dona.spring_rest.feature.company;

import java.util.List;

public interface CompanyService {

    List<Company> getAllCompanies();

    Company getCompanyById(Long id);

    Company createCompany(Company company);

    Company updateCompany(Long id, Company company);

    void deleteCompany(Long id);

    boolean existsByEmail(String email);

    boolean existsByTaxCode(String taxCode);
}
