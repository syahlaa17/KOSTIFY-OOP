package com.kostify.view;

import com.kostify.controller.KostifyController;
import com.kostify.model.KamarPenuhException;
import com.kostify.model.Kost;
import com.kostify.model.Penyewa;
import com.kostify.model.Transaksi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MainFrame extends JFrame {
    private KostifyController controller;

    // Komponen Tabel GUI
    private JTable tabelKost, tabelPenyewa, tabelTransaksi;
    private DefaultTableModel modelKost, modelPenyewa, modelTransaksi;

    public MainFrame() {
        controller = new KostifyController();

        // Pengaturan Awal Window Frame 
        setTitle("Kostify Management System & POS Kasir");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Judul Aplikasi Atas
        JLabel lblHeader = new JLabel("KOSTIFY DASHBOARD MANAGEMENT", JLabel.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(lblHeader, BorderLayout.NORTH);

        // Membuat Panel Tab (JTabbedPane)
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Masukkan masing-masing panel menu tab
        tabbedPane.addTab("Manajemen Kamar Kost", buatPanelKost());
        tabbedPane.addTab("Pendaftaran Penyewa", buatPanelPenyewa());
        tabbedPane.addTab("POS Kasir & Transaksi", buatPanelTransaksi());

        add(tabbedPane, BorderLayout.CENTER);

        // Load data awal dari database ke tabel GUI
        refreshSemuaTabel();
    }

    // TAB 1: PANEL MANAJEMEN KOST
    private JPanel buatPanelKost() {
        JPanel panel = new JPanel(new BorderLayout());

        // Setup Tabel Kost
        String[] kolom = {"ID Kost", "Nama Kost", "Tipe Kost", "Harga Dasar", "Kapasitas", "Terisi"};
        modelKost = new DefaultTableModel(kolom, 0);
        tabelKost = new JTable(modelKost);
        panel.add(new JScrollPane(tabelKost), BorderLayout.CENTER);

        // Panel Formulir & Tombol Aksi di Bawah Tabel
        JPanel panelAksi = new JPanel(new FlowLayout());
        JTextField txtNama = new JTextField(12);
        JComboBox<String> cbTipe = new JComboBox<>(new String[]{"Ekonomis", "Elite"});
        JTextField txtHarga = new JTextField(10);
        JTextField txtKapasitas = new JTextField(5);
        JButton btnTambah = new JButton("Tambah Kost");
        JButton btnHapus = new JButton("Hapus Terpilih");

        panelAksi.add(new JLabel("Nama:")); panelAksi.add(txtNama);
        panelAksi.add(new JLabel("Tipe:")); panelAksi.add(cbTipe);
        panelAksi.add(new JLabel("Harga:")); panelAksi.add(txtHarga);
        panelAksi.add(new JLabel("Kuota:")); panelAksi.add(txtKapasitas);
        panelAksi.add(btnTambah);
        panelAksi.add(btnHapus);
        panel.add(panelAksi, BorderLayout.SOUTH);

        // Event Listener Tambah Kost
        btnTambah.addActionListener(e -> {
            try {
                String nama = txtNama.getText().trim();
                String tipe = cbTipe.getSelectedItem().toString();
                double harga = Double.parseDouble(txtHarga.getText().trim());
                int kapasitas = Integer.parseInt(txtKapasitas.getText().trim());

                if (controller.tambahKost(nama, tipe, harga, kapasitas)) {
                    JOptionPane.showMessageDialog(this, "Kamar kost baru berhasil ditambahkan!");
                    refreshSemuaTabel();
                    txtNama.setText(""); txtHarga.setText(""); txtKapasitas.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Harap isi kolom angka dengan benar!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Event Listener Hapus Kost
        btnHapus.addActionListener(e -> {
            int barisTerpilih = tabelKost.getSelectedRow();
            if (barisTerpilih >= 0) {
                int idKost = (int) modelKost.getValueAt(barisTerpilih, 0);
                int konfirmasi = JOptionPane.showConfirmDialog(this, "Hapus kost ini? Penyewa di dalamnya akan ikut terhapus otomatis.", "Konfirmasi", JOptionPane.YES_NO_OPTION);
                if (konfirmasi == JOptionPane.YES_OPTION) {
                    controller.hapusKost(idKost);
                    refreshSemuaTabel();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Silakan pilih baris kost yang ingin dihapus terlebih dahulu.");
            }
        });

        return panel;
    }

    // TAB 2: PANEL PENDAFTARAN PENYEWA
    private JPanel buatPanelPenyewa() {
        JPanel panel = new JPanel(new BorderLayout());

        // Setup Tabel Penyewa
        String[] kolom = {"ID Penyewa", "Nama Penyewa", "No. KTP", "ID Tempat Kost"};
        modelPenyewa = new DefaultTableModel(kolom, 0);
        tabelPenyewa = new JTable(modelPenyewa);
        panel.add(new JScrollPane(tabelPenyewa), BorderLayout.CENTER);

        // Panel Formulir Input Penyewa
        JPanel panelAksi = new JPanel(new FlowLayout());
        JTextField txtNama = new JTextField(12);
        JTextField txtKtp = new JTextField(12);
        JTextField txtIdKost = new JTextField(5);
        JButton btnDaftar = new JButton("Daftarkan Penyewa");

        panelAksi.add(new JLabel("Nama Penyewa:")); panelAksi.add(txtNama);
        panelAksi.add(new JLabel("No. KTP:")); panelAksi.add(txtKtp);
        panelAksi.add(new JLabel("ID Kost Tujuan:")); panelAksi.add(txtIdKost);
        panelAksi.add(btnDaftar);
        panel.add(panelAksi, BorderLayout.SOUTH);

        // Event Listener Pendaftaran Penyewa dengan Custom Exception Handling 
        btnDaftar.addActionListener(e -> {
            try {
                String nama = txtNama.getText().trim();
                String ktp = txtKtp.getText().trim();
                int idKost = Integer.parseInt(txtIdKost.getText().trim());

                if (controller.tambahPenyewa(nama, ktp, idKost)) {
                    JOptionPane.showMessageDialog(this, "Penyewa berhasil didaftarkan ke kamar!");
                    refreshSemuaTabel();
                    txtNama.setText(""); txtKtp.setText(""); txtIdKost.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mendaftar. ID Kost tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (KamarPenuhException ex) {
                // Menangkap Custom Exception Kamar Penuh 
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Kamar Penuh", JOptionPane.WARNING_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID Kost harus berupa angka integer!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }


    // TAB 3: PANEL POS KASIR TRANSAKSI
    private JPanel buatPanelTransaksi() {
        JPanel panel = new JPanel(new BorderLayout());

        // Setup Tabel Transaksi
        String[] kolom = {"ID Trx", "ID Penyewa", "ID Kost", "Bulan Tagihan", "Batas Tempo", "Tgl Bayar", "Hari Telat", "Denda", "Total Bayar"};
        modelTransaksi = new DefaultTableModel(kolom, 0);
        tabelTransaksi = new JTable(modelTransaksi);
        panel.add(new JScrollPane(tabelTransaksi), BorderLayout.CENTER);

        // Panel Formulir POS Pembayaran
        JPanel panelAksi = new JPanel(new FlowLayout());
        JTextField txtIdPenyewa = new JTextField(4);
        JTextField txtIdKost = new JTextField(4);
        JTextField txtBulan = new JTextField(8); // Contoh: "Juni 2026"
        JTextField txtHariTelatManual = new JTextField(3); // jumlah hari telat
        JButton btnBayar = new JButton("Proses Bayar POS");

        panelAksi.add(new JLabel("ID Penyewa:")); panelAksi.add(txtIdPenyewa);
        panelAksi.add(new JLabel("ID Kost:")); panelAksi.add(txtIdKost);
        panelAksi.add(new JLabel("Bulan:")); panelAksi.add(txtBulan);
        panelAksi.add(new JLabel("Hari Telat (Simulasi):")); panelAksi.add(txtHariTelatManual);
        panelAksi.add(btnBayar);
        panel.add(panelAksi, BorderLayout.SOUTH);

        // Event Listener Kasir POS
        btnBayar.addActionListener(e -> {
            try {
                int idPenyewa = Integer.parseInt(txtIdPenyewa.getText().trim());
                int idKost = Integer.parseInt(txtIdKost.getText().trim());
                String bulan = txtBulan.getText().trim();
                int hariTelat = Integer.parseInt(txtHariTelatManual.getText().trim());

                // Logika Tanggal: Menggunakan waktu hari ini sebagai acuan bayar riil
                LocalDate tglJatuhTempo = LocalDate.now();
                // Jika disimulasikan telat x hari, maka tglBayar diundur ke depan supaya ada penghitungan denda otomatis di objek Transaksi
                LocalDate tglBayar = tglJatuhTempo.plusDays(hariTelat);

                if (controller.bayarKost(idPenyewa, idKost, bulan, tglJatuhTempo, tglBayar)) {
                    JOptionPane.showMessageDialog(this, "Pembayaran Kasir POS Sukses Ditambahkan!");
                    refreshSemuaTabel();
                    txtIdPenyewa.setText(""); txtIdKost.setText(""); txtBulan.setText(""); txtHariTelatManual.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal memproses pembayaran. Periksa kembali ID Kost & ID Penyewa.", "POS Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Harap masukkan data angka input dengan benar!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

   
    // UTILITY: REFRESH SYNCHRONIZE DATA
    private void refreshSemuaTabel() {
        // Refresh Tabel Kost
        modelKost.setRowCount(0);
        List<Kost> listKost = controller.getAllKost();
        for (Kost k : listKost) {
            modelKost.addRow(new Object[]{k.getIdKost(), k.getNamaKost(), k.getTipeKost(), k.getHargaDasar(), k.getKapasitas(), k.getTerisi()});
        }

        // Refresh Tabel Penyewa
        modelPenyewa.setRowCount(0);
        List<Penyewa> listPenyewa = controller.getAllPenyewa();
        for (Penyewa p : listPenyewa) {
            modelPenyewa.addRow(new Object[]{p.getIdPenyewa(), p.getNamaPenyewa(), p.getNomorKtp(), p.getIdKost()});
        }

        // Refresh Tabel Transaksi
        modelTransaksi.setRowCount(0);
        List<Transaksi> listTrx = controller.getAllTransaksi();
        for (Transaksi t : listTrx) {
            modelTransaksi.addRow(new Object[]{
                t.getIdTransaksi(), t.getIdPenyewa(), t.getIdKost(), t.getBulanTagihan(),
                t.getTanggalJatuhTempo(), t.getTanggalBayar(), t.getJumlahHariTelat(), t.getDenda(), t.getTotalBayar()
            });
        }
    }
}