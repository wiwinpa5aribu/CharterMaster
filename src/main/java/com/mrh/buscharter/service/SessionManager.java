package com.mrh.buscharter.service;

import com.mrh.buscharter.model.Tenant;
import com.mrh.buscharter.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton untuk mengelola session user yang sedang login.
 * Menyimpan informasi user dan tenant yang aktif.
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    private static SessionManager instance;
    
    private User currentUser;
    private Tenant currentTenant;
    private boolean loggedIn;

    private SessionManager() {
        this.loggedIn = false;
    }

    /**
     * Mendapatkan instance singleton SessionManager.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set session setelah login berhasil.
     */
    public void setSession(User user, Tenant tenant) {
        this.currentUser = user;
        this.currentTenant = tenant;
        this.loggedIn = true;
        logger.info("Session dimulai untuk user: {} di tenant: {}", 
            user.getNamaLengkap(), tenant.getNama());
    }

    /**
     * Set session dengan user saja (tenant diambil dari user).
     */
    public void setSession(User user) {
        this.currentUser = user;
        this.currentTenant = user.getTenant();
        this.loggedIn = true;
        logger.info("Session dimulai untuk user: {}", user.getNamaLengkap());
    }

    /**
     * Hapus session (logout).
     */
    public void clearSession() {
        String userName = currentUser != null ? currentUser.getNamaLengkap() : "unknown";
        this.currentUser = null;
        this.currentTenant = null;
        this.loggedIn = false;
        logger.info("Session dihapus untuk user: {}", userName);
    }

    /**
     * Mendapatkan user yang sedang login.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Mendapatkan tenant yang sedang aktif.
     */
    public Tenant getCurrentTenant() {
        return currentTenant;
    }

    /**
     * Mendapatkan ID tenant yang sedang aktif.
     * Digunakan untuk filter query multi-tenant.
     */
    public Long getCurrentTenantId() {
        return currentTenant != null ? currentTenant.getId() : null;
    }

    /**
     * Cek apakah ada user yang sedang login.
     */
    public boolean isLoggedIn() {
        return loggedIn && currentUser != null;
    }

    /**
     * Validasi session masih valid.
     * Bisa diperluas untuk cek timeout, dll.
     */
    public boolean isSessionValid() {
        return isLoggedIn() && currentUser.getAktif();
    }

    /**
     * Mendapatkan nama user untuk display.
     */
    public String getCurrentUserDisplayName() {
        return currentUser != null ? currentUser.getNamaLengkap() : "";
    }

    /**
     * Mendapatkan nama tenant untuk display.
     */
    public String getCurrentTenantDisplayName() {
        return currentTenant != null ? currentTenant.getNama() : "";
    }

    /**
     * Reset instance (untuk testing).
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.clearSession();
        }
        instance = null;
    }
}
