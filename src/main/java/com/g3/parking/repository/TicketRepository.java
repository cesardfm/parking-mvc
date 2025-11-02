package com.g3.parking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g3.parking.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByVehicle_LicensePlate(String licensePlate);
    Optional<Ticket> findByPaidFalseAndVehicle_LicensePlate(String licensePlate);
}
