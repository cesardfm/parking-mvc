package com.g3.parking.controller.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.parking.datatransfer.ParkingDTO;
import com.g3.parking.datatransfer.TicketDTO;
import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.TicketService;

@Controller
@RequestMapping("/reports")
public class ReportsController extends BaseController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ParkingService parkingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public String reports(Model model, @RequestParam(required = false) Long parkingId,@ModelAttribute("currentUser") UserDTO currentUser) {
        List<ParkingDTO> parkings;

        // Si es ADMIN, mostrar solo los parkings que administra
        if (currentUser.hasRole("ROLE_ADMIN") && !currentUser.hasRole("ROLE_OWNER")) {
            parkings = parkingService.findByAdminId(currentUser.getId());
            model.addAttribute("titulo", "Mis Parqueaderos Asignados");
        }
        // Si es OWNER o USER, mostrar todos los parkings de la organización
        else {
            parkings = parkingService.findByUserOrganization(currentUser);
            model.addAttribute("titulo", "Parqueaderos de la Organización");
        }

        model.addAttribute("parkings", parkings);

        // Si no hay parkingId seleccionado, usar el primero disponible
        if (parkingId == null && !parkings.isEmpty()) {
            parkingId = parkings.get(0).getId();
        }

        model.addAttribute("parkings", parkings);
        model.addAttribute("selectedParkingId", parkingId);

        if (parkingId != null) {
            // Obtener todos los tickets del parqueadero
            List<TicketDTO> allTickets = ticketService.findBySite_Level_Parking_Id(parkingId);
            ParkingDTO selectedParking = parkingService.findById(parkingId);

            // === REPORTE 1: Vehículos por tipo (hoy) ===
            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            LocalDateTime endOfToday = LocalDate.now().plusDays(1).atStartOfDay();

            Map<String, Long> vehiclesByType = allTickets.stream()
                .filter(t -> t.getEntryTime().isAfter(startOfToday) && t.getEntryTime().isBefore(endOfToday))
                .collect(Collectors.groupingBy(
                    t -> t.getVehicle().getCategory().getName(),
                    Collectors.counting()
                ));

            // === REPORTE 2: Dinero recaudado ayer ===
            LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
            LocalDateTime endOfYesterday = LocalDate.now().atStartOfDay();

            BigDecimal yesterdayRevenue = allTickets.stream()
                .filter(t -> t.isPaid() && t.getExitTime() != null)
                .filter(t -> t.getExitTime().isAfter(startOfYesterday) && t.getExitTime().isBefore(endOfYesterday))
                .map(TicketDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // === REPORTE 3: Dinero recaudado hoy ===
            BigDecimal todayRevenue = allTickets.stream()
                .filter(t -> t.isPaid() && t.getExitTime() != null)
                .filter(t -> t.getExitTime().isAfter(startOfToday) && t.getExitTime().isBefore(endOfToday))
                .map(TicketDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // === REPORTE 4: Total tickets pagados vs no pagados ===
            long paidTickets = allTickets.stream().filter(TicketDTO::isPaid).count();
            long unpaidTickets = allTickets.stream().filter(t -> !t.isPaid()).count();

            // === REPORTE 5: Vehículos actualmente en el parqueadero ===
            long activeVehicles = allTickets.stream()
                .filter(t -> !t.isPaid() && t.getExitTime() == null)
                .count();

            // === REPORTE 6: Total tickets del mes actual ===
            LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            long monthlyTickets = allTickets.stream()
                .filter(t -> t.getEntryTime().isAfter(startOfMonth))
                .count();

            // === REPORTE 7: Ingresos del mes ===
            BigDecimal monthlyRevenue = allTickets.stream()
                .filter(t -> t.isPaid() && t.getExitTime() != null)
                .filter(t -> t.getExitTime().isAfter(startOfMonth))
                .map(TicketDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // === REPORTE 8: Promedio de tiempo de estadía (últimos 30 días) ===
            LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
            List<TicketDTO> completedTickets = allTickets.stream()
                .filter(t -> t.isPaid() && t.getExitTime() != null && t.getEntryTime().isAfter(last30Days))
                .collect(Collectors.toList());

            double averageHours = 0;
            if (!completedTickets.isEmpty()) {
                long totalMinutes = completedTickets.stream()
                    .mapToLong(t -> java.time.Duration.between(t.getEntryTime(), t.getExitTime()).toMinutes())
                    .sum();
                averageHours = (totalMinutes / 60.0) / completedTickets.size();
            }

            // Agregar datos al modelo
            model.addAttribute("selectedParking", selectedParking);
            model.addAttribute("vehiclesByType", vehiclesByType);
            model.addAttribute("yesterdayRevenue", yesterdayRevenue);
            model.addAttribute("todayRevenue", todayRevenue);
            model.addAttribute("paidTickets", paidTickets);
            model.addAttribute("unpaidTickets", unpaidTickets);
            model.addAttribute("activeVehicles", activeVehicles);
            model.addAttribute("monthlyTickets", monthlyTickets);
            model.addAttribute("monthlyRevenue", monthlyRevenue);
            model.addAttribute("averageHours", String.format("%.2f", averageHours));
            model.addAttribute("totalTickets", allTickets.size());
        }

        return "reports";
    }
}
