package com.kostify.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Deklarasi Driver 
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private static final String URL = "jdbc:mysql://localhost:3306/db_kostify?useSSL=false&serverTimezone=Asia/Jakarta";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            // 3. Menggunakan pesan error yang informatif bagi anggota kelompok
            throw new SQLException("Driver MySQL JDBC tidak ditemukan! Pastikan file .jar sudah ditambahkan ke folder lib.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}