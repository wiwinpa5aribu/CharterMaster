package com.mrh.buscharter.event;

import com.mrh.buscharter.model.Booking;

/**
 * Event yang di-emit saat booking dikonfirmasi (status DP_DITERIMA).
 * 
 * Event ini digunakan untuk:
 * - Notifikasi ke modul Fleet untuk persiapan assignment
 * - Logging audit trail
 * - Trigger notifikasi ke customer (future)
 */
public class BookingConfirmedEvent extends DomainEvent {

    private final Long bookingId;
    private final String kodeBooking;
    private final Long customerId;
    private final String namaCustomer;
    private final int jumlahTrip;

    public BookingConfirmedEvent(Booking booking) {
        super(booking.getTenant().getId());
        this.bookingId = booking.getId();
        this.kodeBooking = booking.getKodeBooking();
        this.customerId = booking.getCustomer().getId();
        this.namaCustomer = booking.getCustomer().getNama();
        this.jumlahTrip = booking.getTrips().size();
    }

    @Override
    public String getEventName() {
        return "BookingConfirmed";
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getKodeBooking() {
        return kodeBooking;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getNamaCustomer() {
        return namaCustomer;
    }

    public int getJumlahTrip() {
        return jumlahTrip;
    }

    @Override
    public String toString() {
        return String.format("BookingConfirmedEvent{bookingId=%d, kode=%s, customer=%s, trips=%d}",
            bookingId, kodeBooking, namaCustomer, jumlahTrip);
    }
}
