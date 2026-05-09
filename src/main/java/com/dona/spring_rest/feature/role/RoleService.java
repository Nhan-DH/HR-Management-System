package com.dona.spring_rest.feature.role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();

    Role getRoleById(Long id);

    Role createRole(Role role);

    Role updateRole(Long id, Role role);

    void deleteRole(Long id);

    boolean existsByName(String name);
}
