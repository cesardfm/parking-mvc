package com.g3.parking.service;

import com.g3.parking.model.Parking;
import com.g3.parking.model.User;
import com.g3.parking.repository.ParkingRepository;
import com.g3.parking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParkingService {
    
    @Autowired
    private ParkingRepository parkingRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<Parking> findAll() {
        return parkingRepository.findAll();
    }

    public Parking findById(Long parkingId){
        return parkingRepository.findById(parkingId).orElse(null);
    }
    
    // Buscar parking y validar que pertenece a la organización del usuario
    public Parking findByIdAndValidateOrganization(Long parkingId, User currentUser) {
        Parking parking = parkingRepository.findById(parkingId)
            .orElseThrow(() -> new RuntimeException("Parqueadero no encontrado"));
        
        // Validar organización
        if (!currentUser.belongsToOrganization(parking.getOrganization().getId())) {
            throw new SecurityException("No tienes acceso a este parqueadero");
        }
        
        return parking;
    }
    
    // Listar solo parkings de la organización del usuario
    public List<Parking> findByUserOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new IllegalStateException("El usuario no pertenece a ninguna organización");
        }
        return parkingRepository.findByOrganization(user.getOrganization());
    }
    
    // Crear parking con validaciones
    public Parking createForUser(Parking parking, User creator) {
        if (creator.getOrganization() == null) {
            throw new IllegalStateException("El usuario no pertenece a ninguna organización");
        }
        
        parking.setOrganization(creator.getOrganization());
        parking.setCreatedBy(creator);
        
        return parkingRepository.save(parking);
    }
    
    // Actualizar solo si tiene permisos
    public Parking updateIfAuthorized(Long parkingId, Parking updatedData, User currentUser) {
        Parking parking = findByIdAndValidateOrganization(parkingId, currentUser);
        
        parking.setName(updatedData.getName());
        parking.setAddress(updatedData.getAddress());
        parking.setLat(updatedData.getLat());
        parking.setLng(updatedData.getLng());
        
        return parkingRepository.save(parking);
    }
    
    // Eliminar solo si es OWNER de la organización
    public void deleteIfAuthorized(Long parkingId, User currentUser) {
        Parking parking = findByIdAndValidateOrganization(parkingId, currentUser);
        
        parkingRepository.deleteById(parkingId);
    }
    
    // Asignar admin a parking
    @Transactional
    public void assignAdmin(Long parkingId, Long adminId, User currentUser) {
        // Validar que el parking pertenece a la organización del OWNER
        Parking parking = findByIdAndValidateOrganization(parkingId, currentUser);
        
        // Validar que solo OWNER puede asignar
        if (!currentUser.hasRole("ROLE_OWNER")) {
            throw new SecurityException("Solo los OWNER pueden asignar administradores");
        }
        
        // Obtener el admin
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar que el admin pertenece a la misma organización
        if (!admin.belongsToOrganization(currentUser.getOrganization().getId())) {
            throw new SecurityException("El usuario no pertenece a tu organización");
        }
        
        // Validar que tiene rol ADMIN
        if (!admin.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("El usuario no tiene rol de ADMIN");
        }
        
        // Validar que no esté ya asignado
        if (parking.hasAdmin(admin)) {
            throw new RuntimeException("Este administrador ya está asignado a este parqueadero");
        }
        
        // Asignar
        parking.addAdmin(admin);
        parkingRepository.save(parking);
    }
    
    // Remover admin de parking
    @Transactional
    public void removeAdmin(Long parkingId, Long adminId, User currentUser) {
        // Validar que el parking pertenece a la organización del OWNER
        Parking parking = findByIdAndValidateOrganization(parkingId, currentUser);
        
        // Validar que solo OWNER puede remover
        if (!currentUser.hasRole("ROLE_OWNER")) {
            throw new SecurityException("Solo los OWNER pueden remover administradores");
        }
        
        // Obtener el admin
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar que está asignado
        if (!parking.hasAdmin(admin)) {
            throw new RuntimeException("Este administrador no está asignado a este parqueadero");
        }
        
        // Remover
        parking.removeAdmin(admin);
        parkingRepository.save(parking);
    }

    // Obtener parkings que administra un usuario (admin)
    public List<Parking> findByAdmin(User admin) {
        if (admin == null) {
            throw new IllegalArgumentException("Admin no puede ser null");
        }
        return parkingRepository.findByAdminsContaining(admin);
    }

    public List<Parking> findByAdminId(Long adminId) {
        if (adminId == null) throw new IllegalArgumentException("adminId no puede ser null");
        return parkingRepository.findByAdmins_Id(adminId);
    }
}
