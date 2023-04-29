package com.appService2.appService2.repository;

import com.appService2.appService2.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRpository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(String roleName);
}
