package com.banking.repository;

import com.banking.entity.Role;
import com.banking.enums.RoleType;  // ← ganti import ini
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);  
}