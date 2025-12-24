package com.mrh.buscharter.model;

import com.mrh.buscharter.model.enums.StatusAssignment;
import jakarta.persistence.*;

/**
 * Entity untuk tabel trip_assignments.
 * Penugasan Armada ke Trip (SPJ - Surat Perintah Jalan).
 * Tabel ini adalah JANTUNG dari sistem Anti-Double-Booking.
 */
@Entity
@Table(name = "trip_assignments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "trip_id"}))
public class TripAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_driver_id")
    private Driver coDriver;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", length = 20)
    private StatusAssignment statusAssignment = StatusAssignment.TERJADWAL;

    @Column(name = "start_km")
    private Integer kmAwal;

    @Column(name = "end_km")
    private Integer kmAkhir;

    // Constructors
    public TripAssignment() {}

    public TripAssignment(Trip trip, Vehicle vehicle) {
        this.trip = trip;
        this.vehicle = vehicle;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public Driver getCoDriver() { return coDriver; }
    public void setCoDriver(Driver coDriver) { this.coDriver = coDriver; }

    public StatusAssignment getStatusAssignment() { return statusAssignment; }
    public void setStatusAssignment(StatusAssignment statusAssignment) { this.statusAssignment = statusAssignment; }

    public Integer getKmAwal() { return kmAwal; }
    public void setKmAwal(Integer kmAwal) { this.kmAwal = kmAwal; }

    public Integer getKmAkhir() { return kmAkhir; }
    public void setKmAkhir(Integer kmAkhir) { this.kmAkhir = kmAkhir; }

    /**
     * Menghitung jarak tempuh (km).
     */
    public Integer hitungJarakTempuh() {
        if (kmAwal == null || kmAkhir == null) return null;
        return kmAkhir - kmAwal;
    }
}
