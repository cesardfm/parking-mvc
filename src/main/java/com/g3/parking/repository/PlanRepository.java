package com.g3.parking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.g3.parking.model.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByActiveTrue();
}
