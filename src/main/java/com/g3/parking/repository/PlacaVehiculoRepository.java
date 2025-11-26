package com.g3.parking.repository;

import com.g3.parking.model.PlacaVehiculo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class PlacaVehiculoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Buscar por ID (sin Optional)
    public PlacaVehiculo findById(Long id) {
        String sql = "SELECT * FROM placa_vehiculo WHERE id = :id";

        List<PlacaVehiculo> result = entityManager
                .createNativeQuery(sql, PlacaVehiculo.class)
                .setParameter("id", id)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Guardar o actualizar
    public PlacaVehiculo save(PlacaVehiculo placa) {
        if (placa.getId() == null) {
            entityManager.persist(placa);
            return placa;
        } else {
            return entityManager.merge(placa);
        }
    }

    // Buscar por placaTexto
    public List<PlacaVehiculo> findByPlacaTexto(String placaTexto) {
        String sql = "SELECT * FROM placa_vehiculo WHERE placa_texto = :texto";

        return entityManager
                .createNativeQuery(sql, PlacaVehiculo.class)
                .setParameter("texto", placaTexto)
                .getResultList();
    }

    // Buscar por estadoProcesamiento
    public List<PlacaVehiculo> findByEstadoProcesamiento(String estado) {
        String sql = "SELECT * FROM placa_vehiculo WHERE estado_procesamiento = :estado";

        return entityManager
                .createNativeQuery(sql, PlacaVehiculo.class)
                .setParameter("estado", estado)
                .getResultList();
    }

    // Buscar por rango de fecha
    public List<PlacaVehiculo> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin) {
        String sql =
                "SELECT * FROM placa_vehiculo " +
                "WHERE fecha_registro BETWEEN :inicio AND :fin";

        return entityManager
                .createNativeQuery(sql, PlacaVehiculo.class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();
    }

    // Top 10 ordenado por fechaRegistro DESC
    public List<PlacaVehiculo> findTop10ByOrderByFechaRegistroDesc() {
        String sql =
                "SELECT * FROM placa_vehiculo " +
                "ORDER BY fecha_registro DESC " +
                "LIMIT 10";

        return entityManager
                .createNativeQuery(sql, PlacaVehiculo.class)
                .getResultList();
    }

    // Obtener todos
    public List<PlacaVehiculo> findAll() {
        String sql = "SELECT * FROM placa_vehiculo";
        return entityManager
                .createNativeQuery(sql, PlacaVehiculo.class)
                .getResultList();
    }

    // Eliminar por ID
    public void deleteById(Long id) {
        String sql = "DELETE FROM placa_vehiculo WHERE id = :id";
        entityManager
                .createNativeQuery(sql)
                .setParameter("id", id)
                .executeUpdate();
    }
}
