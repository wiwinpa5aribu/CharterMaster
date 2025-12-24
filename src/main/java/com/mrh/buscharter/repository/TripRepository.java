package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.Trip;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Trip.
 */
public class TripRepository extends BaseRepository<Trip> {

    public TripRepository() {
        super(Trip.class);
    }

    @Override
    protected Long getEntityId(Trip entity) {
        return entity.getId();
    }

    /**
     * Cari trip berdasarkan booking.
     */
    public List<Trip> findByBookingId(Long bookingId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Trip> query = em.createQuery(
                "SELECT t FROM Trip t WHERE t.booking.id = :bookingId " +
                "ORDER BY t.waktuMulai", 
                Trip.class);
            query.setParameter("bookingId", bookingId);
            return query.getResultList();
        });
    }

    /**
     * Cari trip dalam rentang tanggal untuk tenant tertentu.
     */
    public List<Trip> findByTanggalAndTenantId(LocalDateTime mulai, LocalDateTime selesai, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Trip> query = em.createQuery(
                "SELECT t FROM Trip t " +
                "WHERE t.booking.tenant.id = :tenantId " +
                "AND t.waktuMulai <= :selesai " +
                "AND t.waktuSelesai >= :mulai " +
                "ORDER BY t.waktuMulai", 
                Trip.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("mulai", mulai);
            query.setParameter("selesai", selesai);
            return query.getResultList();
        });
    }

    /**
     * Cari trip yang belum di-assign armada.
     */
    public List<Trip> findBelumDiAssignByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Trip> query = em.createQuery(
                "SELECT t FROM Trip t " +
                "LEFT JOIN t.assignments ta " +
                "WHERE t.booking.tenant.id = :tenantId " +
                "AND t.booking.status IN ('DP_DITERIMA', 'LUNAS') " +
                "AND (ta IS NULL OR ta.vehicle IS NULL) " +
                "ORDER BY t.waktuMulai", 
                Trip.class);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari trip dengan eager fetch assignments.
     */
    public Optional<Trip> findByIdWithAssignments(Long id) {
        return executeWithEntityManager(em -> {
            TypedQuery<Trip> query = em.createQuery(
                "SELECT t FROM Trip t " +
                "LEFT JOIN FETCH t.assignments ta " +
                "LEFT JOIN FETCH ta.vehicle " +
                "LEFT JOIN FETCH ta.driver " +
                "LEFT JOIN FETCH ta.coDriver " +
                "WHERE t.id = :id", 
                Trip.class);
            query.setParameter("id", id);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari trip hari ini untuk dispatch board.
     */
    public List<Trip> findTripHariIniByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            LocalDateTime hariIniMulai = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime hariIniSelesai = hariIniMulai.plusDays(1);
            
            TypedQuery<Trip> query = em.createQuery(
                "SELECT t FROM Trip t " +
                "JOIN FETCH t.booking b " +
                "LEFT JOIN FETCH t.assignments ta " +
                "WHERE b.tenant.id = :tenantId " +
                "AND b.status IN ('DP_DITERIMA', 'LUNAS') " +
                "AND t.waktuMulai >= :mulai AND t.waktuMulai < :selesai " +
                "ORDER BY t.waktuMulai", 
                Trip.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("mulai", hariIniMulai);
            query.setParameter("selesai", hariIniSelesai);
            return query.getResultList();
        });
    }
}
