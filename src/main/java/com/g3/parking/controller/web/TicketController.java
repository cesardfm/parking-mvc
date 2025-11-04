package com.g3.parking.controller.web;


import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.g3.parking.model.Ticket;
import com.g3.parking.model.User;
import com.g3.parking.repository.ParkingRepository;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.TicketService;
import com.g3.parking.service.UserService;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@RequestMapping("/tickets")
public class TicketController {
    @Autowired
    TicketService ticketService;

    @Autowired
    ParkingRepository parkingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ParkingService parkingService;

    

    @ModelAttribute("currentUser")
    public User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return null;
        return userService.findByUsername(userDetails.getUsername());
    }

    // Listar todos los parkings
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/listarParkings")
    public String listarParkings(Model model, @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("parkings", parkingService.findByUserOrganization(currentUser));
        return "ticket/listParkings";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/{id}")
    public String listarTickets(@PathVariable Long id,
                                Model model,
                                @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("tickets", ticketService.findByParking_Id(id));
        return "ticket/list";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/detail/{id}")
    public String verDetalleTicket(@PathVariable Long id, Model model,
            @ModelAttribute("currentUser") User currentUser) {
        try {
            Ticket ticket = ticketService.findById(id);

            if (ticket == null) {
                model.addAttribute("error", "Ticket no encontrado");
                return "redirect:/tickets/listarParkings";
            }

            // Validar que el usuario actual pertenezca a la organización dueña del parking
            if (currentUser != null && ticket.getParking() != null && ticket.getParking().getOrganization() != null) {
                Long orgId = ticket.getParking().getOrganization().getId();
                if (!currentUser.belongsToOrganization(orgId)) {
                    model.addAttribute("error", "No autorizado para ver este ticket");
                    return "redirect:/dashboard";
                }
            }

            // Parking
            if (ticket.getParking() != null) {
                model.addAttribute("parking_id", ticket.getParking().getId());
                model.addAttribute("parking_name", ticket.getParking().getName());
                model.addAttribute("parking_address", ticket.getParking().getAddress());
                String orgName = ticket.getParking().getOrganization() != null ? ticket.getParking().getOrganization().getName() : "";
                model.addAttribute("parking_organization", orgName);
            }

            // Ticket base (siempre)
            model.addAttribute("id", ticket.getId());
            model.addAttribute("vehicle_entry_time", ticket.getEntryTime());

            // Calculos relacionados con tiempos y totales
            BigDecimal totalPartial = BigDecimal.ZERO;
            BigDecimal computedTotal = BigDecimal.ZERO;
            if (ticket.getExitTime() != null) {
                model.addAttribute("vehicle_exit_time", ticket.getExitTime());
                totalPartial = ticketService.calculateTotalPartial(ticket).setScale(2, RoundingMode.HALF_UP);
                model.addAttribute("total_partial", totalPartial);
                computedTotal = ticketService.calculateTotalAmount(ticket).setScale(2, RoundingMode.HALF_UP);
            }

            // Descuento (si aplica)
            BigDecimal discount = BigDecimal.ZERO;
            if (ticket.getVehicle() != null && ticket.getVehicle().getOwner() != null) {
                discount = ticketService.calculateDiscount(ticket.getVehicle().getOwner(), totalPartial).setScale(2, RoundingMode.HALF_UP);
            }
            model.addAttribute("discount", discount);

            model.addAttribute("total", computedTotal);
            model.addAttribute("paid", ticket.isPaid());

            // Vehicle (detalles opcionales)
            if (ticket.getVehicle() != null) {
                model.addAttribute("vehicle_id", ticket.getVehicle().getId());
                model.addAttribute("vehicle_licence_plate", ticket.getVehicle().getLicensePlate());
                model.addAttribute("vehicle_color", ticket.getVehicle().getColor());
                String category = ticket.getVehicle().getCategory() != null ? ticket.getVehicle().getCategory().getName() : "";
                model.addAttribute("vehicle_category", category);
                String ownerName = "No especificad@";
                if (ticket.getVehicle().getOwner() != null && ticket.getVehicle().getOwner().getUsername() != null) {
                    ownerName = ticket.getVehicle().getOwner().getUsername();
                }
                model.addAttribute("vehicle_owner", ownerName);
            }

            return "ticket/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Ticket no encontrado");
            return "redirect:/tickets/listarParkings";
        }
    }
    
}
