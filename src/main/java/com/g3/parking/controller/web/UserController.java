package com.g3.parking.controller.web;

import com.g3.parking.model.User;
import com.g3.parking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController extends BaseController{
    
    // Mostrar formulario para crear usuario
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        return "user/form";
    }
    
    // Crear nuevo usuario (ADMIN o USER)
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/crear")
    public String crearUsuario(
            @RequestParam("newusername") String username,
            @RequestParam("newpassword") String password,
            @RequestParam("role") String roleName, 
            @ModelAttribute("currentUser") User currentUser,
            Model model) {
        
        try {
            // Validaciones
            System.out.println("name: " + username + "Pass: " + password + " Role: " + roleName);
            if (username == null || username.trim().isEmpty()) {
                model.addAttribute("error", "El nombre de usuario es obligatorio");
                return "user/form";
            }
            
            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "La contraseña es obligatoria");
                return "user/form";
            }
            
            // Solo permitir crear ADMIN o USER
            if (!roleName.equals("ADMIN") && !roleName.equals("USER")) {
                model.addAttribute("error", "Rol no válido");
                return "user/form";
            }
            
            // Crear usuario en la misma organización del OWNER
            userService.createUser(
                username, 
                password, 
                roleName, 
                currentUser.getOrganization()
            );
            
            model.addAttribute("mensaje", "Usuario creado exitosamente");
            // model.addAttribute("createdUser", newUser);
            return "user/form";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "user/form";
        }
    }
    
    // Listar usuarios de la organización
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/list")
    public String listarUsuarios(Model model, @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("users", userService.findByOrganization(currentUser.getOrganization().getId()));
        return "user/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
            Model model, @ModelAttribute("currentUser") User currentUser) {

        model.addAttribute("user", userService.findById(id));
        return "user/detail";
    }
}
