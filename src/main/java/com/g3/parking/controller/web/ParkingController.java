package com.g3.parking.controller.web;

import com.g3.parking.datatransfer.LevelDTO;
import com.g3.parking.datatransfer.ParkingDTO;
import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.service.LevelService;
import com.g3.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        model.addAttribute("parkings", parkingService.findByUserOrganization(currentUser));
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
}
