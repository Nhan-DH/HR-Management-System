package com.dona.spring_rest.feature.user.dto;

import com.dona.spring_rest.feature.company.Company;

public record CompanyBasicResponse(
        Long id,
        String name) {
    public static CompanyBasicResponse fromEntity(Company company) {
        if (company == null) {
            return null;
        }
        return new CompanyBasicResponse(company.getId(), company.getName());
    }
}
