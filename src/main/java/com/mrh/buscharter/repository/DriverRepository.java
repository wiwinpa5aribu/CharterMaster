package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.Driver;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Driver.
 */
public class DriverRepository extends BaseRepository<Driver> {

    public DriverRepository() {
        super(Driver.class);
    }

    @Override
    protected Long getEntityId(Driver entity) {
        return entity.getId();
    }

    /**
     * Cari driver berdasarkan nomor telepon dan tenant.
     */
    public Optional<Driver> findByNomorTeleponAndTenantId(String nomorTelepon, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Driver> query = em.createQuery(
                "SELECT d FROM Driver d WHERE d.nomorTelepon = :nomorTelepon AND d.tenant.id = :tenantId", 
                Driver.class);
            query.setParameter("nomorTelepon", nomorTelepon);
            query.setParameter("tenantId", tenantId);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari semua driver aktif berdasarkan tenant.
     */
    public List<Driver> findAktifByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Driver> query = em.createQuery(
                "SELECT d FROM Driver d WHERE d.tenant.id = :tenantId AND d.status = 'AKTIF' " +
                "ORDER BY d.namaLengkap", 
                Driver.class);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari driver yang tersedia pada rentang tanggal tertentu.
     * Driver dianggap tidak tersedia jika sudah di-assign ke trip yang overlap.
     */
    public List<Driver> findDriverTersedia(Long tenantId, LocalDateTime tanggalMulai, 
                                            LocalDateTime tanggalSelesai) {
        return executeWithEntityManager(em -> {
            String jpql = "SELECT d FROM Driver d " +
                "WHERE d.tenant.id = :tenantId " +
                "AND d.status = 'AKTIF' " +
                "AND d.id NOT IN (" +
                "  SELECT ta.driver.id FROM TripAssignment ta " +
                "  JOIN ta.trip t " +
                "  JOIN t.booking b " +
                "  WHERE b.tenant.id = :tenantId " +
                "  AND b.status IN ('DP_DITERIMA', 'LUNAS', 'SELESAI') " +
                "  AND ta.statusAssignment != 'BATAL' " +
                "  AND ta.driver IS NOT NULL " +
                "  AND t.waktuMulai <= :tanggalSelesai " +
                "  AND t.waktuSelesai >= :tanggalMulai " +
                ") " +
                "ORDER BY d.namaLengkap";
            
            TypedQuery<Driver> query = em.createQuery(jpql, Driver.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("tanggalMulai", tanggalMulai);
            query.setParameter("tanggalSelesai", tanggalSelesai);
            return query.getResultList();
        });
    }
}
