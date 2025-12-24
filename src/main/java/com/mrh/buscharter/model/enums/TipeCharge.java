package com.mrh.buscharter.model.enums;

/**
 * Enum untuk tipe komponen biaya booking.
 * Sesuai dengan constraint di tabel booking_charges.
 */
public enum TipeCharge {
    UTAMA("Biaya Utama"),
    TAMBAHAN("Biaya Tambahan"),
    DISKON("Diskon/Potongan");

    private final String deskripsi;

    TipeCharge(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
