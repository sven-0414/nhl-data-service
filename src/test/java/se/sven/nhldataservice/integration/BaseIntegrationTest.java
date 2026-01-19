package se.sven.nhldataservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
    protected ObjectMapper objectMapper;

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
    protected User createTestAdmin(String username, String email, String password) {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleName.ADMIN);
                    return roleRepository.save(newRole);
                });

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
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