package com.dona.spring_rest.feature.role;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò", "id", id));
    }

    @Override
    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new DuplicateResourceException("Vai trò", "name", role.getName());
        }

        role.setCreatedAt(Instant.now());
        role.setUpdatedAt(Instant.now());
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        Role existingRole = getRoleById(id);

        // Check for duplicate name only if name is changed
        if (!existingRole.getName().equals(role.getName())) {
            if (roleRepository.existsByName(role.getName())) {
                throw new DuplicateResourceException("Vai trò", "name", role.getName());
            }
        }

        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setUpdatedAt(Instant.now());

        return roleRepository.save(existingRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}
