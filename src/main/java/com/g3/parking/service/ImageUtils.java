package com.g3.parking.service;

import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


public class ImageUtils {
    
   
    public static byte[] base64ToBytes(String base64) {
        if (base64 == null || base64.isEmpty()) {
            throw new IllegalArgumentException("Base64 string no puede ser null o vacío");
        }
        
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }
        
        return Base64.getDecoder().decode(base64);
    }
    
    
    public static String bytesToBase64(byte[] bytes, String mimeType) {
        if (bytes == null) {
            return null;
        }
        
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mimeType + ";base64," + base64;
    }
    
    
    public static byte[] bufferedImageToBytes(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo imagen a bytes", e);
        }
    }
    
    
    public static BufferedImage bytesToBufferedImage(byte[] bytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return ImageIO.read(bais);
        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo bytes a imagen", e);
        }
    }
    
   
    public static boolean isValidBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }
        
        try {
            if (base64.contains(",")) {
                base64 = base64.split(",")[1];
            }
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
