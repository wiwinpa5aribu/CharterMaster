package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.Customer;
import com.mrh.buscharter.model.enums.TipeCustomer;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Customer.
 */
public class CustomerRepository extends BaseRepository<Customer> {

    public CustomerRepository() {
        super(Customer.class);
    }

    @Override
    protected Long getEntityId(Customer entity) {
        return entity.getId();
    }

    /**
     * Cari customer berdasarkan nama dan tenant.
     */
    public List<Customer> findByNamaContainingAndTenantId(String nama, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE LOWER(c.nama) LIKE LOWER(:nama) " +
                "AND c.tenant.id = :tenantId ORDER BY c.nama", 
                Customer.class);
            query.setParameter("nama", "%" + nama + "%");
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari customer berdasarkan telepon dan tenant.
     */
    public Optional<Customer> findByTeleponAndTenantId(String telepon, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.telepon = :telepon AND c.tenant.id = :tenantId", 
                Customer.class);
            query.setParameter("telepon", telepon);
            query.setParameter("tenantId", tenantId);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari customer berdasarkan tipe dan tenant.
     */
    public List<Customer> findByTipeAndTenantId(TipeCustomer tipe, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.tipe = :tipe AND c.tenant.id = :tenantId " +
                "ORDER BY c.nama", 
                Customer.class);
            query.setParameter("tipe", tipe);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari customer dengan booking terbanyak (untuk autocomplete).
     */
    public List<Customer> findTopCustomersByTenantId(Long tenantId, int limit) {
        return executeWithEntityManager(em -> {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c LEFT JOIN c.bookings b " +
                "WHERE c.tenant.id = :tenantId " +
                "GROUP BY c.id ORDER BY COUNT(b) DESC", 
                Customer.class);
            query.setParameter("tenantId", tenantId);
            query.setMaxResults(limit);
            return query.getResultList();
        });
    }
}
