package com.mrh.buscharter.model;

import com.mrh.buscharter.model.enums.StatusBooking;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity untuk tabel bookings.
 * Header pemesanan/sewa bus.
 * Satu Booking bisa punya banyak Trip.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "booking_code", nullable = false, length = 50)
    private String kodeBooking;

    @Column(name = "booking_date")
    private LocalDateTime tanggalBooking;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private StatusBooking status = StatusBooking.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_pic_id")
    private User salesPic;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String catatanInternal;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trip> trips = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingCharge> charges = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        tanggalBooking = LocalDateTime.now();
        if (status == null) status = StatusBooking.DRAFT;
    }

    // Constructors
    public Booking() {}

    public Booking(Tenant tenant, Customer customer, String kodeBooking) {
        this.tenant = tenant;
        this.customer = customer;
        this.kodeBooking = kodeBooking;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getKodeBooking() { return kodeBooking; }
    public void setKodeBooking(String kodeBooking) { this.kodeBooking = kodeBooking; }

    public LocalDateTime getTanggalBooking() { return tanggalBooking; }
    public void setTanggalBooking(LocalDateTime tanggalBooking) { this.tanggalBooking = tanggalBooking; }

    public StatusBooking getStatus() { return status; }
    public void setStatus(StatusBooking status) { this.status = status; }

    public User getSalesPic() { return salesPic; }
    public void setSalesPic(User salesPic) { this.salesPic = salesPic; }

    public String getCatatanInternal() { return catatanInternal; }
    public void setCatatanInternal(String catatanInternal) { this.catatanInternal = catatanInternal; }

    public List<Trip> getTrips() { return trips; }
    public void setTrips(List<Trip> trips) { this.trips = trips; }

    public List<BookingCharge> getCharges() { return charges; }
    public void setCharges(List<BookingCharge> charges) { this.charges = charges; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    // Helper methods
    public void addTrip(Trip trip) {
        trips.add(trip);
        trip.setBooking(this);
    }

    public void removeTrip(Trip trip) {
        trips.remove(trip);
        trip.setBooking(null);
    }

    public void addCharge(BookingCharge charge) {
        charges.add(charge);
        charge.setBooking(this);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setBooking(this);
    }
}
