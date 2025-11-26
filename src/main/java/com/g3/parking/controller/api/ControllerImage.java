package com.g3.parking.controller.api;

import com.g3.parking.datatransfer.PlacaResponse;
import com.g3.parking.model.PlacaVehiculo;
import com.g3.parking.repository.PlacaVehiculoRepository;
import com.g3.parking.service.ImageProcessingService;
import com.g3.parking.service.PlacaService;
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


            // Procesar filtros internos
            ImageProcessingService.ImagenProcesadaResult resultado =
                    imageProcessingService.procesarImagenConcurrente(imagenBase64);

                    // Guardar la placa en BD
            PlacaVehiculo placa = new PlacaVehiculo();
            placa.setEstadoProcesamiento("PROCESANDO");

            placa.setImagenOriginal(resultado.getImagenOriginal());
            placa.setTiempoProcesamiento(resultado.getTiempoProcesamiento());

            // Detectar placa con OpenALPR directamente desde Base64
            PlacaResponse resultadoPlaca = placaService.detectarPlate(imagenBase64);

            System.out.println("Fonteca: resultado ALPR → " + resultadoPlaca.getPlaca());

            // Guardar la placa detectada
            placa.setPlacaTexto(
                    resultadoPlaca.isExito() && resultadoPlaca.getPlaca() != null
                            ? resultadoPlaca.getPlaca()
                            : "NO DETECTADA"
            );

            placa.setEstadoProcesamiento("COMPLETADO");

            // Respuesta JSON al frontend con toda la info
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


    private String convertirBytesABase64(byte[] bytes) {
        if (bytes == null) return null;
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
