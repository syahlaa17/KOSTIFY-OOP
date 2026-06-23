package com.kostify.controller;

import com.kostify.db.DatabaseConnection;
import com.kostify.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KostifyController {

    // OPERASI CRUD: TABEL KOST
        // Select semua data Kost dari database
    public List<Kost> getAllKost() {
        List<Kost> listKost = new ArrayList<>();
        String sql = "SELECT * FROM kost";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id_kost");
                String nama = rs.getString("nama_kost");
                String tipe = rs.getString("tipe_kost");
                double harga = rs.getDouble("harga_dasar");
                int kapasitas = rs.getInt("kapasitas");
                int terisi = rs.getInt("terisi");

                // Penerapan Polimorfisme berdasarkan tipe kost di database
                if (tipe.equalsIgnoreCase("Elite")) {
                    listKost.add(new KostElite(id, nama, harga, kapasitas, terisi));
                } else {
                    listKost.add(new KostEkonomis(id, nama, harga, kapasitas, terisi));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKost;
    }

    // Insert data Kost baru 
    public boolean tambahKost(String nama, String tipe, double harga, int kapasitas) {
        String sql = "INSERT INTO kost (nama_kost, tipe_kost, harga_dasar, kapasitas, terisi) VALUES (?, ?, ?, ?, 0)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nama);
            pstmt.setString(2, tipe);
            pstmt.setDouble(3, harga);
            pstmt.setInt(4, kapasitas);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete data Kost 
    public boolean hapusKost(int idKost) {
        String sql = "DELETE FROM kost WHERE id_kost = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idKost);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // OPERASI CRUD: TABEL PENYEWA
    // Mengambil semua data Penyewa dan nama kost tempat mereka tinggal (SELECT)
    public List<Penyewa> getAllPenyewa() {
        List<Penyewa> listPenyewa = new ArrayList<>();
        String sql = "SELECT * FROM penyewa";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listPenyewa.add(new Penyewa(
                    rs.getInt("id_penyewa"),
                    rs.getString("nama_penyewa"),
                    rs.getString("nomor_ktp"),
                    rs.getInt("id_kost")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listPenyewa;
    }

    // Mendaftarkan Penyewa baru (INSERT)
    public boolean tambahPenyewa(String nama, String ktp, int idKost) throws KamarPenuhException {
        // Ambil data kost yang dipilih untuk dicek kapasitasnya
        Kost kostTerpilih = getKostById(idKost);
        if (kostTerpilih == null) return false;

        // Validasi Custom Exception jika penuh
        if (kostTerpilih.isPenuh()) {
            throw new KamarPenuhException("Gagal mendaftar! Kamar di kost '" + kostTerpilih.getNamaKost() + "' sudah penuh.");
        }

        String sqlInsert = "INSERT INTO penyewa (nama_penyewa, nomor_ktp, id_kost) VALUES (?, ?, ?)";
        String sqlUpdateKost = "UPDATE kost SET terisi = terisi + 1 WHERE id_kost = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Mulai transaction

            // Insert Penyewa
            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlInsert)) {
                pstmt1.setString(1, nama);
                pstmt1.setString(2, ktp);
                pstmt1.setInt(3, idKost);
                pstmt1.executeUpdate();
            }

            // Update status kuota terisi di tabel Kost
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlUpdateKost)) {
                pstmt2.setInt(1, idKost);
                pstmt2.executeUpdate();
            }

            conn.commit(); // Simpan 
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

 
    // OPERASI POS KASIR: TABEL TRANSAKSI
    // Mencatat transaksi pembayaran bulanan dan menghitung denda otomatis
    public boolean bayarKost(int idPenyewa, int idKost, String bulanTagihan, LocalDate tglJatuhTempo, LocalDate tglBayar) {
        Kost kost = getKostById(idKost);
        if (kost == null) return false;

        // Hitung biaya sewa bulanan berdasarkan fungsi hitungBiayaBulanan()
        double biayaSewa = kost.hitungBiayaBulanan();

        // Buat objek model transaksi untuk memicu kalkulasi logika denda otomatis 
        Transaksi trx = new Transaksi(0, idPenyewa, idKost, bulanTagihan, tglJatuhTempo, tglBayar, biayaSewa);

        String sql = "INSERT INTO transaksi (id_penyewa, id_kost, bulan_tagihan, tanggal_jatuh_tempo, " +
                     "tanggal_bayar, jumlah_hari_telat, denda, total_bayar) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trx.getIdPenyewa());
            pstmt.setInt(2, trx.getIdKost());
            pstmt.setString(3, trx.getBulanTagihan());
            pstmt.setDate(4, Date.valueOf(trx.getTanggalJatuhTempo()));
            pstmt.setDate(5, Date.valueOf(trx.getTanggalBayar()));
            pstmt.setInt(6, trx.getJumlahHariTelat());
            pstmt.setDouble(7, trx.getDenda());
            pstmt.setDouble(8, trx.getTotalBayar());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Mengambil seluruh riwayat transaksi untuk laporan POS Kasir
    public List<Transaksi> getAllTransaksi() {
        List<Transaksi> listTrx = new ArrayList<>();
        String sql = "SELECT * FROM transaksi";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Rekonstruksi objek transaksi dari database
                Transaksi t = new Transaksi(
                    rs.getInt("id_transaksi"),
                    rs.getInt("id_penyewa"),
                    rs.getInt("id_kost"),
                    rs.getString("bulan_tagihan"),
                    rs.getDate("tanggal_jatuh_tempo").toLocalDate(),
                    rs.getDate("tanggal_bayar").toLocalDate(),
                    0 // Biaya dasar dilewati karena akan dihitung dibawah
                );
                t.setJumlahHariTelat(rs.getInt("jumlah_hari_telat"));
                t.setDenda(rs.getDouble("denda"));
                t.setTotalBayar(rs.getDouble("total_bayar"));
                listTrx.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listTrx;
    }

    
    // HELPER METHOD
    public Kost getKostById(int idKost) {
        String sql = "SELECT * FROM kost WHERE id_kost = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idKost);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nama = rs.getString("nama_kost");
                    String tipe = rs.getString("tipe_kost");
                    double harga = rs.getDouble("harga_dasar");
                    int kapasitas = rs.getInt("kapasitas");
                    int terisi = rs.getInt("terisi");

                    if (tipe.equalsIgnoreCase("Elite")) {
                        return new KostElite(idKost, nama, harga, kapasitas, terisi);
                    } else {
                        return new KostEkonomis(idKost, nama, harga, kapasitas, terisi);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}