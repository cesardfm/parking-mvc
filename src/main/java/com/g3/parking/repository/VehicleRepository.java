package com.g3.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g3.parking.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByOwner_Id(Long ownerId);
    Optional<Vehicle> findByIdAndOwner_Id(Long id, Long ownerId);
    long countByCategory_Id(Long categoryId);

    List<Vehicle> findByOwnerIsNull(); // Vehículos sin dueño (visitantes)
    List<Vehicle> findByOwnerIsNotNull(); // Vehículos con dueño registrado
    
    List<Vehicle> findByLicensePlateContaining(String plateFragment); // Búsqueda flexible por placa (contiene)
    boolean existsByLicensePlate(String licensePlate); // Verificar existencia por placa
}
