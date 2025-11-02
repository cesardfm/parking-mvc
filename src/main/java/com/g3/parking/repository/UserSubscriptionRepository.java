package com.g3.parking.repository;

import com.g3.parking.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserIdAndStatus(Long userId, String status);
    Optional<UserSubscription> findByPlanId(Long planId);
}
