package com.mrh.buscharter.event;

import com.mrh.buscharter.model.Payment;

import java.math.BigDecimal;

/**
 * Event yang di-emit saat pembayaran diterima.
 */
public class PaymentReceivedEvent extends DomainEvent {

    private final Long paymentId;
    private final Long bookingId;
    private final String kodeBooking;
    private final BigDecimal jumlah;
    private final String metode;

    public PaymentReceivedEvent(Payment payment, String kodeBooking, Long tenantId) {
        super(tenantId);
        this.paymentId = payment.getId();
        this.bookingId = payment.getBooking().getId();
        this.kodeBooking = kodeBooking;
        this.jumlah = payment.getJumlah();
        this.metode = payment.getMetode();
    }

    @Override
    public String getEventName() {
        return "PaymentReceived";
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getKodeBooking() {
        return kodeBooking;
    }

    public BigDecimal getJumlah() {
        return jumlah;
    }

    public String getMetode() {
        return metode;
    }

    @Override
    public String toString() {
        return String.format("PaymentReceivedEvent{bookingId=%d, kode=%s, jumlah=%s, metode=%s}",
            bookingId, kodeBooking, jumlah, metode);
    }
}
