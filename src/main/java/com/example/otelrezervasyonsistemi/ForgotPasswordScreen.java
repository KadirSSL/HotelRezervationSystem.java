package com.example.otelrezervasyonsistemi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ForgotPasswordScreen extends JFrame {
    private JTextField nameField, surnameField, emailField;
    private JPasswordField newPasswordField, confirmPasswordField;
    private JButton resetBtn;
    private String userType;
    private DBHelper dbHelper;

    public ForgotPasswordScreen(String userType) {
        this.userType = userType;
        dbHelper = new DBHelper();

        setTitle(userType.equals("Master") ? "Müşteri Şifre Sıfırlama" : "Admin Şifre Sıfırlama");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
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

        panel.add(new JLabel("Yeni Şifre:"));
        newPasswordField = new JPasswordField();
        panel.add(newPasswordField);

        panel.add(new JLabel("Şifre Tekrar:"));
        confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        resetBtn = new JButton("Şifreyi Sıfırla");
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPassword();
            }
        });
        panel.add(resetBtn);

        add(panel);
        setVisible(true);
    }

    private void resetPassword() {
        String name = nameField.getText();
        String surname = surnameField.getText();
        String email = emailField.getText();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tüm alanlar doldurulmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Şifreler uyuşmuyor!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Kullanıcı doğrulama
            String checkQuery = "SELECT * FROM " + userType + " WHERE ad = ? AND soyad = ? AND email = ?";
            PreparedStatement checkStmt = dbHelper.getConnection().prepareStatement(checkQuery);
            checkStmt.setString(1, name);
            checkStmt.setString(2, surname);
            checkStmt.setString(3, email);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Şifre güncelleme
                String updateQuery = "UPDATE " + userType + " SET password = ? WHERE email = ?";
                PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(updateQuery);
                pstmt.setString(1, newPassword);
                pstmt.setString(2, email);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Şifre başarıyla güncellendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Şifre güncelleme sırasında bir hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Bilgileriniz doğrulanamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veritabanı hatası: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}