package com.dona.spring_rest.feature.user;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user, List<Long> roleIds);

    User updateUser(Long id, User user, List<Long> roleIds);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    User getUserByEmail(String email);
}
