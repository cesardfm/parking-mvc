package com.g3.parking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g3.parking.model.VehicleCategory;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {

    // Buscar por nombre exacto
    @Query(
        value = "SELECT * FROM vehicle_category WHERE name = :name LIMIT 1",
        nativeQuery = true
    )
    VehicleCategory findByName(@Param("name") String name);

    // Buscar categorías activas
    @Query(
        value = "SELECT * FROM vehicle_category WHERE active = TRUE",
        nativeQuery = true
    )
    List<VehicleCategory> findByActiveTrue();

    // Buscar por nombre y activas
    @Query(
        value = "SELECT * FROM vehicle_category WHERE name = :name AND active = TRUE LIMIT 1",
        nativeQuery = true
    )
    VehicleCategory findByNameAndActiveTrue(@Param("name") String name);
}
