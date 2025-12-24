package com.mrh.buscharter.model;

import com.mrh.buscharter.model.enums.TipeCharge;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity untuk tabel booking_charges.
 * Rincian biaya (Line Items) untuk booking.
 * Harga dimasukkan MANUAL oleh admin, tidak ada rumus otomatis.
 */
@Entity
@Table(name = "booking_charges")
public class BookingCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "description", nullable = false, length = 255)
    private String deskripsi;

    @Column(name = "quantity")
    private Integer kuantitas = 1;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaSatuan;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalHarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type", length = 20)
    private TipeCharge tipeCharge = TipeCharge.UTAMA;

    // Constructors
    public BookingCharge() {}

    public BookingCharge(Booking booking, String deskripsi, Integer kuantitas, 
                         BigDecimal hargaSatuan, TipeCharge tipeCharge) {
        this.booking = booking;
        this.deskripsi = deskripsi;
        this.kuantitas = kuantitas;
        this.hargaSatuan = hargaSatuan;
        this.tipeCharge = tipeCharge;
        hitungTotalHarga();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public Integer getKuantitas() { return kuantitas; }
    public void setKuantitas(Integer kuantitas) { 
        this.kuantitas = kuantitas;
        hitungTotalHarga();
    }

    public BigDecimal getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(BigDecimal hargaSatuan) { 
        this.hargaSatuan = hargaSatuan;
        hitungTotalHarga();
    }

    public BigDecimal getTotalHarga() { return totalHarga; }
    public void setTotalHarga(BigDecimal totalHarga) { this.totalHarga = totalHarga; }

    public TipeCharge getTipeCharge() { return tipeCharge; }
    public void setTipeCharge(TipeCharge tipeCharge) { this.tipeCharge = tipeCharge; }

    /**
     * Menghitung total harga = kuantitas * harga satuan.
     */
    public void hitungTotalHarga() {
        if (kuantitas != null && hargaSatuan != null) {
            this.totalHarga = hargaSatuan.multiply(BigDecimal.valueOf(kuantitas));
        }
    }
}
