package com.g3.parking.datatransfer;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCategoryDTO {
    private Long id;
    private String name; // Moto, Carro, Camion
    private BigDecimal ratePerHour; // Tarifa por hora
    private boolean active;
}
