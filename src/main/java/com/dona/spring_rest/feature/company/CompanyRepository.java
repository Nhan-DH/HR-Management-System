package com.dona.spring_rest.feature.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    boolean existsByTaxCode(String taxCode);

    Company findByEmail(String email);

    Company findByTaxCode(String taxCode);
}
