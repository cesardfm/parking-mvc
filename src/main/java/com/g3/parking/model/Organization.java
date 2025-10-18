package com.g3.parking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "organizations")
public class Organization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(unique = true)
    private String taxId; // RUC, NIT, etc.
    
    private String address;
    private String phone;
    private String email;
    
    // Una organización tiene muchos usuarios
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private Set<User> users = new HashSet<>();
    
    // Una organización tiene muchos parqueaderos
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Parking> parkings = new ArrayList<>();
    
    private Boolean active = true;
    
    // Métodos helper
    public void addUser(User user) {
        users.add(user);
        user.setOrganization(this);
    }
    
    public void removeUser(User user) {
        users.remove(user);
        user.setOrganization(null);
    }
    
    public void addParking(Parking parking) {
        parkings.add(parking);
        parking.setOrganization(this);
    }
}
