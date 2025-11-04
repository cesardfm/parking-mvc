package com.g3.parking.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.Ticket;
import com.g3.parking.model.User;
import com.g3.parking.repository.TicketRepository;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    public List<Ticket> findByParking_Id(Long parkingId) {
        return ticketRepository.findByParking_Id(parkingId);
    }

    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
        .orElseThrow(() -> new IllegalStateException("Tiquete no encontrado"));
    }

    public BigDecimal calculateTotalPartial(Ticket ticket) {
        if (ticket.getEntryTime() == null || ticket.getExitTime() == null) {
            return BigDecimal.ZERO;
        }

        long durationInHours = java.time.Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toHours();
        BigDecimal ratePerHour = ticket.getVehicle().getCategory().getRate_per_hour();
        BigDecimal totalPartial = ratePerHour.multiply(BigDecimal.valueOf(durationInHours));
        
        return totalPartial;
    }

    public BigDecimal calculateTotalAmount(Ticket ticket) {
        BigDecimal totalPartial = calculateTotalPartial(ticket);
        BigDecimal totalAmount = totalPartial;
        // Aplicar descuento si existe
        if (ticket.getVehicle().getOwner() != null) {
            BigDecimal discountAmount = calculateDiscount(ticket.getVehicle().getOwner(), totalAmount);
            totalAmount = totalAmount.subtract(discountAmount);
        }

        return totalAmount;
    }

    public BigDecimal calculateDiscount(User owner, BigDecimal totalPartial) {
        BigDecimal discount_percent = new BigDecimal(0);
        BigDecimal discount = new BigDecimal(0);
        if (owner != null) {
            discount_percent = userSubscriptionService.findByUserIdAndStatus(
                    owner.getId(), 
                    SubscriptionStatus.ACTIVE
                ).getPlan().getDiscountPercent();
            if (discount_percent != null) {
                discount = totalPartial.multiply(discount_percent);
            }
        }
        return discount;
    }
}
