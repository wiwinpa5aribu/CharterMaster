package com.mrh.buscharter.model.enums;

/**
 * Enum untuk role/peran pengguna dalam sistem.
 * Sesuai dengan constraint di tabel users.
 */
public enum RoleUser {
    ADMIN("Admin"),
    SALES("Sales/Marketing"),
    OPS("Operasional"),
    SOPIR("Sopir/Driver"),
    KEUANGAN("Keuangan/Finance");

    private final String deskripsi;

    RoleUser(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
