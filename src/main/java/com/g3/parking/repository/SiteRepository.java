package com.g3.parking.repository;

import com.g3.parking.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    
    // Buscar por posición específica
    Optional<Site> findByPosXAndPosY(Integer posX, Integer posY);
    
    // Buscar por status
    List<Site> findByStatus(String status);

    List<Site> findByStatusAndLevel_Parking_Id(String status, Long parkingId);
    
    // Buscar sitios por nivel
    List<Site> findByLevelId(Long levelId);
}