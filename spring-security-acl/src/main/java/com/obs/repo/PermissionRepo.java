package com.obs.repo;

import com.obs.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepo extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
