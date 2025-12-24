package com.mrh.buscharter.ui;

import com.mrh.buscharter.model.User;
import com.mrh.buscharter.service.AuthService;
import com.mrh.buscharter.service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Dialog login untuk autentikasi user.
 * Menampilkan form dengan field: Email, Password, Kode Tenant.
 */
public class LoginDialog extends JDialog {
    
    private final AuthService authService;
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField kodeTenantField;
    private JButton loginButton;
    private JButton batalButton;
    private JLabel statusLabel;
    
    private boolean loginBerhasil = false;
    
    public LoginDialog(Frame parent, AuthService authService) {
        super(parent, "Login - MRH Bus Charter", true);
        this.authService = authService;
        
        initComponents();
        setupLayout();
        setupActions();
        setupKeyboardShortcuts();
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        emailField = new JTextField(20);
        emailField.putClientProperty("JTextField.placeholderText", "Masukkan email");
        
        passwordField = new JPasswordField(20);
        passwordField.putClientProperty("JTextField.placeholderText", "Masukkan password");
        
        kodeTenantField = new JTextField(20);
        kodeTenantField.putClientProperty("JTextField.placeholderText", "Contoh: MRH");
        
        loginButton = AppTheme.createPrimaryButton("Login");
        batalButton = AppTheme.createSecondaryButton("Batal");
        
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(AppTheme.DANGER_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = AppTheme.createTitleLabel("MRH Bus Charter");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitleLabel = AppTheme.createSubtitleLabel("Sistem Manajemen Sewa Bus");
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0;
        formPanel.add(emailField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(passwordField, gbc);
        
        // Kode Tenant
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Kode Tenant:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.weightx = 1.0;
        formPanel.add(kodeTenantField, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(statusLabel, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(batalButton);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void setupActions() {
        loginButton.addActionListener(e -> doLogin());
        batalButton.addActionListener(e -> {
            loginBerhasil = false;
            dispose();
        });
        
        // Enter key pada password field untuk login
        passwordField.addActionListener(e -> doLogin());
        kodeTenantField.addActionListener(e -> doLogin());
    }
    
    private void setupKeyboardShortcuts() {
        // Escape untuk batal
        getRootPane().registerKeyboardAction(
            e -> {
                loginBerhasil = false;
                dispose();
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Enter untuk login
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String kodeTenant = kodeTenantField.getText().trim().toUpperCase();
        
        // Validasi input
        if (email.isEmpty()) {
            showError("Email tidak boleh kosong");
            emailField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Password tidak boleh kosong");
            passwordField.requestFocus();
            return;
        }
        
        if (kodeTenant.isEmpty()) {
            showError("Kode Tenant tidak boleh kosong");
            kodeTenantField.requestFocus();
            return;
        }
        
        // Disable form saat proses login
        setFormEnabled(false);
        statusLabel.setText("Memproses login...");
        statusLabel.setForeground(AppTheme.INFO_COLOR);
        
        // Proses login di background thread
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.login(email, password, kodeTenant).orElse(null);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        // Login berhasil
                        SessionManager.getInstance().setSession(user);
                        loginBerhasil = true;
                        dispose();
                    } else {
                        showError("Email, password, atau kode tenant salah");
                        setFormEnabled(true);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    showError("Gagal login: " + e.getMessage());
                    setFormEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(AppTheme.DANGER_COLOR);
    }
    
    private void setFormEnabled(boolean enabled) {
        emailField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        kodeTenantField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        batalButton.setEnabled(enabled);
    }
    
    /**
     * Cek apakah login berhasil.
     */
    public boolean isLoginBerhasil() {
        return loginBerhasil;
    }
    
    /**
     * Set email untuk pre-fill (opsional).
     */
    public void setEmail(String email) {
        emailField.setText(email);
    }
    
    /**
     * Set kode tenant untuk pre-fill (opsional).
     */
    public void setKodeTenant(String kodeTenant) {
        kodeTenantField.setText(kodeTenant);
    }
}
