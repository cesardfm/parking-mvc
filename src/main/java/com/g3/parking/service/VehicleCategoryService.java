package com.g3.parking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.model.VehicleCategory;
import com.g3.parking.repository.VehicleCategoryRepository;

@Service
public class VehicleCategoryService {
    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepo;

    public List<VehicleCategory> getAll(){
        return vehicleCategoryRepo.findAll();
    }
}
