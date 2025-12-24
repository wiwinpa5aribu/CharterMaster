package com.mrh.buscharter.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog login untuk autentikasi pengguna.
 * Memvalidasi email, password, dan kode tenant.
 */
public class LoginDialog extends JDialog {
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField tenantCodeField;
    private JButton loginButton;
    private JButton cancelButton;
    
    private boolean loginSuccessful = false;
    
    public LoginDialog(Frame parent) {
        super(parent, "Login - MRH Bus Charter", true);
        initComponents();
        setupLayout();
        setupActions();
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        tenantCodeField = new JTextField(20);
        loginButton = new JButton("Login");
        cancelButton = new JButton("Batal");
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel titleLabel = new JLabel("MRH Bus Charter Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);
        
        // Kode Tenant
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Kode Tenant:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(tenantCodeField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        setContentPane(mainPanel);
    }
    
    private void setupActions() {
        loginButton.addActionListener(e -> performLogin());
        cancelButton.addActionListener(e -> dispose());
        
        // Enter key untuk login
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String tenantCode = tenantCodeField.getText().trim();
        
        if (email.isEmpty() || password.isEmpty() || tenantCode.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Semua field harus diisi",
                "Validasi Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // TODO: Implementasi autentikasi dengan AuthService
        // Untuk sementara, set login successful
        loginSuccessful = true;
        dispose();
    }
    
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
