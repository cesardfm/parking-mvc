package com.g3.parking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.controller.web.TicketController;
import com.g3.parking.model.Site;
import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.Ticket;
import com.g3.parking.model.User;
import com.g3.parking.model.Vehicle;
import com.g3.parking.repository.SiteRepository;
import com.g3.parking.repository.TicketRepository;

@Service
public class TicketService extends BaseService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SiteRepository siteRepository;

    public List<Ticket> findBySite_Id(Long siteId) {
        return ticketRepository.findBySite_Id(siteId);
    }

    public List<Ticket> findBySite_Level_Parking_Id(Long parkingId) {
        return ticketRepository.findBySite_Level_Parking_Id(parkingId);
    }

    public boolean existAnySiteAvailableByParkingId(Long parkingId) {
        List<Site> sitesAvailable = siteRepository.findByStatusAndLevel_Parking_Id("available", parkingId);
        return !sitesAvailable.isEmpty();
    }

    public Ticket findById(Long id) {
        // IMPORTANTE: Usar findByIdWithRelations en lugar de findById
        // para evitar que Hibernate intente reacoplar User con IDs conflictivos
        // cuando se cargen las relaciones lazy.
        Ticket ticket = ticketRepository.findByIdWithRelations(id)
                .orElse(null);
        return ticket;
    }

    public BigDecimal calculateTotalPartial(Long id) {
        Ticket ticket = findById(id);
        if (ticket.getEntryTime() == null || ticket.getExitTime() == null) {
            return BigDecimal.ZERO;
        }

        // Calcular duración exacta en minutos
        long durationInMinutes = java.time.Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toMinutes();

        BigDecimal hours = BigDecimal.valueOf((durationInMinutes + 59) / 60);

        BigDecimal ratePerHour = ticket.getVehicle().getCategory().getRatePerHour();
        BigDecimal totalPartial = ratePerHour.multiply(hours);

        return totalPartial.setScale(2, RoundingMode.CEILING);
    }

    public BigDecimal calculateTotalAmount(Long id) {
        Ticket ticket = findById(id);
        BigDecimal totalPartial = calculateTotalPartial(id);
        BigDecimal totalAmount = totalPartial;
        // Aplicar descuento si existe
        if (ticket.getVehicle().getOwner() != null) {
            BigDecimal discountAmount = calculateDiscount(ticket.getVehicle().getOwner().getId(), totalAmount);
            totalAmount = totalAmount.subtract(discountAmount);
        }

        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDiscount(Long ownerId, BigDecimal totalPartial) {

        BigDecimal discount_percent = new BigDecimal(0);
        BigDecimal discount = new BigDecimal(0);
        if (ownerId != null) {
            discount_percent = userSubscriptionService.findByUserIdAndStatus(
                    ownerId,
                    SubscriptionStatus.ACTIVE).getPlan().getDiscountPercent();
            if (discount_percent != null) {
                discount = totalPartial.multiply(discount_percent);
            }
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public Ticket setTotalAmount(Long id) {
        Ticket ticket = findById(id);
        BigDecimal totalAmount = calculateTotalAmount(id);
        ticket.setTotalAmount(totalAmount);
        return ticketRepository.save(ticket);
    }

    public Ticket create(
            Long parkingId,
            Long siteId,
            String licensePlate,
            String color,
            Long categoryId,
            String entryTime,
            User currentUser) {

        // Buscar o crear vehículo
        Vehicle vehicle = vehicleService.findByLicensePlate(licensePlate);
        if (vehicle == null) {
            vehicle = vehicleService.createVehicle(licensePlate, color, categoryId, null);
        } else if (!vehicle.getCategory().getId().equals(categoryId) || !vehicle.getColor().equalsIgnoreCase(color)) {
            return null;
        }

        Site site = siteRepository.getReferenceById(siteId);
        site.setStatus("disabled");
        // Crear ticket
        Ticket ticket = Ticket.builder()
                .site(site)
                .vehicle(vehicle)
                .entryTime(LocalDateTime.parse(entryTime))
                .paid(false)
                .build();

        siteRepository.save(site);
        ticketRepository.save(ticket);
        return ticketRepository.save(ticket);
    }

    public Ticket pay(Long id) {
        Ticket ticket = ticketRepository.getReferenceById(id);
        if (ticket.getExitTime() == null) {
            ticket.setPaid(true);
            ticket.setExitTime(LocalDateTime.now());
            ticketRepository.save(ticket);
            return ticket;
        } else {
            return null;
        }
    }

    /**
     * Calcula el descuento de forma segura sin cargar la entidad User del
     * propietario
     * en la sesión actual, evitando conflictos de identidad de Hibernate.
     * 
     * IMPORTANTE: Este método obtiene el ID del propietario a través de una
     * consulta
     * separada para evitar lazy loading que podría causar:
     * "Identifier of an instance of 'User' was altered from X to Y"
     */
    public BigDecimal calculateDiscountSafe(Long ticketId, BigDecimal totalPartial) {
        BigDecimal discount_percent = new BigDecimal(0);
        BigDecimal discount = new BigDecimal(0);

        try {
            // Obtener el ticket con sus relaciones (sin User del owner)
            Ticket ticket = findById(ticketId);
            if (ticket == null || ticket.getVehicle() == null) {
                return BigDecimal.ZERO;
            }

            Vehicle vehicle = ticket.getVehicle();
            // NO acceder a vehicle.getOwner() directamente para evitar lazy loading
            // En su lugar, consultar el ID del propietario a través de la BD
            Long ownerId = vehicleService.getVehicleOwnerId(vehicle.getId());

            if (ownerId != null) {
                discount_percent = userSubscriptionService.findByUserIdAndStatus(
                        ownerId,
                        SubscriptionStatus.ACTIVE).getPlan().getDiscountPercent();
                if (discount_percent != null) {
                    discount = totalPartial.multiply(discount_percent);
                }
            }
        } catch (Exception e) {
            return null;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Obtiene el nombre del propietario del vehículo de forma segura.
     * Evita que Hibernate intente cargar/reacoplar la entidad User en la sesión
     * actual.
     */
    public String getVehicleOwnerNameSafe(Long vehicleId) {
        String ownerName = "No especificad@";
        try {
            String ownerUsername = vehicleService.getVehicleOwnerUsername(vehicleId);
            if (ownerUsername != null) {
                ownerName = ownerUsername;
            }
        } catch (Exception e) {

        }
        return ownerName;
    }
}
