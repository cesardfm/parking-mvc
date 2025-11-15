package com.g3.parking.datatransfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.g3.parking.model.Parking;
import com.g3.parking.model.Ticket;
import com.g3.parking.model.Vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private ParkingDTO parking;
    private VehicleDTO vehicle;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal totalAmount;
    private boolean paid;

}