package com.g3.parking.controller.web;

import com.g3.parking.model.Site;
import com.g3.parking.repository.SiteRepository;
import com.g3.parking.request.SiteUpdateRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/sites")
public class SiteController {
    
    @Autowired
    private SiteRepository siteRepository;
    
    // Actualizar múltiples sites en batch
    @PostMapping("/batch-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpdateSites(@RequestBody List<SiteUpdateRequest> changes) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            for (SiteUpdateRequest change : changes) {
                Optional<Site> siteOpt = siteRepository.findById(change.getSiteId());
                if (siteOpt.isPresent()) {
                    Site site = siteOpt.get();
                    site.setStatus(change.getStatus());
                    siteRepository.save(site);
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
    
   
}
