package com.mrh.buscharter.model.enums;

/**
 * Enum untuk status penugasan armada ke trip.
 * Sesuai dengan constraint di tabel trip_assignments.
 */
public enum StatusAssignment {
    TERJADWAL("Terjadwal"),
    DALAM_PERJALANAN("Dalam Perjalanan"),
    SELESAI("Selesai"),
    BATAL("Batal");

    private final String deskripsi;

    StatusAssignment(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
