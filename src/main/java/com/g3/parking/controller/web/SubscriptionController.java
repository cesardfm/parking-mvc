package com.g3.parking.controller.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.g3.parking.datatransfer.SubscriptionDTO;
import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.model.SubscriptionStatus;
import com.g3.parking.service.PlanService;
import com.g3.parking.service.SubscriptionService;
import com.g3.parking.service.UserService;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController extends BaseController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private PlanService planService;

    @Autowired
    private UserService userService;

    @GetMapping("/listar/{userId}")
    public String listar(@PathVariable Long userId, Model model, @ModelAttribute("currentUser") UserDTO currentUser) {
        model.addAttribute("subscriptions", subscriptionService.findAllOfAnyUser(userId));
        model.addAttribute("userId", userId);
        return "subscription/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        model.addAttribute("subscription", subscriptionService.findById(id));
        return "subscription/detail";
    }

    @PostMapping("/cancel/{id}")
    public String postMethodName(@PathVariable Long id,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        subscriptionService.cancel(id);
        return "redirect:/subscriptions/detail/" + id;
    }

    @GetMapping("/selectPlan/{userId}")
    public String selectPlan(@PathVariable Long userId, Model model,
            @ModelAttribute("currentUser") UserDTO currentUser) {
        model.addAttribute("plans", planService.findAllActive());
        model.addAttribute("userId", userId);
        return "subscription/buy";
    }

    @GetMapping("/nuevo/{userId}/{planId}")
    public String nuevo(@PathVariable("userId") Long userId,
            @PathVariable("planId") Long planId, Model model,
            @ModelAttribute("currentUser") UserDTO currentUser) {
        model.addAttribute("plan", planService.findById(planId));
        model.addAttribute("userId", userId);
        return "subscription/form";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam("userId") Long userId,
            @RequestParam("planId") Long planId,
            @RequestParam("monthsDuration") int monthsDuration,
            @RequestParam("price") BigDecimal price,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        SubscriptionDTO subscription = SubscriptionDTO.builder()
                .user(userService.findById(userId))
                .plan(planService.findById(planId))
                .activationDate(LocalDateTime.now())
                .monthsDuration(monthsDuration)
                .price(price)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        Long subscriptionId = subscriptionService.create(subscription);

        if (subscriptionId == 0) {
            model.addAttribute("error", "Error al crear la subscription");
            return "subscription/form";
        }
        return "redirect:/subscriptions/detail/" + subscriptionId;
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        model.addAttribute("subscription", subscriptionService.findById(id));
        model.addAttribute("plans", planService.findAllActive());
        model.addAttribute("status", subscriptionService.getAllSubscriptionStatus());
        return "subscription/form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Long id,
            @RequestParam("userId") Long userId,
            @RequestParam("planId") Long planId,
            @RequestParam("activationDate") LocalDateTime activationDate,
            @RequestParam("monthsDuration") int monthsDuration,
            @RequestParam("price") BigDecimal price,
            @RequestParam("status") String status,
            Model model, @ModelAttribute("currentUser") UserDTO currentUser) {

        SubscriptionDTO subscription = SubscriptionDTO.builder()
                .user(userService.findById(userId))
                .plan(planService.findById(planId))
                .activationDate(activationDate)
                .monthsDuration(monthsDuration)
                .price(price)
                .build();
        subscription.setSubscriptionStatusFromString(status);
        boolean res = subscriptionService.update(subscription);

        if (res == false) {
            model.addAttribute("error", "Error al actualizar");
            model.addAttribute("subscription", subscriptionService.findById(id));
            model.addAttribute("plans", planService.findAllActive());
            return "subscription/form";
        }

        return "redirect:/subscriptions/detail/" + id;
    }

}
