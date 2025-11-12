package com.g3.parking.controller.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.parking.model.Ticket;
import com.g3.parking.model.User;
import com.g3.parking.model.Vehicle;
import com.g3.parking.repository.VehicleCategoryRepository;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.TicketService;
import com.g3.parking.service.UserService;
import com.g3.parking.service.VehicleCategoryService;
import com.g3.parking.service.VehicleService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/tickets")
public class TicketController {
    @Autowired
    TicketService ticketService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

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
    @GetMapping("/{parkingId}")
    public String listarTickets(@PathVariable Long parkingId,
            Model model,
            @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("tickets", ticketService.findByParking_Id(parkingId));
        model.addAttribute("parkingId", parkingService.findById(parkingId).getId());
        model.addAttribute("parkingName", parkingService.findById(parkingId).getName());
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
                String orgName = ticket.getParking().getOrganization() != null
                        ? ticket.getParking().getOrganization().getName()
                        : "";
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
                totalPartial = ticketService.calculateTotalPartial(ticket);
                model.addAttribute("total_partial", totalPartial);
                computedTotal = ticketService.calculateTotalAmount(ticket);
            }

            // Descuento (si aplica)
            BigDecimal discount = BigDecimal.ZERO;
            if (ticket.getVehicle() != null && ticket.getVehicle().getOwner() != null) {
                discount = ticketService.calculateDiscount(ticket.getVehicle().getOwner(), totalPartial);
            }
            model.addAttribute("discount", discount);

            model.addAttribute("total", computedTotal);
            model.addAttribute("paid", ticket.isPaid());

            // Vehicle (detalles opcionales)
            if (ticket.getVehicle() != null) {
                model.addAttribute("vehicle_id", ticket.getVehicle().getId());
                model.addAttribute("vehicle_licence_plate", ticket.getVehicle().getLicensePlate());
                model.addAttribute("vehicle_color", ticket.getVehicle().getColor());
                String category = ticket.getVehicle().getCategory() != null
                        ? ticket.getVehicle().getCategory().getName()
                        : "";
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

    // Mostrar formulario para crear parking
    // Mostrar formulario para crear ticket
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/nuevo/{parkingId}")
    public String mostrarFormularioNuevo(@PathVariable("parkingId") Long parkingId, Model model,
            @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("parking", parkingService.findById(parkingId));
        model.addAttribute("categories", vehicleCategoryService.getAll());
        return "ticket/form";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/crear")
    public String crearTicket(
            @RequestParam("parkingId") Long parkingId,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("color") String color,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("entryTime") String entryTime,
            @ModelAttribute("currentUser") User currentUser,
            Model model) {
        try {
            // Validar parking y pertenencia a organización
            var parking = parkingService.findByIdAndValidateOrganization(parkingId, currentUser);

            // Buscar o crear vehículo
            Vehicle vehicle = vehicleService.getVehicleByPlate(licensePlate);
            if (vehicle == null) {
                vehicle = vehicleService.createVehicle(licensePlate, color, categoryId, null);
            }

            // Crear ticket
            Ticket ticket = Ticket.builder()
                    .parking(parking)
                    .vehicle(vehicle)
                    .entryTime(LocalDateTime.parse(entryTime))
                    .build();

            ticketService.save(ticket);

            return "redirect:/tickets/detail/" + ticket.getId();

        } catch (IllegalArgumentException e) {
            // Error de validación de organización
            model.addAttribute("error", "No autorizado para crear tickets en este parqueadero");
            model.addAttribute("parkings", parkingService.findByUserOrganization(currentUser));
            model.addAttribute("categories", vehicleCategoryService.getAll());
            return "ticket/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el ticket: " + e.getMessage());
            model.addAttribute("parkings", parkingService.findByUserOrganization(currentUser));
            model.addAttribute("categories", vehicleCategoryService.getAll());
            return "ticket/form";
        }
    }

}
