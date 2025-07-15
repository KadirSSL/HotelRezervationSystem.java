package com.example.otelrezervasyonsistemi;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class MyReservationsPanel extends JPanel {
    private int customerId;
    private DBHelper dbHelper;
    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;

    public MyReservationsPanel(int customerId) {
        this.customerId = customerId;
        dbHelper = new DBHelper();
        setLayout(new BorderLayout());

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Rezervasyonlarımı Yenile");
        refreshButton.addActionListener(e -> loadMyReservations());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.NORTH);

        // Rezervasyonlar tablosu
        String[] columnNames = {"Rezervasyon ID", "Otel Adı", "Oda Numarası", "Giriş Tarihi", "Çıkış Tarihi", "Toplam Fiyat", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hücrelerin düzenlenemez olmasını sağlar
            }
        };
        reservationsTable = new JTable(tableModel);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(reservationsTable), BorderLayout.CENTER);

        // Panelin başlangıcında rezervasyonları yükle
        loadMyReservations();
    }

    private void loadMyReservations() {
        tableModel.setRowCount(0); // Mevcut verileri temizle
        try {
            // Müşterinin rezervasyonlarını otel ve oda bilgileriyle birlikte getiren sorgu
            String query = "SELECT r.rezervasyon_id, o.ad AS otel_ad, od.oda_no, r.baslangic_tarihi, r.bitis_tarihi, od.fiyat, r.durum " +
                    "FROM Rezervasyon r " +
                    "JOIN Oda od ON r.oda_id = od.oda_id " +
                    "JOIN Otel o ON od.otel_id = o.otel_id " +
                    "WHERE r.musteri_id = ? " +
                    "ORDER BY r.baslangic_tarihi DESC"; // En yeni rezervasyonlar üstte olsun

            PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("rezervasyon_id"));
                row.add(rs.getString("otel_ad"));
                row.add(rs.getInt("oda_no"));
                row.add(rs.getDate("baslangic_tarihi"));
                row.add(rs.getDate("bitis_tarihi"));
                row.add(rs.getDouble("fiyat"));
                row.add(rs.getString("durum"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Rezervasyonlarınız yüklenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}