package com.g3.parking.repository;

import com.g3.parking.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(
        value = "SELECT * FROM roles r WHERE r.name = :name LIMIT 1",
        nativeQuery = true
    )
    Role findByName(@Param("name") String name);
}
