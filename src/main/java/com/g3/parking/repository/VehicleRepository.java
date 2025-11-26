package com.g3.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g3.parking.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // Buscar por placa exacta
    @Query(
        value = "SELECT * FROM vehicle WHERE license_plate = :plate LIMIT 1",
        nativeQuery = true
    )
    Optional<Vehicle> findByLicensePlate(@Param("plate") String licensePlate);


    // Buscar por owner_id
    @Query(
        value = "SELECT * FROM vehicle WHERE owner_id = :ownerId",
        nativeQuery = true
    )
    List<Vehicle> findByOwner_Id(@Param("ownerId") Long ownerId);


    // Buscar por id y owner_id
    @Query(
        value = "SELECT * FROM vehicle WHERE id = :id AND owner_id = :ownerId LIMIT 1",
        nativeQuery = true
    )
    Optional<Vehicle> findByIdAndOwner_Id(
            @Param("id") Long id,
            @Param("ownerId") Long ownerId
    );


    // Contar por categoría
    @Query(
        value = "SELECT COUNT(*) FROM vehicle WHERE category_id = :categoryId",
        nativeQuery = true
    )
    long countByCategory_Id(@Param("categoryId") Long categoryId);


    // Vehículos sin dueño (owner_id IS NULL)
    @Query(
        value = "SELECT * FROM vehicle WHERE owner_id IS NULL",
        nativeQuery = true
    )
    List<Vehicle> findByOwnerIsNull();


    // Vehículos con dueño (owner_id IS NOT NULL)
    @Query(
        value = "SELECT * FROM vehicle WHERE owner_id IS NOT NULL",
        nativeQuery = true
    )
    List<Vehicle> findByOwnerIsNotNull();


    // Buscar placa que contenga fragmento (LIKE %texto%)
    @Query(
        value = "SELECT * FROM vehicle WHERE LOWER(license_plate) LIKE CONCAT('%', LOWER(:plateFragment), '%')",
        nativeQuery = true
    )
    List<Vehicle> findByLicensePlateContaining(@Param("plateFragment") String plateFragment);


    // Verificar si existe una placa
    @Query(
        value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END " +
                "FROM vehicle WHERE license_plate = :plate",
        nativeQuery = true
    )
    boolean existsByLicensePlate(@Param("plate") String licensePlate);
}
