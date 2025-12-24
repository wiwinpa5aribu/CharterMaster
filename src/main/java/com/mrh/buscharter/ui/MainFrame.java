package com.mrh.buscharter.ui;

import com.mrh.buscharter.model.User;
import com.mrh.buscharter.model.enums.RoleUser;
import com.mrh.buscharter.service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Frame utama aplikasi dengan Tab Navigation.
 * Tab: Dashboard, Booking, Armada, Keuangan, Master Data
 * Tab ditampilkan/disembunyikan berdasarkan role user.
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private JLabel userInfoLabel;
    private JLabel tenantInfoLabel;
    private JButton logoutButton;
    
    // Panel untuk setiap tab
    private JPanel dashboardPanel;
    private JPanel bookingPanel;
    private JPanel armadaPanel;
    private JPanel keuanganPanel;
    private JPanel masterDataPanel;
    
    public MainFrame() {
        super("MRH Bus Charter Management System");
        
        initComponents();
        setupLayout();
        setupMenuBar();
        setupActions();
        updateUserInfo();
        setupTabsBasedOnRole();
        
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        userInfoLabel = new JLabel();
        tenantInfoLabel = new JLabel();
        logoutButton = AppTheme.createSecondaryButton("Logout");
        logoutButton.setPreferredSize(new Dimension(80, 28));
        
        // Inisialisasi panel placeholder
        dashboardPanel = createPlaceholderPanel("Dashboard", "Ringkasan data dan statistik");
        bookingPanel = createPlaceholderPanel("Booking", "Manajemen pesanan sewa bus");
        armadaPanel = createPlaceholderPanel("Armada", "Dispatch board dan manajemen armada");
        keuanganPanel = createPlaceholderPanel("Keuangan", "Pembayaran dan laporan keuangan");
        masterDataPanel = createPlaceholderPanel("Master Data", "Data armada, driver, dan customer");
    }
    
    private JPanel createPlaceholderPanel(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JLabel titleLabel = AppTheme.createTitleLabel(title);
        JLabel descLabel = AppTheme.createSubtitleLabel(description);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        centerPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        centerPanel.add(descLabel, gbc);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        centerPanel.add(new JLabel("(Panel dalam pengembangan)"), gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header dengan info user
        JPanel headerPanel = createHeaderPanel();
        
        // Content dengan tabs
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(8, 15, 8, 15));
        headerPanel.setBackground(new Color(248, 249, 250));
        
        // Logo dan nama aplikasi
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        JLabel logoLabel = new JLabel("🚌");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel appNameLabel = new JLabel("MRH Bus Charter");
        appNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        leftPanel.add(logoLabel);
        leftPanel.add(appNameLabel);
        
        // Info user dan logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JPanel userPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        userPanel.setOpaque(false);
        userInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tenantInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tenantInfoLabel.setForeground(AppTheme.SECONDARY_COLOR);
        userPanel.add(userInfoLabel);
        userPanel.add(tenantInfoLabel);
        
        rightPanel.add(userPanel);
        rightPanel.add(logoutButton);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        // Separator
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(headerPanel, BorderLayout.CENTER);
        wrapperPanel.add(new JSeparator(), BorderLayout.SOUTH);
        
        return wrapperPanel;
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu File
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem refreshItem = new JMenuItem("Refresh Data");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshData());
        
        JMenuItem exitItem = new JMenuItem("Keluar");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitItem.addActionListener(e -> confirmExit());
        
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Menu Bantuan
        JMenu helpMenu = new JMenu("Bantuan");
        helpMenu.setMnemonic('B');
        
        JMenuItem aboutItem = new JMenuItem("Tentang Aplikasi");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupActions() {
        logoutButton.addActionListener(e -> doLogout());
    }
    
    private void updateUserInfo() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userInfoLabel.setText(currentUser.getNamaLengkap() + " (" + currentUser.getRole().name() + ")");
            if (currentUser.getTenant() != null) {
                tenantInfoLabel.setText(currentUser.getTenant().getNama());
            }
        }
    }
    
    private void setupTabsBasedOnRole() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        RoleUser role = currentUser.getRole();
        
        // Dashboard - semua role
        tabbedPane.addTab("Dashboard", dashboardPanel);
        
        // Booking - ADMIN, SALES
        if (role == RoleUser.ADMIN || role == RoleUser.SALES) {
            tabbedPane.addTab("Booking", bookingPanel);
        }
        
        // Armada - ADMIN, OPS
        if (role == RoleUser.ADMIN || role == RoleUser.OPS) {
            tabbedPane.addTab("Armada", armadaPanel);
        }
        
        // Keuangan - ADMIN, KEUANGAN
        if (role == RoleUser.ADMIN || role == RoleUser.KEUANGAN) {
            tabbedPane.addTab("Keuangan", keuanganPanel);
        }
        
        // Master Data - ADMIN only
        if (role == RoleUser.ADMIN) {
            tabbedPane.addTab("Master Data", masterDataPanel);
        }
    }
    
    private void refreshData() {
        // TODO: Implementasi refresh data
        JOptionPane.showMessageDialog(this, 
            "Data berhasil di-refresh", 
            "Refresh", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin logout?",
            "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().clearSession();
            dispose();
            
            // Restart aplikasi dengan login dialog
            SwingUtilities.invokeLater(() -> {
                // Aplikasi akan di-restart dari Main.java
                System.exit(0);
            });
        }
    }
    
    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin keluar dari aplikasi?",
            "Konfirmasi Keluar",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().clearSession();
            dispose();
            System.exit(0);
        }
    }
    
    private void showAboutDialog() {
        String message = """
            MRH Bus Charter Management System
            Versi 1.0.0 (MVP)
            
            Sistem Manajemen Sewa Bus
            PT. MANDIRI RAJAWALI HUTAMA
            
            © 2025 All Rights Reserved
            """;
        
        JOptionPane.showMessageDialog(this,
            message,
            "Tentang Aplikasi",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ==================== Public Methods untuk Panel ====================
    
    /**
     * Set panel untuk tab Dashboard.
     */
    public void setDashboardPanel(JPanel panel) {
        int index = tabbedPane.indexOfTab("Dashboard");
        if (index >= 0) {
            tabbedPane.setComponentAt(index, panel);
        }
    }
    
    /**
     * Set panel untuk tab Booking.
     */
    public void setBookingPanel(JPanel panel) {
        int index = tabbedPane.indexOfTab("Booking");
        if (index >= 0) {
            tabbedPane.setComponentAt(index, panel);
        }
    }
    
    /**
     * Set panel untuk tab Armada.
     */
    public void setArmadaPanel(JPanel panel) {
        int index = tabbedPane.indexOfTab("Armada");
        if (index >= 0) {
            tabbedPane.setComponentAt(index, panel);
        }
    }
    
    /**
     * Set panel untuk tab Keuangan.
     */
    public void setKeuanganPanel(JPanel panel) {
        int index = tabbedPane.indexOfTab("Keuangan");
        if (index >= 0) {
            tabbedPane.setComponentAt(index, panel);
        }
    }
    
    /**
     * Set panel untuk tab Master Data.
     */
    public void setMasterDataPanel(JPanel panel) {
        int index = tabbedPane.indexOfTab("Master Data");
        if (index >= 0) {
            tabbedPane.setComponentAt(index, panel);
        }
    }
}
