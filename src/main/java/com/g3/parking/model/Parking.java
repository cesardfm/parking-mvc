package com.g3.parking.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "parkings")
public class Parking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "latitude")
    private Double lat;
    
    @Column(name = "longitude")
    private Double lng;
    
    @Column(name = "address")
    private String address;
    
    // Un parqueadero pertenece a UNA organización
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    // Usuario que creó el parqueadero (dentro de la organización)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    // Administradores asignados a este parqueadero
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "parking_admins",
        joinColumns = @JoinColumn(name = "parking_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();
    
    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL)
    private List<Level> levels = new ArrayList<>();
    
    // Constructores
    public Parking() {
    }
    
    public Parking(String name, Organization organization, User createdBy) {
        this.name = name;
        this.organization = organization;
        this.createdBy = createdBy;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Double getLat() {
        return lat;
    }
    
    public void setLat(Double lat) {
        this.lat = lat;
    }
    
    public Double getLng() {
        return lng;
    }
    
    public void setLng(Double lng) {
        this.lng = lng;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Organization getOrganization() {
        return organization;
    }
    
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<Level> getLevels() {
        return levels;
    }
    
    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }
    
    public Set<User> getAdmins() {
        return admins;
    }
    
    public void setAdmins(Set<User> admins) {
        this.admins = admins;
    }
    
    // Métodos helper para gestionar admins
    public void addAdmin(User user) {
        this.admins.add(user);
    }
    
    public void removeAdmin(User user) {
        this.admins.remove(user);
    }
    
    public boolean hasAdmin(User user) {
        return this.admins.contains(user);
    }
    
    public boolean isAdminById(Long userId) {
        return this.admins.stream()
            .anyMatch(admin -> admin.getId().equals(userId));
    }
}
