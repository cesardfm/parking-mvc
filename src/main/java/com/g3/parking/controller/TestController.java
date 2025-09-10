package com.g3.parking.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @GetMapping("/publico/hola")
    public String publico() {
        return "Hola mundo (sin login)";
    }

    @GetMapping("/owner/solo-owner")
    @PreAuthorize("hasRole('OWNER')")
    public String ownerOnly() {
        return "Solo OWNER puede ver esto";
    }

    @GetMapping("/admin/solo-admin")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public String adminOwner() {
        return "ADMIN y OWNER pueden ver esto";
    }

    @GetMapping("/usuario/perfil")
    public String perfil() {
        return "Cualquier usuario autenticado puede ver su perfil";
    }
}
