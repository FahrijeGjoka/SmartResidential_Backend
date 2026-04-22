package com.smartresidential.backend.services;

import com.smartresidential.backend.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    List<User> getAllActiveUsers();

    List<User> getUsersByRoleId(Long roleId);

    Optional<User> getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    Optional<User> getActiveUserByEmail(String email);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    User activateUser(Long id);

    User deactivateUser(Long id);

    boolean existsByEmail(String email);
}