package com.g3.parking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter 
@Table(name="plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(precision = 10, scale = 2, nullable = false) // 10 dígitos en total, 2 decimales
    private BigDecimal price;

    private Boolean active = true;

    @Column(precision = 3, scale = 2, nullable = false) // 3 dígitos en total, 2 decimales
    private BigDecimal discountPercent;


}
