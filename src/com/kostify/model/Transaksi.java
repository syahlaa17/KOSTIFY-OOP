package com.kostify.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Transaksi {
    // Static Final Variable: Tarif denda per hari 
    public static final double DENDA_PER_HARI = 20000.0;

    private int idTransaksi;
    private int idPenyewa;
    private int idKost;
    private String bulanTagihan;
    private LocalDate tanggalJatuhTempo;
    private LocalDate tanggalBayar;
    private int jumlahHariTelat;
    private double denda;
    private double totalBayar;

    // Constructor untuk inisialisasi awal transaksi baru
    public Transaksi(int idTransaksi, int idPenyewa, int idKost, String bulanTagihan, 
                     LocalDate tanggalJatuhTempo, LocalDate tanggalBayar, double hargaDasarAtauBiayaBulanan) {
        this.idTransaksi = idTransaksi;
        this.idPenyewa = idPenyewa;
        this.idKost = idKost;
        this.bulanTagihan = bulanTagihan;
        this.tanggalJatuhTempo = tanggalJatuhTempo;
        this.tanggalBayar = tanggalBayar;
        
        // Jalankan kalkulasi otomatis berdasarkan User Defined Method 
        this.jumlahHariTelat = hitungHariTelat();
        this.denda = hitungDenda();
        this.totalBayar = hargaDasarAtauBiayaBulanan + this.denda;
    }

    // User Defined Method: Menghitung selisih hari jika telat bayar 
    public int hitungHariTelat() {
        if (tanggalBayar.isAfter(tanggalJatuhTempo)) {
            return (int) ChronoUnit.DAYS.between(tanggalJatuhTempo, tanggalBayar);
        }
        return 0; // Tepat waktu atau bayar duluan
    }

    // User Defined Method: Menghitung akumulasi denda 
    public double hitungDenda() {
        return this.jumlahHariTelat * DENDA_PER_HARI;
    }

    // Getter & Setter
    public int getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(int idTransaksi) { this.idTransaksi = idTransaksi; }

    public int getIdPenyewa() { return idPenyewa; }
    public void setIdPenyewa(int idPenyewa) { this.idPenyewa = idPenyewa; }

    public int getIdKost() { return idKost; }
    public void setIdKost(int idKost) { this.idKost = idKost; }

    public String getBulanTagihan() { return bulanTagihan; }
    public void setBulanTagihan(String bulanTagihan) { this.bulanTagihan = bulanTagihan; }

    public LocalDate getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public void setTanggalJatuhTempo(LocalDate tanggalJatuhTempo) { this.tanggalJatuhTempo = tanggalJatuhTempo; }

    public LocalDate getTanggalBayar() { return tanggalBayar; }
    public void setTanggalBayar(LocalDate tanggalBayar) { this.tanggalBayar = tanggalBayar; }

    public int getJumlahHariTelat() { return jumlahHariTelat; }
    public void setJumlahHariTelat(int jumlahHariTelat) { this.jumlahHariTelat = jumlahHariTelat; }

    public double getDenda() { return denda; }
    public void setDenda(double denda) { this.denda = denda; }

    public double getTotalBayar() { return totalBayar; }
    public void setTotalBayar(double totalBayar) { this.totalBayar = totalBayar; }
}