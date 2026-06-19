package com.kostify.model;

public class Penyewa {
    private int idPenyewa;
    private String namaPenyewa;
    private String nomorKtp;
    private int idKost; // Relasi FK ke tabel Kost

    // Constructor
    public Penyewa(int idPenyewa, String namaPenyewa, String nomorKtp, int idKost) {
        this.idPenyewa = idPenyewa;
        this.namaPenyewa = namaPenyewa;
        this.nomorKtp = nomorKtp;
        this.idKost = idKost;
    }

    // Getter & Setter
    public int getIdPenyewa() { return idPenyewa; }
    public void setIdPenyewa(int idPenyewa) { this.idPenyewa = idPenyewa; }

    public String getNamaPenyewa() { return namaPenyewa; }
    public void setNamaPenyewa(String namaPenyewa) { this.namaPenyewa = namaPenyewa; }

    public String getNomorKtp() { return nomorKtp; }
    public void setNomorKtp(String nomorKtp) { this.nomorKtp = nomorKtp; }

    public int getIdKost() { return idKost; }
    public void setIdKost(int idKost) { this.idKost = idKost; }
}