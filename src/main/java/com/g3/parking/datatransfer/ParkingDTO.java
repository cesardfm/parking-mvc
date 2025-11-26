package com.g3.parking.datatransfer;

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
    private Set<UserDTO> admins;
    private List<LevelDTO> levels;
    private UserDTO createdBy;

    public boolean isAdminById(Long userId) {
        return this.admins.stream()
            .anyMatch(admin -> admin.getId().equals(userId));
    }
}
