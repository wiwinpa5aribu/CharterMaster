package com.mrh.buscharter.model.enums;

/**
 * Enum untuk tipe customer/pelanggan.
 * Sesuai dengan constraint di tabel customers.
 */
public enum TipeCustomer {
    KORPORAT("Korporat/Perusahaan"),
    SEKOLAH("Sekolah/Instansi Pendidikan"),
    UMUM("Umum/Perorangan"),
    AGENT("Agent Travel");

    private final String deskripsi;

    TipeCustomer(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
