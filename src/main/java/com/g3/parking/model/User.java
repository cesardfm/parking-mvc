package com.g3.parking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    
    private String password;

    // Un usuario pertenece a UNA organización
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;

    // Un usuario tiene múltiples roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    //@OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = {})
    //private Set<Vehicle> vehicles = new HashSet<>();
    
    private Boolean active = true;
    
    // Métodos helper para verificar roles
    public boolean hasRole(String roleName) {
        return roles.stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    // Verificar si pertenece a una organización específica
    public boolean belongsToOrganization(Long organizationId) {
        return this.organization != null && 
               this.organization.getId().equals(organizationId);
    }
}
