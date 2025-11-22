package com.g3.parking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="user_subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private LocalDateTime activationDate;

    @Column(nullable = false)
    private int monthsDuration;

    @Column(precision = 10, scale = 2, nullable = false) // 10 dígitos en total, 2 decimales
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    
    // Método para saber si expiró
    @Transient 
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(activationDate.plusMonths(monthsDuration));
    }

    @Transient 
    public void upgradeStatus() {
        boolean expired = isExpired();
        if (expired) this.status = SubscriptionStatus.EXPIRED;
    }

}


