package com.g3.parking.datatransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Long id;
    private String licensePlate;
    private String color;
    private UserDTO owner;
    private VehicleCategoryDTO category;
}
