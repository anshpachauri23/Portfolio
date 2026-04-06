package com.company.portal.user.repository;

import com.company.portal.auth.domain.Role;
import com.company.portal.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findFirstByRoleAndActiveTrue(Role role);

    List<User> findByManagerId(Long managerId);
}
