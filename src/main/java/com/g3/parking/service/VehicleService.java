package com.g3.parking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g3.parking.datatransfer.VehicleDTO;
import com.g3.parking.model.User;
import com.g3.parking.model.Vehicle;
import com.g3.parking.model.VehicleCategory;
import com.g3.parking.repository.VehicleRepository;
import com.g3.parking.repository.UserRepository;
import com.g3.parking.repository.VehicleCategoryRepository;

@Service
@Transactional
public class VehicleService extends BaseService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    public List<VehicleDTO> getUserVehicles(Long userId) {
        List<Vehicle> vehicles = vehicleRepository.findByOwner_Id(userId);
        return vehicles.stream()
                .map(vehicle -> convert(vehicle, VehicleDTO.class))
                .collect(Collectors.toList());
    }

    public VehicleDTO getById(Long id) {
        Vehicle vehicle = vehicleRepository.getReferenceById(id);
        return convert(vehicle, VehicleDTO.class);
    }

    // Registrar vehículo con validaciones
    public Long create(VehicleDTO vehicleDTO) {

        // Verificar que no exista
        if (vehicleRepository.existsByLicensePlate(vehicleDTO.getLicensePlate())) {
            throw new IllegalStateException("La placa " + vehicleDTO.getLicensePlate() + " ya está registrada");
        }

        // Validar que la categoría exista
        VehicleCategory category = categoryRepository.findById(vehicleDTO.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Crear y guardar
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(vehicleDTO.getLicensePlate().toUpperCase()); // Normalizar
        vehicle.setColor(vehicleDTO.getColor());
        vehicle.setCategory(category);
        vehicle.setOwner(vehicleDTO.getOwner() != null ? convert(vehicleDTO.getOwner(), User.class) : null);

        vehicleRepository.save(vehicle);
        return vehicle.getId();
    }

    public VehicleDTO getVehicleByIdAndOwner_Id(Long id, Long ownerId) {
        Vehicle vehicle = vehicleRepository.findByIdAndOwner_Id(id, ownerId)
                .orElse(null);
        return convert(vehicle, VehicleDTO.class);
    }

    // Vincular vehículo a usuario
    public VehicleDTO assignOwner(String licensePlate, Long ownerId) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new IllegalStateException("Vehículo no encontrado"));

        // Validar que no tenga dueño
        if (vehicle.getOwner() != null) {
            throw new IllegalStateException("El vehículo ya tiene dueño");
        }

        // Asignar dueño
        User owner = userRepository.findById(ownerId).orElseThrow();
        vehicle.setOwner(owner);

        return convert(vehicleRepository.save(vehicle), VehicleDTO.class);
    }

    // Actualizar vehículo con validaciones
    public boolean update(VehicleDTO vehicleDTO) {
        try {
            Vehicle vehicle = vehicleRepository.findById(vehicleDTO.getId())
                    .orElseThrow(() -> new IllegalStateException("Vehículo no encontrado"));

            // Solo actualizar campos permitidos
            if (vehicleDTO.getColor() != null && !vehicleDTO.getColor().isBlank()) {
                vehicle.setColor(vehicleDTO.getColor());
            }

            if (vehicleDTO.getCategory() != null) {
                vehicle.setCategory(convert(vehicleDTO.getCategory(), VehicleCategory.class));
            }

            vehicleRepository.save(vehicle);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Eliminar con validaciones
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Vehículo no encontrado"));

        // Validar que no tenga tickets activos
        // if (ticketRepository.existsByVehicle_IdAndExitTimeIsNull(id)) {
        // throw new IllegalStateException("No se puede eliminar, tiene tickets
        // activos");
        // }

        vehicleRepository.delete(vehicle);
    }

    // Buscar vehículo por placa
    public VehicleDTO findByLicensePlate(String licensePlate) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate).orElse(null);
        return convert(vehicle, VehicleDTO.class);
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
    public List<VehicleDTO> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return vehicles.stream()
                .map(vehicle -> convert(vehicle, VehicleDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el ID del propietario del vehículo de forma segura sin cargar
     * la entidad User completa en la sesión actual de Hibernate.
     * 
     * IMPORTANTE: Este método evita lazy loading que podría causar conflictos
     * de identidad: "Identifier of an instance of 'User' was altered from X to Y"
     */
    public Long getVehicleOwnerId(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle != null && vehicle.getOwner() != null) {
            // Acceder solo al ID, sin materializar toda la entidad User
            return vehicle.getOwner().getId();
        }
        return null;
    }

    /**
     * Obtiene el nombre de usuario del propietario del vehículo sin cargar
     * la entidad User completa en la sesión actual de Hibernate.
     * 
     * IMPORTANTE: Este método evita lazy loading conflictivo.
     */
    public String getVehicleOwnerUsername(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle != null && vehicle.getOwner() != null) {
            return vehicle.getOwner().getUsername();
        }
        return null;
    }
}