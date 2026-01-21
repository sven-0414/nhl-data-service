package se.sven.nhldataservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.repository.UserRepository;

/**
 * Custom UserDetailsService that loads user details and roles from the database
 * for Spring Security authentication and authorization.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user by username and converts to Spring Security UserDetails.
     * Maps database roles to Spring Security authorities with "ROLE_" prefix.
     *
     * @param username the username to load
     * @return UserDetails with user info and authorities
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert roles to Spring Security authorities
        String[] authorities = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName().toString())
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}