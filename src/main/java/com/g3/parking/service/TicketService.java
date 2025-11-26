package com.g3.parking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.SiteDTO;
import com.g3.parking.datatransfer.TicketDTO;
import com.g3.parking.datatransfer.VehicleDTO;
import com.g3.parking.model.Site;
import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.model.Ticket;
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
    private SiteService siteService;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

    public List<TicketDTO> findBySite_Id(Long siteId) {
        List<Ticket> tickets = ticketRepository.findBySite_Id(siteId);
        return tickets.stream()
                .map(ticket -> convert(ticket, TicketDTO.class))
                .collect(Collectors.toList());
    }

    public List<TicketDTO> findBySite_Level_Parking_Id(Long parkingId) {
        List<Ticket> tickets = ticketRepository.findBySite_Level_Parking_Id(parkingId);
        return tickets.stream()
                .map(ticket -> convert(ticket, TicketDTO.class))
                .collect(Collectors.toList());
    }

    public List<TicketDTO> findBySite_Level_Parking_IdAndVehicle_Owner_Id(Long parkingId, Long ownerId) {
        List<Ticket> tickets = ticketRepository.findBySite_Level_Parking_IdAndVehicle_Owner_Id(parkingId, ownerId);
        return tickets.stream()
                .map(ticket -> convert(ticket, TicketDTO.class))
                .collect(Collectors.toList());
    }

    public boolean existAnySiteAvailableByParkingId(Long parkingId) {
        List<SiteDTO> sitesAvailable = siteService.findByStatusAndLevel_Parking_Id("available", parkingId);
        return !sitesAvailable.isEmpty();
    }

    public TicketDTO findById(Long id) {
        // IMPORTANTE: Se usa findByIdWithRelations en lugar de findById
        // para evitar que Hibernate intente reacoplar User con IDs conflictivos
        // cuando se cargen las relaciones lazy.
        Ticket ticket = ticketRepository.findByIdWithRelations(id)
                .orElse(null);
        return convert(ticket, TicketDTO.class);
    }

    public BigDecimal calculateTotalPartial(Long id) {
        TicketDTO ticket = findById(id);
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
        TicketDTO ticket = findById(id);
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

    public TicketDTO setTotalAmount(Long id) {
        TicketDTO ticket = findById(id);
        BigDecimal totalAmount = calculateTotalAmount(id);
        ticket.setTotalAmount(totalAmount);
        ticketRepository.save(convert(ticket, Ticket.class));
        return ticket;
    }

    public Long create(TicketDTO ticket) {

        // Buscar o crear vehículo
        if (ticket == null) {
            return 0L;
        }

        VehicleDTO vehicle = vehicleService.findByLicensePlate(ticket.getVehicle().getLicensePlate());

        if (vehicle == null) {
            Long vehicleId = vehicleService.create(ticket.getVehicle());
            ticket.getVehicle().setId(vehicleId);
            if (vehicleId == 0L) {
                return 0L;
            }
        } else if (!vehicle.getCategory().getId().equals(ticket.getVehicle().getCategory().getId())
                || !vehicle.getColor().equalsIgnoreCase(ticket.getVehicle().getColor())) {
            return 0L;
        }

        siteService.changeStatus(ticket.getSite().getId(), "occupied");
        
        return ticketRepository.save(convert(ticket, Ticket.class)).getId();
    }

    public TicketDTO pay(Long id) {
        Ticket ticket = ticketRepository.getReferenceById(id);
        if (ticket.getExitTime() == null) {
            ticket.setPaid(true);
            ticket.setExitTime(LocalDateTime.now());
            ticketRepository.save(ticket);
            return convert(ticket, TicketDTO.class);
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
            TicketDTO ticket = findById(ticketId);
            if (ticket == null || ticket.getVehicle() == null) {
                return BigDecimal.ZERO;
            }

            VehicleDTO vehicle = ticket.getVehicle();
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
