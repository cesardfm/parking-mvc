package com.g3.parking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        Ticket ticket = ticketRepository.findById(id)
                    .orElse(null);
        return ticket;
    }

    public BigDecimal calculateTotalPartial(Ticket ticket) {
        if (ticket.getEntryTime() == null || ticket.getExitTime() == null) {
            return BigDecimal.ZERO;
        }

        long durationInHours = java.time.Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toHours();
        BigDecimal ratePerHour = ticket.getVehicle().getCategory().getRatePerHour();
        BigDecimal totalPartial = ratePerHour.multiply(BigDecimal.valueOf(durationInHours));
        
        return totalPartial.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalAmount(Ticket ticket) {
        BigDecimal totalPartial = calculateTotalPartial(ticket);
        BigDecimal totalAmount = totalPartial;
        // Aplicar descuento si existe
        if (ticket.getVehicle().getOwner() != null) {
            BigDecimal discountAmount = calculateDiscount(ticket.getVehicle().getOwner(), totalAmount);
            totalAmount = totalAmount.subtract(discountAmount);
        }

        return totalAmount.setScale(2, RoundingMode.HALF_UP);
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
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
}
