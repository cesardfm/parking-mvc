package com.g3.parking.repository;

import com.g3.parking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar usuario por username
    @Query(
        value = "SELECT * FROM users WHERE username = :username LIMIT 1",
        nativeQuery = true
    )
    Optional<User> findByUsername(@Param("username") String username);


    // Buscar usuarios por organización
    @Query(
        value = "SELECT * FROM users WHERE organization_id = :organizationId",
        nativeQuery = true
    )
    List<User> findByOrganizationId(@Param("organizationId") Long organizationId);
}
