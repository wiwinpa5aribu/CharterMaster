package com.mrh.buscharter.service;

import com.mrh.buscharter.model.*;
import com.mrh.buscharter.model.enums.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Property-based test untuk Anti-Double Booking.
 * 
 * **Property 1: Anti-Double Booking**
 * **Validates: Requirements 1.4, 6.2, 6.5**
 * 
 * Memastikan bahwa satu bus tidak bisa di-assign ke dua trip yang waktunya overlap.
 */
public class AntiDoubleBookingPropertyTest {

    /**
     * Property: Satu vehicle tidak bisa di-assign ke dua trip yang overlap.
     * 
     * For any vehicle V and two trips T1, T2 where time ranges overlap,
     * if V is assigned to T1, then assigning V to T2 should be rejected.
     */
    @Property(tries = 100)
    void vehicleTidakBisaDoubleBooking(
            @ForAll("vehicleGenerator") Vehicle vehicle,
            @ForAll("overlappingTripsGenerator") OverlappingTrips trips) {
        
        // Setup: vehicle sudah di-assign ke trip1
        TripAssignment existingAssignment = new TripAssignment(trips.trip1, vehicle);
        existingAssignment.setStatusAssignment(StatusAssignment.TERJADWAL);
        
        List<TripAssignment> existingAssignments = Collections.singletonList(existingAssignment);
        
        // Cek apakah bisa assign ke trip2 (yang overlap)
        boolean canAssign = canAssignVehicle(vehicle, trips.trip2, existingAssignments);
        
        // Property: tidak boleh bisa assign karena overlap
        assert !canAssign : "Vehicle tidak boleh di-assign ke trip yang overlap";
    }

    /**
     * Property: Vehicle bisa di-assign ke trip yang tidak overlap.
     * 
     * For any vehicle V and two trips T1, T2 where time ranges do NOT overlap,
     * if V is assigned to T1, then assigning V to T2 should be allowed.
     */
    @Property(tries = 100)
    void vehicleBisaDiAssignKeTripTidakOverlap(
            @ForAll("vehicleGenerator") Vehicle vehicle,
            @ForAll("nonOverlappingTripsGenerator") NonOverlappingTrips trips) {
        
        // Setup: vehicle sudah di-assign ke trip1
        TripAssignment existingAssignment = new TripAssignment(trips.trip1, vehicle);
        existingAssignment.setStatusAssignment(StatusAssignment.TERJADWAL);
        
        List<TripAssignment> existingAssignments = Collections.singletonList(existingAssignment);
        
        // Cek apakah bisa assign ke trip2 (yang tidak overlap)
        boolean canAssign = canAssignVehicle(vehicle, trips.trip2, existingAssignments);
        
        // Property: harus bisa assign karena tidak overlap
        assert canAssign : "Vehicle harus bisa di-assign ke trip yang tidak overlap";
    }

    /**
     * Property: Assignment dengan status BATAL tidak menghalangi assignment baru.
     * 
     * For any vehicle V with a CANCELLED assignment to trip T1,
     * V should be available for assignment to trip T2 even if times overlap.
     */
    @Property(tries = 100)
    void assignmentBatalTidakMenghalangi(
            @ForAll("vehicleGenerator") Vehicle vehicle,
            @ForAll("overlappingTripsGenerator") OverlappingTrips trips) {
        
        // Setup: vehicle punya assignment BATAL ke trip1
        TripAssignment cancelledAssignment = new TripAssignment(trips.trip1, vehicle);
        cancelledAssignment.setStatusAssignment(StatusAssignment.BATAL);
        
        List<TripAssignment> existingAssignments = Collections.singletonList(cancelledAssignment);
        
        // Cek apakah bisa assign ke trip2
        boolean canAssign = canAssignVehicle(vehicle, trips.trip2, existingAssignments);
        
        // Property: harus bisa assign karena assignment sebelumnya BATAL
        assert canAssign : "Assignment BATAL tidak boleh menghalangi assignment baru";
    }

