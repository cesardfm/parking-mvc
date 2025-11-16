package com.g3.parking.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.model.VehicleCategory;
import com.g3.parking.repository.VehicleCategoryRepository;

@Service
public class VehicleCategoryService {
    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepo;

    public List<VehicleCategory> getAll() {
        return vehicleCategoryRepo.findAll();
    }

    public List<VehicleCategory> getAllActive() {
        return vehicleCategoryRepo.findByActiveTrue();
    }

    public VehicleCategory findById(Long id) {
        return vehicleCategoryRepo.getReferenceById(id);
    }

    public VehicleCategory create(String name, BigDecimal ratePerHour) {
        VehicleCategory categoryAlreadyExist = vehicleCategoryRepo.findByName(name);
        if (categoryAlreadyExist != null) {
            return null;
        }
        VehicleCategory category = new VehicleCategory();
        category.setName(name);
        category.setRatePerHour(ratePerHour);
        return vehicleCategoryRepo.save(category);
    }

    public VehicleCategory upgrade(Long id, String name, BigDecimal ratePerHour) {
        VehicleCategory category = vehicleCategoryRepo.getReferenceById(id);
        if (category == null) {
            return null;
        }
        category.setName(name);
        category.setRatePerHour(ratePerHour);
        return vehicleCategoryRepo.save(category);
    }

    public VehicleCategory changeState(Long id) {
        VehicleCategory category = findById(id);
        if (category != null) {
            category.setActive(!category.isActive());
            vehicleCategoryRepo.save(category);
        }

        return category;
    }
}
