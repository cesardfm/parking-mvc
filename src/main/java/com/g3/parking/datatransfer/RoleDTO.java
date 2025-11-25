package com.g3.parking.datatransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name; // "ROLE_OWNER", "ROLE_ADMIN", "ROLE_USER"
}
