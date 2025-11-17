package com.g3.parking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.PlanDTO;
import com.g3.parking.model.Plan;
import com.g3.parking.repository.PlanRepository;

@Service
public class PlanService extends BaseService {
    @Autowired
    private PlanRepository planRepo;

    public List<PlanDTO> findAll() {
        List<Plan> planes = planRepo.findAll();
        return planes.stream()
                .map(plan -> convert(plan, PlanDTO.class))
                .collect(Collectors.toList());
    }

    public PlanDTO findById(Long id) {
        try {
            Plan plan = planRepo.getReferenceById(id);
            return convert(plan, PlanDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean changeStatus(Long id) {
        try {
            Plan plan = planRepo.getReferenceById(id);
            plan.setActive(!plan.getActive());
            planRepo.save(plan);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long create(PlanDTO planDTO) {
        try {
            Plan plan = convert(planDTO, Plan.class);
            planRepo.save(plan);
            return plan.getId();
        } catch (Exception e) {
            return 0L;
        }
    }

    public boolean update(PlanDTO planDTO) {
        try {
            Plan plan = planRepo.getReferenceById(planDTO.getId());
            plan.setName(planDTO.getName());
            plan.setDescription(planDTO.getDescription());
            plan.setPrice(planDTO.getPrice());
            plan.setDiscountPercent(planDTO.getDiscountPercent().divide(BigDecimal.valueOf(100)));
            planRepo.save(plan);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
