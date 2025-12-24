package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.Booking;
import com.mrh.buscharter.model.enums.StatusBooking;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Booking.
 */
public class BookingRepository extends BaseRepository<Booking> {

    public BookingRepository() {
        super(Booking.class);
    }

    @Override
    protected Long getEntityId(Booking entity) {
        return entity.getId();
    }

    /**
     * Cari booking berdasarkan kode booking dan tenant.
     */
    public Optional<Booking> findByKodeBookingAndTenantId(String kodeBooking, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.kodeBooking = :kodeBooking " +
                "AND b.tenant.id = :tenantId", 
                Booking.class);
            query.setParameter("kodeBooking", kodeBooking);
            query.setParameter("tenantId", tenantId);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari booking berdasarkan status dan tenant.
     */
    public List<Booking> findByStatusAndTenantId(StatusBooking status, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.status = :status " +
                "AND b.tenant.id = :tenantId ORDER BY b.tanggalBooking DESC", 
                Booking.class);
            query.setParameter("status", status);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari booking berdasarkan customer dan tenant.
     */
    public List<Booking> findByCustomerIdAndTenantId(Long customerId, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.customer.id = :customerId " +
                "AND b.tenant.id = :tenantId ORDER BY b.tanggalBooking DESC", 
                Booking.class);
            query.setParameter("customerId", customerId);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari booking dalam rentang tanggal.
     */
    public List<Booking> findByTanggalBookingBetweenAndTenantId(
            LocalDateTime mulai, LocalDateTime selesai, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.tanggalBooking BETWEEN :mulai AND :selesai " +
                "AND b.tenant.id = :tenantId ORDER BY b.tanggalBooking DESC", 
                Booking.class);
            query.setParameter("mulai", mulai);
            query.setParameter("selesai", selesai);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari booking yang belum lunas (untuk modul keuangan).
     */
    public List<Booking> findBelumLunasByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.status IN ('DP_DITERIMA') " +
                "AND b.tenant.id = :tenantId ORDER BY b.tanggalBooking", 
                Booking.class);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari booking yang perlu di-assign armada.
     * Booking dengan status DP_DITERIMA yang trip-nya belum ada assignment.
     */
    public List<Booking> findPerluAssignmentByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT DISTINCT b FROM Booking b " +
                "JOIN b.trips t " +
                "LEFT JOIN t.assignments ta " +
                "WHERE b.tenant.id = :tenantId " +
                "AND b.status IN ('DP_DITERIMA', 'LUNAS') " +
                "AND (ta IS NULL OR ta.vehicle IS NULL) " +
                "ORDER BY b.tanggalBooking", 
                Booking.class);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Generate kode booking baru.
     * Format: BOOK/YYYY/MM/NNN
     */
    public String generateKodeBooking(Long tenantId) {
        return executeWithEntityManager(em -> {
            LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            
            // Hitung jumlah booking bulan ini
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(b) FROM Booking b WHERE b.tenant.id = :tenantId " +
                "AND YEAR(b.tanggalBooking) = :year AND MONTH(b.tanggalBooking) = :month", 
                Long.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("year", year);
            query.setParameter("month", month);
            
            long count = query.getSingleResult() + 1;
            
            return String.format("BOOK/%d/%02d/%03d", year, month, count);
        });
    }

    /**
     * Cari semua booking dengan eager fetch trips dan charges.
     */
    public Optional<Booking> findByIdWithDetails(Long id) {
        return executeWithEntityManager(em -> {
            TypedQuery<Booking> query = em.createQuery(
                "SELECT DISTINCT b FROM Booking b " +
                "LEFT JOIN FETCH b.trips t " +
                "LEFT JOIN FETCH b.charges " +
                "LEFT JOIN FETCH b.payments " +
                "LEFT JOIN FETCH b.customer " +
                "WHERE b.id = :id", 
                Booking.class);
            query.setParameter("id", id);
            return query.getResultStream().findFirst();
        });
    }
}
