package com.mrh.buscharter.service;

import com.mrh.buscharter.model.Driver;
import com.mrh.buscharter.model.Trip;
import com.mrh.buscharter.model.TripAssignment;
import com.mrh.buscharter.model.Vehicle;
import com.mrh.buscharter.model.enums.StatusAssignment;
import com.mrh.buscharter.model.enums.StatusKepemilikan;
import com.mrh.buscharter.model.enums.TipeVehicle;
import com.mrh.buscharter.repository.DriverRepository;
import com.mrh.buscharter.repository.TripAssignmentRepository;
import com.mrh.buscharter.repository.TripRepository;
import com.mrh.buscharter.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service untuk modul Fleet/Armada.
 * Menangani Availability Engine dan Assignment.
 */
public class FleetService {

    private static final Logger logger = LoggerFactory.getLogger(FleetService.class);
    
    // Buffer waktu minimum antara trip (4 jam untuk cuci bus & istirahat driver)
    private static final int BUFFER_JAM_MINIMUM = 4;
    
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;
    private final TripAssignmentRepository tripAssignmentRepository;

    public FleetService() {
        this.vehicleRepository = new VehicleRepository();
        this.driverRepository = new DriverRepository();
        this.tripRepository = new TripRepository();
        this.tripAssignmentRepository = new TripAssignmentRepository();
    }

    // ==================== AVAILABILITY ENGINE ====================

    /**
     * Cek ketersediaan armada pada rentang tanggal tertentu.
     * 
     * Rumus: Available = Total_Active_Bus - Assigned_Bus_on_Date_Range
     * 
     * @param tenantId ID tenant
     * @param tanggalMulai Tanggal mulai yang diminta
     * @param tanggalSelesai Tanggal selesai yang diminta
     * @param tipeBus Tipe bus (opsional, null untuk semua tipe)
     * @return List vehicle yang tersedia
     */
    public List<Vehicle> cekKetersediaan(Long tenantId, LocalDateTime tanggalMulai, 
                                          LocalDateTime tanggalSelesai, TipeVehicle tipeBus) {
        logger.info("Cek ketersediaan armada: {} - {} untuk tipe: {}", 
            tanggalMulai, tanggalSelesai, tipeBus);
        
        return vehicleRepository.findVehicleTersedia(tenantId, tanggalMulai, tanggalSelesai, tipeBus);
    }

    /**
     * Hitung ketersediaan per tipe bus pada rentang tanggal tertentu.
     * 
     * @return Map dengan key = TipeVehicle, value = jumlah tersedia
     */
    public Map<TipeVehicle, Long> hitungKetersediaanPerTipe(Long tenantId, 
            LocalDateTime tanggalMulai, LocalDateTime tanggalSelesai) {
        
        Map<TipeVehicle, Long> hasil = new EnumMap<>(TipeVehicle.class);
        
        for (TipeVehicle tipe : TipeVehicle.values()) {
            long jumlah = vehicleRepository.countVehicleTersediaByTipe(
                tenantId, tanggalMulai, tanggalSelesai, tipe);
            hasil.put(tipe, jumlah);
        }
        
        logger.info("Ketersediaan per tipe: {}", hasil);
        return hasil;
    }

    /**
     * Cek apakah ada vehicle tersedia untuk tipe tertentu.
     */
    public boolean adaVehicleTersedia(Long tenantId, LocalDateTime tanggalMulai, 
                                       LocalDateTime tanggalSelesai, TipeVehicle tipeBus) {
        List<Vehicle> tersedia = cekKetersediaan(tenantId, tanggalMulai, tanggalSelesai, tipeBus);
        return !tersedia.isEmpty();
    }

    /**
     * Cari driver yang tersedia pada rentang tanggal tertentu.
     */
    public List<Driver> cekKetersediaanDriver(Long tenantId, LocalDateTime tanggalMulai, 
                                               LocalDateTime tanggalSelesai) {
        return driverRepository.findDriverTersedia(tenantId, tanggalMulai, tanggalSelesai);
    }

