package com.g3.parking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g3.parking.model.User;
import com.g3.parking.model.Vehicle;
import com.g3.parking.model.VehicleCategory;
import com.g3.parking.repository.VehicleRepository;
import com.g3.parking.repository.UserRepository;
import com.g3.parking.repository.VehicleCategoryRepository;

@Service
@Transactional
public class VehicleService {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private VehicleCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;
    
    // Registrar vehículo con validaciones
    public Vehicle registerVehicle(String licensePlate, String color, Long categoryId, Long ownerId) {
       
        // Verificar que no exista
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new IllegalStateException("La placa " + licensePlate + " ya está registrada");
        }
        
        // Validar que la categoría exista
        VehicleCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        
        // Crear y guardar
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate.toUpperCase()); // Normalizar
        vehicle.setColor(color);
        vehicle.setCategory(category);
        
        if (ownerId != null) {
            vehicle.setOwner(userRepository.findById(ownerId).orElse(null));
        }
        
        return vehicleRepository.save(vehicle);
    }
    
    // Obtener o crear vehículo (para visitantes)
    public Vehicle getOrCreateVehicle(String licensePlate, String color, Long categoryId) {
        // Buscar si ya existe
        Optional<Vehicle> existing = vehicleRepository.findByLicensePlate(licensePlate);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // No existe, crear como visitante
        return registerVehicle(licensePlate, color, categoryId, null);
    }
    
    // Vincular vehículo a usuario
    public Vehicle assignOwner(String licensePlate, Long ownerId) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
            .orElseThrow(() -> new IllegalStateException("Vehículo no encontrado"));
        
        // Validar que no tenga dueño
        if (vehicle.getOwner() != null) {
            throw new IllegalStateException("El vehículo ya tiene dueño");
        }
        
        // Asignar dueño
        User owner = userRepository.findById(ownerId).orElseThrow();
        vehicle.setOwner(owner);
        
        return vehicleRepository.save(vehicle);
    }
    
    // Actualizar vehículo con validaciones
    public Vehicle updateVehicle(Long id, String color, Long categoryId) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Vehículo no encontrado"));
        
        // Solo actualizar campos permitidos
        if (color != null && !color.isBlank()) {
            vehicle.setColor(color);
        }
        
        if (categoryId != null) {
            VehicleCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
            vehicle.setCategory(category);
        }
        
        return vehicleRepository.save(vehicle);
    }
    
    // Eliminar con validaciones
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Vehículo no encontrado"));
        
        // Validar que no tenga tickets activos
        // if (ticketRepository.existsByVehicle_IdAndExitTimeIsNull(id)) {
        //     throw new IllegalStateException("No se puede eliminar, tiene tickets activos");
        // }
        
        vehicleRepository.delete(vehicle);
    }
    
    // Obtener vehículos del usuario
    public List<Vehicle> getUserVehicles(Long userId) {
        return vehicleRepository.findByOwner_Id(userId);
    }
    
    // Buscar vehículo por placa
    public Vehicle getVehicleByPlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate)
            .orElseThrow(() -> new IllegalStateException("Vehículo con placa " + licensePlate + " no encontrado"));
    }
    
    // Verificar disponibilidad de placa
    public boolean isPlateAvailable(String licensePlate) {
        return !vehicleRepository.existsByLicensePlate(licensePlate);
    }
    
    // Obtener estadísticas
    public long countVehiclesByCategory(Long categoryId) {
        return vehicleRepository.countByCategory_Id(categoryId);
    }
    
    // Listar todos
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}