package com.g3.parking.datatransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingDTO {
    private Long id;
    private String name;
    private Double lat;
    private Double lng;
    private String address;
    private OrganizationDTO organization;
    private UserDTO createdBy;
}
