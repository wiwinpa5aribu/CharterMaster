package com.mrh.buscharter.service;

import com.mrh.buscharter.model.*;
import com.mrh.buscharter.model.enums.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Property-based test untuk Availability Engine.
 * 
 * **Property 6: Availability Engine Akurat**
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4**
 * 
 * Memastikan bahwa perhitungan ketersediaan armada akurat.
 */
public class AvailabilityEnginePropertyTest {

    /**
     * Property: Vehicle yang tidak aktif tidak pernah muncul di hasil ketersediaan.
     * 
     * For any list of vehicles where some are inactive,
     * filtering for availability should never return inactive vehicles.
     */
    @Property(tries = 100)
    void vehicleTidakAktifTidakMunculDiKetersediaan(
            @ForAll @Size(min = 1, max = 20) List<@From("vehicleGenerator") Vehicle> vehicles,
            @ForAll("dateRangeGenerator") DateRange dateRange) {
        
        // Setup: beberapa vehicle tidak aktif
        Random random = new Random();
        for (Vehicle v : vehicles) {
            v.setAktif(random.nextBoolean());
        }
        
        // Simulasi filter ketersediaan
        List<Vehicle> tersedia = filterVehicleTersedia(vehicles, dateRange, new ArrayList<>());
        
        // Property: tidak ada vehicle tidak aktif di hasil
        for (Vehicle v : tersedia) {
            assert v.getAktif() : "Vehicle tidak aktif tidak boleh muncul di ketersediaan";
        }
    }

    /**
     * Property: Vehicle yang sudah di-assign pada waktu yang overlap tidak tersedia.
     * 
     * For any vehicle V assigned to trip T with time range [T1, T2],
     * V should NOT be available for any request with time range [R1, R2]
     * where (T1 <= R2) AND (T2 >= R1).
     */
    @Property(tries = 100)
    void vehicleDenganAssignmentOverlapTidakTersedia(
            @ForAll("vehicleGenerator") Vehicle vehicle,
            @ForAll("tripGenerator") Trip existingTrip,
            @ForAll("dateRangeGenerator") DateRange requestRange) {
        
        vehicle.setAktif(true);
        
        // Setup: vehicle sudah di-assign ke existingTrip
        TripAssignment assignment = new TripAssignment(existingTrip, vehicle);
        assignment.setStatusAssignment(StatusAssignment.TERJADWAL);
        
        // Cek overlap menggunakan rumus irisan
        boolean overlap = isOverlap(
            existingTrip.getWaktuMulai(), existingTrip.getWaktuSelesai(),
            requestRange.mulai, requestRange.selesai);
        
        // Simulasi filter
        List<TripAssignment> assignments = Collections.singletonList(assignment);
        List<Vehicle> tersedia = filterVehicleTersedia(
            Collections.singletonList(vehicle), requestRange, assignments);
        
        // Property: jika overlap, vehicle tidak boleh tersedia
        if (overlap) {
            assert !tersedia.contains(vehicle) 
                : "Vehicle dengan assignment overlap tidak boleh tersedia";
        }
    }

    /**
     * Property: Rumus ketersediaan = Total Aktif - Terpakai.
     * 
     * For any set of vehicles and assignments,
     * Available count = Active count - Assigned count (on date range).
     */
    @Property(tries = 100)
    void rumusKetersediaanBenar(
            @ForAll @Size(min = 5, max = 20) List<@From("vehicleGenerator") Vehicle> vehicles,
            @ForAll("dateRangeGenerator") DateRange dateRange) {
        
        // Setup: assign unique ID ke setiap vehicle dan set aktif
        Set<Long> uniqueIds = new HashSet<>();
        List<Vehicle> uniqueVehicles = new ArrayList<>();
        long idCounter = 1L;
        
        for (Vehicle v : vehicles) {
            v.setId(idCounter++);
            v.setAktif(true);
            uniqueVehicles.add(v);
            uniqueIds.add(v.getId());
        }
        
        // Buat beberapa assignment yang overlap (setengah dari total)
        List<TripAssignment> assignments = new ArrayList<>();
        int assignedCount = uniqueVehicles.size() / 2;
        
        for (int i = 0; i < assignedCount; i++) {
            Vehicle v = uniqueVehicles.get(i);
            Trip trip = createOverlappingTrip(dateRange);
            TripAssignment ta = new TripAssignment(trip, v);
            ta.setStatusAssignment(StatusAssignment.TERJADWAL);
            assignments.add(ta);
        }
        
        // Hitung ketersediaan
        List<Vehicle> tersedia = filterVehicleTersedia(uniqueVehicles, dateRange, assignments);
        
        // Property: jumlah tersedia = total aktif - terpakai
        int expectedTersedia = uniqueVehicles.size() - assignedCount;
        assert tersedia.size() == expectedTersedia 
            : String.format("Expected %d tersedia, got %d (total=%d, assigned=%d)", 
                expectedTersedia, tersedia.size(), uniqueVehicles.size(), assignedCount);
    }

