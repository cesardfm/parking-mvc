package com.g3.parking.controller.web;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.parking.model.Level;
import com.g3.parking.model.Parking;
import com.g3.parking.model.Ticket;
import com.g3.parking.model.User;
import com.g3.parking.repository.LevelRepository;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.TicketService;
import com.g3.parking.service.VehicleCategoryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/tickets")
public class TicketController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

    @Autowired
    private LevelRepository levelRepository;

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

        Parking parking = parkingService.findById(parkingId);
        if (parking == null) {
            model.addAttribute("error", "Parking no encontrado");
            return "redirect:/tickets/listarParkings";
        }

        List<Ticket> tickets = ticketService.findBySite_Level_Parking_Id(parkingId);

        // Forzar carga de relaciones lazy
        for (Ticket ticket : tickets) {
            if (ticket.getVehicle() != null) {
                // Acceder a las relaciones para forzar carga
                ticket.getVehicle().getLicensePlate();
                if (ticket.getVehicle().getCategory() != null) {
                    ticket.getVehicle().getCategory().getName();
                }
            }
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("parkingId", parking.getId());
        model.addAttribute("parkingName", parking.getName());
        model.addAttribute("siteAvailable", ticketService.existAnySiteAvailableByParkingId(parkingId));
        return "ticket/list";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/detail/{id}")
    public String verDetalleTicket(@PathVariable Long id,
            Model model,
            @ModelAttribute("currentUser") User currentUser) {

        try {
            log.info("/detail/{id} - ID: {}", id);
            log.info("Current User ID al inicio: {}", currentUser.getId());
            Ticket ticket = ticketService.findById(id);

            if (ticket == null) {
                model.addAttribute("error", "Ticket no encontrado");
                return "redirect:/tickets/listarParkings";
            }

            // Site
            if (ticket.getSite() != null && ticket.getSite().getLevel() != null) {
                Parking parking = ticket.getSite().getLevel().getParking();
                if (parking != null) {
                    model.addAttribute("parking_id", parking.getId());
                    model.addAttribute("parking_name", parking.getName());
                    model.addAttribute("parking_address", parking.getAddress());
                    String orgName = parking.getOrganization() != null
                            ? parking.getOrganization().getName()
                            : "";
                    model.addAttribute("parking_organization", orgName);
                }
            }

            // Ticket base (siempre)
            model.addAttribute("ticket_id", ticket.getId());
            model.addAttribute("vehicle_entry_time", ticket.getEntryTime());
            model.addAttribute("paid", ticket.isPaid());
            model.addAttribute("siteId", ticket.getSite().getId());
            model.addAttribute("levelId", ticket.getSite().getLevel().getId());

            // Calculos relacionados con tiempos y totales
            BigDecimal totalPartial = BigDecimal.ZERO;
            BigDecimal computedTotal = BigDecimal.ZERO;
            if (ticket.getExitTime() != null) {
                model.addAttribute("vehicle_exit_time", ticket.getExitTime());
                totalPartial = ticketService.calculateTotalPartial(id);
                model.addAttribute("total_partial", totalPartial);
                computedTotal = ticketService.calculateTotalAmount(id);
                ticketService.setTotalAmount(id);
            }

            // Descuento (si aplica)
            // IMPORTANTE: Solo acceder al owner si es estrictamente necesario
            // y usar un contexto seguro para evitar conflictos de identidad
            BigDecimal discount = BigDecimal.ZERO;
            if (ticket.getVehicle() != null) {
                // No acceder directamente a ticket.getVehicle().getOwner()
                // para evitar lazy loading que podría causar conflictos con el User actual
                discount = ticketService.calculateDiscountSafe(ticket.getId(), totalPartial);
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

                // IMPORTANTE: Obtener el nombre del propietario de forma segura
                // sin acceder directamente a la entidad User que podría estar en sesión
                String ownerName = ticketService.getVehicleOwnerNameSafe(ticket.getVehicle().getId());
                model.addAttribute("vehicle_owner", ownerName);
            }
            log.info("Current User ID al final: {}", currentUser.getId());
            return "ticket/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Ticket no encontrado");
            return "redirect:/tickets/listarParkings";
        }
    }

    // Mostrar formulario para crear ticket
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/nuevo/{parkingId}")
    public String mostrarFormularioNuevo(@PathVariable("parkingId") Long parkingId, Model model,
            @ModelAttribute("currentUser") User currentUser) {

        List<Level> levels = levelRepository.findByParkingId(parkingId);

        // Forzar carga de sitios para cada nivel
        for (Level level : levels) {
            // Esto inicializa la colección lazy de sitios
            level.getSites().size();
        }

        model.addAttribute("parking", parkingService.findById(parkingId));
        model.addAttribute("categories", vehicleCategoryService.getAllActive());
        model.addAttribute("levels", levels);

        return "ticket/form";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/crear")
    public String crearTicket(
            @RequestParam("parkingId") Long parkingId,
            @RequestParam("siteId") Long siteId,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("color") String color,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("entryTime") String entryTime,
            @ModelAttribute("currentUser") User currentUser,
            Model model) {
        try {
            Ticket ticket = ticketService.create(
                    parkingId,
                    siteId,
                    licensePlate,
                    color,
                    categoryId,
                    entryTime,
                    currentUser);
            if (ticket == null) {
                model.addAttribute("error",
                        "El vehiculo con la placa ingresada no coincide con las demás caracteristicas registradas en el sistema");
                model.addAttribute("parking", parkingService.findById(parkingId));
                model.addAttribute("categories", vehicleCategoryService.getAll());
                return "ticket/form";
            }
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

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/pagar/{id}")
    public String postMethodName(@PathVariable Long id, Model model, @ModelAttribute("currentUser") User currentUser) {
        try {
            ticketService.setPaid(id);
            return "redirect:/tickets/detail/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al pagar");
            return "redirect:/tickets/listarParkings";
        }
    }

    /*
     * @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
     * 
     * @GetMapping("/editar/{id}")
     * public String mostrarFormularioEditar(
     * 
     * @PathVariable("id") Long id,
     * Model model,
     * 
     * @ModelAttribute("currentUser") User currentUser) {
     * 
     * try {
     * log.info("Current User ID: {}", currentUser.getId());
     * log.info("Current User Class: {}", currentUser.getClass().getName());
     * Ticket ticket = ticketService.findById(id);
     * if (ticket == null) {
     * model.addAttribute("error", "Ticket no encontrado");
     * return "redirect:/tickets/listarParkings";
     * }
     * 
     * // Validar organización
     * parkingService.findByIdAndValidateOrganization(
     * ticket.getParking().getId(),
     * currentUser);
     * 
     * model.addAttribute("ticket", ticket);
     * model.addAttribute("categories", vehicleCategoryService.getAll());
     * return "ticket/form";
     * } catch (IllegalArgumentException e) {
     * model.addAttribute("error", "No autorizado");
     * return "redirect:/tickets/listarParkings";
     * }
     * }
     */

    /*
     * @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
     * 
     * @PostMapping("/actualizar/{id}")
     * public String actualizarTicket(
     * 
     * @PathVariable Long id,
     * 
     * @RequestParam("color") String color,
     * 
     * @RequestParam("licensePlate") String licensePlate,
     * 
     * @RequestParam("categoryId") Long categoryId,
     * 
     * @RequestParam(value = "paid", defaultValue = "false") boolean paid,
     * Model model,
     * 
     * @ModelAttribute("currentUser") User currentUser) {
     * 
     * try {
     * 
     * Ticket ticket = ticketService.findById(id);
     * 
     * if (ticket == null) {
     * model.addAttribute("error", "No se encontró el tiquete");
     * return "redirect:/tickets/listarParkings";
     * }
     * 
     * log.info("ANTES: ");
     * Vehicle vehicle = vehicleService.findByLicensePlate(licensePlate);
     * log.info("DESPUES: ");
     * 
     * boolean needsNewVehicle = false;
     * 
     * if (vehicle == null) {
     * needsNewVehicle = true;
     * } else {
     * // Solo verificar si el vehículo existe
     * if (!color.equalsIgnoreCase(vehicle.getColor()) ||
     * !categoryId.equals(vehicle.getCategory().getId())) {
     * needsNewVehicle = true;
     * }
     * }
     * 
     * if (needsNewVehicle) {
     * // Crear nuevo vehículo
     * vehicle = vehicleService.createVehicle(licensePlate, color, categoryId,
     * null);
     * log.info("Nuevo vehículo creado: {}", vehicle.getLicensePlate());
     * }
     * 
     * ticket.setVehicle(vehicle);
     * ticket.setPaid(paid);
     * 
     * ticketService.save(ticket);
     * 
     * return "redirect:/tickets/detail/" + id;
     * 
     * } catch (IllegalArgumentException e) {
     * model.addAttribute("error", "No autorizado para actualizar este ticket");
     * return "redirect:/tickets/detail/" + id;
     * } catch (Exception e) {
     * model.addAttribute("error", e);
     * log.info("ERROR: ", e);
     * return "redirect:/tickets/editar/" + id;
     * }
     * }
     */

}
