package com.mrh.buscharter;

import com.formdev.flatlaf.FlatLightLaf;
import com.mrh.buscharter.config.DatabaseConfig;
import com.mrh.buscharter.ui.LoginDialog;
import com.mrh.buscharter.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point aplikasi MRH Bus Charter Management System.
 * Aplikasi desktop untuk manajemen charter bus PT. MANDIRI RAJAWALI HUTAMA.
 * 
 * @author MRH Development Team
 * @version 1.0.0
 */
public class Main {
    
    public static void main(String[] args) {
        // Setup Look and Feel
        setupLookAndFeel();
        
        // Jalankan di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Inisialisasi database connection
                DatabaseConfig.initialize();
                
                // Tampilkan login dialog
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);
                
                // Jika login berhasil, tampilkan main frame
                if (loginDialog.isLoginSuccessful()) {
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                } else {
                    System.exit(0);
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Gagal memulai aplikasi: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
    
    /**
     * Setup FlatLaf Look and Feel dengan tema Enterprise Classic.
     */
    private static void setupLookAndFeel() {
        try {
            // Gunakan FlatLaf Light theme
            FlatLightLaf.setup();
            
            // Konfigurasi UI defaults untuk Enterprise Classic look
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Table.rowHeight", 32);
            UIManager.put("Tree.rowHeight", 28);
            UIManager.put("List.rowHeight", 28);
            UIManager.put("TabbedPane.showTabSeparators", true);
            
            // Warna status indicator
            UIManager.put("mrh.status.konflik", new Color(220, 53, 69));    // Merah
            UIManager.put("mrh.status.vendor", new Color(255, 193, 7));     // Kuning
            UIManager.put("mrh.status.siap", new Color(40, 167, 69));       // Hijau
            
        } catch (Exception e) {
            System.err.println("Gagal setup Look and Feel: " + e.getMessage());
        }
    }
}
