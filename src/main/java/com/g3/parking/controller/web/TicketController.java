package com.g3.parking.controller.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.internal.util.logging.Log_.logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.parking.datatransfer.LevelDTO;
import com.g3.parking.datatransfer.ParkingDTO;
import com.g3.parking.datatransfer.RoleDTO;
import com.g3.parking.datatransfer.TicketDTO;
import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.datatransfer.VehicleDTO;
import com.g3.parking.service.LevelService;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.RoleService;
import com.g3.parking.service.SiteService;
import com.g3.parking.service.TicketService;
import com.g3.parking.service.VehicleCategoryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/tickets")
public class TicketController extends BaseController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SiteService siteService;

    // Listar todos los parkings
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','USER')")
    @GetMapping("/listarParkings")
    public String listarParkings(Model model, @ModelAttribute("currentUser") UserDTO currentUser) {
        model.addAttribute("parkings", parkingService.findByUserOrganization(currentUser));
        return "ticket/listParkings";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/{parkingId}")
    public String listarTickets(@PathVariable Long parkingId,
            Model model,
            @ModelAttribute("currentUser") UserDTO currentUser) {

        ParkingDTO parking = parkingService.findById(parkingId);
        if (parking == null) {
            model.addAttribute("error", "Parking no encontrado");
            return "redirect:/tickets/listarParkings";
        }

        List<TicketDTO> tickets = new ArrayList<>();

        System.out.println(currentUser.getRoles());
        boolean isAdminOrOwner = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ADMIN".equals(role.getName()) || "ROLE_OWNER".equals(role.getName()) || "OWNER".equals(role.getName()));

        
        if (isAdminOrOwner) {
            tickets = ticketService.findBySite_Level_Parking_Id(parkingId);
        } else {
            tickets = ticketService.findBySite_Level_Parking_IdAndVehicle_Owner_Id(parkingId, currentUser.getId());
        }

        // Forzar carga de relaciones lazy
        for (TicketDTO ticket : tickets) {
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
            @ModelAttribute("currentUser") UserDTO currentUser) {

        try {
            TicketDTO ticket = ticketService.findById(id);

            if (ticket == null) {
                model.addAttribute("error", "Ticket no encontrado");
                return "redirect:/tickets/listarParkings";
            }

            // Site
            if (ticket.getSite() != null && ticket.getSite().getLevel() != null) {
                ParkingDTO parking = ticket.getSite().getLevel().getParking();
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
            BigDecimal discount = BigDecimal.ZERO;
            if (ticket.getVehicle() != null) {
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
            @ModelAttribute("currentUser") UserDTO currentUser) {

        List<LevelDTO> levels = levelService.findByParkingId(parkingId);

        // Forzar carga de sitios para cada nivel
        for (LevelDTO level : levels) {
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
            @ModelAttribute("currentUser") UserDTO currentUser,
            Model model) {
        try {
            VehicleDTO vehicle = VehicleDTO.builder()
                    .licensePlate(licensePlate)
                    .color(color)
                    .category(vehicleCategoryService.findById(categoryId))
                    .build();

            TicketDTO ticket = TicketDTO.builder()
                    .entryTime(LocalDateTime.parse(entryTime))
                    .paid(false)
                    .site(siteService.findById(siteId))
                    .vehicle(vehicle)
                    .build();

            Long res = ticketService.create(ticket);
            ticket.setId(res);
            if (res == 0) {
                model.addAttribute("error",
                        "El vehiculo con la placa ingresada no coincide con las demás caracteristicas registradas en el sistema");

                List<LevelDTO> levels = levelService.findByParkingId(parkingId);

                // Forzar carga de sitios para cada nivel
                for (LevelDTO level : levels) {
                    // Esto inicializa la colección lazy de sitios
                    level.getSites().size();
                }
                model.addAttribute("parking", parkingService.findById(parkingId));
                model.addAttribute("categories", vehicleCategoryService.getAll());

                model.addAttribute("levels", levels);
                return "ticket/form";
            }
            return "redirect:/tickets/detail/" + ticket.getId();

        } catch (IllegalArgumentException e) {
            // Error de validación de organización
            model.addAttribute("error", "No autorizado para crear tickets en este parqueadero");
            model.addAttribute("parking", parkingService.findById(parkingId));
            model.addAttribute("categories", vehicleCategoryService.getAll());
            List<LevelDTO> levels = levelService.findByParkingId(parkingId);
            for (LevelDTO level : levels) {
                // Esto inicializa la colección lazy de sitios
                level.getSites().size();
            }
            model.addAttribute("levels", levels);
            return "ticket/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el ticket: " + e.getMessage());
            model.addAttribute("parking", parkingService.findById(parkingId));
            model.addAttribute("categories", vehicleCategoryService.getAll());
            List<LevelDTO> levels = levelService.findByParkingId(parkingId);
            for (LevelDTO level : levels) {
                // Esto inicializa la colección lazy de sitios
                level.getSites().size();
            }
            model.addAttribute("levels", levels);
            return "ticket/form";
        }
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/pagar/{id}")
    public String postMethodName(@PathVariable Long id, Model model,
            @ModelAttribute("currentUser") UserDTO currentUser) {
        try {
            ticketService.pay(id);
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
