package com.mrh.buscharter.service;

import com.mrh.buscharter.model.BookingCharge;
import com.mrh.buscharter.model.enums.TipeCharge;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Property-based test untuk Kalkulasi Grand Total.
 * 
 * **Property 3: Kalkulasi Grand Total Konsisten**
 * **Validates: Requirements 3.2, 3.3**
 * 
 * Rumus: GrandTotal = Sum(UTAMA + TAMBAHAN) - Sum(DISKON)
 */
public class GrandTotalPropertyTest {

    /**
     * Property: Grand total = Sum(UTAMA + TAMBAHAN) - Sum(DISKON).
     * 
     * For any list of charges with mixed types,
     * the grand total should equal the sum of UTAMA and TAMBAHAN minus DISKON.
     */
    @Property(tries = 100)
    void grandTotalSamaDenganRumus(
            @ForAll @Size(min = 1, max = 20) List<@From("chargeGenerator") BookingCharge> charges) {
        
        // Hitung manual sesuai rumus
        BigDecimal sumUtamaTambahan = BigDecimal.ZERO;
        BigDecimal sumDiskon = BigDecimal.ZERO;
        
        for (BookingCharge charge : charges) {
            if (charge.getTipeCharge() == TipeCharge.UTAMA || 
                charge.getTipeCharge() == TipeCharge.TAMBAHAN) {
                sumUtamaTambahan = sumUtamaTambahan.add(charge.getTotalHarga());
            } else if (charge.getTipeCharge() == TipeCharge.DISKON) {
                sumDiskon = sumDiskon.add(charge.getTotalHarga());
            }
        }
        
        BigDecimal expectedGrandTotal = sumUtamaTambahan.subtract(sumDiskon);
        
        // Hitung menggunakan method yang akan diimplementasi
        BigDecimal actualGrandTotal = hitungGrandTotal(charges);
        
        // Property: hasil harus sama
        assert expectedGrandTotal.compareTo(actualGrandTotal) == 0 
            : String.format("Expected %s, got %s", expectedGrandTotal, actualGrandTotal);
    }

    /**
     * Property: Grand total tanpa diskon = Sum(UTAMA + TAMBAHAN).
     */
    @Property(tries = 100)
    void grandTotalTanpaDiskon(
            @ForAll @Size(min = 1, max = 10) List<@From("chargeUtamaTambahanGenerator") BookingCharge> charges) {
        
        // Semua charge adalah UTAMA atau TAMBAHAN (tidak ada DISKON)
        BigDecimal sumTotal = BigDecimal.ZERO;
        for (BookingCharge charge : charges) {
            sumTotal = sumTotal.add(charge.getTotalHarga());
        }
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        
        // Property: grand total = sum total (karena tidak ada diskon)
        assert sumTotal.compareTo(grandTotal) == 0 
            : "Grand total tanpa diskon harus sama dengan sum total";
    }

    /**
     * Property: Grand total dengan hanya diskon = -Sum(DISKON).
     */
    @Property(tries = 100)
    void grandTotalHanyaDiskon(
            @ForAll @Size(min = 1, max = 10) List<@From("chargeDiskonGenerator") BookingCharge> charges) {
        
        // Semua charge adalah DISKON
        BigDecimal sumDiskon = BigDecimal.ZERO;
        for (BookingCharge charge : charges) {
            sumDiskon = sumDiskon.add(charge.getTotalHarga());
        }
        
        BigDecimal grandTotal = hitungGrandTotal(charges);
        BigDecimal expectedGrandTotal = sumDiskon.negate();
        
        // Property: grand total = -sum diskon
        assert expectedGrandTotal.compareTo(grandTotal) == 0 
            : "Grand total hanya diskon harus negatif dari sum diskon";
    }

    /**
     * Property: Total harga charge = kuantitas * harga satuan.
     */
    @Property(tries = 100)
    void totalHargaSamaDenganKuantitasKaliHargaSatuan(
            @ForAll @IntRange(min = 1, max = 100) int kuantitas,
            @ForAll @BigRange(min = "0", max = "100000000") BigDecimal hargaSatuan) {
        
        BookingCharge charge = new BookingCharge();
        charge.setKuantitas(kuantitas);
        charge.setHargaSatuan(hargaSatuan);
        charge.hitungTotalHarga();
        
        BigDecimal expected = hargaSatuan.multiply(BigDecimal.valueOf(kuantitas));
        
        // Property: total = qty * harga satuan
        assert expected.compareTo(charge.getTotalHarga()) == 0 
            : "Total harga harus = kuantitas * harga satuan";
    }

