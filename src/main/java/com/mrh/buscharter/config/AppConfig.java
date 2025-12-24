package com.mrh.buscharter.config;

/**
 * Konfigurasi aplikasi MRH Bus Charter.
 * Menyimpan konstanta dan konfigurasi global.
 */
public class AppConfig {
    
    // Informasi Aplikasi
    public static final String APP_NAME = "MRH Bus Charter Management";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_COMPANY = "PT. MANDIRI RAJAWALI HUTAMA";
    
    // Format Kode Booking
    public static final String BOOKING_CODE_PREFIX = "BOOK";
    public static final String BOOKING_CODE_FORMAT = "%s/%d/%02d/%03d"; // BOOK/2025/12/001
    
    // Batas Waktu
    public static final int BUFFER_JAM_ANTAR_TRIP = 4; // Jam buffer untuk cuci bus & istirahat driver
    
    // UI Settings
    public static final int DEFAULT_FONT_SIZE = 13;
    public static final int TABLE_ROW_HEIGHT = 32;
    public static final int TREE_ROW_HEIGHT = 28;
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 50;
    
    private AppConfig() {
        // Private constructor
    }
}
