package com.mrh.buscharter;

import com.mrh.buscharter.config.DatabaseConfig;
import com.mrh.buscharter.service.AuthService;
import com.mrh.buscharter.ui.AppTheme;
import com.mrh.buscharter.ui.LoginDialog;
import com.mrh.buscharter.ui.MainFrame;

import javax.swing.*;

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
        AppTheme.setupLightTheme();
        
        // Jalankan di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Inisialisasi database connection
                DatabaseConfig.initialize();
                
                // Inisialisasi services
                AuthService authService = new AuthService();
                
                // Tampilkan login dialog
                LoginDialog loginDialog = new LoginDialog(null, authService);
                loginDialog.setVisible(true);
                
                // Jika login berhasil, tampilkan main frame
                if (loginDialog.isLoginBerhasil()) {
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
}
