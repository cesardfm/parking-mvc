package com.g3.parking.repository;

import com.g3.parking.model.Organization;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class OrganizationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // Obtener todos
    public List<Organization> findAll() {
        String sql = "SELECT * FROM organizations";
        return entityManager
                .createNativeQuery(sql, Organization.class)
                .getResultList();
    }

    // Buscar por ID (sin Optional)
    public Organization findById(Long id) {
        String sql = "SELECT * FROM organizations WHERE id = :id";

        List<Organization> result = entityManager
                .createNativeQuery(sql, Organization.class)
                .setParameter("id", id)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Buscar por nombre (sin Optional)
    public Organization findByName(String name) {
        String sql = "SELECT * FROM organizations WHERE name = :name";

        List<Organization> result = entityManager
                .createNativeQuery(sql, Organization.class)
                .setParameter("name", name)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Buscar por TaxId / NIT (sin Optional)
    public Organization findByTaxId(String taxId) {
        String sql = "SELECT * FROM organizations WHERE tax_id = :taxId";

        List<Organization> result = entityManager
                .createNativeQuery(sql, Organization.class)
                .setParameter("taxId", taxId)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    // Verificar si existe por nombre
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM organizations WHERE name = :name";

        Number count = (Number) entityManager
                .createNativeQuery(sql)
                .setParameter("name", name)
                .getSingleResult();

        return count.intValue() > 0;
    }

    // Verificar si existe por TaxId
    public boolean existsByTaxId(String taxId) {
        String sql = "SELECT COUNT(*) FROM organizations WHERE tax_id = :taxId";

        Number count = (Number) entityManager
                .createNativeQuery(sql)
                .setParameter("taxId", taxId)
                .getSingleResult();

        return count.intValue() > 0;
    }

    // Guardar organización
    public Organization save(Organization organization) {
        if (organization.getId() == null) {
            entityManager.persist(organization);
            return organization;
        } else {
            return entityManager.merge(organization);
        }
    }

    // Eliminar por id
    public void deleteById(Long id) {
        String sql = "DELETE FROM organizations WHERE id = :id";

        entityManager
                .createNativeQuery(sql)
                .setParameter("id", id)
                .executeUpdate();
    }
}
