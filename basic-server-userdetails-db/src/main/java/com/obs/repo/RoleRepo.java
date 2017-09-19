package com.obs.repo;

import com.obs.entity.Role;
import com.obs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(String role);
}
