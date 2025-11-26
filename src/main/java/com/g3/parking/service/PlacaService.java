package com.g3.parking.service;

import com.g3.parking.datatransfer.PlacaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PlacaService {

    private static final Logger log = LoggerFactory.getLogger(PlacaService.class);

    @Value("${alpr.path}")
    private String alprPath;

    /**
     * Recibe imagen en Base64 y la procesa
     */
    public PlacaResponse detectarPlate(String imagenBase64) {
        try {
            log.info("⏺ Recibida imagen en Base64");
            log.info("⏺ Longitud del string Base64: {} caracteres", imagenBase64.length());

            if (imagenBase64 == null || imagenBase64.isEmpty()) {
                log.error("❌ La imagen Base64 viene VACÍA");
                return PlacaResponse.error("La imagen está vacía");
            }

            // Decodificar Base64 a bytes
            byte[] imagenBytes = Base64.getDecoder().decode(imagenBase64);
            log.info("⏺ Tamaño de imagen decodificada: {} bytes", imagenBytes.length);

            // Guardar imagen temporalmente
            File temp = File.createTempFile("placa_", ".jpg");
            log.info("📁 Archivo temporal creado: {}", temp.getAbsolutePath());

            Files.write(temp.toPath(), imagenBytes);

            log.info("📸 Imagen escrita correctamente. Tamaño final: {} bytes", temp.length());

            // Ejecutar OpenALPR
            String placa = detectPlateFromFile(temp.getAbsolutePath());

            log.info("📥 Placa detectada: {}", placa);

            PlacaResponse response = new PlacaResponse();
            response.setPlaca(placa);
            response.setExito(placa != null && !placa.isEmpty());

            boolean deleted = temp.delete();
            log.info("🧹 ¿Archivo temporal borrado?: {}", deleted);

            return response;

        } catch (IllegalArgumentException e) {
            log.error("❌ ERROR: Base64 inválido: {}", e.getMessage());
            return PlacaResponse.error("Formato Base64 inválido");
        } catch (Exception e) {
            log.error("❌ ERROR EN detectarPlate(String): {}", e.getMessage());
            log.error("❌ STACKTRACE COMPLETO", e);
            return PlacaResponse.error("Error procesando la imagen: " + e.getMessage());
        }
    }

    /**
     * Recibe ruta de archivo y la procesa
     */
    public PlacaResponse detectarPlateFromPath(String imagePath) {
        try {
            log.info("⏺ Recibida ruta de archivo: {}", imagePath);

            File img = new File(imagePath);
            if (!img.exists()) {
                log.error("❌ El archivo no existe: {}", imagePath);
                return PlacaResponse.error("El archivo no existe");
            }

            log.info("⏺ Tamaño del archivo: {} bytes", img.length());

            // Ejecutar OpenALPR
            String placa = detectPlateFromFile(imagePath);

            log.info("📥 Placa detectada: {}", placa);

            PlacaResponse response = new PlacaResponse();
            response.setPlaca(placa);
            response.setExito(placa != null && !placa.isEmpty());

            return response;

        } catch (Exception e) {
            log.error("❌ ERROR EN detectarPlateFromPath: {}", e.getMessage());
            log.error("❌ STACKTRACE COMPLETO", e);
            return PlacaResponse.error("Error procesando la imagen: " + e.getMessage());
        }
    }

    /** Método interno que ejecuta OpenALPR */
    private String detectPlateFromFile(String imagePath) {
        log.info("🚀 Iniciando detectPlateFromFile con ruta: {}", imagePath);

        try {
            File img = new File(imagePath);
            log.info("📁 Verificando archivo...");
            log.info("    → Existe: {}", img.exists());
            log.info("    → Tamaño: {} bytes", img.length());

            if (!img.exists()) {
                throw new RuntimeException("La imagen no existe: " + imagePath);
            }

            ProcessBuilder pb = buildCommand(imagePath);
            pb.redirectErrorStream(true);

            log.info("🔧 Ejecutando comando OpenALPR...");
            log.info("🔧 Comando literal: {}", String.join(" ", pb.command()));

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8")
            );

            log.info("📥 Leyendo salida de OpenALPR...");

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("🔹 ALPR OUTPUT → {}", line);
                jsonBuilder.append(line);
            }

            int exit = process.waitFor();
            log.info("🏁 Proceso OpenALPR finalizado con código: {}", exit);

            String json = jsonBuilder.toString();

            if (json.isEmpty()) {
                log.warn("⚠ JSON recibido VACÍO desde OpenALPR");
            } else {
                log.info("📦 JSON COMPLETO RECIBIDO: {}", json);
            }

            return parsePlate(json);

        } catch (Exception e) {
            log.error("❌ ERROR ejecutando OpenALPR: {}", e.getMessage());
            log.error("❌ STACKTRACE COMPLETO", e);
            throw new RuntimeException("Error ejecutando OpenALPR", e);
        }
    }

    private String parsePlate(String json) {
        try {
            log.info("🔍 Iniciando parseo de JSON...");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            log.info("🔍 Nodo principal leído correctamente.");

            JsonNode results = root.path("results");

            log.info("🔍 Tamaño del array de resultados: {}", results.size());

            if (results.isArray() && results.size() > 0) {
                JsonNode first = results.get(0);

                if (first.has("plate")) {
                    String plate = first.get("plate").asText();
                    log.info("💡 PLACA DETECTADA: {}", plate);
                    return plate;
                } else {
                    log.warn("⚠ 'plate' no encontrado en resultado[0]");
                }
            } else {
                log.warn("⚠ No se encontraron resultados en el JSON.");
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Error parseando JSON: {}", e.getMessage());
            log.error("❌ JSON RECIBIDO: {}", json);
            log.error("❌ STACKTRACE COMPLETO", e);

            throw new RuntimeException("Error parseando respuesta de OpenALPR: " + e.getMessage());
        }
    }

    private ProcessBuilder buildCommand(String imagePath) {

        log.info("🟨 Sistema operativo detectado: {}", System.getProperty("os.name"));
        log.info("🟨 alprPath configurado: {}", alprPath);

        ProcessBuilder pb = new ProcessBuilder(
                alprPath,
                "-c", "us",
                "-j",
                imagePath
        );

        File baseDir = new File(alprPath).getParentFile();
        pb.directory(baseDir);

        log.info("🟩 Directorio de ejecución final: {}", baseDir.getAbsolutePath());
        log.info("🟩 Comando final: {}", String.join(" ", pb.command()));

        return pb;
    }
}