package com.example.otelrezervasyonsistemi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class MyReviewsPanel extends JPanel {
    private int customerId;
    private DBHelper dbHelper;
    private JTable reviewsTable;
    private DefaultTableModel tableModel;
    private JButton refreshReviewsButton, addReviewButton;

    public MyReviewsPanel(int customerId) {
        this.customerId = customerId;
        dbHelper = new DBHelper();
        setLayout(new BorderLayout());

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshReviewsButton = new JButton("Yorumlarımı Yenile");
        refreshReviewsButton.addActionListener(e -> loadMyReviews());
        addReviewButton = new JButton("Yeni Yorum Ekle");
        addReviewButton.addActionListener(e -> showAddReviewDialog());
        buttonPanel.add(refreshReviewsButton);
        buttonPanel.add(addReviewButton);
        add(buttonPanel, BorderLayout.NORTH);

        // Yorumlar tablosu
        String[] columnNames = {"Yorum ID", "Otel Adı", "Puan", "Yorum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hücrelerin düzenlenemez olmasını sağlar
            }
        };
        reviewsTable = new JTable(tableModel);
        reviewsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(reviewsTable), BorderLayout.CENTER);

        // Panel yüklendiğinde yorumları getir
        loadMyReviews();
    }

    private void loadMyReviews() {
        tableModel.setRowCount(0); // Mevcut verileri temizle
        try {
            // Müşterinin kendi yorumlarını ilgili otel bilgisiyle birlikte getiren sorgu
            String query = "SELECT y.yorum_id, o.ad AS otel_ad, y.puan, y.yorum_metni " +
                    "FROM Yorum y " +
                    "JOIN Otel o ON y.otel_id = o.otel_id " +
                    "WHERE y.musteri_id = ? " +
                    "ORDER BY y.tarih DESC";

            PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("yorum_id"));
                row.add(rs.getString("otel_ad"));
                row.add(rs.getDouble("puan"));
                row.add(rs.getString("yorum_metni"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Yorumlarınız yüklenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddReviewDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Yeni Yorum Ekle");
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Müşterinin yorum yapabileceği otelleri yükle
        JComboBox<String> hotelComboBox = new JComboBox<>();
        populateHotelsForReview(hotelComboBox);

        JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 5.0, 0.5));
        JTextArea reviewTextArea = new JTextArea(5, 20);
        JButton saveButton = new JButton("Yorumu Kaydet");

        panel.add(new JLabel("Otel Seçin:"));
        panel.add(hotelComboBox);
        panel.add(new JLabel("Puan (1.0 - 5.0):"));
        panel.add(ratingSpinner);
        panel.add(new JLabel("Yorumunuz:"));
        panel.add(new JScrollPane(reviewTextArea));
        panel.add(new JLabel("")); // Boşluk bırakmak için
        panel.add(saveButton);

        saveButton.addActionListener(e -> {
            String selectedHotelInfo = (String) hotelComboBox.getSelectedItem();
            if (selectedHotelInfo == null || selectedHotelInfo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Lütfen yorum yapmak için bir otel seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Otel adından otel ID'sini çek (örneğin "123 - Otel Adı" formatından)
            int hotelId = Integer.parseInt(selectedHotelInfo.split(" - ")[0]);
            double rating = (Double) ratingSpinner.getValue();
            String reviewText = reviewTextArea.getText();

            if (reviewText.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Lütfen bir yorum yazın.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String insertQuery = "INSERT INTO Yorum (musteri_id, otel_id, puan, yorum_metni, tarih) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(insertQuery);
                pstmt.setInt(1, customerId);
                pstmt.setInt(2, hotelId);
                pstmt.setDouble(3, rating);
                pstmt.setString(4, reviewText);
                pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(dialog, "Yorumunuz başarıyla eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadMyReviews(); // Yorumları yenile
                    updateHotelOverallRating(hotelId); // Otelin genel puanını güncelle
                } else {
                    JOptionPane.showMessageDialog(dialog, "Yorum eklenirken hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Yorum eklenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void populateHotelsForReview(JComboBox<String> hotelComboBox) {
        try {
            // Müşterinin daha önce rezervasyon yaptığı ve konakladığı otelleri getir
            String query = "SELECT DISTINCT o.otel_id, o.ad FROM Otel o " +
                    "JOIN Oda od ON o.otel_id = od.otel_id " +
                    "JOIN Rezervasyon r ON od.oda_id = r.oda_id " +
                    "WHERE r.musteri_id = ? AND r.durum = 'Tamamlandı'"; // Sadece tamamlanmış rezervasyonlar için

            PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                hotelComboBox.addItem(rs.getInt("otei_id") + " - " + rs.getString("ad"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Yorum yapabileceğiniz oteller yüklenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateHotelOverallRating(int hotelId) {
        try {
            // Otelin yeni ortalama puanını hesapla
            String avgRatingQuery = "SELECT AVG(puan) AS avg_puan FROM Yorum WHERE otel_id = ?";
            PreparedStatement avgPstmt = dbHelper.getConnection().prepareStatement(avgRatingQuery);
            avgPstmt.setInt(1, hotelId);
            ResultSet rs = avgPstmt.executeQuery();

            if (rs.next()) {
                double newAvgRating = rs.getDouble("avg_puan");

                // Otelin psan (puan) değerini güncelle
                String updateHotelQuery = "UPDATE Otel SET puan = ? WHERE otel_id = ?";
                PreparedStatement updatePstmt = dbHelper.getConnection().prepareStatement(updateHotelQuery);
                updatePstmt.setDouble(1, newAvgRating);
                updatePstmt.setInt(2, hotelId);
                updatePstmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Otel puanı güncellenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}