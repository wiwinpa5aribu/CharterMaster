package com.mrh.buscharter.model.enums;

/**
 * Enum untuk status kepemilikan kendaraan.
 * Sesuai dengan constraint di tabel vehicles.
 */
public enum StatusKepemilikan {
    MILIK_SENDIRI("Milik Sendiri"),
    MITRA_VENDOR("Mitra/Vendor/Subkon");

    private final String deskripsi;

    StatusKepemilikan(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
