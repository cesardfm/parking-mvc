package com.g3.parking.imageprocessing.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class ImageProcessingService {
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);


    public ImagenProcesadaResult procesarImagenConcurrente(String base64Image) throws Exception {
        long startTime = System.currentTimeMillis();

        byte[] imageBytes = convertirBase64ABytes(base64Image);
        BufferedImage imagenOriginal = ByteArrayInputStream2BufferedImage(imageBytes);

        CompletableFuture<byte[]> filtroGrises = CompletableFuture.supplyAsync(
                () -> aplicarEscalaGrises(imagenOriginal), executorService
        );

        CompletableFuture<byte[]> filtroReduccion = CompletableFuture.supplyAsync(
                () -> aplicarReduccionTamano(imagenOriginal, 0.5), executorService
        );

        CompletableFuture<byte[]> filtroBrillo = CompletableFuture.supplyAsync(
                () -> aplicarBrillo(imagenOriginal, 1.3f), executorService
        );

        CompletableFuture<byte[]> filtroRotacion = CompletableFuture.supplyAsync(
                () -> aplicarRotacion(imagenOriginal, 45), executorService
        );

        CompletableFuture.allOf(filtroGrises, filtroReduccion, filtroBrillo, filtroRotacion).join();

        long endTime = System.currentTimeMillis();
        long tiempoProcesamiento = endTime - startTime;

        // ================================
        // 🔥 GUARDAR IMÁGENES EN DISCO
        // ================================
        guardarImagenEnDirectorio(imageBytes, "original");
        guardarImagenEnDirectorio(filtroGrises.get(), "gris");
        guardarImagenEnDirectorio(filtroReduccion.get(), "reducida");
        guardarImagenEnDirectorio(filtroBrillo.get(), "brillo");
        guardarImagenEnDirectorio(filtroRotacion.get(), "rotada");
        // ================================

        return new ImagenProcesadaResult(
                imageBytes,
                filtroGrises.get(),
                filtroReduccion.get(),
                filtroBrillo.get(),
                filtroRotacion.get(),
                tiempoProcesamiento
        );
    }



    private byte[] aplicarEscalaGrises(BufferedImage imagen) {
        try {
            int width = imagen.getWidth();
            int height = imagen.getHeight();
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            
            Graphics2D g2d = grayImage.createGraphics();
            g2d.drawImage(imagen, 0, 0, null);
            g2d.dispose();
            
            return bufferedImage2Bytes(grayImage);
        } catch (Exception e) {
            throw new RuntimeException("Error aplicando escala de grises", e);
        }
    }
    
   
    private byte[] aplicarReduccionTamano(BufferedImage imagen, double escala) {
        try {
            int newWidth = (int) (imagen.getWidth() * escala);
            int newHeight = (int) (imagen.getHeight() * escala);
            
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, imagen.getType());
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(imagen, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            return bufferedImage2Bytes(resizedImage);
        } catch (Exception e) {
            throw new RuntimeException("Error reduciendo tamaño", e);
        }
    }
    
   
    private byte[] aplicarBrillo(BufferedImage imagen, float factor) {
        try {
            int width = imagen.getWidth();
            int height = imagen.getHeight();
            BufferedImage brightImage = new BufferedImage(width, height, imagen.getType());
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = imagen.getRGB(x, y);
                    
                    int alpha = (rgb >> 24) & 0xff;
                    int red = (rgb >> 16) & 0xff;
                    int green = (rgb >> 8) & 0xff;
                    int blue = rgb & 0xff;
                    
                    red = Math.min(255, (int) (red * factor));
                    green = Math.min(255, (int) (green * factor));
                    blue = Math.min(255, (int) (blue * factor));
                    
                    int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    brightImage.setRGB(x, y, newRgb);
                }
            }
            
            return bufferedImage2Bytes(brightImage);
        } catch (Exception e) {
            throw new RuntimeException("Error aplicando brillo", e);
        }
    }
    
    
    private byte[] aplicarRotacion(BufferedImage imagen, int grados) {
        try {
            double radianes = Math.toRadians(grados);
            
            double sin = Math.abs(Math.sin(radianes));
            double cos = Math.abs(Math.cos(radianes));
            int newWidth = (int) Math.floor(imagen.getWidth() * cos + imagen.getHeight() * sin);
            int newHeight = (int) Math.floor(imagen.getHeight() * cos + imagen.getWidth() * sin);
            
            BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, imagen.getType());
            Graphics2D g2d = rotatedImage.createGraphics();
            
            AffineTransform at = new AffineTransform();
            at.translate(newWidth / 2.0, newHeight / 2.0);
            at.rotate(radianes);
            at.translate(-imagen.getWidth() / 2.0, -imagen.getHeight() / 2.0);
            
            g2d.setTransform(at);
            g2d.drawImage(imagen, 0, 0, null);
            g2d.dispose();
            
            return bufferedImage2Bytes(rotatedImage);
        } catch (Exception e) {
            throw new RuntimeException("Error rotando imagen", e);
        }
    }
    
    
    private byte[] convertirBase64ABytes(String base64) {
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }
        return Base64.getDecoder().decode(base64);
    }
    
    private BufferedImage ByteArrayInputStream2BufferedImage(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return ImageIO.read(bais);
    }
    
    private byte[] bufferedImage2Bytes(BufferedImage imagen) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imagen, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error convirtiendo imagen a bytes", e);
        }
    }
    
    
    public static class ImagenProcesadaResult {
        private final byte[] imagenOriginal;
        private final byte[] imagenEscalaGrises;
        private final byte[] imagenReducida;
        private final byte[] imagenBrillo;
        private final byte[] imagenRotada;
        private final long tiempoProcesamiento;
        
        public ImagenProcesadaResult(byte[] imagenOriginal, byte[] imagenEscalaGrises, 
                                     byte[] imagenReducida, byte[] imagenBrillo, 
                                     byte[] imagenRotada, long tiempoProcesamiento) {
            this.imagenOriginal = imagenOriginal;
            this.imagenEscalaGrises = imagenEscalaGrises;
            this.imagenReducida = imagenReducida;
            this.imagenBrillo = imagenBrillo;
            this.imagenRotada = imagenRotada;
            this.tiempoProcesamiento = tiempoProcesamiento;
        }
        
        public byte[] getImagenOriginal() { return imagenOriginal; }
        public byte[] getImagenEscalaGrises() { return imagenEscalaGrises; }
        public byte[] getImagenReducida() { return imagenReducida; }
        public byte[] getImagenBrillo() { return imagenBrillo; }
        public byte[] getImagenRotada() { return imagenRotada; }
        public long getTiempoProcesamiento() { return tiempoProcesamiento; }
    }
    private void guardarImagenEnDirectorio(byte[] imagenBytes, String nombreArchivo) {
        try {

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            Path carpeta = Paths.get("filtros/" + timestamp);

            // Crear carpeta si no existe
            Files.createDirectories(carpeta);

            // Ruta final del archivo
            Path path = carpeta.resolve(nombreArchivo + ".png");

            Files.write(path, imagenBytes);

        } catch (IOException e) {
            throw new RuntimeException("Error guardando imagen en disco", e);
        }
    }


    public void shutdown() {
        executorService.shutdown();
    }
}
