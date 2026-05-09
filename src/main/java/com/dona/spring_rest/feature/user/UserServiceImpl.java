package com.dona.spring_rest.feature.user;

import com.dona.spring_rest.exception.DuplicateResourceException;
import com.dona.spring_rest.exception.ResourceNotFoundException;
import com.dona.spring_rest.feature.company.Company;
import com.dona.spring_rest.feature.company.CompanyRepository;
import com.dona.spring_rest.feature.role.Role;
import com.dona.spring_rest.feature.role.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", id));
    }

    @Override
    public User createUser(User user, List<Long> roleIds) {
        // Check email duplicate
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Người dùng", "email", user.getEmail());
        }

        // Validate and set company if provided
        if (user.getCompany() != null && user.getCompany().getId() != null) {
            Company company = companyRepository.findById(user.getCompany().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", user.getCompany().getId()));
            user.setCompany(company);
        }

        // Hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set timestamps
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        // Assign roles
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                throw new ResourceNotFoundException("Vai trò", "ids", roleIds);
            }
            savedUser.setRoles(roles);
            savedUser = userRepository.save(savedUser);
        }

        return savedUser;
    }

    @Override
    public User updateUser(Long id, User user, List<Long> roleIds) {
        User existingUser = getUserById(id);

        // Check email unique (exclude self)
        if (!existingUser.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new DuplicateResourceException("Người dùng", "email", user.getEmail());
            }
        }

        // Update basic fields
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setAge(user.getAge());
        existingUser.setAddress(user.getAddress());
        existingUser.setGender(user.getGender());
        existingUser.setAvatar(user.getAvatar());

        // Update password only if provided and not blank
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Validate and update company if provided
        if (user.getCompany() != null && user.getCompany().getId() != null) {
            Company company = companyRepository.findById(user.getCompany().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Công ty", "id", user.getCompany().getId()));
            existingUser.setCompany(company);
        } else if (user.getCompany() == null) {
            existingUser.setCompany(null);
        }

        // Update roles
        if (roleIds != null && !roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllById(roleIds);
            if (roles.size() != roleIds.size()) {
                throw new ResourceNotFoundException("Vai trò", "ids", roleIds);
            }
            existingUser.setRoles(roles);
        } else {
            existingUser.setRoles(List.of());
        }

        existingUser.setUpdatedAt(Instant.now());
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        // Join table user_role is automatically cleared by JPA cascade
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "email", email));
    }
}
