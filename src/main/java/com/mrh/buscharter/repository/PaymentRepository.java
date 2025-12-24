package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.Payment;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository untuk entity Payment.
 */
public class PaymentRepository extends BaseRepository<Payment> {

    public PaymentRepository() {
        super(Payment.class);
    }

    @Override
    protected Long getEntityId(Payment entity) {
        return entity.getId();
    }

    /**
     * Cari semua payment berdasarkan booking.
     */
    public List<Payment> findByBookingId(Long bookingId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.booking.id = :bookingId " +
                "ORDER BY p.tanggalPembayaran DESC", 
                Payment.class);
            query.setParameter("bookingId", bookingId);
            return query.getResultList();
        });
    }

    /**
     * Hitung total pembayaran untuk booking.
     */
    public BigDecimal sumPembayaranByBookingId(Long bookingId) {
        return executeWithEntityManager(em -> {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(p.jumlah), 0) FROM Payment p " +
                "WHERE p.booking.id = :bookingId", 
                BigDecimal.class);
            query.setParameter("bookingId", bookingId);
            return query.getSingleResult();
        });
    }

    /**
     * Cari payment dalam rentang tanggal untuk tenant.
     */
    public List<Payment> findByTanggalAndTenantId(LocalDateTime mulai, LocalDateTime selesai, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p " +
                "WHERE p.booking.tenant.id = :tenantId " +
                "AND p.tanggalPembayaran BETWEEN :mulai AND :selesai " +
                "ORDER BY p.tanggalPembayaran DESC", 
                Payment.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("mulai", mulai);
            query.setParameter("selesai", selesai);
            return query.getResultList();
        });
    }

    /**
     * Hitung total pembayaran dalam rentang tanggal untuk tenant.
     */
    public BigDecimal sumPembayaranByTanggalAndTenantId(LocalDateTime mulai, LocalDateTime selesai, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(p.jumlah), 0) FROM Payment p " +
                "WHERE p.booking.tenant.id = :tenantId " +
                "AND p.tanggalPembayaran BETWEEN :mulai AND :selesai", 
                BigDecimal.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("mulai", mulai);
            query.setParameter("selesai", selesai);
            return query.getSingleResult();
        });
    }

    /**
     * Cari payment berdasarkan metode pembayaran.
     */
    public List<Payment> findByMetodeAndTenantId(String metode, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p " +
                "WHERE p.booking.tenant.id = :tenantId " +
                "AND p.metode = :metode " +
                "ORDER BY p.tanggalPembayaran DESC", 
                Payment.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("metode", metode);
            return query.getResultList();
        });
    }
}
