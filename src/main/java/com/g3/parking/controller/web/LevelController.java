package com.g3.parking.controller.web;

import com.g3.parking.model.Level;
import com.g3.parking.model.Parking;
import com.g3.parking.model.Site;
import com.g3.parking.repository.LevelRepository;
import com.g3.parking.repository.ParkingRepository;
import com.g3.parking.repository.SiteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/levels")
public class LevelController {

    // Crear logger
    private static final Logger log = LoggerFactory.getLogger(LevelController.class);

    @Autowired
    private LevelRepository levelRepository;
    
    @Autowired
    private ParkingRepository parkingRepository;
    
    @Autowired
    private SiteRepository siteRepository;
    
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
        Optional<Parking> parkingOpt = parkingRepository.findById(parkingId);
        if (parkingOpt.isEmpty()) {
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
        
        Parking parking = parkingOpt.get();
        
        // Crear nivel
        Level level = new Level(columns, rows);
        level.setParking(parking);
        level = levelRepository.save(level);
        
        // Crear espacios automáticamente
        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= columns; col++) {
                Site site = new Site();
                site.setPosX(col);
                site.setPosY(row);
                site.setLevel(level);
                site.setStatus("available"); // Por defecto disponible
                siteRepository.save(site);
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
        Optional<Level> levelOpt = levelRepository.findById(id);
        
        if (levelOpt.isEmpty()) {
            model.addAttribute("error", "Nivel no encontrado");
            return "redirect:/parking/listar";
        }
        
        Level level = levelOpt.get();
        model.addAttribute("level", level);
        model.addAttribute("sites", level.getSites());
      
        return "level/detail";
    }
    
    // Eliminar nivel
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/eliminar/{id}")
    public String eliminarNivel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Level> levelOpt = levelRepository.findById(id);
        
        if (levelOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nivel no encontrado");
            return "redirect:/parking/listar";
        }
        
        Level level = levelOpt.get();
        Long parkingId = level.getParking().getId();
        
        // Eliminar todos los sites del nivel
        siteRepository.deleteAll(level.getSites());
        
        // Eliminar nivel
        levelRepository.delete(level);
        
        redirectAttributes.addFlashAttribute("success", "Nivel eliminado exitosamente");
        
        return "redirect:/parking/" + parkingId;
    }
}
