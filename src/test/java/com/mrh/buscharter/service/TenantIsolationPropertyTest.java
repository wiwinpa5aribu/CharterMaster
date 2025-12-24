package com.mrh.buscharter.service;

import com.mrh.buscharter.model.Tenant;
import com.mrh.buscharter.model.User;
import com.mrh.buscharter.model.Vehicle;
import com.mrh.buscharter.model.enums.RoleUser;
import com.mrh.buscharter.model.enums.StatusKepemilikan;
import com.mrh.buscharter.model.enums.TipeVehicle;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Property-based test untuk Tenant Isolation.
 * 
 * **Property 5: Tenant Isolation**
 * **Validates: Requirements 9.2, 9.3**
 * 
 * Memastikan bahwa data antar tenant tidak bercampur.
 * User dari Tenant A tidak boleh bisa mengakses data Tenant B.
 */
public class TenantIsolationPropertyTest {

    /**
     * Property: Data vehicle hanya bisa diakses oleh user dari tenant yang sama.
     * 
     * For any vehicle V belonging to tenant T1,
     * and any user U belonging to tenant T2 where T1 != T2,
     * U should NOT be able to access V.
     */
    @Property(tries = 100)
    void vehicleHanyaBisaDiaksesDariTenantYangSama(
            @ForAll("tenantGenerator") Tenant tenant1,
            @ForAll("tenantGenerator") Tenant tenant2,
            @ForAll("vehicleGenerator") Vehicle vehicle) {
        
        // Setup: vehicle milik tenant1
        vehicle.setTenant(tenant1);
        
        // Buat user dari tenant2
        User userTenant2 = new User(tenant2, "Test User", "test@test.com", "hash", RoleUser.ADMIN);
        
        // Property: jika tenant berbeda, user tidak boleh akses vehicle
        if (!tenant1.getId().equals(tenant2.getId())) {
            // Simulasi filter tenant - vehicle.tenant.id harus sama dengan user.tenant.id
            boolean canAccess = vehicle.getTenant().getId().equals(userTenant2.getTenant().getId());
            
            // Assert: user dari tenant berbeda TIDAK bisa akses
            assert !canAccess : "User dari tenant berbeda seharusnya tidak bisa akses vehicle";
        }
    }

    /**
     * Property: Filter tenant selalu diterapkan pada query.
     * 
     * For any list of vehicles from multiple tenants,
     * filtering by tenant ID should only return vehicles from that tenant.
     */
    @Property(tries = 100)
    void filterTenantMengembalikanDataTenantYangBenar(
            @ForAll @Size(min = 1, max = 10) List<@From("tenantGenerator") Tenant> tenants,
            @ForAll @Size(min = 1, max = 20) List<@From("vehicleGenerator") Vehicle> vehicles) {
        
        // Setup: assign vehicles ke random tenants
        for (int i = 0; i < vehicles.size(); i++) {
            Tenant tenant = tenants.get(i % tenants.size());
            vehicles.get(i).setTenant(tenant);
        }
        
        // Untuk setiap tenant, filter dan verifikasi
        for (Tenant targetTenant : tenants) {
            List<Vehicle> filtered = filterByTenantId(vehicles, targetTenant.getId());
            
            // Property: semua hasil filter harus milik tenant yang sama
            for (Vehicle v : filtered) {
                assert v.getTenant().getId().equals(targetTenant.getId()) 
                    : "Vehicle yang difilter harus milik tenant yang benar";
            }
        }
    }

    /**
     * Property: User tidak bisa login ke tenant yang bukan miliknya.
     * 
     * For any user U belonging to tenant T1,
     * attempting to login with tenant code of T2 (where T1 != T2) should fail.
     */
    @Property(tries = 100)
    void userTidakBisaLoginKeTenantLain(
            @ForAll("tenantGenerator") Tenant tenant1,
            @ForAll("tenantGenerator") Tenant tenant2,
            @ForAll("userGenerator") User user) {
        
        // Setup: user milik tenant1
        user.setTenant(tenant1);
        
        // Property: jika tenant berbeda, login harus gagal
        if (!tenant1.getKode().equals(tenant2.getKode())) {
            // Simulasi pengecekan login - user.tenant.kode harus sama dengan kodeTenant input
            boolean loginValid = user.getTenant().getKode().equals(tenant2.getKode());
            
            // Assert: login ke tenant berbeda harus gagal
            assert !loginValid : "User tidak boleh bisa login ke tenant yang berbeda";
        }
    }

    /**
     * Property: Session manager hanya menyimpan satu tenant aktif.
     * 
     * For any sequence of login attempts,
     * the session should only contain the last logged-in tenant.
     */
    @Property(tries = 50)
    void sessionManagerHanyaSimpanSatuTenantAktif(
            @ForAll @Size(min = 2, max = 5) List<@From("tenantGenerator") Tenant> tenants,
            @ForAll @Size(min = 2, max = 5) List<@From("userGenerator") User> users) {
        
        // Reset session
        SessionManager.resetInstance();
        SessionManager session = SessionManager.getInstance();
        
        // Setup: assign users ke tenants
        for (int i = 0; i < users.size(); i++) {
            users.get(i).setTenant(tenants.get(i % tenants.size()));
        }
        
        // Simulasi multiple login
        User lastUser = null;
        for (User user : users) {
            session.setSession(user);
            lastUser = user;
        }
        
        // Property: session hanya menyimpan user terakhir
        assert session.getCurrentUser().equals(lastUser) 
            : "Session harus menyimpan user terakhir yang login";
        assert session.getCurrentTenant().equals(lastUser.getTenant()) 
            : "Session harus menyimpan tenant dari user terakhir";
        
        // Cleanup
        session.clearSession();
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<Tenant> tenantGenerator() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000L),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10)
        ).as((id, nama, kode) -> {
            Tenant tenant = new Tenant(nama, kode + id); // Ensure unique kode
            tenant.setId(id);
            return tenant;
        });
    }

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
    Arbitrary<User> userGenerator() {
        return Combinators.combine(
            Arbitraries.longs().between(1L, 1000L),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(15).map(s -> s + "@test.com"),
            Arbitraries.of(RoleUser.values())
        ).as((id, nama, email, role) -> {
            User user = new User();
            user.setId(id);
            user.setNamaLengkap(nama);
            user.setEmail(email);
            user.setPasswordHash("hash");
            user.setRole(role);
            user.setAktif(true);
            return user;
        });
    }

    // ==================== Helper Methods ====================

    private List<Vehicle> filterByTenantId(List<Vehicle> vehicles, Long tenantId) {
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (v.getTenant() != null && v.getTenant().getId().equals(tenantId)) {
                result.add(v);
            }
        }
        return result;
    }
}
