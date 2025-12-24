package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.User;
import com.mrh.buscharter.model.enums.RoleUser;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity User.
 */
public class UserRepository extends BaseRepository<User> {

    public UserRepository() {
        super(User.class);
    }

    @Override
    protected Long getEntityId(User entity) {
        return entity.getId();
    }

    /**
     * Cari user berdasarkan email dan tenant.
     */
    public Optional<User> findByEmailAndTenantId(String email, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email AND u.tenant.id = :tenantId", 
                User.class);
            query.setParameter("email", email);
            query.setParameter("tenantId", tenantId);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari user aktif berdasarkan email dan kode tenant.
     * Digunakan untuk login.
     */
    public Optional<User> findAktifByEmailAndKodeTenant(String email, String kodeTenant) {
        return executeWithEntityManager(em -> {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.email = :email " +
                "AND u.tenant.kode = :kodeTenant AND u.aktif = true", 
                User.class);
            query.setParameter("email", email);
            query.setParameter("kodeTenant", kodeTenant);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari semua user berdasarkan role dan tenant.
     */
    public List<User> findByRoleAndTenantId(RoleUser role, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role AND u.tenant.id = :tenantId", 
                User.class);
            query.setParameter("role", role);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari semua user aktif berdasarkan tenant.
     */
    public List<User> findAktifByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.aktif = true", 
                User.class);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }
}
