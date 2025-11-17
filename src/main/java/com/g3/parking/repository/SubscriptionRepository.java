package com.g3.parking.repository;

import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    Optional<Subscription> findByPlanId(Long planId);
}
