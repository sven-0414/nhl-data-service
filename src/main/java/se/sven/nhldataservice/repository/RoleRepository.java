package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sven.nhldataservice.model.Role;
import se.sven.nhldataservice.model.enums.RoleName;

import java.util.Optional;

/**
 * Repository for Role entities.
 * Provides database access for user role management.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}