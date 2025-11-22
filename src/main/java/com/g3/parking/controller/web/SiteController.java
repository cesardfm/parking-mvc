package com.g3.parking.controller.web;

import com.g3.parking.datatransfer.SiteDTO;
import com.g3.parking.repository.SiteRepository;
import com.g3.parking.request.SiteUpdateRequest;
import com.g3.parking.service.SiteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sites")
public class SiteController extends BaseController {
    
    @Autowired
    private SiteService siteService;

    @Autowired
    private  SiteRepository siteRepository;
    
    // Actualizar múltiples sites en batch
    @PostMapping("/batch-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpdateSites(@RequestBody List<SiteUpdateRequest> changes) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            for (SiteUpdateRequest change : changes) {
                SiteDTO site = siteService.findById(change.getSiteId());
                if (site != null ) {
                    site.setStatus(change.getStatus());
                    siteService.create(site);
                }
            }
            
            response.put("success", true);
            response.put("message", "Cambios guardados exitosamente");
            response.put("updated", changes.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar cambios: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Actualizar un solo site (compatibilidad con level-detail-admin.js -> /sites/update)
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSite(@RequestBody SiteUpdateRequest change) {
        Map<String, Object> response = new HashMap<>();

        // try {
        //     SiteDTO siteOpt = siteService.findById(change.getSiteId());
        //     if (siteOpt  == null) {
        //         response.put("success", false);
        //         response.put("message", "Site not found");
        //         return ResponseEntity.status(404).body(response);
        //     }

        //     SiteDTO site = siteOpt;

        //     // No permitir cambios en sites deshabilitados
        //     if ("disabled".equals(site.getStatus())) {
        //         response.put("success", false);
        //         response.put("message", "Este espacio está deshabilitado y no se puede modificar.");
        //         return ResponseEntity.status(400).body(response);
        //     }

        //     if (change.getStatus() != null) {
        //         site.setStatus(change.getStatus());
        //     }
        //     if (change.getVehicleType() != null) {
        //         site.setVehicleType(change.getVehicleType());
        //     }

        //     siteRepository.save(site);

        //     response.put("success", true);
        //     response.put("message", "Sitio actualizado");
        //     response.put("siteId", change.getSiteId());
        //     return ResponseEntity.ok(response);
        // } catch (Exception e) {
        //     response.put("success", false);
        //     response.put("message", "Error al guardar cambios: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        // }
    }

}
