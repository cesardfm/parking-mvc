package com.g3.parking.repository;

import com.g3.parking.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    Optional<Organization> findByName(String name);
    
    Optional<Organization> findByTaxId(String taxId);
    
    boolean existsByName(String name);
    
    boolean existsByTaxId(String taxId);
}