    // ==================== ASSIGNMENT ====================

    /**
     * Assign bus ke trip dengan validasi konflik.
     * 
     * @param tripId ID trip
     * @param vehicleId ID vehicle
     * @param driverId ID driver (opsional)
     * @param coDriverId ID co-driver (opsional)
     * @return TripAssignment yang dibuat
     * @throws IllegalStateException jika ada konflik jadwal
     */
    public TripAssignment assignBusKeTrip(Long tripId, Long vehicleId, 
                                           Long driverId, Long coDriverId) {
        logger.info("Assign vehicle {} ke trip {}", vehicleId, tripId);
        
        // Ambil trip
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trip tidak ditemukan: " + tripId));
        
        // Ambil vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle tidak ditemukan: " + vehicleId));
        
        // Validasi konflik jadwal
        HasilValidasiKonflik validasi = cekKonflikJadwal(vehicleId, 
            trip.getWaktuMulai(), trip.getWaktuSelesai(), tripId);
        
        if (validasi.adaKonflik()) {
            throw new IllegalStateException("Konflik jadwal: " + validasi.getPesan());
        }
        
        // Buat assignment
        TripAssignment assignment = new TripAssignment(trip, vehicle);
        assignment.setStatusAssignment(StatusAssignment.TERJADWAL);
        
        // Set driver jika ada
        if (driverId != null) {
            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver tidak ditemukan: " + driverId));
            assignment.setDriver(driver);
        }
        
        // Set co-driver jika ada
        if (coDriverId != null) {
            Driver coDriver = driverRepository.findById(coDriverId)
                .orElseThrow(() -> new IllegalArgumentException("Co-driver tidak ditemukan: " + coDriverId));
            assignment.setCoDriver(coDriver);
        }
        
        // Simpan
        TripAssignment saved = tripAssignmentRepository.save(assignment);
        logger.info("Assignment berhasil dibuat: {}", saved.getId());
        
        // Log warning jika jeda kurang dari 4 jam
        if (validasi.adaWarning()) {
            logger.warn("Warning: {}", validasi.getPesan());
        }
        
        return saved;
    }

    /**
     * Cek konflik jadwal untuk vehicle.
     * 
     * @return HasilValidasiKonflik dengan status konflik dan warning
     */
    public HasilValidasiKonflik cekKonflikJadwal(Long vehicleId, LocalDateTime mulai, 
                                                  LocalDateTime selesai, Long excludeTripId) {
        // Cek overlap langsung
        boolean adaOverlap = tripAssignmentRepository.hasKonflikJadwal(
            vehicleId, mulai, selesai, excludeTripId);
        
        if (adaOverlap) {
            return new HasilValidasiKonflik(true, false, 
                "Vehicle sudah di-assign ke trip lain pada waktu yang sama");
        }
        
        // Cek jeda dengan trip sebelumnya
        Optional<TripAssignment> tripSebelumnya = tripAssignmentRepository
            .findLastAssignmentByVehicleId(vehicleId, mulai);
        
        if (tripSebelumnya.isPresent()) {
            LocalDateTime selesaiSebelumnya = tripSebelumnya.get().getTrip().getWaktuSelesai();
            long jedaJam = Duration.between(selesaiSebelumnya, mulai).toHours();
            
            if (jedaJam < BUFFER_JAM_MINIMUM) {
                return new HasilValidasiKonflik(false, true, 
                    String.format("Jeda dengan trip sebelumnya hanya %d jam (minimum %d jam)", 
                        jedaJam, BUFFER_JAM_MINIMUM));
            }
        }
        
        // Cek jeda dengan trip berikutnya
        Optional<TripAssignment> tripBerikutnya = tripAssignmentRepository
            .findNextAssignmentByVehicleId(vehicleId, selesai);
        
        if (tripBerikutnya.isPresent()) {
            LocalDateTime mulaiBerikutnya = tripBerikutnya.get().getTrip().getWaktuMulai();
            long jedaJam = Duration.between(selesai, mulaiBerikutnya).toHours();
            
            if (jedaJam < BUFFER_JAM_MINIMUM) {
                return new HasilValidasiKonflik(false, true, 
                    String.format("Jeda dengan trip berikutnya hanya %d jam (minimum %d jam)", 
                        jedaJam, BUFFER_JAM_MINIMUM));
            }
        }
        
        return new HasilValidasiKonflik(false, false, "OK");
    }

