package com.g3.parking.controller.web;

import com.g3.parking.datatransfer.LevelDTO;
import com.g3.parking.datatransfer.ParkingDTO;
import com.g3.parking.datatransfer.SiteDTO;
import com.g3.parking.service.LevelService;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.SiteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/levels")
public class LevelController extends BaseController {

    @Autowired
    private LevelService levelService;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private SiteService siteService;

    // Mostrar paleta de colores
    @GetMapping
    public String mostrarPaletaColores(Model model) {
        return "levels";
    }

    // Crear nuevo nivel
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/crear")
    public String crearNivel(@RequestParam("parkingId") Long parkingId,
            @RequestParam("rows") Integer rows,
            @RequestParam("columns") Integer columns,
            RedirectAttributes redirectAttributes) {

        // Validar parking
        ParkingDTO parking = parkingService.findById(parkingId);
        if (parking == null) {
            redirectAttributes.addFlashAttribute("error", "Parqueadero no encontrado");
            return "redirect:/parking/listar";
        }

        // Validar dimensiones
        if (rows == null || rows < 1 || rows > 50) {
            redirectAttributes.addFlashAttribute("error", "El número de filas debe estar entre 1 y 50");
            return "redirect:/parking/" + parkingId;
        }

        if (columns == null || columns < 1 || columns > 50) {
            redirectAttributes.addFlashAttribute("error", "El número de columnas debe estar entre 1 y 50");
            return "redirect:/parking/" + parkingId;
        }

        // Crear nivel
        LevelDTO level = LevelDTO.builder()
                .columns(columns)
                .rows(rows)
                .parking(parking)
                .build();
        level = levelService.create(level);

        // Crear espacios automáticamente
        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= columns; col++) {
                SiteDTO site = new SiteDTO();
                site.setPosX(col);
                site.setPosY(row);
                site.setLevel(level);
                site.setStatus("available"); // Por defecto disponible
                siteService.create(site);
            }
        }

        redirectAttributes.addFlashAttribute("success",
                "Nivel creado exitosamente con " + (rows * columns) + " espacios");

        return "redirect:/parking/" + parkingId;
    }

    // Ver detalles de un nivel
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/{id}")
    public String verDetalleNivel(@PathVariable Long id, Model model) {
        LevelDTO level = levelService.findById(id);

        if (level == null) {
            model.addAttribute("error", "Nivel no encontrado");
            return "redirect:/parking/listar";
        }

        model.addAttribute("level", level);
        model.addAttribute("sites", level.getSites());

        return "level/detail";
    }

    // Eliminar nivel
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/eliminar/{id}")
    public String eliminarNivel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        LevelDTO level = levelService.findById(id);

        if (level == null ) {
            redirectAttributes.addFlashAttribute("error", "Nivel no encontrado");
            return "redirect:/parking/listar";
        }
        Long parkingId = level.getParking().getId();

        // Eliminar todos los sites del nivel
        siteService.deleteAll(level.getSites());

        // Eliminar nivel
        levelService.delete(level);

        redirectAttributes.addFlashAttribute("success", "Nivel eliminado exitosamente");

        return "redirect:/parking/" + parkingId;
    }
}
