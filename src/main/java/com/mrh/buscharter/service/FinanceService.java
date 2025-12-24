package com.mrh.buscharter.service;

import com.mrh.buscharter.model.*;
import com.mrh.buscharter.model.enums.StatusBooking;
import com.mrh.buscharter.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service untuk modul Finance/Keuangan.
 * Menangani pencatatan pembayaran dan update status otomatis.
 */
public class FinanceService {

    private static final Logger logger = LoggerFactory.getLogger(FinanceService.class);
    
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookingChargeRepository bookingChargeRepository;

    public FinanceService() {
        this.bookingRepository = new BookingRepository();
        this.paymentRepository = new PaymentRepository();
        this.bookingChargeRepository = new BookingChargeRepository();
    }

    // ==================== PEMBAYARAN ====================

    /**
     * Catat pembayaran baru.
     * 
     * @param bookingId ID booking
     * @param nominal Jumlah pembayaran
     * @param metode Metode pembayaran (Transfer BCA, Cash, dll)
     * @param buktiUrl URL bukti transfer (opsional)
     * @param verifiedBy User yang memverifikasi
     * @return Payment yang dicatat
     */
    public Payment catatPembayaran(Long bookingId, BigDecimal nominal, String metode, 
                                    String buktiUrl, User verifiedBy) {
        logger.info("Catat pembayaran untuk booking {}: {} via {}", bookingId, nominal, metode);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
        
        // Validasi
        if (nominal == null || nominal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal pembayaran harus lebih dari 0");
        }
        
        // Validasi status booking - hanya bisa bayar jika sudah QUOTATION_SENT ke atas
        if (booking.getStatus() == StatusBooking.DRAFT) {
            throw new IllegalStateException("Tidak bisa bayar booking dengan status DRAFT");
        }
        if (booking.getStatus() == StatusBooking.BATAL) {
            throw new IllegalStateException("Tidak bisa bayar booking yang sudah BATAL");
        }
        
        // Buat payment
        Payment payment = new Payment(booking, nominal, metode);
        payment.setBuktiUrl(buktiUrl);
        payment.setVerifiedBy(verifiedBy);
        payment.setTanggalPembayaran(LocalDateTime.now());
        
        Payment saved = paymentRepository.save(payment);
        
        // Auto-update status pembayaran
        updateStatusPembayaran(bookingId);
        
        logger.info("Pembayaran berhasil dicatat: {}", saved.getId());
        return saved;
    }

    /**
     * Hitung outstanding (sisa yang harus dibayar).
     * Rumus: GrandTotal - Sum(Payments)
     */
    public BigDecimal hitungOutstanding(Long bookingId) {
        BigDecimal grandTotal = bookingChargeRepository.hitungGrandTotal(bookingId);
        BigDecimal totalPembayaran = paymentRepository.sumPembayaranByBookingId(bookingId);
        return grandTotal.subtract(totalPembayaran);
    }

    /**
     * Hitung total pembayaran untuk booking.
     */
    public BigDecimal hitungTotalPembayaran(Long bookingId) {
        return paymentRepository.sumPembayaranByBookingId(bookingId);
    }

    /**
     * Ambil semua pembayaran untuk booking.
     */
    public List<Payment> getPembayaranByBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    // ==================== AUTO-UPDATE STATUS ====================