    /**
     * Property: Validasi konflik mendeteksi semua jenis overlap.
     * 
     * Tests all overlap scenarios:
     * 1. T2 completely inside T1
     * 2. T1 completely inside T2
     * 3. T2 starts before T1 ends
     * 4. T1 starts before T2 ends
     */
    @Property(tries = 100)
    void validasiKonflikMendeteksiSemuaOverlap(
            @ForAll("vehicleGenerator") Vehicle vehicle,
            @ForAll("allOverlapScenariosGenerator") OverlapScenario scenario) {
        
        // Setup: vehicle sudah di-assign ke trip1
        TripAssignment existingAssignment = new TripAssignment(scenario.trip1, vehicle);
        existingAssignment.setStatusAssignment(StatusAssignment.TERJADWAL);
        
        List<TripAssignment> existingAssignments = Collections.singletonList(existingAssignment);
        
        // Cek overlap
        boolean isOverlap = isTimeOverlap(
            scenario.trip1.getWaktuMulai(), scenario.trip1.getWaktuSelesai(),
            scenario.trip2.getWaktuMulai(), scenario.trip2.getWaktuSelesai());
        
        boolean canAssign = canAssignVehicle(vehicle, scenario.trip2, existingAssignments);
        
        // Property: jika overlap, tidak boleh assign; jika tidak overlap, boleh assign
        if (isOverlap) {
            assert !canAssign : "Overlap terdeteksi tapi assignment diizinkan";
        } else {
            assert canAssign : "Tidak ada overlap tapi assignment ditolak";
        }
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<Vehicle> vehicleGenerator() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000L),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(10),
            Arbitraries.of(TipeVehicle.values())
        ).as((id, plat, tipe) -> {
            Vehicle vehicle = new Vehicle();
            vehicle.setId(id);
            vehicle.setPlatNomor(plat);
            vehicle.setTipeVehicle(tipe);
            vehicle.setKapasitasKursi(40);
            vehicle.setStatusKepemilikan(StatusKepemilikan.MILIK_SENDIRI);
            vehicle.setAktif(true);
            return vehicle;
        });
    }

    @Provide
    Arbitrary<OverlappingTrips> overlappingTripsGenerator() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 15, 8, 0);
        
        return Combinators.combine(
            Arbitraries.integers().between(0, 5),  // trip1 start offset
            Arbitraries.integers().between(3, 10), // trip1 duration (cukup panjang)
            Arbitraries.integers().between(1, 2)   // trip2 start offset dari trip1 start (pasti di dalam trip1)
        ).as((t1Offset, t1Duration, t2Offset) -> {
            Trip trip1 = createTrip(1L, base.plusDays(t1Offset), t1Duration);
            // trip2 starts DURING trip1 dan berakhir sebelum trip1 selesai (guaranteed overlap)
            // trip2 mulai di hari ke t2Offset dari trip1 start, dengan durasi lebih pendek
            Trip trip2 = createTrip(2L, base.plusDays(t1Offset).plusDays(t2Offset), Math.min(2, t1Duration - t2Offset));
            return new OverlappingTrips(trip1, trip2);
        });
    }

    @Provide
    Arbitrary<NonOverlappingTrips> nonOverlappingTripsGenerator() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 15, 8, 0);
        
        return Combinators.combine(
            Arbitraries.integers().between(0, 5),   // trip1 start offset
            Arbitraries.integers().between(2, 5),   // trip1 duration
            Arbitraries.integers().between(1, 10)   // gap between trips
        ).as((t1Offset, t1Duration, gap) -> {
            Trip trip1 = createTrip(1L, base.plusDays(t1Offset), t1Duration);
            // trip2 starts after trip1 ends + gap
            Trip trip2 = createTrip(2L, base.plusDays(t1Offset + t1Duration + gap), 3);
            return new NonOverlappingTrips(trip1, trip2);
        });
    }

    @Provide
    Arbitrary<OverlapScenario> allOverlapScenariosGenerator() {
        LocalDateTime base = LocalDateTime.of(2025, 1, 15, 8, 0);
        
        return Arbitraries.integers().between(0, 4).flatMap(scenarioType -> {
            return Arbitraries.integers().between(0, 10).map(offset -> {
                Trip trip1, trip2;
                
                switch (scenarioType) {
                    case 0: // T2 completely inside T1
                        trip1 = createTrip(1L, base.plusDays(offset), 10);
                        trip2 = createTrip(2L, base.plusDays(offset + 2), 3);
                        break;
                    case 1: // T1 completely inside T2
                        trip1 = createTrip(1L, base.plusDays(offset + 2), 3);
                        trip2 = createTrip(2L, base.plusDays(offset), 10);
                        break;
                    case 2: // Partial overlap - T2 starts before T1 ends
                        trip1 = createTrip(1L, base.plusDays(offset), 5);
                        trip2 = createTrip(2L, base.plusDays(offset + 3), 5);
                        break;
                    case 3: // No overlap
                        trip1 = createTrip(1L, base.plusDays(offset), 3);
                        trip2 = createTrip(2L, base.plusDays(offset + 10), 3);
                        break;
                    default: // Same time
                        trip1 = createTrip(1L, base.plusDays(offset), 5);
                        trip2 = createTrip(2L, base.plusDays(offset), 5);
                        break;
                }
                
                return new OverlapScenario(trip1, trip2);
            });
        });
    }

    // ==================== Helper Methods ====================

    private Trip createTrip(Long id, LocalDateTime start, int durationDays) {
        Trip trip = new Trip();
        trip.setId(id);
        trip.setWaktuMulai(start);
        trip.setWaktuSelesai(start.plusDays(durationDays));
        trip.setLokasiJemput("Jakarta");
        trip.setLokasiTujuan("Bandung");
        return trip;
    }

    private boolean isTimeOverlap(LocalDateTime t1Start, LocalDateTime t1End,
                                   LocalDateTime t2Start, LocalDateTime t2End) {
        // Rumus irisan: (T1Start <= T2End) AND (T1End >= T2Start)
        return !t1Start.isAfter(t2End) && !t1End.isBefore(t2Start);
    }

    private boolean canAssignVehicle(Vehicle vehicle, Trip newTrip, 
                                      List<TripAssignment> existingAssignments) {
        for (TripAssignment ta : existingAssignments) {
            // Skip assignment yang BATAL
            if (ta.getStatusAssignment() == StatusAssignment.BATAL) {
                continue;
            }
            
            // Cek apakah vehicle sama
            if (!ta.getVehicle().getId().equals(vehicle.getId())) {
                continue;
            }
            
            // Cek overlap
            if (isTimeOverlap(
                    ta.getTrip().getWaktuMulai(), ta.getTrip().getWaktuSelesai(),
                    newTrip.getWaktuMulai(), newTrip.getWaktuSelesai())) {
                return false;
            }
        }
        return true;
    }

    // ==================== Inner Classes ====================

    public static class OverlappingTrips {
        public final Trip trip1;
        public final Trip trip2;

        public OverlappingTrips(Trip trip1, Trip trip2) {
            this.trip1 = trip1;
            this.trip2 = trip2;
        }
    }

    public static class NonOverlappingTrips {
        public final Trip trip1;
        public final Trip trip2;

        public NonOverlappingTrips(Trip trip1, Trip trip2) {
            this.trip1 = trip1;
            this.trip2 = trip2;
        }
    }

    public static class OverlapScenario {
        public final Trip trip1;
        public final Trip trip2;

        public OverlapScenario(Trip trip1, Trip trip2) {
            this.trip1 = trip1;
            this.trip2 = trip2;
        }
    }
}
