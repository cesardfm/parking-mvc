package com.g3.parking.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "levels")
public class  Level {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "columns")
    private Integer columns;
    
    @Column(name = "rows")
    private Integer rows;
    
    @ManyToOne
    @JoinColumn(name = "parking_id")
    private Parking parking;
    
    @OneToMany(mappedBy = "level")
    private List<Site> sites = new ArrayList<>();
    
    // Constructores
    public Level() {
    }
    
    public Level(Integer columns, Integer rows) {
        this.columns = columns;
        this.rows = rows;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getColumns() {
        return columns;
    }
    
    public void setColumns(Integer columns) {
        this.columns = columns;
    }
    
    public Integer getRows() {
        return rows;
    }
    
    public void setRows(Integer rows) {
        this.rows = rows;
    }
    
    public Parking getParking() {
        return parking;
    }
    
    public void setParking(Parking parking) {
        this.parking = parking;
    }
    
    public List<Site> getSites() {
        return sites;
    }
    
    public void setSites(List<Site> sites) {
        this.sites = sites;
    }
}
