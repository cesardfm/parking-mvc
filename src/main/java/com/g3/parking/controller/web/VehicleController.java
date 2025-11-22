package com.g3.parking.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.datatransfer.VehicleDTO;
import com.g3.parking.service.VehicleCategoryService;
import com.g3.parking.service.VehicleService;

@Controller
@RequestMapping("/vehicles")
public class VehicleController extends BaseController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleCategoryService vehicleCategoryService;

    @GetMapping("/listar/{userId}")
    public String listar(@PathVariable Long userId, Model model, @ModelAttribute("currentUser") UserDTO currentUser) {
        model.addAttribute("vehicles", vehicleService.getUserVehicles(userId));
        model.addAttribute("userId", userId);
        return "vehicle/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        model.addAttribute("vehicle", vehicleService.getById(id));
        return "vehicle/detail";
    }

    @GetMapping("/nuevo/{userId}")
    public String nuevo(@PathVariable("userId") Long userId,
            Model model,
            @ModelAttribute("currentUser") UserDTO currentUser) {
        model.addAttribute("categories", vehicleCategoryService.getAllActive());
        model.addAttribute("userId", userId);
        return "vehicle/form";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam("userId") Long userId,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("color") String color,
            @RequestParam("categoryId") Long categoryId,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        VehicleDTO vehicle = VehicleDTO.builder()
                .owner(userService.findById(userId))
                .licensePlate(licensePlate)
                .color(color)
                .category(vehicleCategoryService.findById(categoryId))
                .build();
        Long vehicleId = vehicleService.create(vehicle);

        if (vehicleId == 0) {
            model.addAttribute("error", "Error al inscribir el vehiculo");
            return "vehicle/form";
        }
        return "redirect:/vehicles/detail/" + vehicleId;
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        model.addAttribute("vehicle", vehicleService.getById(id));
        model.addAttribute("categories", vehicleCategoryService.getAllActive());
        return "vehicle/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Long id,
            @RequestParam("userId") Long userId,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("color") String color,
            @RequestParam("categoryId") Long categoryId,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        VehicleDTO vehicle = VehicleDTO.builder()
                .id(id)
                .owner(userService.findById(userId))
                .licensePlate(licensePlate)
                .color(color)
                .category(vehicleCategoryService.findById(categoryId))
                .build();
        boolean res = vehicleService.update(vehicle);

        if (res == false) {
            model.addAttribute("error", "Error al actualizar");
            model.addAttribute("vehicle", vehicleService.getById(id));
            model.addAttribute("categories", vehicleCategoryService.getAllActive());
            return "vehicle/form";
        }

        return "redirect:/vehicles/detail/" + id;
    }

}
