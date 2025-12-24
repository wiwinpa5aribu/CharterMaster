package com.mrh.buscharter.repository;

import com.mrh.buscharter.model.Vehicle;
import com.mrh.buscharter.model.enums.StatusKepemilikan;
import com.mrh.buscharter.model.enums.TipeVehicle;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository untuk entity Vehicle.
 * Termasuk query untuk Availability Engine.
 */
public class VehicleRepository extends BaseRepository<Vehicle> {

    public VehicleRepository() {
        super(Vehicle.class);
    }

    @Override
    protected Long getEntityId(Vehicle entity) {
        return entity.getId();
    }

    /**
     * Cari vehicle berdasarkan plat nomor dan tenant.
     */
    public Optional<Vehicle> findByPlatNomorAndTenantId(String platNomor, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Vehicle> query = em.createQuery(
                "SELECT v FROM Vehicle v WHERE v.platNomor = :platNomor AND v.tenant.id = :tenantId", 
                Vehicle.class);
            query.setParameter("platNomor", platNomor);
            query.setParameter("tenantId", tenantId);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Cari semua vehicle aktif berdasarkan tenant.
     */
    public List<Vehicle> findAktifByTenantId(Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Vehicle> query = em.createQuery(
                "SELECT v FROM Vehicle v WHERE v.tenant.id = :tenantId AND v.aktif = true " +
                "ORDER BY v.tipeVehicle, v.namaPanggilan", 
                Vehicle.class);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari vehicle berdasarkan tipe dan tenant.
     */
    public List<Vehicle> findByTipeAndTenantId(TipeVehicle tipe, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Vehicle> query = em.createQuery(
                "SELECT v FROM Vehicle v WHERE v.tipeVehicle = :tipe " +
                "AND v.tenant.id = :tenantId AND v.aktif = true", 
                Vehicle.class);
            query.setParameter("tipe", tipe);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * Cari vehicle berdasarkan status kepemilikan dan tenant.
     */
    public List<Vehicle> findByStatusKepemilikanAndTenantId(StatusKepemilikan status, Long tenantId) {
        return executeWithEntityManager(em -> {
            TypedQuery<Vehicle> query = em.createQuery(
                "SELECT v FROM Vehicle v WHERE v.statusKepemilikan = :status " +
                "AND v.tenant.id = :tenantId AND v.aktif = true", 
                Vehicle.class);
            query.setParameter("status", status);
            query.setParameter("tenantId", tenantId);
            return query.getResultList();
        });
    }

    /**
     * AVAILABILITY ENGINE - Cari vehicle yang tersedia pada rentang tanggal tertentu.
     * Rumus irisan: (TripStart <= RequestEnd) AND (TripEnd >= RequestStart)
     * 
     * Vehicle dianggap TIDAK tersedia jika:
     * - Ada TripAssignment dengan status booking DP_DITERIMA, LUNAS, atau SELESAI
     * - Trip tersebut overlap dengan rentang tanggal yang diminta
     * 
     * @param tenantId ID tenant
     * @param tanggalMulai Tanggal mulai yang diminta
     * @param tanggalSelesai Tanggal selesai yang diminta
     * @param tipe Tipe vehicle (opsional, null untuk semua tipe)
     * @return List vehicle yang tersedia
     */
    public List<Vehicle> findVehicleTersedia(Long tenantId, LocalDateTime tanggalMulai, 
                                              LocalDateTime tanggalSelesai, TipeVehicle tipe) {
        return executeWithEntityManager(em -> {
            StringBuilder jpql = new StringBuilder();
            jpql.append("SELECT DISTINCT v FROM Vehicle v ");
            jpql.append("WHERE v.tenant.id = :tenantId ");
            jpql.append("AND v.aktif = true ");
            
            if (tipe != null) {
                jpql.append("AND v.tipeVehicle = :tipe ");
            }
            
            // Exclude vehicle yang sudah di-assign ke trip yang overlap
            // dengan status booking DP_DITERIMA ke atas
            jpql.append("AND v.id NOT IN (");
            jpql.append("  SELECT ta.vehicle.id FROM TripAssignment ta ");
            jpql.append("  JOIN ta.trip t ");
            jpql.append("  JOIN t.booking b ");
            jpql.append("  WHERE b.tenant.id = :tenantId ");
            jpql.append("  AND b.status IN ('DP_DITERIMA', 'LUNAS', 'SELESAI') ");
            jpql.append("  AND ta.statusAssignment != 'BATAL' ");
            // Rumus irisan tanggal
            jpql.append("  AND t.waktuMulai <= :tanggalSelesai ");
            jpql.append("  AND t.waktuSelesai >= :tanggalMulai ");
            jpql.append(") ");
            jpql.append("ORDER BY v.tipeVehicle, v.namaPanggilan");
            
            TypedQuery<Vehicle> query = em.createQuery(jpql.toString(), Vehicle.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("tanggalMulai", tanggalMulai);
            query.setParameter("tanggalSelesai", tanggalSelesai);
            
            if (tipe != null) {
                query.setParameter("tipe", tipe);
            }
            
            return query.getResultList();
        });
    }

    /**
     * Hitung jumlah vehicle tersedia per tipe pada rentang tanggal tertentu.
     */
    public long countVehicleTersediaByTipe(Long tenantId, LocalDateTime tanggalMulai, 
                                           LocalDateTime tanggalSelesai, TipeVehicle tipe) {
        List<Vehicle> tersedia = findVehicleTersedia(tenantId, tanggalMulai, tanggalSelesai, tipe);
        return tersedia.size();
    }

    /**
     * Cari vehicle yang sedang di-assign pada rentang tanggal tertentu.
     * Kebalikan dari findVehicleTersedia.
     */
    public List<Vehicle> findVehicleTerpakai(Long tenantId, LocalDateTime tanggalMulai, 
                                              LocalDateTime tanggalSelesai) {
        return executeWithEntityManager(em -> {
            String jpql = "SELECT DISTINCT v FROM Vehicle v " +
                "JOIN v.assignments ta " +
                "JOIN ta.trip t " +
                "JOIN t.booking b " +
                "WHERE v.tenant.id = :tenantId " +
                "AND v.aktif = true " +
                "AND b.status IN ('DP_DITERIMA', 'LUNAS', 'SELESAI') " +
                "AND ta.statusAssignment != 'BATAL' " +
                "AND t.waktuMulai <= :tanggalSelesai " +
                "AND t.waktuSelesai >= :tanggalMulai " +
                "ORDER BY v.tipeVehicle, v.namaPanggilan";
            
            TypedQuery<Vehicle> query = em.createQuery(jpql, Vehicle.class);
            query.setParameter("tenantId", tenantId);
            query.setParameter("tanggalMulai", tanggalMulai);
            query.setParameter("tanggalSelesai", tanggalSelesai);
            return query.getResultList();
        });
    }
}
