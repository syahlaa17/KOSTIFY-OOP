# Kostify: Aplikasi Manajemen Properti & POS Tagihan Kost

## 1. Tentang Proyek
**Kostify** adalah sistem manajemen properti berbasis digital yang dirancang khusus untuk membantu pemilik bisnis kos-kosan mengelola banyak cabang dari satu pintu akses. Aplikasi ini bertindak sebagai pusat digital operasional yang menangani pencatatan ketersediaan kamar di berbagai daerah, pendaftaran data penyewa, hingga pemrosesan transaksi pembayaran bulanan kasir (POS) lengkap dengan perhitungan denda keterlambatan secara otomatis.

## 2. Anggota Kelompok & Pembagian Tugas
| Nama Anggota | Modul / Fitur | Tanggung Jawab Kode & File |
| **Syahla** | Database & Arsitektur Inti | `db_kostify.sql`, `DatabaseConnection.java`, `Main.java`, Setup Proyek Git |
| **Maulina** | Fitur Kost (Kamar) | `Kost.java`, `KostElite.java`, `KostEkonomis.java`, Panel Kost GUI, Controller Kost |
| **Nadia** | Fitur Penyewa | `Penyewa.java`, `KamarPenuhException.java`, Panel Penyewa GUI, Controller Penyewa |
| **Aul** | Fitur POS Transaksi | `Transaksi.java`, Panel POS GUI, Controller Transaksi (Sistem Kasir & Denda) |
| **Zaidan** | Integrasi & Dashboard | `LaporanRingkasan` (Inner Class), Integrasi JTabbedPane, Analisis Bug & Editor Laporan |

## 3. Prasyarat Sistem (Prerequisites)
Instal ini
1. **Java Development Kit (JDK)** versi 17 atau yang lebih baru.
2. **Extension Pack for Java** instal di VSCode
3. **XAMPP Control Panel** (untuk menjalankan MySQL Server).
4. **Driver JDBC**: `mysql-connector-j-8.x.x.jar` (taro di folder `lib/`).

## 4. Struktur Folder Proyek
Menggunakan struktur arsitektur **MVC** di VSCode:
Kostify/
│
├── database/
│   └── db_kostify.sql                <- Skema database MySQL
│
├── lib/
│   └── mysql-connector-j-8.x.x.jar   <- Driver JDBC MySQL
│
└── src/
    └── com/
        └── kostify/
            ├── Main.java                       <- Entry point utama aplikasi
            │
            ├── db/
            │   └── DatabaseConnection.java      <- Manajemen koneksi JDBC
            │
            ├── model/                           <- Lapisan MODEL
            │   ├── Kost.java                    <- Abstract Class Induk Kost
            │   ├── KostElite.java                <- Subclass Tipe Kost Elite
            │   ├── KostEkonomis.java             <- Subclass Tipe Kost Ekonomis
            │   ├── Penyewa.java                  <- Enkapsulasi Data Penyewa
            │   ├── Transaksi.java                 <- Logika Perhitungan Kasir POS
            │   └── KamarPenuhException.java       <- Custom Exception Handling
            │
            ├── view/                            <- Lapisan VIEW (GUI Swing)
            │   └── MainFrame.java                <- Desain interface JTabbedPane
            │
            └── controller/                      <- Lapisan CONTROLLER
                └── KostifyController.java        <- Logika Query SQL & Event Handler

## 5. Databse
1. Download driver JDBC https://dev.mysql.com/downloads/connector/j/ (instal yg Zip) trs cari file .jar pindahin ke folder lib
2. Start XAMPP: Nyalakan Apache dan MySQL di XAMPP Control Panel
3. Buat Database Baru: Buka http://localhost/phpmyadmin, buat database baru dengan nama persis: db_kostify
4. Import File SQL: Klik database db_kostify, pilih menu Impor (Import) di atas, masukkan file db_kostify.sql yang ada di dalam folder project, trs klik Kirim/Go
5. Selesai:  Run pake tombol tulisan "run" kecil, biasanya diatas public static void 