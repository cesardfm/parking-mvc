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
public class OrganizationDTO {
    private Long id;
    private String name;
    private String description;
    private String taxId; // RUC, NIT, etc.
    private String address;
    private String phone;
    private String email;
    private Set<UserDTO> users;
    private List<ParkingDTO> parkings;;
    private Boolean active;
}
