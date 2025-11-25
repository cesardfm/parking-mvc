package com.g3.parking.imageprocessing.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "placas_vehiculos")
public class PlacaVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "placa_texto")
    private String placaTexto;

    @Lob
    @Column(name = "imagen_original", columnDefinition = "LONGBLOB")
    private byte[] imagenOriginal;  // ÚNICA imagen que se guarda en BD

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "estado_procesamiento")
    private String estadoProcesamiento;

    @Column(name = "tiempo_procesamiento_ms")
    private Long tiempoProcesamiento;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (estadoProcesamiento == null) {
            estadoProcesamiento = "PENDIENTE";
        }
    }
}
