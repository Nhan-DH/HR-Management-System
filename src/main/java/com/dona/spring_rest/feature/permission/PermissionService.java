package com.dona.spring_rest.feature.permission;

import java.util.List;

public interface PermissionService {
    List<Permission> getAllPermissions();

    Permission getPermissionById(Long id);

    Permission createPermission(Permission permission);

    Permission updatePermission(Long id, Permission permission);

    void deletePermission(Long id);

    boolean existsByName(String name);

    boolean existsByApiPathAndMethod(String apiPath, String method);
}
