package com.g3.parking.controller.web;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.g3.parking.datatransfer.PlanDTO;
import com.g3.parking.model.Plan;
import com.g3.parking.model.User;
import com.g3.parking.service.PlanService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequestMapping("/plans")
public class PlanController extends BaseController {
    @Autowired
    private PlanService planService;

    @GetMapping("/listar")
    public String getMethodName(Model model, @ModelAttribute("currentUser") User currentUser) {
        model.addAttribute("plans", planService.findAll());
        return "plan/list";
    }

    @GetMapping("/detail/{id}")
    public String getMethodName(@PathVariable Long id,
            Model model, @ModelAttribute("currentUser") User currentUser) {

        model.addAttribute("plan", planService.findById(id));
        return "plan/detail";
    }

    @PostMapping("/changeStatus/{id}")
    public String changeStatus(@PathVariable Long id,
            Model model, @ModelAttribute("currentUser") User currentUser) {

        planService.changeStatus(id);
        return "redirect:/plans/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model, @ModelAttribute("currentUser") User currentUser) {
        return "plan/form";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("discountPercent") BigDecimal discountPercent,
            @RequestParam("price") BigDecimal price,
            Model model, @ModelAttribute("currentUser") User currentUser) {

        PlanDTO plan = PlanDTO.builder()
                .name(name)
                .description(description)
                .discountPercent(discountPercent.divide(BigDecimal.valueOf(100)))
                .price(price)
                .active(true)
                .build();
        Long planId = planService.create(plan);

        if (planId == 0){
            model.addAttribute("error", "Error al crear el Plan");
            return "plan/form";
        }
        return "redirect:/plans/detail/" + planId;
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, 
            Model model, @ModelAttribute("currentUser") User currentUser) {
        
        model.addAttribute("plan", planService.findById(id));
        return "plan/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("discountPercent") BigDecimal discountPercent,
            Model model, @ModelAttribute("currentUser") User currentUser) {
        
        PlanDTO plan = PlanDTO.builder()
                        .id(id)
                        .name(name)
                        .description(description)
                        .price(price)
                        .discountPercent(discountPercent)
                        .build();
        boolean res = planService.update(plan);

        if (res == false){
            model.addAttribute("error", "Error al actualizar");
            model.addAttribute("plan", planService.findById(id));
            return "plan/form";
        }

        return "redirect:/plans/detail/" + id;
    }
    

}
