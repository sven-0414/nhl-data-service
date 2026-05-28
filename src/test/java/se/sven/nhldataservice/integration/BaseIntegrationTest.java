package se.sven.nhldataservice.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.sven.nhldataservice.model.Role;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.model.enums.RoleName;
import se.sven.nhldataservice.repository.RoleRepository;
import se.sven.nhldataservice.repository.UserRepository;
import se.sven.nhldataservice.util.JwtUtil;

import java.util.Set;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtUtil jwtUtil;

    /**
     * Creates a test user with USER role and returns the saved entity.
     */
    protected User createTestUser(String username, String email, String password) {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.USER);
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(userRole));
        return userRepository.save(user);
    }

    /**
     * Creates a test admin user with ADMIN role.
     */
    protected User createTestAdmin() {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.ADMIN);
                    return roleRepository.save(newRole);
                });

        User admin = new User();
        admin.setUsername("testadmin");
        admin.setEmail("testadmin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of(adminRole));
        return userRepository.save(admin);
    }

    /**
     * Generates a valid JWT token for the given user.
     */
    protected String generateToken(User user) {
        return jwtUtil.generateToken(user.getUsername());
    }

    /**
     * Returns the Authorization header value with Bearer token.
     */
    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}