package com.g3.parking.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.User;
import com.g3.parking.datatransfer.SubscriptionDTO;
import com.g3.parking.model.Plan;
import com.g3.parking.model.Subscription;
import com.g3.parking.repository.SubscriptionRepository;

@Service
public class SubscriptionService extends BaseService {

    @Autowired
    private SubscriptionRepository subscriptionRepo;

    public Subscription upgradeStatus(Subscription subscription) {
        if (subscription != null && subscription.isExpired()) {
            // Actualizar automáticamente si expiró
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepo.save(subscription);
        }

        return subscription;
    }

    public Subscription getActiveSubscription(Long userId) {
        Subscription subscription = subscriptionRepo
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElse(null);

        subscription = upgradeStatus(subscription);
        return subscription.getStatus() == SubscriptionStatus.ACTIVE ? subscription : null;
    }

    public List<SubscriptionDTO> findAll() {
        List<Subscription> subscriptions = subscriptionRepo.findAll();
        return subscriptions.stream()
                .map(subscription -> convert(subscription, SubscriptionDTO.class))
                .collect(Collectors.toList());
    }

    public List<SubscriptionDTO> findAllOfAnyUser(Long userId) {
        List<Subscription> subscriptions = subscriptionRepo.findByUser_id(userId);
        return subscriptions.stream()
                .map(
                        subscription -> {
                            subscription = upgradeStatus(subscription);
                            return convert(subscription, SubscriptionDTO.class);
                        })
                .collect(Collectors.toList());
    }

    public SubscriptionDTO findById(Long id) {
        try {
            Subscription subscription = subscriptionRepo.getReferenceById(id);
            return convert(subscription, SubscriptionDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Long create(SubscriptionDTO subscriptionDTO) {
        try {
            Subscription subscription = convert(subscriptionDTO, Subscription.class);
            subscriptionRepo.save(subscription);
            return subscription.getId();
        } catch (Exception e) {
            return 0L;
        }
    }

    public boolean update(SubscriptionDTO subscriptionDTO) {
        try {
            Subscription subscription = subscriptionRepo.getReferenceById(subscriptionDTO.getId());
            subscription.setUser(convert(subscriptionDTO.getUser(), User.class));
            subscription.setPlan(convert(subscriptionDTO.getPlan(), Plan.class));
            subscription.setActivationDate(subscriptionDTO.getActivationDate());
            subscription.setMonthsDuration(subscriptionDTO.getMonthsDuration());
            subscription.setPrice(subscriptionDTO.getPrice());
            subscription.setStatus(subscriptionDTO.getStatus());
            subscriptionRepo.save(subscription);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getAllSubscriptionStatus() {
        return Arrays.stream(SubscriptionStatus.values())
                .map(status -> status.name())
                .collect(Collectors.toList());
    }

    public Subscription cancel(Long id) {
        Subscription subscription = subscriptionRepo.getReferenceById(id);
        if (subscription != null) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepo.save(subscription);
        }
        return subscription;
    }
}
