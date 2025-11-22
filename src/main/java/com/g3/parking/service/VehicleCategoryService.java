package com.g3.parking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.VehicleCategoryDTO;
import com.g3.parking.model.VehicleCategory;
import com.g3.parking.repository.VehicleCategoryRepository;

@Service
public class VehicleCategoryService extends BaseService {
    @Autowired
    private VehicleCategoryRepository vehicleCategoryRepo;

    public List<VehicleCategoryDTO> getAll() {
        List<VehicleCategory> categories = vehicleCategoryRepo.findAll();
        return categories.stream()
                .map(category -> convert(category, VehicleCategoryDTO.class))
                .collect(Collectors.toList());
    }

    public List<VehicleCategoryDTO> getAllActive() {
        List<VehicleCategory> categories = vehicleCategoryRepo.findByActiveTrue();
        return categories.stream()
                .map(category -> convert(category, VehicleCategoryDTO.class))
                .collect(Collectors.toList());
    }

    public VehicleCategoryDTO findById(Long id) {
        VehicleCategory category = vehicleCategoryRepo.getReferenceById(id);
        return convert(category, VehicleCategoryDTO.class);
    }

    public VehicleCategoryDTO create(String name, BigDecimal ratePerHour) {
        VehicleCategory categoryAlreadyExist = vehicleCategoryRepo.findByName(name);
        if (categoryAlreadyExist != null) {
            return null;
        }
        VehicleCategory category = new VehicleCategory();
        category.setName(name);
        category.setRatePerHour(ratePerHour);
        return convert(vehicleCategoryRepo.save(category), VehicleCategoryDTO.class);
    }

    public VehicleCategoryDTO upgrade(Long id, String name, BigDecimal ratePerHour) {
        VehicleCategory category = vehicleCategoryRepo.getReferenceById(id);
        if (category == null) {
            return null;
        }
        category.setName(name);
        category.setRatePerHour(ratePerHour);
        return convert(vehicleCategoryRepo.save(category), VehicleCategoryDTO.class);
    }

    public boolean changeStatus(Long id) {
        try {
            VehicleCategoryDTO category = findById(id);
            if (category != null) {
                category.setActive(!category.isActive());
                vehicleCategoryRepo.save(convert(category, VehicleCategory.class));
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
