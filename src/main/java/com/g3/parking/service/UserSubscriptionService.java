package com.g3.parking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.UserSubscription;
import com.g3.parking.repository.UserSubscriptionRepository;

@Service
public class UserSubscriptionService extends BaseService{
    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscription findByUserIdAndStatus(Long userId, SubscriptionStatus status) {
        return userSubscriptionRepository.findByUserIdAndStatus(userId, status)
            .orElseThrow(() -> new IllegalStateException("Subscripcion no encontrada"));
    }
}
