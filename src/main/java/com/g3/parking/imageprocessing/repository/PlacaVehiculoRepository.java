package com.g3.parking.imageprocessing.repository;

import com.g3.parking.imageprocessing.model.PlacaVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlacaVehiculoRepository extends JpaRepository<PlacaVehiculo, Long> {
    
    List<PlacaVehiculo> findByPlacaTexto(String placaTexto);
    
    List<PlacaVehiculo> findByEstadoProcesamiento(String estado);
    
    List<PlacaVehiculo> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);
    
    List<PlacaVehiculo> findTop10ByOrderByFechaRegistroDesc();
}
