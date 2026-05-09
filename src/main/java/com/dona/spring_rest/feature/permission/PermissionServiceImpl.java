package com.dona.spring_rest.feature.permission;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quyền", "id", id));
    }

    @Override
    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByName(permission.getName())) {
            throw new DuplicateResourceException("Quyền", "name", permission.getName());
        }

        if (permissionRepository.existsByApiPathAndMethod(permission.getApiPath(), permission.getMethod())) {
            throw new DuplicateResourceException("Quyền", "apiPath + method",
                    permission.getApiPath() + " " + permission.getMethod());
        }

        permission.setCreatedAt(Instant.now());
        permission.setUpdatedAt(Instant.now());
        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Long id, Permission permission) {
        Permission existingPermission = getPermissionById(id);

        // Check for duplicate name only if name is changed
        if (!existingPermission.getName().equals(permission.getName())) {
            if (permissionRepository.existsByName(permission.getName())) {
                throw new DuplicateResourceException("Quyền", "name", permission.getName());
            }
        }

        // Check for duplicate apiPath + method only if changed
        if (!existingPermission.getApiPath().equals(permission.getApiPath()) ||
                !existingPermission.getMethod().equals(permission.getMethod())) {
            if (permissionRepository.existsByApiPathAndMethod(permission.getApiPath(), permission.getMethod())) {
                throw new DuplicateResourceException("Quyền", "apiPath + method",
                        permission.getApiPath() + " " + permission.getMethod());
            }
        }

        existingPermission.setName(permission.getName());
        existingPermission.setApiPath(permission.getApiPath());
        existingPermission.setMethod(permission.getMethod());
        existingPermission.setModule(permission.getModule());
        existingPermission.setUpdatedAt(Instant.now());

        return permissionRepository.save(existingPermission);
    }

    @Override
    public void deletePermission(Long id) {
        Permission permission = getPermissionById(id);
        permissionRepository.delete(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByApiPathAndMethod(String apiPath, String method) {
        return permissionRepository.existsByApiPathAndMethod(apiPath, method);
    }
}
