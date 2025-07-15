package com.example.otelrezervasyonsistemi;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class ReservationManagementPanel extends JPanel {
    private DBHelper dbHelper;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private JButton refreshBtn, confirmBtn, cancelBtn;

    public ReservationManagementPanel() {
        dbHelper = new DBHelper();
        setLayout(new BorderLayout());

        // Butonlar için panel
        JPanel buttonPanel = new JPanel();
        refreshBtn = new JButton("Yenile");
        refreshBtn.addActionListener(e -> loadReservations());
        confirmBtn = new JButton("Rezervasyonu Onayla");
        confirmBtn.addActionListener(e -> confirmReservation());
        cancelBtn = new JButton("Rezervasyonu İptal Et");
        cancelBtn.addActionListener(e -> cancelReservation());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.NORTH);

        // Rezervasyonlar için tablo
        String[] columnNames = {"Rezervasyon ID", "Müşteri Adı", "Otel Adı", "Oda No", "Giriş Tarihi", "Çıkış Tarihi", "Toplam Fiyat", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tüm hücreleri düzenlenemez yap
            }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(reservationTable), BorderLayout.CENTER);

        loadReservations(); // Panel başlatıldığında rezervasyonları yükle
    }

    private void loadReservations() {
        tableModel.setRowCount(0); // Mevcut verileri temizle
        try {
            String query = "SELECT r.rezervasyon_id, c.ad, c.soyad, o.ad AS otel_ad, od.oda_no, r.baslangic_tarihi, r.bitis_tarihi, r.durum " +
                    "FROM Rezervasyon r " +
                    "JOIN Musteri c ON r.musteri_id = c.musteri_id " +
                    "JOIN Oda od ON r.oda_id = od.oda_id " +
                    "JOIN Otel o ON od.otel_id = o.otel_id " +
                    "ORDER BY r.rezervasyon_id DESC";
            ResultSet rs = dbHelper.executeQuery(query);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("rezervasyon_id"));
                row.add(rs.getString("ad") + " " + rs.getString("soyad"));
                row.add(rs.getString("otel_ad"));
                row.add(rs.getInt("oda_no"));
                row.add(rs.getDate("giris_tarihi"));
                row.add(rs.getDate("cikis_tarihi"));
                row.add(rs.getDouble("toplam_fiyat"));
                row.add(rs.getString("durum"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Rezervasyonlar yüklenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateReservationStatus(String status) {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellemek için bir rezervasyon seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reservationId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            String query = "UPDATE Rezervasyon SET durum = ? WHERE rezervasyon_id = ?";
            PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
            pstmt.setString(1, status);
            pstmt.setInt(2, reservationId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Rezervasyon başarıyla " + status.toLowerCase() + " edildi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                loadReservations(); // Tabloyu yenile
            } else {
                JOptionPane.showMessageDialog(this, "Rezervasyon güncellenirken hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Rezervasyon güncellenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmReservation() {
        updateReservationStatus("Onaylandı");
    }

    private void cancelReservation() {
        updateReservationStatus("İptal Edildi");
    }
}