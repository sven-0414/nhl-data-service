package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.model.enums.RoleName;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // För att HITTA användare (returnerar objektet)
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // För att KOLLA om användare finns (returnerar true/false)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Räkna admins
    long countByRoles_Name(RoleName roleName);
}