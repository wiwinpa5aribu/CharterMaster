package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.TripAssignment;
import com.mrh.buscharter.model.enums.StatusAssignment;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity TripAssignment.
 * JANTUNG dari sistem Anti-Double-Booking.
 */
public class TripAssignmentRepository extends BaseRepository<TripAssignment> {

    public TripAssignmentRepository() {
        super(TripAssignment.class);
    }

    @Override
    protected Long getEntityId(TripAssignment entity) {
        return entity.getId();
    }

    /**
     * Cari assignment berdasarkan trip.
     */
    public List<TripAssignment> findByTripId(Long tripId) {
        return executeWithEntityManager(em -> {
            TypedQuery<TripAssignment> query = em.createQuery(
                "SELECT ta FROM TripAssignment ta " +
                "LEFT JOIN FETCH ta.vehicle " +
                "LEFT JOIN FETCH ta.driver " +
                "LEFT JOIN FETCH ta.coDriver " +
                "WHERE ta.trip.id = :tripId", 
                TripAssignment.class);
            query.setParameter("tripId", tripId);
            return query.getResultList();
        });
    }

    /**
     * Cari assignment berdasarkan vehicle dan rentang tanggal.
     * Digunakan untuk cek konflik jadwal.
     */
    public List<TripAssignment> findByVehicleIdAndTanggal(Long vehicleId, 
            LocalDateTime tanggalMulai, LocalDateTime tanggalSelesai) {
        return executeWithEntityManager(em -> {
            TypedQuery<TripAssignment> query = em.createQuery(
                "SELECT ta FROM TripAssignment ta " +
                "JOIN ta.trip t " +
                "JOIN t.booking b " +
                "WHERE ta.vehicle.id = :vehicleId " +
                "AND ta.statusAssignment != 'BATAL' " +
                "AND b.status IN ('DP_DITERIMA', 'LUNAS', 'SELESAI') " +
                "AND t.waktuMulai <= :tanggalSelesai " +
                "AND t.waktuSelesai >= :tanggalMulai " +
                "ORDER BY t.waktuMulai", 
                TripAssignment.class);
            query.setParameter("vehicleId", vehicleId);
            query.setParameter("tanggalMulai", tanggalMulai);
            query.setParameter("tanggalSelesai", tanggalSelesai);
            return query.getResultList();
        });
    }

    /**
     * Cek apakah ada konflik jadwal untuk vehicle.
     * Return true jika ada konflik.
     */
    public boolean hasKonflikJadwal(Long vehicleId, LocalDateTime tanggalMulai, 
            LocalDateTime tanggalSelesai, Long excludeTripId) {
        return executeWithEntityManager(em -> {
            StringBuilder jpql = new StringBuilder();
            jpql.append("SELECT COUNT(ta) FROM TripAssignment ta ");
            jpql.append("JOIN ta.trip t ");
            jpql.append("JOIN t.booking b ");
            jpql.append("WHERE ta.vehicle.id = :vehicleId ");
            jpql.append("AND ta.statusAssignment != 'BATAL' ");
            jpql.append("AND b.status IN ('DP_DITERIMA', 'LUNAS', 'SELESAI') ");
            jpql.append("AND t.waktuMulai <= :tanggalSelesai ");
            jpql.append("AND t.waktuSelesai >= :tanggalMulai ");
            
            if (excludeTripId != null) {
                jpql.append("AND t.id != :excludeTripId ");
            }
            
            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            query.setParameter("vehicleId", vehicleId);
            query.setParameter("tanggalMulai", tanggalMulai);
            query.setParameter("tanggalSelesai", tanggalSelesai);
            
            if (excludeTripId != null) {
                query.setParameter("excludeTripId", excludeTripId);
            }
            
            return query.getSingleResult() > 0;
        });
    }

    /**
     * Cari assignment terakhir untuk vehicle (untuk cek jeda waktu).
     */
    public Optional<TripAssignment> findLastAssignmentByVehicleId(Long vehicleId, LocalDateTime sebelumTanggal) {
        return executeWithEntityManager(em -> {
            TypedQuery<TripAssignment> query = em.createQuery(
                "SELECT ta FROM TripAssignment ta " +
                "JOIN ta.trip t " +
                "JOIN t.booking b " +
                "WHERE ta.vehicle.id = :vehicleId " +
                "AND ta.statusAssignment != 'BATAL' " +
                "AND b.status IN ('DP_DITERIMA', 'LUNAS', 'SELESAI') " +
                "AND t.waktuSelesai < :sebelumTanggal " +
                "ORDER BY t.waktuSelesai DESC", 
                TripAssignment.class);
            query.setParameter("vehicleId", vehicleId);
            query.setParameter("sebelumTanggal", sebelumTanggal);
            query.setMaxResults(1);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari assignment berikutnya untuk vehicle (untuk cek jeda waktu).
     */
    public Optional<TripAssignment> findNextAssignmentByVehicleId(Long vehicleId, LocalDateTime setelahTanggal) {
        return executeWithEntityManager(em -> {
            TypedQuery<TripAssignment> query = em.createQuery(
                "SELECT ta FROM TripAssignment ta " +
                "JOIN ta.trip t " +
                "JOIN t.booking b " +
                "WHERE ta.vehicle.id = :vehicleId " +
                "AND ta.statusAssignment != 'BATAL' " +
                "AND b.status IN ('DP_DITERIMA', 'LUNAS', 'SELESAI') " +
                "AND t.waktuMulai > :setelahTanggal " +
                "ORDER BY t.waktuMulai ASC", 
                TripAssignment.class);
            query.setParameter("vehicleId", vehicleId);
            query.setParameter("setelahTanggal", setelahTanggal);
            query.setMaxResults(1);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari assignment berdasarkan status.
     */
    public List<TripAssignment> findByStatusAndTenantId(StatusAssignment status, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<TripAssignment> query = em.createQuery(
                "SELECT ta FROM TripAssignment ta " +
                "JOIN ta.trip t " +
                "JOIN t.booking b " +
                "WHERE ta.statusAssignment = :status " +
                "AND b.tenant.id = :tenantId " +
                "ORDER BY t.waktuMulai", 
                TripAssignment.class);
            query.setParameter("status", status);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari assignment untuk dispatch board (hari ini dan besok).
     */
    public List<TripAssignment> findForDispatchBoard(Long tenantId, LocalDateTime mulai, LocalDateTime selesai) {
        return executeWithEntityManager(em -> {
            TypedQuery<TripAssignment> query = em.createQuery(
                "SELECT ta FROM TripAssignment ta " +
                "JOIN FETCH ta.trip t " +
                "JOIN FETCH t.booking b " +
                "JOIN FETCH b.customer " +
                "LEFT JOIN FETCH ta.vehicle " +
                "LEFT JOIN FETCH ta.driver " +
                "WHERE b.tenant.id = :tenantId " +
                "AND b.status IN ('DP_DITERIMA', 'LUNAS') " +
                "AND t.waktuMulai >= :mulai AND t.waktuMulai < :selesai " +
                "ORDER BY t.waktuMulai", 
                TripAssignment.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("mulai", mulai);
            query.setParameter("selesai", selesai);
            return query.getResultList();
        });
    }
}
