package com.g3.parking.datatransfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDTO {
    private Long id;
    private Integer posX;
    private Integer posY;
    private String status;
    private LevelDTO level;
}
