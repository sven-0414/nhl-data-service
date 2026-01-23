package se.sven.nhldataservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.sven.nhldataservice.exception.ApplicationInitializationException;
import se.sven.nhldataservice.model.Role;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.model.enums.RoleName;
import se.sven.nhldataservice.repository.RoleRepository;
import se.sven.nhldataservice.repository.UserRepository;
import se.sven.nhldataservice.service.RoleService;

@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      RoleRepository roleRepository,
                      RoleService roleService,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");

        // Create default roles first
        createDefaultRoles();

        // Create default admin user
        createDefaultAdminUser();

        log.info("Data initialization completed");
    }

    /**
     * Creates default USER and ADMIN roles if they don't exist.
     */
    private void createDefaultRoles() {
        log.info("Checking and creating default roles...");

        try {
            roleService.createDefaultRoles();
            log.info("Default roles ensured (USER, ADMIN)");
        } catch (Exception e) {
            log.error("Failed to create default roles: {}", e.getMessage(), e);
            throw new ApplicationInitializationException("Failed to initialize default roles", e);
        }
    }

    /**
     * Creates default admin user if none exists.
     * Uses ADMIN_PASSWORD environment variable or falls back to default.
     */
    private void createDefaultAdminUser() {
        log.info("Checking for admin user...");

        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        try {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found - roles must be created first"));

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setEnabled(true);

            String adminPassword = getAdminPassword();
            admin.setPassword(passwordEncoder.encode(adminPassword));

            admin.addRole(adminRole);
            userRepository.save(admin);

            log.info("Admin user created successfully");
            if (isDefaultPassword(adminPassword)) {
                log.warn("WARNING: Using default admin password! Set ADMIN_PASSWORD environment variable in production!");
            }

        } catch (Exception e) {
            log.error("Failed to create admin user: {}", e.getMessage(), e);
            throw new ApplicationInitializationException("Failed to create default admin user", e);
        }
    }

    /**
     * Gets admin password from environment variable or returns default.
     *
     * @return admin password to use
     */
    private String getAdminPassword() {
        String envPassword = System.getenv("ADMIN_PASSWORD");
        if (envPassword != null && !envPassword.trim().isEmpty()) {
            log.info("Using admin password from ADMIN_PASSWORD environment variable");
            return envPassword;
        }

        log.warn("Using default admin password (development only)");
        return "defaultPassword123";
    }

    /**
     * Checks if the provided password is the default development password.
     *
     * @param password password to check
     * @return true if it's the default password
     */
    private boolean isDefaultPassword(String password) {
        return "defaultPassword123".equals(password);
    }
}