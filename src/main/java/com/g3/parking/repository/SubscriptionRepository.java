package com.g3.parking.repository;

import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Buscar suscripción por usuario y estado
    @Query(
        value = "SELECT * FROM subscriptions s WHERE s.user_id = :userId AND s.status = :status LIMIT 1",
        nativeQuery = true
    )
    Optional<Subscription> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") SubscriptionStatus status
    );

    // Buscar por plan
    @Query(
        value = "SELECT * FROM subscriptions s WHERE s.plan_id = :planId LIMIT 1",
        nativeQuery = true
    )
    Optional<Subscription> findByPlanId(@Param("planId") Long planId);

    // Buscar todas las suscripciones por usuario
    @Query(
        value = "SELECT * FROM subscriptions s WHERE s.user_id = :userId",
        nativeQuery = true
    )
    List<Subscription> findByUserId(@Param("userId") Long userId);
}

