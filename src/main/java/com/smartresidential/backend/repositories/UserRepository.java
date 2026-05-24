package com.smartresidential.backend.repositories;

import com.smartresidential.backend.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    boolean existsByEmail(String email);

    List<User> findAllByIsActiveTrue();

    List<User> findAllByRoleId(Long roleId);
}
