package com.example.otelrezervasyonsistemi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class DBHelper {
    private static final String DB_URL = "jdbc:sqlserver://DESKTOP-1JTGLF8;databaseName=OtelDB;integratedSecurity=True;encrypt=True;trustServerCertificate=True";
    private Connection connection;



    public DBHelper() {
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Genel sorgu çalıştırma metodu
    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    // Genel update/insert/delete metodu
    public int executeUpdate(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeUpdate(query);
    }
}