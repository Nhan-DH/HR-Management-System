package com.dona.spring_rest.feature.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String name);

    boolean existsByApiPathAndMethod(String apiPath, String method);

    Optional<Permission> findByName(String name);

    Optional<Permission> findByApiPathAndMethod(String apiPath, String method);
}
