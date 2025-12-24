package com.mrh.buscharter.service;

import com.mrh.buscharter.model.Tenant;
import com.mrh.buscharter.model.User;
import com.mrh.buscharter.model.enums.RoleUser;
import com.mrh.buscharter.repository.TenantRepository;
import com.mrh.buscharter.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/**
 * Service untuk autentikasi dan otorisasi.
 * Menangani login, validasi password, dan pengecekan role.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
        this.tenantRepository = new TenantRepository();
    }

    /**
     * Login user dengan email, password, dan kode tenant.
     * 
     * @param email Email user
     * @param password Password plain text
     * @param kodeTenant Kode tenant (PO)
     * @return Optional<User> jika login berhasil
     */
    public Optional<User> login(String email, String password, String kodeTenant) {
        logger.info("Mencoba login untuk email: {} di tenant: {}", email, kodeTenant);
        
        // Validasi input
        if (email == null || email.isBlank()) {
            logger.warn("Login gagal: email kosong");
            return Optional.empty();
        }
        if (password == null || password.isBlank()) {
            logger.warn("Login gagal: password kosong");
            return Optional.empty();
        }
        if (kodeTenant == null || kodeTenant.isBlank()) {
            logger.warn("Login gagal: kode tenant kosong");
            return Optional.empty();
        }

        // Cari user aktif berdasarkan email dan kode tenant
        Optional<User> userOpt = userRepository.findAktifByEmailAndKodeTenant(email, kodeTenant);
        
        if (userOpt.isEmpty()) {
            logger.warn("Login gagal: user tidak ditemukan atau tidak aktif");
            return Optional.empty();
        }

        User user = userOpt.get();
        
        // Validasi password
        if (!validatePassword(password, user.getPasswordHash())) {
            logger.warn("Login gagal: password salah untuk user: {}", email);
            return Optional.empty();
        }

        logger.info("Login berhasil untuk user: {} dengan role: {}", email, user.getRole());
        return Optional.of(user);
    }

    /**
     * Validasi password dengan hash yang tersimpan.
     * Menggunakan SHA-256 untuk hashing.
     */
    public boolean validatePassword(String plainPassword, String storedHash) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(storedHash);
    }

    /**
     * Hash password menggunakan SHA-256.
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 tidak tersedia", e);
        }
    }

    /**
     * Cek apakah user memiliki role tertentu.
     */
    public boolean hasRole(User user, RoleUser role) {
        if (user == null || role == null) {
            return false;
        }
        return user.getRole() == role;
    }

    /**
     * Cek apakah user memiliki salah satu dari role yang diberikan.
     */
    public boolean hasAnyRole(User user, RoleUser... roles) {
        if (user == null || roles == null || roles.length == 0) {
            return false;
        }
        for (RoleUser role : roles) {
            if (user.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cek apakah user adalah Admin.
     */
    public boolean isAdmin(User user) {
        return hasRole(user, RoleUser.ADMIN);
    }

    /**
     * Cek apakah user bisa mengakses modul Booking.
     * RACI: ADMIN (A), SALES (R), OPS (C)
     */
    public boolean canAccessBooking(User user) {
        return hasAnyRole(user, RoleUser.ADMIN, RoleUser.SALES, RoleUser.OPS);
    }

    /**
     * Cek apakah user bisa mengakses modul Fleet/Armada.
     * RACI: ADMIN (A), OPS (R), SALES (I)
     */
    public boolean canAccessFleet(User user) {
        return hasAnyRole(user, RoleUser.ADMIN, RoleUser.OPS, RoleUser.SALES);
    }

    /**
     * Cek apakah user bisa mengakses modul Finance/Keuangan.
     * RACI: ADMIN (A), KEUANGAN (R)
     */
    public boolean canAccessFinance(User user) {
        return hasAnyRole(user, RoleUser.ADMIN, RoleUser.KEUANGAN);
    }

    /**
     * Cek apakah user bisa mengakses Master Data.
     * Hanya ADMIN yang bisa akses penuh.
     */
    public boolean canAccessMasterData(User user) {
        return hasRole(user, RoleUser.ADMIN);
    }

    /**
     * Cek apakah user bisa membuat booking baru.
     */
    public boolean canCreateBooking(User user) {
        return hasAnyRole(user, RoleUser.ADMIN, RoleUser.SALES);
    }

    /**
     * Cek apakah user bisa melakukan assignment armada.
     */
    public boolean canAssignVehicle(User user) {
        return hasAnyRole(user, RoleUser.ADMIN, RoleUser.OPS);
    }

    /**
     * Cek apakah user bisa mencatat pembayaran.
     */
    public boolean canRecordPayment(User user) {
        return hasAnyRole(user, RoleUser.ADMIN, RoleUser.KEUANGAN);
    }

    /**
     * Cari tenant berdasarkan kode.
     */
    public Optional<Tenant> findTenantByKode(String kodeTenant) {
        return tenantRepository.findByKode(kodeTenant);
    }

    /**
     * Buat user baru dengan password yang di-hash.
     */
    public User createUser(Tenant tenant, String namaLengkap, String email, 
                           String password, RoleUser role) {
        User user = new User(tenant, namaLengkap, email, hashPassword(password), role);
        return userRepository.save(user);
    }
}
