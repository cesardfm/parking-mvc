package com.g3.parking.datatransfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.g3.parking.model.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDTO {
    private Long id;
    private UserDTO user;
    private PlanDTO plan;
    private LocalDateTime activationDate;
    private int monthsDuration;
    private BigDecimal price;
    private SubscriptionStatus status;
    
    public SubscriptionStatus setSubscriptionStatusFromString(String status){
        return SubscriptionStatus.valueOf(status);
    }
}
