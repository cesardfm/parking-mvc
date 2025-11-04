package com.g3.parking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    @Column(nullable = true)
    private LocalDateTime exitTime;

    @Column(precision = 10, scale = 2, nullable = true) // 10 dígitos en total, 2 decimales
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private boolean paid = false;
}
