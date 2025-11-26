package com.g3.parking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g3.parking.model.VehicleCategory;


public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {
    VehicleCategory findByName(String name);
    List<VehicleCategory> findByActiveTrue();
    VehicleCategory findByNameAndActiveTrue(String name);

} 
