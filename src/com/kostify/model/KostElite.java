package com.kostify.model;

public class KostElite extends Kost {
    // Static Final Variable 
    public static final double BIAYA_FASILITAS = 500000.0; // Tambahan AC, WiFi, Kamar Mandi Dalam

    // Constructor mewarisi bapaknya (P6)
    public KostElite(int idKost, String namaKost, double hargaDasar, int kapasitas, int terisi) {
        super(idKost, namaKost, hargaDasar, kapasitas, terisi);
    }

    // Polymorphism: Overriding method abstrak 
    @Override
    public double hitungBiayaBulanan() {
        return getHargaDasar() + BIAYA_FASILITAS;
    }

    @Override
    public String getTipeKost() {
        return "Elite";
    }
}