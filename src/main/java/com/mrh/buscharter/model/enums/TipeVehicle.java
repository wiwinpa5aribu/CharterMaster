package com.mrh.buscharter.model.enums;

/**
 * Enum untuk tipe kendaraan/armada.
 * Sesuai dengan constraint di tabel vehicles.
 */
public enum TipeVehicle {
    BIG_BUS("Big Bus", 40, 60),
    MEDIUM_BUS("Medium Bus", 25, 35),
    HIACE("Hiace/Commuter", 12, 16),
    ELF("Elf/Minibus", 14, 19),
    MPV("MPV/Innova", 6, 8);

    private final String deskripsi;
    private final int kapasitasMin;
    private final int kapasitasMax;

    TipeVehicle(String deskripsi, int kapasitasMin, int kapasitasMax) {
        this.deskripsi = deskripsi;
        this.kapasitasMin = kapasitasMin;
        this.kapasitasMax = kapasitasMax;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public int getKapasitasMin() {
        return kapasitasMin;
    }

    public int getKapasitasMax() {
        return kapasitasMax;
    }
}
