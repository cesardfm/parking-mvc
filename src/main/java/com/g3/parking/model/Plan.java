package com.g3.parking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private Boolean active = true;

    @Column(precision = 3, scale = 2, nullable = false) // 3 dígitos en total, 2 decimales
    private BigDecimal discountPercent;


}
