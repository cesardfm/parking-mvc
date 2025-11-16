package com.g3.parking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g3.parking.model.Site;

/**
 * Helper para convertir datos de sitios a JSON para usar en Thymeleaf
 */
@Component("siteDataJson")
public class SiteDataJsonHelper {

    private static final Logger log = LoggerFactory.getLogger(SiteDataJsonHelper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte una lista de sitios a JSON string
     * Incluye solo los campos necesarios para el frontend
     */
    public String toJson(List<Site> sites) {
        log.info("🔄 Convirtiendo sitios a JSON. Total sitios: {}", sites != null ? sites.size() : 0);
        
        if (sites == null || sites.isEmpty()) {
            log.warn("⚠️ Lista de sitios vacía o nula");
            return "[]";
        }
        
        try {
            // Crear DTOs simplificados
            List<SiteDto> siteDtos = sites.stream()
                .map(site -> {
                    log.debug("  - Procesando sitio ID: {}, Posición: ({},{}), Estado: {}", 
                        site.getId(), site.getPosX(), site.getPosY(), site.getStatus());
                    return new SiteDto(
                        site.getId(),
                        site.getPosX(),
                        site.getPosY(),
                        site.getStatus()
                    );
                })
                .collect(Collectors.toList());
            
            String json = objectMapper.writeValueAsString(siteDtos);
            log.info("✅ JSON generado exitosamente. Tamaño: {} caracteres", json.length());
            log.debug("JSON: {}", json.length() > 200 ? json.substring(0, 200) + "..." : json);
            
            return json;
        } catch (JsonProcessingException e) {
            log.error("❌ Error al convertir sitios a JSON", e);
            // En caso de error, retornar array vacío
            return "[]";
        }
    }

    /**
     * DTO interno para simplificar la serialización
     */
    public static class SiteDto {
        public Long id;
        public Integer posX;
        public Integer posY;
        public String status;

        public SiteDto(Long id, Integer posX, Integer posY, String status) {
            this.id = id;
            this.posX = posX;
            this.posY = posY;
            this.status = status;
        }

        // Getters necesarios para Jackson
        public Long getId() {
            return id;
        }

        public Integer getPosX() {
            return posX;
        }

        public Integer getPosY() {
            return posY;
        }

        public String getStatus() {
            return status;
        }
    }
}