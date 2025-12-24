package com.mrh.buscharter.ui;

import com.mrh.buscharter.config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Frame utama aplikasi dengan Tab Navigation.
 * Menampilkan tab: Dashboard, Booking, Armada, Keuangan, Master Data.
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    
    public MainFrame() {
        super(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);
        initComponents();
        setupLayout();
        setupActions();
        
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        
        // Tambah tab-tab utama (placeholder panels untuk sementara)
        tabbedPane.addTab("Dashboard", createPlaceholderPanel("Dashboard"));
        tabbedPane.addTab("Booking", createPlaceholderPanel("Booking"));
        tabbedPane.addTab("Armada", createPlaceholderPanel("Armada"));
        tabbedPane.addTab("Keuangan", createPlaceholderPanel("Keuangan"));
        tabbedPane.addTab("Master Data", createPlaceholderPanel("Master Data"));
    }
    
    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Panel " + name + " - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        label.setForeground(Color.GRAY);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Toolbar di atas
        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);
        
        // Tab pane di tengah
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar di bawah
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton newButton = new JButton("Baru (Ctrl+N)");
        JButton saveButton = new JButton("Simpan (Ctrl+S)");
        JButton refreshButton = new JButton("Refresh (F5)");
        
        toolBar.add(newButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(refreshButton);
        toolBar.add(Box.createHorizontalGlue());
        
        JLabel userLabel = new JLabel("User: Admin | Tenant: MRH");
        toolBar.add(userLabel);
        
        return toolBar;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        
        JLabel statusLabel = new JLabel("Ready");
        JLabel versionLabel = new JLabel(AppConfig.APP_COMPANY + " | " + AppConfig.APP_VERSION);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private void setupActions() {
        // Konfirmasi sebelum keluar
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Apakah Anda yakin ingin keluar?",
                    "Konfirmasi Keluar",
                    JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void setupKeyboardShortcuts() {
        // Ctrl+N untuk New
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control N"), "new");
        getRootPane().getActionMap().put("new", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // TODO: Implementasi new action berdasarkan tab aktif
                JOptionPane.showMessageDialog(MainFrame.this, "New action - Coming soon");
            }
        });
        
        // Ctrl+S untuk Save
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control S"), "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // TODO: Implementasi save action
                JOptionPane.showMessageDialog(MainFrame.this, "Save action - Coming soon");
            }
        });
        
        // F5 untuk Refresh
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("F5"), "refresh");
        getRootPane().getActionMap().put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // TODO: Implementasi refresh action
                JOptionPane.showMessageDialog(MainFrame.this, "Refresh action - Coming soon");
            }
        });
    }
}
