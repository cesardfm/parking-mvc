package com.g3.parking.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class DatabaseConfigLoade implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String[] DB_CONFIG_PATHS = {
        "/config/db_config",           // Docker filesystem
        "classpath:/config/db_config"  // Desarrollo local
    };

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        
        try {
            // 1. Cargar configuración JSON
            Resource jsonResource = loadJsonConfig();
            if (!jsonResource.exists()) {
                System.out.println("⚠️ No se encontró database-config.json, usando configuración por defecto");
                return;
            }
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResource.getInputStream());

            // 2. Leer tipo de BD desde db_config
            String dbType = readDBFromFile();
            
            if (dbType == null || dbType.trim().isEmpty()) {
                System.out.println("INFO: No se pudo leer la configuración de db_config.");
                return;
            }
            
            // 3. Obtener la base de datos activa
            String activeDb = dbType.trim().split(" ")[0].toLowerCase();
            System.out.println("Base de datos detectada: " + activeDb);
            
            // 4. Cargar configuración de la BD seleccionada
            JsonNode dbConfig = root.path("databases").path(activeDb);
            
            if (dbConfig.isMissingNode()) {
                System.err.println("Error: No se encontró configuración para: " + activeDb);
                return;
            }
            
            // 5. Configurar propiedades de Spring
            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", dbConfig.path("url").asText());
            props.put("spring.datasource.username", dbConfig.path("username").asText());
            props.put("spring.datasource.password", dbConfig.path("password").asText());
            props.put("spring.datasource.driver-class-name", dbConfig.path("driver").asText());
            props.put("spring.jpa.database-platform", dbConfig.path("dialect").asText());
         
            environment.getPropertySources().addFirst(
                new MapPropertySource("jsonDatabaseConfig", props)
            );
            
            System.out.println("Configuración de base de datos cargada: " + activeDb);
            System.out.println("URL: " + props.get("spring.datasource.url"));
            
        } catch (IOException e) {
            System.err.println("Error cargando configuración: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Resource loadJsonConfig() {
        // Intentar desde filesystem (Docker)
        Resource resource = new FileSystemResource("/config/database-config.json");
        if (!resource.exists()) {
            // Fallback a classpath (desarrollo)
            resource = new ClassPathResource("config/database-config.json");
        }
        return resource;
    }

    private String readDBFromFile() {
        for (String path : DB_CONFIG_PATHS) {
            try {
                Resource resource = path.startsWith("classpath:") 
                    ? new ClassPathResource(path.substring(10))
                    : new FileSystemResource(path);
                    
                if (resource.exists()) {
                    return readDBConfigFromResource(resource);
                }
            } catch (Exception e) {
                System.out.println("No se pudo leer desde: " + path);
            }
        }
        return "";
    }

    private String readDBConfigFromResource(Resource resource) {
        try (InputStream in = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                String trimmedLine = linea.trim();
                // Buscar línea no vacía y no comentada
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    System.out.println("🔍 Línea detectada en db_config: " + trimmedLine);
                    return trimmedLine;
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo db_config: " + e.getMessage());
        }
        return "";
    }
}