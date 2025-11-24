package com.g3.parking.imageprocessing.controller;

import com.g3.parking.imageprocessing.DTO.PlacaResponse;
import com.g3.parking.imageprocessing.model.PlacaVehiculo;
import com.g3.parking.imageprocessing.repository.PlacaVehiculoRepository;
import com.g3.parking.imageprocessing.service.ImageProcessingService;
import com.g3.parking.imageprocessing.service.PlacaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mock.web.MockMultipartFile;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/api/image-processing")
@CrossOrigin(origins = "*")
public class ControllerImage {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private PlacaVehiculoRepository placaVehiculoRepository;
    
    @Autowired
    private PlacaService placaService;


    @PostMapping("/procesar")
    public ResponseEntity<Map<String, Object>> procesarImagen(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String imagenBase64 = request.get("imagen");

            if (imagenBase64 == null || imagenBase64.isEmpty()) {
                response.put("success", false);
                response.put("message", "No se recibió la imagen");
                return ResponseEntity.badRequest().body(response);
            }

            // Guardar base64 como archivo temporal para OpenALPR
            byte[] imagenBytes = Base64.getDecoder().decode(imagenBase64);
            File temp = File.createTempFile("placa_", ".jpg");
            Files.write(temp.toPath(), imagenBytes);

            // Guardar la placa en BD
            PlacaVehiculo placa = new PlacaVehiculo();
            placa.setEstadoProcesamiento("PROCESANDO");
            placa = placaVehiculoRepository.save(placa);

            // Procesar filtros internos
            ImageProcessingService.ImagenProcesadaResult resultado =
                    imageProcessingService.procesarImagenConcurrente(imagenBase64);

            placa.setImagenOriginal(resultado.getImagenOriginal());
            placa.setImagenEscalaGrises(resultado.getImagenEscalaGrises());
            placa.setImagenReducida(resultado.getImagenReducida());
            placa.setImagenBrillo(resultado.getImagenBrillo());
            resultado.getImagenesRotadas().get("45");
            resultado.getImagenesRotadas().get("90");
            resultado.getImagenesRotadas().get("180");
            placa.setTiempoProcesamiento(resultado.getTiempoProcesamiento());

            // 🔥 Detectar placa con OpenALPR (retorna PlacaResponse)
            // Convertir archivo temporal a MultipartFile
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    temp.getName(),
                    "image/jpeg",
                    Files.readAllBytes(temp.toPath())
            );

// Llamar al servicio que necesita MultipartFile
            PlacaResponse resultadoPlaca = placaService.detectarPlate(multipartFile);

            System.out.println("Fonteca: resultado ALPR → " + resultadoPlaca.getPlaca());

            // Guardar en BD la placa detectada
            placa.setPlacaTexto(
                    resultadoPlaca.isExito() && resultadoPlaca.getPlaca() != null
                            ? resultadoPlaca.getPlaca()
                            : "NO DETECTADA"
            );

            placa.setEstadoProcesamiento("COMPLETADO");
            placaVehiculoRepository.save(placa);

            temp.delete(); // borrar archivo temporal

