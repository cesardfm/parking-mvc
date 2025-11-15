package com.g3.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g3.parking.model.VehicleCategory;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
    
} 
