package com.mrh.buscharter.model;

import com.mrh.buscharter.model.enums.TipeVehicle;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity untuk tabel trips.
 * Detail perjalanan dalam satu booking.
 * Satu Booking bisa punya banyak Trip.
 */
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime waktuMulai;

    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime waktuSelesai;

    @Column(name = "origin_location", nullable = false, columnDefinition = "TEXT")
    private String lokasiJemput;

    @Column(name = "destination_location", nullable = false, columnDefinition = "TEXT")
    private String lokasiTujuan;

    @Column(name = "route_description", columnDefinition = "TEXT")
    private String deskripsiRute;

    @Column(name = "passenger_count_estim")
    private Integer estimasiPenumpang;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_bus_type", length = 50)
    private TipeVehicle tipeBusDiminta;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripAssignment> assignments = new ArrayList<>();

    // Constructors
    public Trip() {}

    public Trip(Booking booking, LocalDateTime waktuMulai, LocalDateTime waktuSelesai,
                String lokasiJemput, String lokasiTujuan) {
        this.booking = booking;
        this.waktuMulai = waktuMulai;
        this.waktuSelesai = waktuSelesai;
        this.lokasiJemput = lokasiJemput;
        this.lokasiTujuan = lokasiTujuan;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public LocalDateTime getWaktuMulai() { return waktuMulai; }
    public void setWaktuMulai(LocalDateTime waktuMulai) { this.waktuMulai = waktuMulai; }

    public LocalDateTime getWaktuSelesai() { return waktuSelesai; }
    public void setWaktuSelesai(LocalDateTime waktuSelesai) { this.waktuSelesai = waktuSelesai; }

    public String getLokasiJemput() { return lokasiJemput; }
    public void setLokasiJemput(String lokasiJemput) { this.lokasiJemput = lokasiJemput; }

    public String getLokasiTujuan() { return lokasiTujuan; }
    public void setLokasiTujuan(String lokasiTujuan) { this.lokasiTujuan = lokasiTujuan; }

    public String getDeskripsiRute() { return deskripsiRute; }
    public void setDeskripsiRute(String deskripsiRute) { this.deskripsiRute = deskripsiRute; }

    public Integer getEstimasiPenumpang() { return estimasiPenumpang; }
    public void setEstimasiPenumpang(Integer estimasiPenumpang) { this.estimasiPenumpang = estimasiPenumpang; }

    public TipeVehicle getTipeBusDiminta() { return tipeBusDiminta; }
    public void setTipeBusDiminta(TipeVehicle tipeBusDiminta) { this.tipeBusDiminta = tipeBusDiminta; }

    public List<TripAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<TripAssignment> assignments) { this.assignments = assignments; }

    // Helper methods
    public void addAssignment(TripAssignment assignment) {
        assignments.add(assignment);
        assignment.setTrip(this);
    }

    /**
     * Mendapatkan ringkasan rute untuk display.
     */
    public String getRingkasanRute() {
        return lokasiJemput + " → " + lokasiTujuan;
    }
}
