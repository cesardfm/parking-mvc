package com.g3.parking.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.UserSubscription;
import com.g3.parking.repository.UserSubscriptionRepository;

public class SubscriptionService {
    
    @Autowired
    private UserSubscriptionRepository subscriptionRepository;
    
    public UserSubscription getActiveSubscription(Long userId) {
        UserSubscription subscription = subscriptionRepository
            .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .orElse(null);
        
        if (subscription != null && subscription.isExpired()) {
            // Actualizar automáticamente si expiró
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
        }
        
        return subscription;
    }
}