            // 🔥 Respuesta JSON al frontend con toda la info
            response.put("success", true);
            response.put("resultadoPlaca", resultadoPlaca.getPlaca());
            response.put("resultadoExito", resultadoPlaca.isExito());
            response.put("resultadoError", resultadoPlaca.getError());
            response.put("placaTexto", placa.getPlacaTexto());
            response.put("placaId", placa.getId());
            response.put("tiempoProcesamiento", resultado.getTiempoProcesamiento());
            response.put("filtrosAplicados", Arrays.asList(
                    "Escala de grises",
                    "Reducción 50%",
                    "Ajuste brillo 30%",
                    "Rotación 45°"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error procesando imagen: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/placa/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPlaca(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<PlacaVehiculo> placaOpt = placaVehiculoRepository.findById(id);
        
        if (placaOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Placa no encontrada");
            return ResponseEntity.notFound().build();
        }
        
        PlacaVehiculo placa = placaOpt.get();
        
        response.put("success", true);
        response.put("id", placa.getId());
        response.put("placaTexto", placa.getPlacaTexto());
        response.put("fechaRegistro", placa.getFechaRegistro());
        response.put("estadoProcesamiento", placa.getEstadoProcesamiento());
        response.put("tiempoProcesamiento", placa.getTiempoProcesamiento());
        
        response.put("imagenOriginal", convertirBytesABase64(placa.getImagenOriginal()));
        response.put("imagenEscalaGrises", convertirBytesABase64(placa.getImagenEscalaGrises()));
        response.put("imagenReducida", convertirBytesABase64(placa.getImagenReducida()));
        response.put("imagenBrillo", convertirBytesABase64(placa.getImagenBrillo()));
        response.put("imagenRotada", convertirBytesABase64(placa.getImagenRotada()));
        
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/placas")
    public ResponseEntity<List<Map<String, Object>>> listarPlacas() {
        List<PlacaVehiculo> placas = placaVehiculoRepository.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (PlacaVehiculo placa : placas) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", placa.getId());
            item.put("placaTexto", placa.getPlacaTexto());
            item.put("fechaRegistro", placa.getFechaRegistro());
            item.put("estadoProcesamiento", placa.getEstadoProcesamiento());
            item.put("tiempoProcesamiento", placa.getTiempoProcesamiento());
            
            item.put("tieneImagenOriginal", placa.getImagenOriginal() != null);
            item.put("tieneImagenGrises", placa.getImagenEscalaGrises() != null);
            item.put("tieneImagenReducida", placa.getImagenReducida() != null);
            item.put("tieneImagenBrillo", placa.getImagenBrillo() != null);
            item.put("tieneImagenRotada", placa.getImagenRotada() != null);
            
            if (placa.getImagenOriginal() != null) {
                item.put("thumbnailOriginal", convertirBytesABase64(placa.getImagenOriginal()));
            }
            
            resultado.add(item);
        }
        
        return ResponseEntity.ok(resultado);
    }
    
    
    @DeleteMapping("/placa/{id}")
    public ResponseEntity<Map<String, Object>> eliminarPlaca(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        if (placaVehiculoRepository.existsById(id)) {
            placaVehiculoRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Placa eliminada exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Placa no encontrada");
            return ResponseEntity.notFound().build();
        }
    }
    
    
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        List<PlacaVehiculo> todasLasPlacas = placaVehiculoRepository.findAll();
        
        stats.put("totalPlacas", todasLasPlacas.size());
        stats.put("placasCompletadas", todasLasPlacas.stream()
            .filter(p -> "COMPLETADO".equals(p.getEstadoProcesamiento()))
            .count());
        
        long conImagenOriginal = todasLasPlacas.stream()
            .filter(p -> p.getImagenOriginal() != null)
            .count();
        long conImagenGrises = todasLasPlacas.stream()
            .filter(p -> p.getImagenEscalaGrises() != null)
            .count();
        long conImagenReducida = todasLasPlacas.stream()
            .filter(p -> p.getImagenReducida() != null)
            .count();
        long conImagenBrillo = todasLasPlacas.stream()
            .filter(p -> p.getImagenBrillo() != null)
            .count();
        long conImagenRotada = todasLasPlacas.stream()
            .filter(p -> p.getImagenRotada() != null)
            .count();
        
        Map<String, Long> imagenesGuardadas = new HashMap<>();
        imagenesGuardadas.put("original", conImagenOriginal);
        imagenesGuardadas.put("escalaGrises", conImagenGrises);
        imagenesGuardadas.put("reducida", conImagenReducida);
        imagenesGuardadas.put("brillo", conImagenBrillo);
        imagenesGuardadas.put("rotada", conImagenRotada);
        
        stats.put("imagenesGuardadas", imagenesGuardadas);
        
        double tiempoPromedio = todasLasPlacas.stream()
            .filter(p -> p.getTiempoProcesamiento() != null)
            .mapToLong(PlacaVehiculo::getTiempoProcesamiento)
            .average()
            .orElse(0.0);
        
        stats.put("tiempoPromedioMs", Math.round(tiempoPromedio));
        
        return ResponseEntity.ok(stats);
    }
    
    private String convertirBytesABase64(byte[] bytes) {
        if (bytes == null) return null;
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
