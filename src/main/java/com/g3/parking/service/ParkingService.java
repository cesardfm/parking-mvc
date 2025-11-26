package com.g3.parking.service;

import com.g3.parking.datatransfer.ParkingDTO;
import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.model.Organization;
import com.g3.parking.model.Parking;
import com.g3.parking.model.User;
import com.g3.parking.repository.ParkingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingService extends BaseService {

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private UserService userService;

    public List<ParkingDTO> findAll() {
        List<Parking> parkings = parkingRepository.findAll();
        return parkings.stream()
                .map(parking -> convert(parkings, ParkingDTO.class))
                .collect(Collectors.toList());
    }

    public ParkingDTO findById(Long parkingId) {
        return convert(parkingRepository.findById(parkingId), ParkingDTO.class);
    }

    // Buscar parking y validar que pertenece a la organización del usuario
    public ParkingDTO findByIdAndValidateOrganization(Long parkingId, UserDTO currentUserDTO) {
        Parking parking = parkingRepository.findById(parkingId);

        User currentUser = convert(currentUserDTO, User.class);

        // Validar organización
        if (!currentUser.belongsToOrganization(parking.getOrganization().getId())) {
            throw new SecurityException("No tienes acceso a este parqueadero");
        }

        return convert(parking, ParkingDTO.class);
    }

    // Listar solo parkings de la organización del usuario
    public List<ParkingDTO> findByUserOrganization(UserDTO user) {
        if (user.getOrganization() == null) {
            throw new IllegalStateException("El usuario no pertenece a ninguna organización");
        }
        List<Parking> parkings = parkingRepository
                .findByOrganization(convert(user.getOrganization(), Organization.class));
        return parkings.stream()
                .map(parking -> convert(parking, ParkingDTO.class))
                .collect(Collectors.toList());
    }

    // Crear parking con validaciones
    public ParkingDTO createForUser(ParkingDTO parking, UserDTO creator) {
        if (creator.getOrganization() == null) {
            throw new IllegalStateException("El usuario no pertenece a ninguna organización");
        }

        parking.setOrganization(creator.getOrganization());
        parking.setCreatedBy(creator);

        parkingRepository.save(convert(parking, Parking.class));
        return parking;
    }

    // Actualizar solo si tiene permisos
    public ParkingDTO updateIfAuthorized(Long parkingId, Parking updatedData, UserDTO currentUser) {
        ParkingDTO parking = findByIdAndValidateOrganization(parkingId, currentUser);

        parking.setName(updatedData.getName());
        parking.setAddress(updatedData.getAddress());
        parking.setLat(updatedData.getLat());
        parking.setLng(updatedData.getLng());

        parkingRepository.save(convert(parking, Parking.class));
        return parking;
    }

    // Eliminar solo si es OWNER de la organización
    public void deleteIfAuthorized(Long parkingId, UserDTO currentUser) {
        ParkingDTO parking = findByIdAndValidateOrganization(parkingId, currentUser);

        parkingRepository.deleteById(parkingId);
    }

    // Asignar admin a parking
    @Transactional
    public void assignAdmin(Long parkingId, Long adminId, UserDTO currentUserDTO) {
        // Validar que el parking pertenece a la organización del OWNER
        Parking parking = convert(findByIdAndValidateOrganization(parkingId, currentUserDTO), Parking.class);

        User currentUser = convert(currentUserDTO, User.class);

        // Validar que solo OWNER puede asignar
        if (!currentUser.hasRole("ROLE_OWNER")) {
            throw new SecurityException("Solo los OWNER pueden asignar administradores");
        }

        // Obtener el admin
        User admin = convert(userService.findById(adminId), User.class);

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
    public void removeAdmin(Long parkingId, Long adminId, UserDTO currentUserDTO) {
        // Validar que el parking pertenece a la organización del OWNER
        Parking parking = convert(findByIdAndValidateOrganization(parkingId, currentUserDTO), Parking.class);

        User currentUser = convert(currentUserDTO, User.class);

        // Validar que solo OWNER puede remover
        if (!currentUser.hasRole("ROLE_OWNER")) {
            throw new SecurityException("Solo los OWNER pueden remover administradores");
        }

        // Obtener el admin
        User admin = convert(userService.findById(adminId), User.class);

        // Validar que está asignado
        if (!parking.hasAdmin(admin)) {
            throw new RuntimeException("Este administrador no está asignado a este parqueadero");
        }

        // Remover
        parking.removeAdmin(admin);
        parkingRepository.save(parking);
    }

    // Obtener parkings que administra un usuario (admin)
    public List<ParkingDTO> findByAdmin(UserDTO admin) {
        if (admin == null) {
            throw new IllegalArgumentException("Admin no puede ser null");
        }
        User currentUser = convert(admin, User.class);
        List<Parking> parkings = parkingRepository.findByAdminsContaining(currentUser);

        return parkings.stream()
                .map(parking -> convert(parkings, ParkingDTO.class))
                .collect(Collectors.toList());
    }

    public List<Parking> findByAdminId(Long adminId) {
        if (adminId == null) throw new IllegalArgumentException("adminId no puede ser null");
        return parkingRepository.findByAdmins_Id(adminId);
    }
}
