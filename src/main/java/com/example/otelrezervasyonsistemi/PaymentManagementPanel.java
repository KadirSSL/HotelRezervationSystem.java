package com.example.otelrezervasyonsistemi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class PaymentManagementPanel extends JPanel {
    private DBHelper dbHelper;
    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;

    public PaymentManagementPanel() {
        dbHelper = new DBHelper();
        setLayout(new BorderLayout());

        // Buton paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Ödemeleri Yenile");
        refreshButton.addActionListener(e -> loadPayments());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.NORTH);

        // Ödemeler tablosu
        String[] columnNames = {"Ödeme ID", "Rezervasyon ID", "Müşteri Adı", "Otel Adı", "Ödeme Tarihi", "Miktar", "Durum"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hücrelerin düzenlenemez olmasını sağlar
            }
        };
        paymentTable = new JTable(tableModel);
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(paymentTable), BorderLayout.CENTER);

        // Panel yüklendiğinde ödemeleri getir
        loadPayments();
    }

    private void loadPayments() {
        tableModel.setRowCount(0); // Mevcut verileri temizle
        try {
            String query = "SELECT p.odeme_id, p.rezervasyon_id, c.ad, c.soyad, o.ad AS otel_ad, p.odeme_tarihi, p.durum " +
                    "FROM Odeme p " +
                    "JOIN Rezervasyon r ON p.rezervasyon_id = r.rezervasyon_id " +
                    "JOIN Musteri c ON r.musteri_id = c.musteri_id " +
                    "JOIN Oda od ON r.oda_id = od.oda_id " +
                    "JOIN Otel o ON od.otel_id = o.otel_id " +
                    "ORDER BY p.odeme_tarihi DESC";
            ResultSet rs = dbHelper.executeQuery(query);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("odeme_id"));
                row.add(rs.getInt("rezervasyon_id"));
                row.add(rs.getString("ad") + " " + rs.getString("soyad"));
                row.add(rs.getString("otel_ad"));
                row.add(rs.getTimestamp("odeme_tarihi")); // Ödeme tarihi TIMESTAMP formatında olabilir
                row.add(rs.getDouble("miktar"));
                row.add(rs.getString("durum"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ödemeler yüklenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}