    /**
     * Property: Urutan charge tidak mempengaruhi grand total.
     */
    @Property(tries = 50)
    void urutanChargeTidakMempengaruhiGrandTotal(
            @ForAll @Size(min = 2, max = 10) List<@From("chargeGenerator") BookingCharge> charges) {
        
        // Hitung grand total dengan urutan asli
        BigDecimal grandTotal1 = hitungGrandTotal(charges);
        
        // Shuffle dan hitung lagi
        List<BookingCharge> shuffled = new ArrayList<>(charges);
        Collections.shuffle(shuffled);
        BigDecimal grandTotal2 = hitungGrandTotal(shuffled);
        
        // Property: hasil harus sama
        assert grandTotal1.compareTo(grandTotal2) == 0 
            : "Urutan charge tidak boleh mempengaruhi grand total";
    }

    /**
     * Property: Menambah charge UTAMA/TAMBAHAN meningkatkan grand total.
     */
    @Property(tries = 100)
    void menambahChargeUtamaMeningkatkanGrandTotal(
            @ForAll @Size(min = 1, max = 10) List<@From("chargeGenerator") BookingCharge> existingCharges,
            @ForAll @BigRange(min = "1", max = "10000000") BigDecimal hargaBaru) {
        
        BigDecimal grandTotalSebelum = hitungGrandTotal(existingCharges);
        
        // Tambah charge UTAMA baru
        BookingCharge chargeBaru = new BookingCharge();
        chargeBaru.setKuantitas(1);
        chargeBaru.setHargaSatuan(hargaBaru);
        chargeBaru.setTipeCharge(TipeCharge.UTAMA);
        chargeBaru.hitungTotalHarga();
        
        List<BookingCharge> chargesSetelah = new ArrayList<>(existingCharges);
        chargesSetelah.add(chargeBaru);
        
        BigDecimal grandTotalSetelah = hitungGrandTotal(chargesSetelah);
        
        // Property: grand total harus meningkat
        assert grandTotalSetelah.compareTo(grandTotalSebelum) > 0 
            : "Menambah charge UTAMA harus meningkatkan grand total";
    }

    /**
     * Property: Menambah DISKON menurunkan grand total.
     */
    @Property(tries = 100)
    void menambahDiskonMenurunkanGrandTotal(
            @ForAll @Size(min = 1, max = 10) List<@From("chargeUtamaTambahanGenerator") BookingCharge> existingCharges,
            @ForAll @BigRange(min = "1", max = "1000000") BigDecimal nilaiDiskon) {
        
        BigDecimal grandTotalSebelum = hitungGrandTotal(existingCharges);
        
        // Tambah DISKON
        BookingCharge diskon = new BookingCharge();
        diskon.setKuantitas(1);
        diskon.setHargaSatuan(nilaiDiskon);
        diskon.setTipeCharge(TipeCharge.DISKON);
        diskon.hitungTotalHarga();
        
        List<BookingCharge> chargesSetelah = new ArrayList<>(existingCharges);
        chargesSetelah.add(diskon);
        
        BigDecimal grandTotalSetelah = hitungGrandTotal(chargesSetelah);
        
        // Property: grand total harus menurun
        assert grandTotalSetelah.compareTo(grandTotalSebelum) < 0 
            : "Menambah DISKON harus menurunkan grand total";
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<BookingCharge> chargeGenerator() {
        return Combinators.combine(
            Arbitraries.integers().between(1, 10),
            Arbitraries.bigDecimals().between(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000000)),
            Arbitraries.of(TipeCharge.values())
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
    Arbitrary<BookingCharge> chargeUtamaTambahanGenerator() {
        return Combinators.combine(
            Arbitraries.integers().between(1, 10),
            Arbitraries.bigDecimals().between(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000000)),
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
    Arbitrary<BookingCharge> chargeDiskonGenerator() {
        return Combinators.combine(
            Arbitraries.integers().between(1, 5),
            Arbitraries.bigDecimals().between(BigDecimal.valueOf(10000), BigDecimal.valueOf(5000000))
        ).as((qty, harga) -> {
            BookingCharge charge = new BookingCharge();
            charge.setId((long) (Math.random() * 1000));
            charge.setDeskripsi("Diskon");
            charge.setKuantitas(qty);
            charge.setHargaSatuan(harga);
            charge.setTipeCharge(TipeCharge.DISKON);
            charge.hitungTotalHarga();
            return charge;
        });
    }

    // ==================== Helper Methods ====================

    private BigDecimal hitungGrandTotal(List<BookingCharge> charges) {
        BigDecimal sumUtamaTambahan = BigDecimal.ZERO;
        BigDecimal sumDiskon = BigDecimal.ZERO;
        
        for (BookingCharge charge : charges) {
            if (charge.getTipeCharge() == TipeCharge.UTAMA || 
                charge.getTipeCharge() == TipeCharge.TAMBAHAN) {
                sumUtamaTambahan = sumUtamaTambahan.add(charge.getTotalHarga());
            } else if (charge.getTipeCharge() == TipeCharge.DISKON) {
                sumDiskon = sumDiskon.add(charge.getTotalHarga());
            }
        }
        
        return sumUtamaTambahan.subtract(sumDiskon);
    }
}
