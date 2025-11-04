package com.g3.parking.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="vehicle_categories")
public class VehicleCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Moto, Carro, Camion

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rate_per_hour; // Tarifa por hora
}
