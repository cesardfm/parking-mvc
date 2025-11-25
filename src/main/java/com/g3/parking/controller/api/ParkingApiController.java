package com.g3.parking.controller.api;

import com.g3.parking.datatransfer.LevelDTO;
import com.g3.parking.datatransfer.ParkingDTO;
import com.g3.parking.datatransfer.SiteDTO;
import com.g3.parking.datatransfer.TicketDTO;
import com.g3.parking.service.LevelService;
import com.g3.parking.service.ParkingService;
import com.g3.parking.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parking")
public class ParkingApiController {

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private LevelService levelService;

    // @Autowired
    // private SiteService siteService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerInformacionParqueadero(@PathVariable Long id) {
        try {
            ParkingDTO parking = parkingService.findById(id);
            
            if (parking == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", parking.getId());
            response.put("name", parking.getName());
            response.put("address", parking.getAddress());
            response.put("organization", parking.getOrganization() != null ? 
                parking.getOrganization().getName() : null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}/location")
    public ResponseEntity<Map<String, Object>> obtenerPosicionParqueadero(@PathVariable Long id) {
        try {
            ParkingDTO parking = parkingService.findById(id);
            
            if (parking == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", parking.getId());
            response.put("name", parking.getName());
            response.put("lat", parking.getLat());
            response.put("lng", parking.getLng());
            response.put("address", parking.getAddress());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> obtenerDisponibilidad(@PathVariable Long id) {
        try {
            ParkingDTO parking = parkingService.findById(id);
            
            if (parking == null) {
                return ResponseEntity.notFound().build();
            }

            List<LevelDTO> levels = levelService.findByParkingId(id);
            
            int totalSpaces = 0;
            int availableSpaces = 0;
            int occupiedSpaces = 0;
            
            for (LevelDTO level : levels) {
                List<SiteDTO> sites = level.getSites();
                totalSpaces += sites.size();
                availableSpaces += sites.stream()
                    .filter(site -> "available".equals(site.getStatus()))
                    .count();
                occupiedSpaces += sites.stream()
                    .filter(site -> "disabled".equals(site.getStatus()))
                    .count();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("parkingId", id);
            response.put("parkingName", parking.getName());
            response.put("totalSpaces", totalSpaces);
            response.put("availableSpaces", availableSpaces);
            response.put("occupiedSpaces", occupiedSpaces);
            response.put("levels", levels.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 4. Recibo de pago de un usuario
     * GET /api/parking/receipt/{ticketId}
     * (Este endpoint podría estar en TicketApiController, pero lo incluyo aquí por completitud)
     */
    @GetMapping("/receipt/{ticketId}")
    public ResponseEntity<Map<String, Object>> obtenerReciboPago(@PathVariable Long ticketId) {
        // Este servicio debería implementarse en un TicketApiController
        // pero lo dejo como referencia
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Este endpoint debe implementarse en TicketApiController");
        return ResponseEntity.status(501).body(response);
    }

    /**
     * 5. Ubicación de vehículo en un parqueadero por número de placa
     * GET /api/parking/{parkingId}/vehicle/{licensePlate}
     */
    @GetMapping("/{parkingId}/vehicle/{licensePlate}")
    public ResponseEntity<Map<String, Object>> ubicarVehiculo(
            @PathVariable Long parkingId,
            @PathVariable String licensePlate) {
        try {
            ParkingDTO parking = parkingService.findById(parkingId);
            
            if (parking == null) {
                return ResponseEntity.notFound().build();
            }

            // Buscar tickets activos (no pagados) en este parqueadero con la placa especificada
            List<TicketDTO> tickets = ticketService.findBySite_Level_Parking_Id(parkingId);
            
            for (TicketDTO ticket : tickets) {
                // Verificar que el ticket no esté pagado y que la placa coincida
                if (ticket.getVehicle() != null && 
                    !ticket.isPaid() &&
                    licensePlate.equalsIgnoreCase(ticket.getVehicle().getLicensePlate())) {
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("found", true);
                    response.put("parkingId", parkingId);
                    response.put("parkingName", parking.getName());
                    response.put("ticketId", ticket.getId());
                    response.put("levelId", ticket.getSite().getLevel().getId());
                    response.put("siteId", ticket.getSite().getId());
                    response.put("licensePlate", licensePlate);
                    response.put("vehicleColor", ticket.getVehicle().getColor());
                    response.put("vehicleCategory", ticket.getVehicle().getCategory() != null ? 
                        ticket.getVehicle().getCategory().getName() : null);
                    response.put("entryTime", ticket.getEntryTime());
                    
                    return ResponseEntity.ok(response);
                }
            }

            // No se encontró el vehículo
            Map<String, Object> response = new HashMap<>();
            response.put("found", false);
            response.put("message", "Vehículo no encontrado en este parqueadero");
            response.put("licensePlate", licensePlate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Endpoint adicional: Listar todos los parqueaderos con su ubicación
     * GET /api/parking/locations
     */
    @GetMapping("/locations")
    public ResponseEntity<List<Map<String, Object>>> listarUbicaciones() {
        try {
            List<ParkingDTO> parkings = parkingService.findAll();
            
            List<Map<String, Object>> locations = parkings.stream()
                .filter(p -> p.getLat() != null && p.getLng() != null)
                .map(p -> {
                    Map<String, Object> loc = new HashMap<>();
                    loc.put("id", p.getId());
                    loc.put("name", p.getName());
                    loc.put("lat", p.getLat());
                    loc.put("lng", p.getLng());
                    loc.put("address", p.getAddress());
                    return loc;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