    /**
     * Update status pembayaran booking secara otomatis.
     * 
     * Aturan:
     * - Jika Outstanding <= 0 → Status = LUNAS
     * - Jika Outstanding > 0 dan ada payment → Status = DP_DITERIMA
     * 
     * @param bookingId ID booking
     * @return Booking yang diupdate
     */
    public Booking updateStatusPembayaran(Long bookingId) {
        logger.info("Update status pembayaran untuk booking: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
        
        // Skip jika status sudah SELESAI atau BATAL
        if (booking.getStatus() == StatusBooking.SELESAI || 
            booking.getStatus() == StatusBooking.BATAL) {
            logger.info("Skip update - booking sudah {}", booking.getStatus());
            return booking;
        }
        
        BigDecimal outstanding = hitungOutstanding(bookingId);
        BigDecimal totalPembayaran = hitungTotalPembayaran(bookingId);
        
        StatusBooking statusBaru = null;
        
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            // Lunas
            if (booking.getStatus() != StatusBooking.LUNAS) {
                statusBaru = StatusBooking.LUNAS;
            }
        } else if (totalPembayaran.compareTo(BigDecimal.ZERO) > 0) {
            // Ada pembayaran tapi belum lunas
            if (booking.getStatus() == StatusBooking.QUOTATION_SENT) {
                statusBaru = StatusBooking.DP_DITERIMA;
            }
        }
        
        if (statusBaru != null && booking.getStatus().bisaTransisiKe(statusBaru)) {
            booking.setStatus(statusBaru);
            booking = bookingRepository.save(booking);
            logger.info("Status booking {} diupdate ke {}", bookingId, statusBaru);
        }
        
        return booking;
    }

    /**
     * Cek apakah booking sudah lunas.
     */
    public boolean isLunas(Long bookingId) {
        BigDecimal outstanding = hitungOutstanding(bookingId);
        return outstanding.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Hitung persentase pembayaran.
     */
    public double hitungPersentasePembayaran(Long bookingId) {
        BigDecimal grandTotal = bookingChargeRepository.hitungGrandTotal(bookingId);
        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            return 100.0;
        }
        
        BigDecimal totalPembayaran = hitungTotalPembayaran(bookingId);
        return totalPembayaran.divide(grandTotal, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    // ==================== QUERY ====================

    /**
     * Ambil booking yang belum lunas untuk tenant.
     */
    public List<Booking> getBookingBelumLunas(Long tenantId) {
        return bookingRepository.findBelumLunasByTenantId(tenantId);
    }

    /**
     * Ambil pembayaran dalam rentang tanggal.
     */
    public List<Payment> getPembayaranByTanggal(Long tenantId, LocalDateTime mulai, LocalDateTime selesai) {
        return paymentRepository.findByTanggalAndTenantId(mulai, selesai, tenantId);
    }

    /**
     * Hitung total pembayaran dalam rentang tanggal.
     */
    public BigDecimal hitungTotalPembayaranByTanggal(Long tenantId, LocalDateTime mulai, LocalDateTime selesai) {
        return paymentRepository.sumPembayaranByTanggalAndTenantId(mulai, selesai, tenantId);
    }

    /**
     * Ringkasan keuangan booking.
     */
    public RingkasanKeuangan getRingkasanKeuangan(Long bookingId) {
        BigDecimal grandTotal = bookingChargeRepository.hitungGrandTotal(bookingId);
        BigDecimal totalPembayaran = hitungTotalPembayaran(bookingId);
        BigDecimal outstanding = grandTotal.subtract(totalPembayaran);
        double persentase = hitungPersentasePembayaran(bookingId);
        
        return new RingkasanKeuangan(grandTotal, totalPembayaran, outstanding, persentase);
    }

    // ==================== INNER CLASS ====================

    /**
     * Ringkasan keuangan booking.
     */
    public static class RingkasanKeuangan {
        private final BigDecimal grandTotal;
        private final BigDecimal totalPembayaran;
        private final BigDecimal outstanding;
        private final double persentasePembayaran;

        public RingkasanKeuangan(BigDecimal grandTotal, BigDecimal totalPembayaran, 
                                  BigDecimal outstanding, double persentasePembayaran) {
            this.grandTotal = grandTotal;
            this.totalPembayaran = totalPembayaran;
            this.outstanding = outstanding;
            this.persentasePembayaran = persentasePembayaran;
        }

        public BigDecimal getGrandTotal() { return grandTotal; }
        public BigDecimal getTotalPembayaran() { return totalPembayaran; }
        public BigDecimal getOutstanding() { return outstanding; }
        public double getPersentasePembayaran() { return persentasePembayaran; }
        
        public boolean isLunas() { return outstanding.compareTo(BigDecimal.ZERO) <= 0; }
    }
}
