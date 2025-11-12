package com.g3.parking.controller.web;

import com.g3.parking.model.User;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private ParkingService parkingService;
    
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Últimos parqueaderos (máximo 5)
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("recentParkings", parkingService.findByUserOrganization(user));
        
        return "dashboard";
    }
}