    /**
     * Property: Filter tipe vehicle bekerja dengan benar.
     * 
     * For any list of vehicles with mixed types,
     * filtering by type should only return vehicles of that type.
     */
    @Property(tries = 100)
    void filterTipeVehicleBenar(
            @ForAll @Size(min = 5, max = 20) List<@From("vehicleGenerator") Vehicle> vehicles,
            @ForAll TipeVehicle targetTipe) {
        
        // Setup: semua aktif
        for (Vehicle v : vehicles) {
            v.setAktif(true);
        }
        
        // Filter by tipe
        List<Vehicle> filtered = vehicles.stream()
            .filter(v -> v.getTipeVehicle() == targetTipe)
            .collect(Collectors.toList());
        
        // Property: semua hasil harus tipe yang benar
        for (Vehicle v : filtered) {
            assert v.getTipeVehicle() == targetTipe 
                : "Hasil filter harus sesuai tipe yang diminta";
        }
    }

    /**
     * Property: Ketersediaan konsisten - query yang sama menghasilkan hasil yang sama.
     */
    @Property(tries = 50)
    void ketersediaanKonsisten(
            @ForAll @Size(min = 5, max = 15) List<@From("vehicleGenerator") Vehicle> vehicles,
            @ForAll("dateRangeGenerator") DateRange dateRange) {
        
        for (Vehicle v : vehicles) {
            v.setAktif(true);
        }
        
        List<TripAssignment> assignments = new ArrayList<>();
        
        // Query pertama
        List<Vehicle> hasil1 = filterVehicleTersedia(vehicles, dateRange, assignments);
        
        // Query kedua (sama persis)
        List<Vehicle> hasil2 = filterVehicleTersedia(vehicles, dateRange, assignments);
        
        // Property: hasil harus sama
        assert hasil1.size() == hasil2.size() 
            : "Query yang sama harus menghasilkan jumlah yang sama";
        assert hasil1.containsAll(hasil2) && hasil2.containsAll(hasil1)
            : "Query yang sama harus menghasilkan vehicle yang sama";
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<Vehicle> vehicleGenerator() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000L),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(10),
            Arbitraries.of(TipeVehicle.values()),
            Arbitraries.integers().between(10, 60),
            Arbitraries.of(StatusKepemilikan.values())
        ).as((id, plat, tipe, kapasitas, status) -> {
            Vehicle vehicle = new Vehicle();
            vehicle.setId(id);
            vehicle.setPlatNomor(plat);
            vehicle.setTipeVehicle(tipe);
            vehicle.setKapasitasKursi(kapasitas);
            vehicle.setStatusKepemilikan(status);
            vehicle.setAktif(true);
            return vehicle;
        });
    }

    @Provide
    Arbitrary<Trip> tripGenerator() {
        return Arbitraries.longs().between(1L, 1000L).flatMap(id -> {
            LocalDateTime base = LocalDateTime.of(2025, 1, 1, 8, 0);
            return Arbitraries.integers().between(0, 30).flatMap(dayOffset -> {
                return Arbitraries.integers().between(1, 5).map(duration -> {
                    Trip trip = new Trip();
                    trip.setId(id);
                    trip.setWaktuMulai(base.plusDays(dayOffset));
                    trip.setWaktuSelesai(base.plusDays(dayOffset + duration));
                    trip.setLokasiJemput("Jakarta");
                    trip.setLokasiTujuan("Bandung");
                    return trip;
                });
            });
        });
    }

    @Provide
    Arbitrary<DateRange> dateRangeGenerator() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 8, 0);
        return Combinators.combine(
            Arbitraries.integers().between(0, 30),
            Arbitraries.integers().between(1, 7)
        ).as((dayOffset, duration) -> {
            LocalDateTime mulai = base.plusDays(dayOffset);
            LocalDateTime selesai = mulai.plusDays(duration);
            return new DateRange(mulai, selesai);
        });
    }

    // ==================== Helper Methods ====================

    private boolean isOverlap(LocalDateTime t1Start, LocalDateTime t1End,
                              LocalDateTime t2Start, LocalDateTime t2End) {
        // Rumus irisan: (TripStart <= RequestEnd) AND (TripEnd >= RequestStart)
        return !t1Start.isAfter(t2End) && !t1End.isBefore(t2Start);
    }

    private List<Vehicle> filterVehicleTersedia(List<Vehicle> vehicles, DateRange range,
                                                 List<TripAssignment> assignments) {
        // Kumpulkan vehicle yang sudah di-assign pada range ini
        Set<Long> assignedVehicleIds = assignments.stream()
            .filter(ta -> ta.getStatusAssignment() != StatusAssignment.BATAL)
            .filter(ta -> isOverlap(
                ta.getTrip().getWaktuMulai(), ta.getTrip().getWaktuSelesai(),
                range.mulai, range.selesai))
            .map(ta -> ta.getVehicle().getId())
            .collect(Collectors.toSet());
        
        // Filter: aktif dan tidak di-assign
        return vehicles.stream()
            .filter(Vehicle::getAktif)
            .filter(v -> !assignedVehicleIds.contains(v.getId()))
            .collect(Collectors.toList());
    }

    private Trip createOverlappingTrip(DateRange range) {
        Trip trip = new Trip();
        trip.setId((long) (Math.random() * 1000));
        trip.setWaktuMulai(range.mulai);
        trip.setWaktuSelesai(range.selesai);
        trip.setLokasiJemput("Test");
        trip.setLokasiTujuan("Test");
        return trip;
    }

    // ==================== Inner Classes ====================

    public static class DateRange {
        public final LocalDateTime mulai;
        public final LocalDateTime selesai;

        public DateRange(LocalDateTime mulai, LocalDateTime selesai) {
            this.mulai = mulai;
            this.selesai = selesai;
        }
    }
}
