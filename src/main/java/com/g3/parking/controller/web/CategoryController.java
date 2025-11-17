package com.g3.parking.controller.web;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.parking.model.User;
import com.g3.parking.model.VehicleCategory;
import com.g3.parking.service.VehicleCategoryService;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/categories")
public class CategoryController extends BaseController {

    @Autowired
    VehicleCategoryService categoryService;

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/listar")
    public String mostrarCategorias(Model model, @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("categories", categoryService.getAll());
        return "category/list";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/detail/{id}")
    public String mostrarDetalle(@PathVariable("id") Long id, Model model,
            @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("category", categoryService.findById(id));
        return "category/detail";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/nuevo")
    public String nuevo(Model model, @ModelAttribute("currentUser") User currentUser) {
        return "category/form";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/crear")
    public String crear(@RequestParam("name") String name,
            @RequestParam("ratePerHour") BigDecimal ratePerHour,
            Model model, @ModelAttribute("currentUser") User currentUser) {
        VehicleCategory categoria = categoryService.create(name, ratePerHour);
        if (categoria == null) {
            model.addAttribute("error", "La categoria ya existe");
            return "category/form";
        }
        return "redirect:/categories/detail/" + categoria.getId();
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("category", categoryService.findById(id));
        return "category/form";
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("ratePerHour") BigDecimal ratePerHour,
            Model model, @ModelAttribute("currentUser") User currentUser) {
        VehicleCategory categoria = categoryService.upgrade(id, name, ratePerHour);
        if (categoria == null) {
            model.addAttribute("category", categoryService.findById(id));
            model.addAttribute("error", "No se pudo actualizar la categoria");
            return "category/form";
        }
        return "redirect:/categories/detail/" + categoria.getId();
    }

    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping("/changeStatus/{id}")
    public String changeStatus(@PathVariable("id") Long id,
            Model model, @ModelAttribute("currentUser") User currentUser) {
        
        categoryService.changeStatus(id);
        return "redirect:/categories/listar";
    }

}
