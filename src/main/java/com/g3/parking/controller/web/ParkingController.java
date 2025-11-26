package com.g3.parking.controller.web;

import com.g3.parking.datatransfer.LevelDTO;
import com.g3.parking.datatransfer.ParkingDTO;
import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.service.LevelService;
import com.g3.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/parking")
public class ParkingController extends BaseController {

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private LevelService levelService;

    // Mostrar formulario para crear parking
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        return "parking/form";
    }

    // Crear nuevo parking
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/crear")
    public String crearParking(@RequestParam("name") String name,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @RequestParam(value = "address", required = false) String address,
            @ModelAttribute("currentUser") UserDTO currentUser,
            Model model) {

        // Validar nombre
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "El nombre es obligatorio");
            return "parking/form";
        }

        // Validar que el usuario tenga una organización
        if (currentUser.getOrganization() == null) {
            model.addAttribute("error", "Tu usuario no está asociado a ninguna organización");
            return "parking/form";
        }

        // Crear parking con organización y usuario creador
        ParkingDTO parking = ParkingDTO.builder()
                .name(name)
                .organization(currentUser.getOrganization())
                .createdBy(currentUser)
                .lat(lat)
                .lng(lng)
                .address(address)
                .build();

        // Guardar
        parkingService.createForUser(parking,currentUser);

        model.addAttribute("mensaje", "Parqueadero creado exitosamente");
        model.addAttribute("parking", parking);
        return "parking/form";
    }

    // Listar todos los parkings
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','USER')")
    @GetMapping("/listar")
    public String listarParkings(Model model, @ModelAttribute("currentUser") UserDTO currentUser) {
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
        return "parking/list";
    }

    // Listar parkings que administra el usuario (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/adminlist")
    public String listarParkingsAdmin(Model model, @ModelAttribute("currentUser") UserDTO currentUser) {
        if (currentUser == null) {
            model.addAttribute("error", "Usuario no autenticado");
            return "redirect:/login";
        }
        model.addAttribute("parkings", parkingService.findByAdmin(currentUser));
        return "parking/list";
    }

    // Ver detalles de un parking específico
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','USER')")
    @GetMapping("/{id}")
    public String verDetalleParking(@PathVariable Long id, Model model,
            @ModelAttribute("currentUser") UserDTO currentUser) {
        try {
            ParkingDTO parking = parkingService.findByIdAndValidateOrganization(id, currentUser);

            List<LevelDTO> levels = levelService.findByParkingId(id);

            // Obtener admins con rol ADMIN de la organización (para el select)
            List<UserDTO> availableAdmins = userService.findByOrganization(currentUser.getOrganization().getId())
                    .stream()
                    .filter(u -> u.hasRole("ROLE_ADMIN"))
                    .toList();

            model.addAttribute("parking", parking);
            model.addAttribute("levels", levels);
            model.addAttribute("newLevel", new LevelDTO());
            model.addAttribute("availableAdmins", availableAdmins);

            return "parking/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Parqueadero no encontrado");
            return "redirect:/parking/listar";
        }
    }

    // Asignar admin a parking
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/asignar-admin")
    public String asignarAdmin(
            @PathVariable Long id,
            @RequestParam("adminId") Long adminId,
            @ModelAttribute("currentUser") UserDTO currentUser,
            Model model) {
        try {
            parkingService.assignAdmin(id, adminId, currentUser);
            return "redirect:/parking/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/parking/" + id;
        }
    }

    // Remover admin de parking
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/remover-admin/{adminId}")
    public String removerAdmin(
            @PathVariable Long id,
            @PathVariable Long adminId,
            @ModelAttribute("currentUser") UserDTO currentUser,
            Model model) {
        try {
            parkingService.removeAdmin(id, adminId, currentUser);
            return "redirect:/parking/" + id;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/parking/" + id;
        }
    }

    // API: Obtener ubicaciones de parqueaderos de la organización del usuario
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/api/locations")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getParkingLocations(
            @ModelAttribute("currentUser") UserDTO currentUser) {
        
        List<ParkingDTO> parkings = parkingService.findByUserOrganization(currentUser);
        
        List<Map<String, Object>> locations = parkings.stream()
            .filter(p -> p.getLat() != null && p.getLng() != null)
            .map(p -> {
                Map<String, Object> location = new HashMap<>();
                location.put("id", p.getId());
                location.put("name", p.getName());
                location.put("lat", p.getLat());
                location.put("lng", p.getLng());
                location.put("address", p.getAddress() != null ? p.getAddress() : "");
                return location;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(locations);
    }

}
