package com.example.otelrezervasyonsistemi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private int adminId;
    private DBHelper dbHelper;
    private JTabbedPane tabbedPane;

    public AdminDashboard(int adminId) {
        this.adminId = adminId;
        dbHelper = new DBHelper();

        setTitle("Admin Paneli");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Otel Yönetim Paneli
        tabbedPane.addTab("Otel Yönetimi", new HotelManagementPanel(adminId));

        // Rezervasyon Yönetim Paneli
        tabbedPane.addTab("Rezervasyonlar", new ReservationManagementPanel());

        // Ödeme Yönetim Paneli
        tabbedPane.addTab("Ödemeler", new PaymentManagementPanel());

        add(tabbedPane);
        setVisible(true);
    }
}

class HotelManagementPanel extends JPanel {
    private int adminId;
    private DBHelper dbHelper;
    private JButton addHotelBtn, editHotelBtn, deleteHotelBtn;
    private JList<String> hotelList;
    private DefaultListModel<String> hotelListModel;

    public HotelManagementPanel(int adminId) {
        this.adminId = adminId;
        dbHelper = new DBHelper();
        setLayout(new BorderLayout());

        // Buton paneli
        JPanel buttonPanel = new JPanel();
        addHotelBtn = new JButton("Otel Ekle");
        addHotelBtn.addActionListener(e -> showAddHotelDialog());

        editHotelBtn = new JButton("Düzenle");
        editHotelBtn.addActionListener(e -> showEditHotelDialog());

        deleteHotelBtn = new JButton("Sil");
        deleteHotelBtn.addActionListener(e -> deleteHotel());

        buttonPanel.add(addHotelBtn);
        buttonPanel.add(editHotelBtn);
        buttonPanel.add(deleteHotelBtn);

        add(buttonPanel, BorderLayout.NORTH);

        // Otel listesi
        hotelListModel = new DefaultListModel<>();
        hotelList = new JList<>(hotelListModel);
        hotelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        loadHotels();

        add(new JScrollPane(hotelList), BorderLayout.CENTER);
    }

    private void loadHotels() {
        try {
            String query = "SELECT otel_id, ad, sehir FROM Otel WHERE admin_id = ?";
            PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
            pstmt.setInt(1, adminId);

            ResultSet rs = pstmt.executeQuery();
            hotelListModel.clear();

            while (rs.next()) {
                String hotelInfo = String.format("%d - %s (%s)",
                        rs.getInt("otel_id"),
                        rs.getString("ad"),
                        rs.getString("sehir"));
                hotelListModel.addElement(hotelInfo);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showAddHotelDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Yeni Otel Ekle");
        dialog.setSize(500, 400);
        dialog.setModal(true);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField addressField = new JTextField();
        JTextArea descriptionArea = new JTextArea();
        JSpinner roomCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        JButton addPhotoBtn = new JButton("Fotoğraf Ekle");
        JButton saveBtn = new JButton("Kaydet");

        panel.add(new JLabel("Otel Adı:"));
        panel.add(nameField);
        panel.add(new JLabel("Şehir:"));
        panel.add(cityField);
        panel.add(new JLabel("Adres:"));
        panel.add(addressField);
        panel.add(new JLabel("Açıklama:"));
        panel.add(new JScrollPane(descriptionArea));
        panel.add(new JLabel("Oda Sayısı:"));
        panel.add(roomCountSpinner);
        panel.add(new JLabel("Fotoğraf:"));
        panel.add(addPhotoBtn);
        panel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            // Otel ekleme işlemi
            try {
                String query = "INSERT INTO Otel (ad, sehir, adres, aciklama, puan, admin_id) VALUES (?, ?, ?, ?, 0, ?)";
                PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, cityField.getText());
                pstmt.setString(3, addressField.getText());
                pstmt.setString(4, descriptionArea.getText());
                pstmt.setInt(5, adminId);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    // Otel eklendikten sonra odaları oluştur
                    createRooms((Integer)roomCountSpinner.getValue());
                    JOptionPane.showMessageDialog(dialog, "Otel başarıyla eklendi!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadHotels();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Otel eklenirken hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void createRooms(int roomCount) throws SQLException {
        // Oda dağılımını hesapla (%4 kral dairesi, %10 süit, %86 normal)
        int royalRooms = (int) Math.round(roomCount * 0.04);
        int suiteRooms = (int) Math.round(roomCount * 0.10);
        int standardRooms = roomCount - royalRooms - suiteRooms;

        // Son eklenen otelin ID'sini al
        String getHotelIdQuery = "SELECT MAX(otel_id) AS last_id FROM Otel";
        ResultSet rs = dbHelper.executeQuery(getHotelIdQuery);
        int hotelId = rs.next() ? rs.getInt("last_id") : -1;

        if (hotelId == -1) return;

        // Oda türlerinin ID'lerini al
        int royalTypeId = getRoomTypeId("Kral Dairesi");
        int suiteTypeId = getRoomTypeId("Süit Oda");
        int standardTypeId = getRoomTypeId("Normal Oda");

        // Odaları oluştur
        createRoomBatch(hotelId, royalTypeId, royalRooms, 4, 500); // Kral dairesi
        createRoomBatch(hotelId, suiteTypeId, suiteRooms, 3, 300);  // Süit oda
        createRoomBatch(hotelId, standardTypeId, standardRooms, 2, 100); // Normal oda
    }

    private int getRoomTypeId(String typeName) throws SQLException {
        String query = "SELECT tur_id FROM OdaTuru WHERE ad = ?";
        PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);
        pstmt.setString(1, typeName);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getInt("tur_id") : -1;
    }

    private void createRoomBatch(int hotelId, int typeId, int count, int capacity, int basePrice) throws SQLException {
        if (count <= 0) return;

        String query = "INSERT INTO Oda (otel_id, oda_no, kapasite, fiyat, tur_id, aktif) VALUES (?, ?, ?, ?, ?, 1)";
        PreparedStatement pstmt = dbHelper.getConnection().prepareStatement(query);

        // Mevcut en yüksek oda numarasını bul
        String maxRoomNoQuery = "SELECT MAX(oda_no) AS max_no FROM Oda WHERE otel_id = ?";
        PreparedStatement maxStmt = dbHelper.getConnection().prepareStatement(maxRoomNoQuery);
        maxStmt.setInt(1, hotelId);
        ResultSet rs = maxStmt.executeQuery();
        int startNo = rs.next() ? rs.getInt("max_no") + 1 : 1;

        for (int i = 0; i < count; i++) {
            pstmt.setInt(1, hotelId);
            pstmt.setInt(2, startNo + i);
            pstmt.setInt(3, capacity);
            pstmt.setInt(4, basePrice);
            pstmt.setInt(5, typeId);
            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }

    private void showEditHotelDialog() {
        // Otel düzenleme dialogu
    }

    private void deleteHotel() {
        // Otel silme işlemi
    }
}