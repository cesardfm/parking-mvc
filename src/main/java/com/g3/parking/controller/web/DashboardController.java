package com.g3.parking.controller.web;

import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.service.ParkingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class DashboardController extends BaseController{

    @Autowired
    private ParkingService parkingService;

    @GetMapping("/map")
    public String getMap() {
        return "map";   
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Últimos parqueaderos (máximo 5)
        if (userDetails == null)
            return "/login";
        UserDTO user = userService.findByUsername(userDetails.getUsername());
        if (user.hasRole("ROLE_ADMIN")){
            return "admin/dashboard";
        }
        model.addAttribute("recentParkings", parkingService.findByUserOrganization(user));
        
        return "dashboard";
    }
}
