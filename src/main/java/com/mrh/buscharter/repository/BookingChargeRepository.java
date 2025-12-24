package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.BookingCharge;
import com.mrh.buscharter.model.enums.TipeCharge;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository untuk entity BookingCharge.
 */
public class BookingChargeRepository extends BaseRepository<BookingCharge> {

    public BookingChargeRepository() {
        super(BookingCharge.class);
    }

    @Override
    protected Long getEntityId(BookingCharge entity) {
        return entity.getId();
    }

    /**
     * Cari semua charge berdasarkan booking.
     */
    public List<BookingCharge> findByBookingId(Long bookingId) {
        return executeWithEntityManager(em -> {
            TypedQuery<BookingCharge> query = em.createQuery(
                "SELECT bc FROM BookingCharge bc WHERE bc.booking.id = :bookingId " +
                "ORDER BY bc.tipeCharge, bc.id", 
                BookingCharge.class);
            query.setParameter("bookingId", bookingId);
            return query.getResultList();
        });
    }

    /**
     * Cari charge berdasarkan tipe dan booking.
     */
    public List<BookingCharge> findByTipeAndBookingId(TipeCharge tipe, Long bookingId) {
        return executeWithEntityManager(em -> {
            TypedQuery<BookingCharge> query = em.createQuery(
                "SELECT bc FROM BookingCharge bc WHERE bc.tipeCharge = :tipe " +
                "AND bc.booking.id = :bookingId", 
                BookingCharge.class);
            query.setParameter("tipe", tipe);
            query.setParameter("bookingId", bookingId);
            return query.getResultList();
        });
    }

    /**
     * Hitung total charge (UTAMA + TAMBAHAN) untuk booking.
     */
    public BigDecimal sumChargeByBookingId(Long bookingId) {
        return executeWithEntityManager(em -> {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(bc.totalHarga), 0) FROM BookingCharge bc " +
                "WHERE bc.booking.id = :bookingId " +
                "AND bc.tipeCharge IN ('UTAMA', 'TAMBAHAN')", 
                BigDecimal.class);
            query.setParameter("bookingId", bookingId);
            return query.getSingleResult();
        });
    }

    /**
     * Hitung total diskon untuk booking.
     */
    public BigDecimal sumDiskonByBookingId(Long bookingId) {
        return executeWithEntityManager(em -> {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT COALESCE(SUM(bc.totalHarga), 0) FROM BookingCharge bc " +
                "WHERE bc.booking.id = :bookingId " +
                "AND bc.tipeCharge = 'DISKON'", 
                BigDecimal.class);
            query.setParameter("bookingId", bookingId);
            return query.getSingleResult();
        });
    }

    /**
     * Hitung grand total (charge - diskon) untuk booking.
     */
    public BigDecimal hitungGrandTotal(Long bookingId) {
        BigDecimal totalCharge = sumChargeByBookingId(bookingId);
        BigDecimal totalDiskon = sumDiskonByBookingId(bookingId);
        return totalCharge.subtract(totalDiskon);
    }

    /**
     * Hapus semua charge untuk booking.
     */
    public void deleteByBookingId(Long bookingId) {
        executeInTransactionVoid(em -> {
            em.createQuery("DELETE FROM BookingCharge bc WHERE bc.booking.id = :bookingId")
              .setParameter("bookingId", bookingId)
              .executeUpdate();
        });
    }
}
