package com.g3.parking.repository;

import com.g3.parking.model.Organization;
import com.g3.parking.model.Parking;
import com.g3.parking.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ParkingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Obtener por ID (sin Optional)
    public Parking findById(Long id) {
        String sql = "SELECT * FROM parking WHERE id = :id";
        List<Parking> result = entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("id", id)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Guardar o actualizar
    public Parking save(Parking parking) {
        if (parking.getId() == null) {
            entityManager.persist(parking);
            return parking;
        } else {
            return entityManager.merge(parking);
        }
    }

    // Buscar por nombre exacto (sin Optional)
    public Parking findByName(String name) {
        String sql = "SELECT * FROM parking WHERE LOWER(name) = LOWER(:name)";

        List<Parking> result = entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("name", name)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Buscar por nombre parcial (ignore case)
    public List<Parking> findByNameContainingIgnoreCase(String name) {
        String sql = "SELECT * FROM parking WHERE LOWER(name) LIKE LOWER(:name)";
        return entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    // Buscar por organización
    public List<Parking> findByOrganization(Organization organization) {
        String sql = "SELECT * FROM parking WHERE organization_id = :orgId";

        return entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("orgId", organization.getId())
                .getResultList();
    }

    // Buscar por id y organización (sin Optional)
    public Parking findByIdAndOrganization(Long parkingId, Organization organization) {
        String sql = "SELECT * FROM parking WHERE id = :pid AND organization_id = :oid";

        List<Parking> result = entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("pid", parkingId)
                .setParameter("oid", organization.getId())
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Buscar por id y organization_id (versión con IDs, sin Optional)
    public Parking findByIdAndOrganizationId(Long parkingId, Long orgId) {
        String sql = "SELECT * FROM parking WHERE id = :pid AND organization_id = :oid";

        List<Parking> result = entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("pid", parkingId)
                .setParameter("oid", orgId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Contar parkings de una organización
    public long countByOrganization(Organization organization) {
        String sql = "SELECT COUNT(*) FROM parking WHERE organization_id = :orgId";

        Number count = (Number) entityManager
                .createNativeQuery(sql)
                .setParameter("orgId", organization.getId())
                .getSingleResult();

        return count.longValue();
    }

    // Buscar parkings donde un usuario es admin
    public List<Parking> findByAdminsContaining(User admin) {
        String sql =
                "SELECT p.* FROM parking p " +
                "JOIN parking_admins pa ON p.id = pa.parking_id " +
                "WHERE pa.admin_id = :adminId";

        return entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("adminId", admin.getId())
                .getResultList();
    }

    // Alternativa por id
    public List<Parking> findByAdmins_Id(Long adminId) {
        String sql =
                "SELECT p.* FROM parking p " +
                "JOIN parking_admins pa ON p.id = pa.parking_id " +
                "WHERE pa.admin_id = :adminId";

        return entityManager
                .createNativeQuery(sql, Parking.class)
                .setParameter("adminId", adminId)
                .getResultList();
    }

    // Eliminar por ID
    public void deleteById(Long id) {
        String sql = "DELETE FROM parking WHERE id = :id";

        entityManager
                .createNativeQuery(sql)
                .setParameter("id", id)
                .executeUpdate();
    }

    // Obtener todos
    public List<Parking> findAll() {
        String sql = "SELECT * FROM parking";
        return entityManager
                .createNativeQuery(sql, Parking.class)
                .getResultList();
    }
}
