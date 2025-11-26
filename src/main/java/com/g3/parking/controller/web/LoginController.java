package com.g3.parking.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController extends BaseController{

    @GetMapping("/")
    public String welcome() {
        return "welcome"; // página de bienvenida
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // retorna la vista login.html
    }

    @GetMapping("/home")
    public String home() {
        return "dashboard"; // después del login redirige aquí
    }
}
