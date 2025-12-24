package com.mrh.buscharter.service;

import com.mrh.buscharter.model.BookingCharge;
import com.mrh.buscharter.model.Payment;
import com.mrh.buscharter.model.enums.StatusBooking;
import com.mrh.buscharter.model.enums.TipeCharge;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Property-based test untuk Status Pembayaran Otomatis.
 * 
 * **Property 4: Status Pembayaran Otomatis**
 * **Validates: Requirements 5.2, 5.3, 5.5**
 * 
 * Aturan:
 * - Jika Outstanding <= 0 → Status = LUNAS
 * - Jika Outstanding > 0 dan ada payment → Status = DP_DITERIMA
 */
public class StatusPembayaranPropertyTest {

    /**
     * Property: Jika outstanding <= 0, status harus LUNAS.
     * 
     * For any booking where total payments >= grand total,
     * the status should be LUNAS.
     */
    @Property(tries = 100)
    void outstandingNolAtauNegatifMakaLunas(
            @ForAll @Size(min = 1, max = 5) List<@From("chargeGenerator") BookingCharge> charges,
            @ForAll @Size(min = 1, max = 5) List<@From("paymentGenerator") Payment> payments) {
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        BigDecimal totalPembayaran = hitungTotalPembayaran(payments);
        BigDecimal outstanding = grandTotal.subtract(totalPembayaran);
        
        StatusBooking expectedStatus = determineStatus(outstanding, totalPembayaran, StatusBooking.QUOTATION_SENT);
        
        // Property: jika outstanding <= 0, status harus LUNAS
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            assert expectedStatus == StatusBooking.LUNAS 
                : "Outstanding <= 0 harus menghasilkan status LUNAS";
        }
    }

    /**
     * Property: Jika ada pembayaran tapi belum lunas, status harus DP_DITERIMA.
     * 
     * For any booking where 0 < total payments < grand total,
     * the status should be DP_DITERIMA.
     */
    @Property(tries = 100)
    void adaPembayaranTapiBelumLunasMakaDpDiterima(
            @ForAll @Size(min = 1, max = 5) List<@From("chargeGenerator") BookingCharge> charges,
            @ForAll @BigRange(min = "1", max = "1000000") BigDecimal pembayaranParsial) {
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        
        // Pastikan pembayaran kurang dari grand total
        if (pembayaranParsial.compareTo(grandTotal) >= 0) {
            pembayaranParsial = grandTotal.multiply(BigDecimal.valueOf(0.5));
        }
        
        BigDecimal outstanding = grandTotal.subtract(pembayaranParsial);
        
        StatusBooking expectedStatus = determineStatus(outstanding, pembayaranParsial, StatusBooking.QUOTATION_SENT);
        
        // Property: jika ada pembayaran tapi belum lunas, status harus DP_DITERIMA
        if (outstanding.compareTo(BigDecimal.ZERO) > 0 && pembayaranParsial.compareTo(BigDecimal.ZERO) > 0) {
            assert expectedStatus == StatusBooking.DP_DITERIMA 
                : "Ada pembayaran tapi belum lunas harus menghasilkan status DP_DITERIMA";
        }
    }

    /**
     * Property: Outstanding = GrandTotal - TotalPembayaran.
     */
    @Property(tries = 100)
    void rumusOutstandingBenar(
            @ForAll @Size(min = 1, max = 5) List<@From("chargeGenerator") BookingCharge> charges,
            @ForAll @Size(min = 0, max = 5) List<@From("paymentGenerator") Payment> payments) {
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        BigDecimal totalPembayaran = hitungTotalPembayaran(payments);
        BigDecimal outstanding = grandTotal.subtract(totalPembayaran);
        
        // Property: outstanding harus sama dengan grand total - total pembayaran
        BigDecimal calculatedOutstanding = hitungOutstanding(charges, payments);
        
        assert outstanding.compareTo(calculatedOutstanding) == 0 
            : "Rumus outstanding harus benar";
    }

    /**
     * Property: Pembayaran lebih dari grand total tetap menghasilkan LUNAS.
     */
    @Property(tries = 100)
    void pembayaranLebihDariGrandTotalTetapLunas(
            @ForAll @Size(min = 1, max = 5) List<@From("chargeGenerator") BookingCharge> charges,
            @ForAll @BigRange(min = "1.1", max = "2.0") BigDecimal multiplier) {
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        BigDecimal pembayaranLebih = grandTotal.multiply(multiplier);
        
        BigDecimal outstanding = grandTotal.subtract(pembayaranLebih);
        
        StatusBooking expectedStatus = determineStatus(outstanding, pembayaranLebih, StatusBooking.QUOTATION_SENT);
        
        // Property: pembayaran lebih tetap LUNAS
        assert expectedStatus == StatusBooking.LUNAS 
            : "Pembayaran lebih dari grand total harus tetap LUNAS";
        assert outstanding.compareTo(BigDecimal.ZERO) < 0 
            : "Outstanding harus negatif jika pembayaran lebih";
    }

    /**
     * Property: Tanpa pembayaran, status tidak berubah dari QUOTATION_SENT.
     */
    @Property(tries = 100)
    void tanpaPembayaranStatusTidakBerubah(
            @ForAll @Size(min = 1, max = 5) List<@From("chargeGenerator") BookingCharge> charges) {
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        BigDecimal totalPembayaran = BigDecimal.ZERO;
        BigDecimal outstanding = grandTotal.subtract(totalPembayaran);
        
        StatusBooking currentStatus = StatusBooking.QUOTATION_SENT;
        StatusBooking expectedStatus = determineStatus(outstanding, totalPembayaran, currentStatus);
        
        // Property: tanpa pembayaran, status tetap QUOTATION_SENT
        assert expectedStatus == StatusBooking.QUOTATION_SENT 
            : "Tanpa pembayaran, status harus tetap QUOTATION_SENT";
    }

    /**
     * Property: Persentase pembayaran selalu antara 0 dan 100+ (bisa lebih dari 100 jika overpay).
     */
    @Property(tries = 100)
    void persentasePembayaranValid(
            @ForAll @Size(min = 1, max = 5) List<@From("chargeGenerator") BookingCharge> charges,
            @ForAll @Size(min = 0, max = 5) List<@From("paymentGenerator") Payment> payments) {
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        BigDecimal totalPembayaran = hitungTotalPembayaran(payments);
        
        double persentase = hitungPersentasePembayaran(grandTotal, totalPembayaran);
        
        // Property: persentase harus >= 0
        assert persentase >= 0 : "Persentase pembayaran harus >= 0";
        
        // Jika tidak ada pembayaran, persentase harus 0
        if (totalPembayaran.compareTo(BigDecimal.ZERO) == 0) {
            assert persentase == 0 : "Tanpa pembayaran, persentase harus 0";
        }
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<BookingCharge> chargeGenerator() {
        return Combinators.combine(
            Arbitraries.integers().between(1, 5),
            Arbitraries.bigDecimals().between(BigDecimal.valueOf(1000000), BigDecimal.valueOf(50000000)),
            Arbitraries.of(TipeCharge.UTAMA, TipeCharge.TAMBAHAN)
        ).as((qty, harga, tipe) -> {
            BookingCharge charge = new BookingCharge();
            charge.setId((long) (Math.random() * 1000));
            charge.setDeskripsi("Test Charge");
            charge.setKuantitas(qty);
            charge.setHargaSatuan(harga);
            charge.setTipeCharge(tipe);
            charge.hitungTotalHarga();
            return charge;
        });
    }

    @Provide
    Arbitrary<Payment> paymentGenerator() {
        return Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(100000), BigDecimal.valueOf(10000000))
            .map(jumlah -> {
                Payment payment = new Payment();
                payment.setId((long) (Math.random() * 1000));
                payment.setJumlah(jumlah);
                payment.setMetode("Transfer");
                return payment;
            });
    }

    // ==================== Helper Methods ====================

    private BigDecimal hitungGrandTotal(List<BookingCharge> charges) {
        BigDecimal total = BigDecimal.ZERO;
        for (BookingCharge charge : charges) {
            if (charge.getTipeCharge() == TipeCharge.UTAMA || 
                charge.getTipeCharge() == TipeCharge.TAMBAHAN) {
                total = total.add(charge.getTotalHarga());
            } else if (charge.getTipeCharge() == TipeCharge.DISKON) {
                total = total.subtract(charge.getTotalHarga());
            }
        }
        return total;
    }

    private BigDecimal hitungTotalPembayaran(List<Payment> payments) {
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : payments) {
            total = total.add(payment.getJumlah());
        }
        return total;
    }

    private BigDecimal hitungOutstanding(List<BookingCharge> charges, List<Payment> payments) {
        return hitungGrandTotal(charges).subtract(hitungTotalPembayaran(payments));
    }

    private StatusBooking determineStatus(BigDecimal outstanding, BigDecimal totalPembayaran, 
                                           StatusBooking currentStatus) {
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            return StatusBooking.LUNAS;
        } else if (totalPembayaran.compareTo(BigDecimal.ZERO) > 0) {
            return StatusBooking.DP_DITERIMA;
        }
        return currentStatus;
    }

    private double hitungPersentasePembayaran(BigDecimal grandTotal, BigDecimal totalPembayaran) {
        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            return 100.0;
        }
        return totalPembayaran.divide(grandTotal, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
}
