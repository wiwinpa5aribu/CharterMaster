package com.mrh.buscharter.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

/**
 * Konfigurasi tema aplikasi menggunakan FlatLaf.
 * Tema Enterprise/Professional dengan warna netral.
 */
public class AppTheme {
    
    // Warna utama aplikasi
    public static final Color PRIMARY_COLOR = new Color(0, 102, 204);      // Biru profesional
    public static final Color SECONDARY_COLOR = new Color(108, 117, 125);  // Abu-abu
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);      // Hijau
    public static final Color WARNING_COLOR = new Color(255, 193, 7);      // Kuning
    public static final Color DANGER_COLOR = new Color(220, 53, 69);       // Merah
    public static final Color INFO_COLOR = new Color(23, 162, 184);        // Cyan
    
    // Warna status booking
    public static final Color STATUS_DRAFT = new Color(108, 117, 125);     // Abu-abu
    public static final Color STATUS_QUOTATION = new Color(0, 123, 255);   // Biru
    public static final Color STATUS_DP = new Color(255, 193, 7);          // Kuning
    public static final Color STATUS_LUNAS = new Color(40, 167, 69);       // Hijau
    public static final Color STATUS_SELESAI = new Color(32, 134, 55);     // Hijau tua
    public static final Color STATUS_BATAL = new Color(220, 53, 69);       // Merah
    
    // Font sizes
    public static final int FONT_SIZE_DEFAULT = 13;
    public static final int FONT_SIZE_TITLE = 18;
    public static final int FONT_SIZE_SUBTITLE = 15;
    public static final int FONT_SIZE_SMALL = 11;
    
    // Row heights
    public static final int ROW_HEIGHT_TABLE = 32;
    public static final int ROW_HEIGHT_TREE = 28;
    public static final int ROW_HEIGHT_LIST = 30;
    
    // Padding dan margin
    public static final int PADDING_SMALL = 5;
    public static final int PADDING_MEDIUM = 10;
    public static final int PADDING_LARGE = 15;
    
    private AppTheme() {
        // Utility class
    }
    
    /**
     * Inisialisasi tema FlatLaf Light.
     */
    public static void setupLightTheme() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            configureUIDefaults();
        } catch (Exception e) {
            System.err.println("Gagal setup FlatLaf theme: " + e.getMessage());
            // Fallback ke system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
    
    /**
     * Inisialisasi tema FlatLaf Dark.
     */
    public static void setupDarkTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            configureUIDefaults();
        } catch (Exception e) {
            System.err.println("Gagal setup FlatLaf dark theme: " + e.getMessage());
        }
    }
    
    /**
     * Konfigurasi default UI components.
     */
    private static void configureUIDefaults() {
        // Font default
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, FONT_SIZE_DEFAULT);
        UIManager.put("defaultFont", defaultFont);
        
        // Table
        UIManager.put("Table.rowHeight", ROW_HEIGHT_TABLE);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        
        // Tree
        UIManager.put("Tree.rowHeight", ROW_HEIGHT_TREE);
        
        // List
        UIManager.put("List.fixedCellHeight", ROW_HEIGHT_LIST);
        
        // Button
        UIManager.put("Button.arc", 8);
        
        // TextField
        UIManager.put("TextField.arc", 5);
        UIManager.put("Component.focusWidth", 1);
        
        // ScrollBar
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        
        // TabbedPane
        UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
        UIManager.put("TabbedPane.tabHeight", 36);
    }
    
    /**
     * Buat button dengan style primary.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }
    
    /**
     * Buat button dengan style secondary.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }
    
    /**
     * Buat button dengan style danger.
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DANGER_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }
    
    /**
     * Buat label dengan style title.
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, FONT_SIZE_TITLE));
        return label;
    }
    
    /**
     * Buat label dengan style subtitle.
     */
    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, FONT_SIZE_SUBTITLE));
        label.setForeground(SECONDARY_COLOR);
        return label;
    }
}
