package com.example.otelrezervasyonsistemi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginScreen extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel customerPanel, adminPanel;
    private JTextField customerEmailField, adminEmailField;
    private JPasswordField customerPasswordField, adminPasswordField;
    private JButton customerLoginBtn, adminLoginBtn, customerSignupBtn, adminSignupBtn, customerForgotPassBtn, adminForgotPassBtn;
    private DBHelper dbHelper;

    public LoginScreen() {
        dbHelper = new DBHelper();

        setTitle("Otel Rezervasyon Sistemi - Giriş");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Müşteri Giriş Paneli
        customerPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        customerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        customerPanel.add(new JLabel("Email:"));
        customerEmailField = new JTextField();
        customerPanel.add(customerEmailField);

        customerPanel.add(new JLabel("Şifre:"));
        customerPasswordField = new JPasswordField();
        customerPanel.add(customerPasswordField);

        customerLoginBtn = new JButton("Giriş Yap");
        customerLoginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser("Musteri");
            }
        });
        customerPanel.add(customerLoginBtn);

        customerSignupBtn = new JButton("Kaydol");
        customerSignupBtn.addActionListener(e -> new SignupScreen("Musteri"));
        customerPanel.add(customerSignupBtn);

        customerForgotPassBtn = new JButton("Şifremi Unuttum");
        customerForgotPassBtn.addActionListener(e -> new ForgotPasswordScreen("Musteri"));
        customerPanel.add(customerForgotPassBtn);

        // Admin Giriş Paneli
        adminPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        adminPanel.add(new JLabel("Email:"));
        adminEmailField = new JTextField();
        adminPanel.add(adminEmailField);

        adminPanel.add(new JLabel("Şifre:"));
        adminPasswordField = new JPasswordField();
        adminPanel.add(adminPasswordField);

        adminLoginBtn = new JButton("Giriş Yap");
        adminLoginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser("Admin");
            }
        });
        adminPanel.add(adminLoginBtn);

        adminSignupBtn = new JButton("Kaydol");
        adminSignupBtn.addActionListener(e -> new SignupScreen("Admin"));
        adminPanel.add(adminSignupBtn);

        adminForgotPassBtn = new JButton("Şifremi Unuttum");
        adminForgotPassBtn.addActionListener(e -> new ForgotPasswordScreen("Admin"));
        adminPanel.add(adminForgotPassBtn);

        tabbedPane.addTab("Müşteri", customerPanel);
        tabbedPane.addTab("Admin", adminPanel);

        add(tabbedPane);
        setVisible(true);
    }

    private void loginUser(String userType) {
        String email = userType.equals("Musteri") ? customerEmailField.getText() : adminEmailField.getText();
        String password = userType.equals("Musteri") ? new String(customerPasswordField.getPassword()) : new String(adminPasswordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email ve şifre alanları boş bırakılamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String query = "SELECT * FROM " + userType + " WHERE email = ? AND sifre = ?";
            PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Giriş başarılı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                dispose();

                if (userType.equals("Musteri")) {
                    int customerId = rs.getInt("musteri_id");
                    new CustomerDashboard(customerId);
                } else {
                    int adminId = rs.getInt("admin_id");
                    new AdminDashboard(adminId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Email veya şifre hatalı!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginScreen();
    }
}