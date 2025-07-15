package com.example.otelrezervasyonsistemi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDashboard extends JFrame {
    private int customerId;
    private DBHelper dbHelper;
    private JTabbedPane tabbedPane;

    public CustomerDashboard(int customerId) {
        this.customerId = customerId;
        dbHelper = new DBHelper();

        setTitle("Müşteri Paneli");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Otel Arama ve Rezervasyon Paneli
        tabbedPane.addTab("Otel Ara & Rezervasyon", new HotelSearchPanel(customerId));

        // Rezervasyonlarım Paneli
        tabbedPane.addTab("Rezervasyonlarım", new MyReservationsPanel(customerId));

        // Yorumlarım Paneli
        tabbedPane.addTab("Yorumlarım", new MyReviewsPanel(customerId));

        add(tabbedPane);
        setVisible(true);
    }
}

class HotelSearchPanel extends JPanel {
    private int customerId;
    private DBHelper dbHelper;
    private JComboBox<String> cityComboBox, roomTypeComboBox, capacityComboBox, mealComboBox;
    private JButton searchBtn;
    private JList<String> hotelList;
    private DefaultListModel<String> hotelListModel;

    public HotelSearchPanel(int customerId) {
        this.customerId = customerId;
        dbHelper = new DBHelper();
        setLayout(new BorderLayout());

        // Filtreleme paneli
        JPanel filterPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        filterPanel.add(new JLabel("Şehir:"));
        cityComboBox = new JComboBox<>();
        populateCities();
        filterPanel.add(cityComboBox);

        filterPanel.add(new JLabel("Oda Türü:"));
        roomTypeComboBox = new JComboBox<>(new String[]{"Normal Oda", "Süit Oda", "Kral Dairesi"});
        filterPanel.add(roomTypeComboBox);

        filterPanel.add(new JLabel("Kapasite:"));
        capacityComboBox = new JComboBox<>(new String[]{ "2 Kişilik", "3 Kişilik", "4 Kişilik"});
        filterPanel.add(capacityComboBox);

        filterPanel.add(new JLabel("Yemek Seçeneği:"));
        mealComboBox = new JComboBox<>();
        populateMealOptions();
        filterPanel.add(mealComboBox);

        searchBtn = new JButton("Ara");
        searchBtn.addActionListener(e -> searchHotels());
        filterPanel.add(searchBtn);

        add(filterPanel, BorderLayout.NORTH);

        // Otel listesi
        hotelListModel = new DefaultListModel<>();
        hotelList = new JList<>(hotelListModel);
        hotelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hotelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showHotelDetails();
            }
        });

        add(new JScrollPane(hotelList), BorderLayout.CENTER);
    }

    private void populateCities() {
        try {
            String query = "SELECT DISTINCT sehir FROM Otel";
            ResultSet rs = dbHelper.executeQuery(query);

            cityComboBox.addItem("Tümü");
            while (rs.next()) {
                cityComboBox.addItem(rs.getString("sehir"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void populateMealOptions() {
        try {
            String query = "SELECT ad FROM YemekSecenegi";
            ResultSet rs = dbHelper.executeQuery(query);

            mealComboBox.addItem("Tümü");
            while (rs.next()) {
                mealComboBox.addItem(rs.getString("ad"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void searchHotels() {
        try {
            String city = cityComboBox.getSelectedItem().toString();
            String roomType = roomTypeComboBox.getSelectedItem().toString();
            String capacity = capacityComboBox.getSelectedItem().toString();
            String meal = mealComboBox.getSelectedItem().toString();

            StringBuilder query = new StringBuilder("SELECT o.oteL_id, o.ad, o.sehir, o.puan FROM Otel o ");

            if (!roomType.equals("Tümü") || !capacity.equals("Tümü")) {
                query.append("JOIN Oda od ON o.otel_id = od.otel_id ");

                if (!roomType.equals("Tümü")) {
                    query.append("JOIN OdaTuru ot ON od.tur_id = ot.tur_id AND ot.ad = '")
                            .append(roomType).append("' ");
                }

                if (!capacity.equals("Tümü")) {
                    int cap = Integer.parseInt(capacity.split(" ")[0]);
                    query.append("AND od.kapasite = ").append(cap).append(" ");
                }
            }

            if (!city.equals("Tümü")) {
                query.append("WHERE o.sehir = '").append(city).append("' ");
            }

            query.append("GROUP BY o.otel_id, o.ad, o.sehir, o.puan");

            ResultSet rs = dbHelper.executeQuery(query.toString());
            hotelListModel.clear();

            while (rs.next()) {
                String hotelInfo = String.format("%s - %s (Puan: %.1f)",
                        rs.getString("ad"),
                        rs.getString("sehir"),
                        rs.getDouble("puan"));
                hotelListModel.addElement(hotelInfo);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showHotelDetails() {
        // Otel detaylarını ve rezervasyon formunu göster
    }
}