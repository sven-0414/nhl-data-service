package se.sven.nhldataservice.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sven.nhldataservice.dto.*;
import se.sven.nhldataservice.exception.*;
import se.sven.nhldataservice.model.Role;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.model.enums.RoleName;
import se.sven.nhldataservice.repository.RoleRepository;
import se.sven.nhldataservice.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for user management operations including CRUD operations,
 * role management, and permission checks.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user with specified roles.
     * Only admins can create users with the ADMIN role.
     */
    public UserResponse createUser(CreateUserRequest request, Authentication auth) {
        validateUniqueUsername(request.getUsername());

        if (request.getEmail() != null) {
            validateUniqueEmail(request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);

        // Set roles - only admins can create other admins
        Set<Role> roles = mapRoleNames(request.getRoles());
        if (containsAdminRole(roles) && !isAdmin(auth)) {
            throw new InsufficientPermissionException("Only admins can create admin users");
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    /**
     * Updates user information. Users can only update themselves unless they are admin.
     */
    public UserResponse updateUser(Long id, UserUpdateRequest request, Authentication auth) {
        User user = findUserById(id);

        // Permission check - users can only update themselves, admins can update anyone
        if (isAllowedToUpdate(id, auth)) {
            updateBasicUserFields(user, request);
            User savedUser = userRepository.save(user);
            return mapToResponse(savedUser);
        } else {
            throw new InsufficientPermissionException("You can only update your own profile");
        }
    }

    /**
     * Changes user password. Users can only change their own password.
     */
    public void changePassword(Long id, ChangePasswordRequest request, Authentication auth) {
        User user = findUserById(id);

        // Only allow users to change their own password
        if (!getCurrentUserId(auth).equals(id)) {
            throw new InsufficientPermissionException("You can only change your own password");
        }

        // Verify the current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Deletes a user with admin check to prevent deleting the last admin.
     */
    public void deleteUserWithAdminCheck(Long id, Authentication auth) {
        User user = findUserById(id);

        Long currentUserId = getCurrentUserId(auth);
        if (currentUserId.equals(id)) {
            throw new IllegalArgumentException("Cannot delete your own account");
        }

        if (user.hasRole(RoleName.ADMIN) && countAdmins() <= 1) {
            throw new LastAdminException("Cannot delete the last admin user");
        }

        userRepository.delete(user);
    }

    /**
     * Retrieves all users. Only accessible by admins.
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves user by ID (accessible by all authenticated users).
     */
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return mapToResponse(user);
    }

    /**
     * Gets current user's profile.
     */
    public UserResponse getCurrentUserProfile(Authentication auth) {
        Long currentUserId = getCurrentUserId(auth);
        User user = findUserById(currentUserId);
        return mapToResponse(user);
    }

    /**
     * Checks if the authenticated user can update the specified user.
     * Users can update themselves, admins can update anyone.
     */
    public boolean isAllowedToUpdate(Long userId, Authentication auth) {
            return getCurrentUserId(auth).equals(userId) || isAdmin(auth);
    }

    /**
     * Gets the current authenticated user's ID.
     */
    public Long getCurrentUserId(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        return user.getId();
    }

    /**
     * Admin update user with full privileges including roles and enabled status.
     * Only admins can use this method.
     */
    public UserResponse adminUpdateUser(Long id, AdminUserRequest request, Authentication auth) {
        if (!isAdmin(auth)) {
            throw new InsufficientPermissionException("Only admins can perform administrative user updates");
        }

        User user = findUserById(id);

        // Prevent admin from disabling themselves
        Long currentUserId = getCurrentUserId(auth);
        if (currentUserId.equals(id) && Boolean.FALSE.equals(request.getEnabled())) {
            throw new IllegalArgumentException("Admins cannot disable their own account");
        }

        // Update basic fields using helper method
        UserUpdateRequest basicRequest = new UserUpdateRequest(request.getUsername(), request.getEmail());
        updateBasicUserFields(user, basicRequest);

        // Update enabled status if provided
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        // Update roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            // Check if trying to remove the admin role from last admin
            if (user.hasRole(RoleName.ADMIN) &&
                    !request.getRoles().contains(RoleName.ADMIN) &&
                    countAdmins() <= 1) {
                throw new LastAdminException("Cannot remove admin role from the last admin user");
            }

            Set<Role> newRoles = mapRoleNames(request.getRoles());
            user.setRoles(newRoles);
        }

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    // === Private helper methods ===

    /**
     * Updates basic user fields (username, email) from request if they are different.
     */
    private void updateBasicUserFields(User user, UserUpdateRequest request) {
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            validateUniqueUsername(request.getUsername());
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateUniqueEmail(request.getEmail());
            user.setEmail(request.getEmail());
        }
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    private void validateUniqueUsername(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists: " + username);
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }
    }
    private Set<Role> mapRoleNames(Set<RoleName> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            // Default to the USER role
            Role userRole = roleRepository.findByName(RoleName.USER)
                    .orElseThrow(() -> new RuntimeException("Default USER role not found"));
            return Set.of(userRole);
        }

        return roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
    }

    private boolean containsAdminRole(Set<Role> roles) {
        return roles.stream().anyMatch(role -> role.getName() == RoleName.ADMIN);
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private long countAdmins() {
        return userRepository.countByRoles_Name(RoleName.ADMIN);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());
        response.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return response;
    }
}