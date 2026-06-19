package com.kostify.model;

public class KostEkonomis extends Kost {
    
    public KostEkonomis(int idKost, String namaKost, double hargaDasar, int kapasitas, int terisi) {
        super(idKost, namaKost, hargaDasar, kapasitas, terisi);
    }

    // Overriding hitung biaya tanpa tambahan 
    @Override
    public double hitungBiayaBulanan() {
        return getHargaDasar();
    }

    @Override
    public String getTipeKost() {
        return "Ekonomis";
    }
}