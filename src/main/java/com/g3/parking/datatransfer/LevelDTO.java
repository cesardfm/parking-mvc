package com.g3.parking.datatransfer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelDTO {
    private Long id;
    private Integer columns;
    private Integer rows;
    private ParkingDTO parking;
    private List<SiteDTO> sites;
}
