package com.g3.parking.repository;

import com.g3.parking.model.Plan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class PlanRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Buscar por ID (sin Optional)
    public Plan findById(Long id) {
        String sql = "SELECT * FROM plan WHERE id = :id";

        List<Plan> result = entityManager
                .createNativeQuery(sql, Plan.class)
                .setParameter("id", id)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Guardar o actualizar
    public Plan save(Plan plan) {
        if (plan.getId() == null) {
            entityManager.persist(plan);
            return plan;
        } else {
            return entityManager.merge(plan);
        }
    }

    // Obtener todos
    public List<Plan> findAll() {
        String sql = "SELECT * FROM plan";

        return entityManager
                .createNativeQuery(sql, Plan.class)
                .getResultList();
    }

    // Buscar los activos: plan.active = true
    public List<Plan> findByActiveTrue() {
        String sql = "SELECT * FROM plan WHERE active = true";

        return entityManager
                .createNativeQuery(sql, Plan.class)
                .getResultList();
    }

    // Eliminar por ID
    public void deleteById(Long id) {
        String sql = "DELETE FROM plan WHERE id = :id";

        entityManager
                .createNativeQuery(sql)
                .setParameter("id", id)
                .executeUpdate();
    }
}
