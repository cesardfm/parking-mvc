package com.g3.parking.controller.web;

import com.g3.parking.model.Parking;
import com.g3.parking.model.User;
import com.g3.parking.model.Level;
import com.g3.parking.repository.ParkingRepository;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.UserService;
import com.g3.parking.repository.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/parking")
public class ParkingController extends BaseController{
    
    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private ParkingService parkingService;
    
    @Autowired
    private LevelRepository levelRepository;
    
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
                                @ModelAttribute("currentUser") User currentUser,
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
        Parking parking = new Parking(name, currentUser.getOrganization(), currentUser);
        parking.setLat(lat);
        parking.setLng(lng);
        parking.setAddress(address);
        
        // Guardar
        parkingRepository.save(parking);
        
        model.addAttribute("mensaje", "Parqueadero creado exitosamente");
        model.addAttribute("parking", parking);
        return "parking/form";
    }
    
    // Listar todos los parkings
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/listar")
    public String listarParkings(Model model, @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("parkings", parkingService.findByUserOrganization(currentUser));
        return "parking/list";
    }

    // Ver detalles de un parking específico
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/{id}")
    public String verDetalleParking(@PathVariable Long id, Model model, 
            @ModelAttribute("currentUser") User currentUser) {
        try {
             Parking parking = parkingService.findByIdAndValidateOrganization(id,currentUser);
            
             List<Level> levels = levelRepository.findByParkingId(id);
             
             // Obtener admins con rol ADMIN de la organización (para el select)
             List<User> availableAdmins = userService.findByOrganization(currentUser.getOrganization().getId())
                 .stream()
                 .filter(u -> u.hasRole("ROLE_ADMIN"))
                 .toList();
             
             model.addAttribute("parking", parking);
             model.addAttribute("levels", levels);
             model.addAttribute("newLevel", new Level());
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
            @ModelAttribute("currentUser") User currentUser,
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
            @ModelAttribute("currentUser") User currentUser,
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
