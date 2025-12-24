package com.mrh.buscharter.model;

import com.mrh.buscharter.model.enums.StatusKepemilikan;
import com.mrh.buscharter.model.enums.TipeVehicle;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity untuk tabel vehicles.
 * Data induk bus dan kendaraan.
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "plate_number", nullable = false, length = 20)
    private String platNomor;

    @Column(name = "code_name", length = 50)
    private String namaPanggilan;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private TipeVehicle tipeVehicle;

    @Column(name = "seat_capacity", nullable = false)
    private Integer kapasitasKursi;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_status", nullable = false, length = 20)
    private StatusKepemilikan statusKepemilikan;

    @Column(name = "vendor_name", length = 100)
    private String namaVendor;

    @Column(name = "is_active")
    private Boolean aktif = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String catatan;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<TripAssignment> assignments = new ArrayList<>();

    // Constructors
    public Vehicle() {}

    public Vehicle(Tenant tenant, String platNomor, TipeVehicle tipeVehicle, 
                   Integer kapasitasKursi, StatusKepemilikan statusKepemilikan) {
        this.tenant = tenant;
        this.platNomor = platNomor;
        this.tipeVehicle = tipeVehicle;
        this.kapasitasKursi = kapasitasKursi;
        this.statusKepemilikan = statusKepemilikan;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getPlatNomor() { return platNomor; }
    public void setPlatNomor(String platNomor) { this.platNomor = platNomor; }

    public String getNamaPanggilan() { return namaPanggilan; }
    public void setNamaPanggilan(String namaPanggilan) { this.namaPanggilan = namaPanggilan; }

    public TipeVehicle getTipeVehicle() { return tipeVehicle; }
    public void setTipeVehicle(TipeVehicle tipeVehicle) { this.tipeVehicle = tipeVehicle; }

    public Integer getKapasitasKursi() { return kapasitasKursi; }
    public void setKapasitasKursi(Integer kapasitasKursi) { this.kapasitasKursi = kapasitasKursi; }

    public StatusKepemilikan getStatusKepemilikan() { return statusKepemilikan; }
    public void setStatusKepemilikan(StatusKepemilikan statusKepemilikan) { this.statusKepemilikan = statusKepemilikan; }

    public String getNamaVendor() { return namaVendor; }
    public void setNamaVendor(String namaVendor) { this.namaVendor = namaVendor; }

    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public List<TripAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<TripAssignment> assignments) { this.assignments = assignments; }

    /**
     * Helper method untuk mendapatkan display name.
     */
    public String getDisplayName() {
        if (namaPanggilan != null && !namaPanggilan.isBlank()) {
            return namaPanggilan + " (" + platNomor + ")";
        }
        return platNomor;
    }
}
