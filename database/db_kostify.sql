-- Tabel Kost (Induk)
CREATE TABLE IF NOT EXISTS kost (
    id_kost INT AUTO_INCREMENT PRIMARY KEY,
    nama_kost VARCHAR(100) NOT NULL,
    tipe_kost VARCHAR(20) NOT NULL, 
    harga_dasar DOUBLE NOT NULL,
    kapasitas INT NOT NULL,
    terisi INT NOT NULL DEFAULT 0
);

-- Tabel Penyewa (Anak dari Tabel Kost)
CREATE TABLE IF NOT EXISTS penyewa (
    id_penyewa INT AUTO_INCREMENT PRIMARY KEY,
    nama_penyewa VARCHAR(100) NOT NULL,
    nomor_ktp VARCHAR(50) NOT NULL,
    id_kost INT NOT NULL,
    FOREIGN KEY (id_kost) REFERENCES kost(id_kost) ON DELETE CASCADE
);

-- Tabel Transaksi (Anak dri Tabel Kost dan Penyewa, Khusus perhitungan denda POS)
CREATE TABLE IF NOT EXISTS transaksi (
    id_transaksi INT AUTO_INCREMENT PRIMARY KEY,
    id_penyewa INT NOT NULL,
    id_kost INT NOT NULL,
    bulan_tagihan VARCHAR(20) NOT NULL,    
    tanggal_jatuh_tempo DATE NOT NULL,     
    tanggal_bayar DATE NOT NULL,           -- Kapan riilnya dia bayar
    jumlah_hari_telat INT NOT NULL DEFAULT 0, -- Hasil hitungHariTelat() dari Java
    denda DOUBLE NOT NULL DEFAULT 0,       -- Hasil hitungDenda() dari Java
    total_bayar DOUBLE NOT NULL,           -- Harga dasar + denda
    FOREIGN KEY (id_penyewa) REFERENCES penyewa(id_penyewa) ON DELETE CASCADE,
    FOREIGN KEY (id_kost) REFERENCES kost(id_kost) ON DELETE CASCADE
);