package com.g3.parking.repository;

import com.g3.parking.model.Organization;
import com.g3.parking.model.Parking;
import com.g3.parking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingRepository extends JpaRepository<Parking, Long> {
    
    // Buscar por nombre
    Optional<Parking> findByName(String name);
    
    // Buscar por nombre (parcial)
    List<Parking> findByNameContainingIgnoreCase(String name);

    // Buscar por organización
    List<Parking> findByOrganization(Organization organization);
    
    // Buscar por ID y organización (validación automática)
    Optional<Parking> findByIdAndOrganization(Long id, Organization organization);
    
    // Query personalizada
    @Query("SELECT p FROM Parking p WHERE p.id = :parkingId AND p.organization.id = :orgId")
    Optional<Parking> findByIdAndOrganizationId(
        @Param("parkingId") Long parkingId, 
        @Param("orgId") Long orgId
    );
    
    // Contar parkings por organización
    long countByOrganization(Organization organization);

    // Buscar parkings donde un usuario está en la colección admins
    List<Parking> findByAdminsContaining(User admin);
    // Alternativa por id
    List<Parking> findByAdmins_Id(Long adminId);
}
