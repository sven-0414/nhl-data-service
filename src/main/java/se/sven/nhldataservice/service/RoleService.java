package se.sven.nhldataservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sven.nhldataservice.model.Role;
import se.sven.nhldataservice.model.enums.RoleName;
import se.sven.nhldataservice.repository.RoleRepository;

/**
 * Service for role management operations.
 */
@Slf4j
@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Creates default roles if they don't exist.
     * Should be called during application startup.
     */
    public void createDefaultRoles() {
        log.info("Initializing default roles");
        createRoleIfNotExists(RoleName.USER);
        createRoleIfNotExists(RoleName.ADMIN);
        log.info("Default roles initialization complete");
    }

    /**
     * Creates a role if it doesn't already exist.
     */
    private void createRoleIfNotExists(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        } else {
            log.debug("Role already exists: {}", roleName);
        }
    }
}