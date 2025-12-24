package com.mrh.buscharter.model.enums;

/**
 * Enum untuk status booking/pesanan.
 * Mengikuti state machine: DRAFT → QUOTATION_SENT → DP_DITERIMA → LUNAS → SELESAI
 * Dengan kemungkinan BATAL dari status QUOTATION_SENT atau DP_DITERIMA.
 */
public enum StatusBooking {
    DRAFT("Draft", "Abu-abu"),
    QUOTATION_SENT("Quotation Terkirim", "Biru"),
    DP_DITERIMA("DP Diterima", "Kuning"),
    LUNAS("Lunas", "Hijau"),
    SELESAI("Selesai", "Hijau Tua"),
    BATAL("Batal", "Merah");

    private final String deskripsi;
    private final String warna;

    StatusBooking(String deskripsi, String warna) {
        this.deskripsi = deskripsi;
        this.warna = warna;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getWarna() {
        return warna;
    }

    /**
     * Cek apakah transisi ke status baru valid.
     * Aturan: DRAFT→QUOTATION_SENT→DP_DITERIMA→LUNAS→SELESAI
     * BATAL hanya dari QUOTATION_SENT atau DP_DITERIMA
     */
    public boolean bisaTransisiKe(StatusBooking statusBaru) {
        return switch (this) {
            case DRAFT -> statusBaru == QUOTATION_SENT;
            case QUOTATION_SENT -> statusBaru == DP_DITERIMA || statusBaru == BATAL;
            case DP_DITERIMA -> statusBaru == LUNAS || statusBaru == BATAL;
            case LUNAS -> statusBaru == SELESAI;
            case SELESAI, BATAL -> false; // Status final, tidak bisa berubah
        };
    }
}
