package com.g3.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.g3.parking.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Buscar tickets por placa del vehículo
    @Query(
        value = "SELECT t.* FROM tickets t " +
                "JOIN vehicles v ON t.vehicle_id = v.id " +
                "WHERE v.license_plate = :licensePlate",
        nativeQuery = true
    )
    List<Ticket> findByVehicleLicensePlate(@Param("licensePlate") String licensePlate);


    // Ticket sin pagar por placa
    @Query(
        value = "SELECT t.* FROM tickets t " +
                "JOIN vehicles v ON t.vehicle_id = v.id " +
                "WHERE t.paid = FALSE AND v.license_plate = :licensePlate " +
                "LIMIT 1",
        nativeQuery = true
    )
    Optional<Ticket> findByPaidFalseAndVehicleLicensePlate(@Param("licensePlate") String licensePlate);


    // Tickets por ID de Site
    @Query(
        value = "SELECT * FROM tickets WHERE site_id = :siteId",
        nativeQuery = true
    )
    List<Ticket> findBySiteId(@Param("siteId") Long siteId);


    // Tickets por parking (a través de site → level → parking)
    @Query(
        value = "SELECT t.* FROM tickets t " +
                "JOIN sites s ON t.site_id = s.id " +
                "JOIN levels l ON s.level_id = l.id " +
                "WHERE l.parking_id = :parkingId",
        nativeQuery = true
    )
    List<Ticket> findByParkingId(@Param("parkingId") Long parkingId);


    // Tickets por parking y owner del vehículo
    @Query(
        value = "SELECT t.* FROM tickets t " +
                "JOIN vehicles v ON t.vehicle_id = v.id " +
                "JOIN sites s ON t.site_id = s.id " +
                "JOIN levels l ON s.level_id = l.id " +
                "WHERE l.parking_id = :parkingId AND v.owner_id = :ownerId",
        nativeQuery = true
    )
    List<Ticket> findByParkingIdAndOwnerId(
            @Param("parkingId") Long parkingId,
            @Param("ownerId") Long ownerId
    );


    // Reemplaza el findById normal
    @Query(
        value = "SELECT * FROM tickets WHERE id = :id LIMIT 1",
        nativeQuery = true
    )
    Optional<Ticket> findById(@Param("id") Long id);


    /**
     * Cargar un ticket con todas sus relaciones
     * usando SQL nativo (equivalente al JPQL con FETCH).
     */
    @Query(
        value = "SELECT t.* FROM tickets t " +
                "LEFT JOIN sites s ON t.site_id = s.id " +
                "LEFT JOIN levels l ON s.level_id = l.id " +
                "LEFT JOIN parkings p ON l.parking_id = p.id " +
                "LEFT JOIN organizations o ON p.organization_id = o.id " +
                "LEFT JOIN vehicles v ON t.vehicle_id = v.id " +
                "LEFT JOIN categories c ON v.category_id = c.id " +
                "WHERE t.id = :id LIMIT 1",
        nativeQuery = true
    )
    Optional<Ticket> findByIdWithRelations(@Param("id") Long id);
}
