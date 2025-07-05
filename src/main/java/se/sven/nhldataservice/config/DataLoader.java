package se.sven.nhldataservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.repository.UserRepository;

/**
 * Creates default admin user on startup if none exists.
 * Uses ADMIN_PASSWORD env var or falls back to default.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            String defaultPassword = System.getenv("ADMIN_PASSWORD") != null ?
                    System.getenv("ADMIN_PASSWORD") : "defaultPassword123";
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setEnabled(true);
            userRepository.save(admin);

            log.info("Admin user created with username: admin");
        }
    }
}