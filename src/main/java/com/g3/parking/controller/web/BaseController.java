package com.g3.parking.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected UserService userService;

    @ModelAttribute("currentUser")
    public UserDTO getCurrentUser(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        if (userDetails == null){
            return null;
        }

        UserDTO user = userService.findByUsername(userDetails.getUsername());

        // IMPORTANTE: Detach el usuario de la sesión Hibernate para evitar conflictos
        // de identidad cuando otras entidades relacionadas sean cargadas lazily.
        // Esto previene: "Identifier of an instance of 'User' was altered from X to Y"
        //entityManager.detach(user);

        // DEBUG COMPLETO
        System.out.println("=== AUTHENTICATION DEBUG ===");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Session ID: " + request.getSession().getId());
        System.out.println("Authenticated Username: " + userDetails.getUsername());
        System.out.println("User ID from DB: " + user.getId());
        System.out.println("============================");

        return user;
    }
}