    // ==================== CRUD VEHICLE ====================

    /**
     * Tambah vehicle baru.
     */
    public Vehicle tambahVehicle(Long tenantId, String platNomor, String namaPanggilan,
                                  TipeVehicle tipe, Integer kapasitas, 
                                  StatusKepemilikan statusKepemilikan, String namaVendor) {
        logger.info("Tambah vehicle baru: {}", platNomor);
        
        // Validasi plat nomor unik per tenant
        if (vehicleRepository.findByPlatNomorAndTenantId(platNomor, tenantId).isPresent()) {
            throw new IllegalArgumentException("Plat nomor sudah terdaftar: " + platNomor);
        }
        
        Vehicle vehicle = new Vehicle();
        vehicle.setPlatNomor(platNomor);
        vehicle.setNamaPanggilan(namaPanggilan);
        vehicle.setTipeVehicle(tipe);
        vehicle.setKapasitasKursi(kapasitas);
        vehicle.setStatusKepemilikan(statusKepemilikan);
        vehicle.setNamaVendor(namaVendor);
        vehicle.setAktif(true);
        
        // Tenant akan di-set oleh caller atau dari session
        
        return vehicleRepository.save(vehicle);
    }

    /**
     * Update vehicle.
     */
    public Vehicle updateVehicle(Long vehicleId, String platNomor, String namaPanggilan,
                                  TipeVehicle tipe, Integer kapasitas, 
                                  StatusKepemilikan statusKepemilikan, String namaVendor) {
        logger.info("Update vehicle: {}", vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle tidak ditemukan: " + vehicleId));
        
        vehicle.setPlatNomor(platNomor);
        vehicle.setNamaPanggilan(namaPanggilan);
        vehicle.setTipeVehicle(tipe);
        vehicle.setKapasitasKursi(kapasitas);
        vehicle.setStatusKepemilikan(statusKepemilikan);
        vehicle.setNamaVendor(namaVendor);
        
        return vehicleRepository.save(vehicle);
    }

    /**
     * Nonaktifkan vehicle (soft delete).
     */
    public void nonaktifkanVehicle(Long vehicleId) {
        logger.info("Nonaktifkan vehicle: {}", vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle tidak ditemukan: " + vehicleId));
        
        vehicle.setAktif(false);
        vehicleRepository.save(vehicle);
    }

    /**
     * Aktifkan kembali vehicle.
     */
    public void aktifkanVehicle(Long vehicleId) {
        logger.info("Aktifkan vehicle: {}", vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle tidak ditemukan: " + vehicleId));
        
        vehicle.setAktif(true);
        vehicleRepository.save(vehicle);
    }

    /**
     * Ambil semua vehicle aktif untuk tenant.
     */
    public List<Vehicle> getVehicleAktif(Long tenantId) {
        return vehicleRepository.findAktifByTenantId(tenantId);
    }

    /**
     * Ambil vehicle berdasarkan tipe.
     */
    public List<Vehicle> getVehicleByTipe(Long tenantId, TipeVehicle tipe) {
        return vehicleRepository.findByTipeAndTenantId(tipe, tenantId);
    }

    // ==================== INNER CLASS ====================

    /**
     * Hasil validasi konflik jadwal.
     */
    public static class HasilValidasiKonflik {
        private final boolean konflik;
        private final boolean warning;
        private final String pesan;

        public HasilValidasiKonflik(boolean konflik, boolean warning, String pesan) {
            this.konflik = konflik;
            this.warning = warning;
            this.pesan = pesan;
        }

        public boolean adaKonflik() { return konflik; }
        public boolean adaWarning() { return warning; }
        public String getPesan() { return pesan; }
    }
}
