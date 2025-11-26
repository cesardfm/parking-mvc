package com.g3.parking.repository;

import com.g3.parking.model.Level;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class LevelRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Obtener todos los levels
    public List<Level> findAll() {
        String sql = "SELECT * FROM levels";
        return entityManager
                .createNativeQuery(sql, Level.class)
                .getResultList();
    }

    // Buscar un level por id SIN Optional
    public Level findById(Long id) {
        String sql = "SELECT * FROM levels WHERE id = :id";
        List<Level> results = entityManager
                .createNativeQuery(sql, Level.class)
                .setParameter("id", id)
                .getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    // Buscar por parking_id SIN Optional
    public List<Level> findByParkingId(Long parkingId) {
        String sql = "SELECT * FROM levels WHERE parking_id = :parkingId";
        return entityManager
                .createNativeQuery(sql, Level.class)
                .setParameter("parkingId", parkingId)
                .getResultList();
    }

    // Guardar un Level
    public Level save(Level level) {
        if (level.getId() == null) {
            entityManager.persist(level);
            return level;
        } else {
            return entityManager.merge(level);
        }
    }

    // Eliminar por id
    public void deleteById(Long id) {
        String sql = "DELETE FROM levels WHERE id = :id";
        entityManager
                .createNativeQuery(sql)
                .setParameter("id", id)
                .executeUpdate();
    }
}
