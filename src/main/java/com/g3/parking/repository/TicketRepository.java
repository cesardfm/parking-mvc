package com.g3.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g3.parking.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByVehicle_LicensePlate(String licensePlate);
    Optional<Ticket> findByPaidFalseAndVehicle_LicensePlate(String licensePlate);
    List<Ticket> findBySite_Id(Long siteId);
    List<Ticket> findBySite_Level_Parking_Id(Long parkingId);
    Optional<Ticket> findById(Long id);
    
    /**
     * Carga un ticket con sus relaciones sin que Hibernate intente
     * reacoplar entidades de User que pudieran estar ya en sesión.
     * Esto previene el error: "Identifier of an instance of 'User' was altered from X to Y"
     */
    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN FETCH t.site s " +
           "LEFT JOIN FETCH s.level l " +
           "LEFT JOIN FETCH l.parking p " +
           "LEFT JOIN FETCH p.organization " +
           "LEFT JOIN FETCH t.vehicle v " +
           "LEFT JOIN FETCH v.category " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithRelations(@Param("id") Long id);
}

