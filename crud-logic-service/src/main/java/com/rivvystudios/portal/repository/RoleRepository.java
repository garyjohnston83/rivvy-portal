package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
