package com.mrh.buscharter.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity untuk tabel drivers.
 * Data pengemudi dan kru.
 */
@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "full_name", nullable = false, length = 100)
    private String namaLengkap;

    @Column(name = "nickname", length = 50)
    private String namaPanggilan;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String nomorTelepon;

    @Column(name = "license_number", length = 50)
    private String nomorSim;

    @Column(name = "license_expiry")
    private LocalDate masaBerlakuSim;

    @Column(name = "status", length = 20)
    private String status = "AKTIF";

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<TripAssignment> assignmentsSebagaiDriver = new ArrayList<>();

    @OneToMany(mappedBy = "coDriver", cascade = CascadeType.ALL)
    private List<TripAssignment> assignmentsSebagaiCoDriver = new ArrayList<>();

    // Constructors
    public Driver() {}

    public Driver(Tenant tenant, String namaLengkap, String nomorTelepon) {
        this.tenant = tenant;
        this.namaLengkap = namaLengkap;
        this.nomorTelepon = nomorTelepon;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }

    public String getNamaPanggilan() { return namaPanggilan; }
    public void setNamaPanggilan(String namaPanggilan) { this.namaPanggilan = namaPanggilan; }

    public String getNomorTelepon() { return nomorTelepon; }
    public void setNomorTelepon(String nomorTelepon) { this.nomorTelepon = nomorTelepon; }

    public String getNomorSim() { return nomorSim; }
    public void setNomorSim(String nomorSim) { this.nomorSim = nomorSim; }

    public LocalDate getMasaBerlakuSim() { return masaBerlakuSim; }
    public void setMasaBerlakuSim(LocalDate masaBerlakuSim) { this.masaBerlakuSim = masaBerlakuSim; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<TripAssignment> getAssignmentsSebagaiDriver() { return assignmentsSebagaiDriver; }
    public void setAssignmentsSebagaiDriver(List<TripAssignment> assignments) { this.assignmentsSebagaiDriver = assignments; }

    public List<TripAssignment> getAssignmentsSebagaiCoDriver() { return assignmentsSebagaiCoDriver; }
    public void setAssignmentsSebagaiCoDriver(List<TripAssignment> assignments) { this.assignmentsSebagaiCoDriver = assignments; }

    /**
     * Helper method untuk mendapatkan display name.
     */
    public String getDisplayName() {
        if (namaPanggilan != null && !namaPanggilan.isBlank()) {
            return namaPanggilan;
        }
        return namaLengkap;
    }

    /**
     * Cek apakah SIM masih berlaku.
     */
    public boolean isSimMasihBerlaku() {
        if (masaBerlakuSim == null) return true;
        return masaBerlakuSim.isAfter(LocalDate.now());
    }
}
