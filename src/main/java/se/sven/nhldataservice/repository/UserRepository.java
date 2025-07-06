package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.model.enums.RoleName;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    long countByRoles_Name(RoleName roleName);
}