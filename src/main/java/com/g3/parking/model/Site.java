package com.g3.parking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sites")
public class Site {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pos_x")
    private Integer posX;
    
    @Column(name = "pos_y")
    private Integer posY;
    
    @Column(name = "status")
    private String status;
    
    @ManyToOne
    @JoinColumn(name = "level_id")
    private Level level;
    
    // Constructores
    public Site() {
    }
    
    public Site(Integer posX, Integer posY, String status) {
        this.posX = posX;
        this.posY = posY;
        this.status = status;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getPosX() {
        return posX;
    }
    
    public void setPosX(Integer posX) {
        this.posX = posX;
    }
    
    public Integer getPosY() {
        return posY;
    }
    
    public void setPosY(Integer posY) {
        this.posY = posY;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Level getLevel() {
        return level;
    }
    
    public void setLevel(Level level) {
        this.level = level;
    }
}