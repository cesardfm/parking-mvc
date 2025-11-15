package com.g3.parking.datatransfer;

import java.util.HashSet;
import java.util.Set;

import com.g3.parking.model.Organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private OrganizationDTO organization;
    private Set<RoleDTO> roles;
    private Boolean active;
}
