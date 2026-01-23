package se.sven.nhldataservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import se.sven.nhldataservice.dto.*;
import se.sven.nhldataservice.service.UserService;

import java.util.List;

/**
 * REST controller for user management operations.
 * Provides endpoints for user CRUD operations with role-based access control.
 */
@Tag(name = "Users", description = "User management operations")
@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current user's profile.
     */
    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the authenticated user's own profile information"
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication auth) {
        UserResponse user = userService.getCurrentUserProfile(auth);
        return ResponseEntity.ok(user);
    }

    /**
     * Update current user's profile.
     */
    @Operation(
            summary = "Update current user profile",
            description = "Updates the authenticated user's own profile information"
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication auth) {
        Long currentUserId = userService.getCurrentUserId(auth);
        UserResponse updatedUser = userService.updateUser(currentUserId, request, auth);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change the current user's password.
     */
    @Operation(
            summary = "Change password",
            description = "Changes the authenticated user's password"
    )
    @ApiResponse(responseCode = "204", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid current password or new password")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication auth) {
        Long currentUserId = userService.getCurrentUserId(auth);
        userService.changePassword(currentUserId, request, auth);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all users (accessible by all authenticated users).
     */
    @Operation(
            summary = "Get all users",
            description = "Retrieves all users in the system. Accessible by all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID (accessible by all authenticated users).
     */
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user by ID. All authenticated users can view user profiles."
    )
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Create new user (Admin only).
     */
    @Operation(
            summary = "Create new user",
            description = "Creates a new user. Admin access required."
    )
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data or username/email already exists")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication auth) {
        UserResponse createdUser = userService.createUser(request, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Update user by ID (Admin or own profile).
     */
    @Operation(
            summary = "Update user",
            description = "Updates user information. Users can update their own profile, admins can update any user."
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    @PreAuthorize("#id == @userService.getCurrentUserId(authentication) or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication auth) {
        UserResponse updatedUser = userService.updateUser(id, request, auth);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user by ID (Admin only).
     */
    @Operation(
            summary = "Delete user",
            description = "Deletes a user. Admin access required. Cannot delete the last admin."
    )
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "409", description = "Cannot delete last admin")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            Authentication auth) {
        userService.deleteUserWithAdminCheck(id, auth);
        return ResponseEntity.noContent().build();
    }

    // === Admin User Management ===

    /**
     * Update user roles and status (Admin only).
     */
    @Operation(
            summary = "Admin update user",
            description = "Updates user roles and enabled status. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminUpdateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody AdminUserRequest request,
            Authentication auth) {
        UserResponse updatedUser = userService.adminUpdateUser(id, request, auth);
        return ResponseEntity.ok(updatedUser);
    }
}