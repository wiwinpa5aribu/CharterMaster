package com.mrh.buscharter.model;

import com.mrh.buscharter.model.enums.TipeCustomer;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity untuk tabel customers.
 * Pelanggan penyewa (Perusahaan atau Perorangan).
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "name", nullable = false, length = 100)
    private String nama;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private TipeCustomer tipe;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "phone", nullable = false, length = 50)
    private String telepon;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String alamat;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    // Constructors
    public Customer() {}

    public Customer(Tenant tenant, String nama, String telepon) {
        this.tenant = tenant;
        this.nama = nama;
        this.telepon = telepon;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public TipeCustomer getTipe() { return tipe; }
    public void setTipe(TipeCustomer tipe) { this.tipe = tipe; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}
