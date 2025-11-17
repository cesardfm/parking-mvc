package com.g3.parking.datatransfer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
