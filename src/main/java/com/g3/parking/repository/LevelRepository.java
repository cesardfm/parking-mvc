package com.g3.parking.repository;

import com.g3.parking.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    
    // Buscar por parking
    List<Level> findByParkingId(Long parkingId);
}
