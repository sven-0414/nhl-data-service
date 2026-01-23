package se.sven.nhldataservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.model.enums.RoleName;

import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing system users with role-based permissions.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Many-to-many relationship with roles.
     * Uses eager fetching to load roles with user for security checks.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Custom constructor for easier creation
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Checks if user has a specific role.
     *
     * @param roleName the role to check for
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(RoleName roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    /**
     * Checks if user has admin privileges.
     *
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole(RoleName.ADMIN);
    }

    /**
     * Adds a role to the user.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Removes all roles and sets new ones.
     *
     * @param newRoles the new set of roles
     */
    public void setRoles(Set<Role> newRoles) {
        this.roles.clear();
        if (newRoles != null) {
            this.roles.addAll(newRoles);
        }
    }
}