package com.example.otelrezervasyonsistemi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SignupScreen extends JFrame {
    private JTextField nameField, surnameField, emailField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton signupBtn;
    private String userType;
    private DBHelper dbHelper;

    public SignupScreen(String userType) {
        this.userType = userType;
        dbHelper = new DBHelper();

        setTitle(userType.equals("Musteri") ? "Müşteri Kayıt" : "Admin Kayıt");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Ad:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Soyad:"));
        surnameField = new JTextField();
        panel.add(surnameField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Telefon:"));
        phoneField = new JTextField();
        panel.add(phoneField);

        panel.add(new JLabel("Şifre:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Şifre Tekrar:"));
        confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        signupBtn = new JButton("Kaydol");
        signupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        panel.add(signupBtn);

        add(panel);
        setVisible(true);
    }

    private void registerUser() {
        String name = nameField.getText();
        String surname = surnameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tüm alanlar doldurulmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Şifreler uyuşmuyor!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Email kontrolü
            String checkQuery = "SELECT * FROM " + userType + " WHERE email = ?";
            PreparedStatement checkStmt = dbHelper.getConnection().prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Bu email zaten kayıtlı!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Yeni kullanıcı ekleme
            String insertQuery = "INSERT INTO " + userType + " (ad, soyad, email, telefon, sifre) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, surname);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, password);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Kayıt başarılı!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Kayıt sırasında bir hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}