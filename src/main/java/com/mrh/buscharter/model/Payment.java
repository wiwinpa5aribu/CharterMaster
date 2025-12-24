package com.mrh.buscharter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity untuk tabel payments.
 * Rekam jejak pembayaran masuk.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "payment_date")
    private LocalDateTime tanggalPembayaran;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal jumlah;

    @Column(name = "method", length = 50)
    private String metode;

    @Column(name = "proof_url", columnDefinition = "TEXT")
    private String buktiUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String catatan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @PrePersist
    protected void onCreate() {
        tanggalPembayaran = LocalDateTime.now();
    }

    // Constructors
    public Payment() {}

    public Payment(Booking booking, BigDecimal jumlah, String metode) {
        this.booking = booking;
        this.jumlah = jumlah;
        this.metode = metode;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public LocalDateTime getTanggalPembayaran() { return tanggalPembayaran; }
    public void setTanggalPembayaran(LocalDateTime tanggalPembayaran) { this.tanggalPembayaran = tanggalPembayaran; }

    public BigDecimal getJumlah() { return jumlah; }
    public void setJumlah(BigDecimal jumlah) { this.jumlah = jumlah; }

    public String getMetode() { return metode; }
    public void setMetode(String metode) { this.metode = metode; }

    public String getBuktiUrl() { return buktiUrl; }
    public void setBuktiUrl(String buktiUrl) { this.buktiUrl = buktiUrl; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public User getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(User verifiedBy) { this.verifiedBy = verifiedBy; }
}
