package com.g3.parking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.Subscription;
import com.g3.parking.repository.SubscriptionRepository;

@Service
public class UserSubscriptionService extends BaseService{
    @Autowired
    private SubscriptionRepository userSubscriptionRepository;

    public Subscription findByUserIdAndStatus(Long userId, SubscriptionStatus status) {
        return userSubscriptionRepository.findByUserIdAndStatus(userId, status)
            .orElseThrow(() -> new IllegalStateException("Subscripcion no encontrada"));
    }
}
