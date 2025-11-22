package com.g3.parking.controller.web;

import com.g3.parking.datatransfer.SiteDTO;
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
    
   
}
