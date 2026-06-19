package com.kostify.model;

public abstract class Kost {
    // Encapsulation
    private int idKost;
    private String namaKost;
    private double hargaDasar;
    private int kapasitas;
    private int terisi;

    // Constructor
    public Kost(int idKost, String namaKost, double hargaDasar, int kapasitas, int terisi) {
        this.idKost = idKost;
        this.namaKost = namaKost;
        this.hargaDasar = hargaDasar;
        this.kapasitas = kapasitas;
        this.terisi = terisi;
    }

    // User Defined Method untuk cek status kamar 
    public boolean isPenuh() {
        return terisi >= kapasitas;
    }

    public void tambahPenghuni() throws KamarPenuhException {
        if (isPenuh()) {
            throw new KamarPenuhException("Maaf, kamar di " + namaKost + " sudah penuh!");
        }
        this.terisi++;
    }

    // Abstract Methods yang WAJIB di-override oleh subclass (P11)
    public abstract double hitungBiayaBulanan();
    public abstract String getTipeKost();

    // Getter & Setter 
    public int getIdKost() { return idKost; }
    public void setIdKost(int idKost) { this.idKost = idKost; }

    public String getNamaKost() { return namaKost; }
    public void setNamaKost(String namaKost) { this.namaKost = namaKost; }

    public double getHargaDasar() { return hargaDasar; }
    public void setHargaDasar(double hargaDasar) { this.hargaDasar = hargaDasar; }

    public int getKapasitas() { return kapasitas; }
    public void setKapasitas(int kapasitas) { this.kapasitas = kapasitas; }

    public int getTerisi() { return terisi; }
    public void setTerisi(int terisi) { this.terisi = terisi; }
}