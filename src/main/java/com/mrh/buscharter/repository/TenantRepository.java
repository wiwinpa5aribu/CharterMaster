package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.Tenant;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

/**
 * Repository untuk entity Tenant.
 */
public class TenantRepository extends BaseRepository<Tenant> {

    public TenantRepository() {
        super(Tenant.class);
    }

    @Override
    protected Long getEntityId(Tenant entity) {
        return entity.getId();
    }

    /**
     * Cari tenant berdasarkan kode.
     */
    public Optional<Tenant> findByKode(String kode) {
        return executeWithEntityManager(em -> {
            TypedQuery<Tenant> query = em.createQuery(
                "SELECT t FROM Tenant t WHERE t.kode = :kode", Tenant.class);
            query.setParameter("kode", kode);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cek apakah kode tenant sudah digunakan.
     */
    public boolean existsByKode(String kode) {
        return findByKode(kode).isPresent();
    }
}
