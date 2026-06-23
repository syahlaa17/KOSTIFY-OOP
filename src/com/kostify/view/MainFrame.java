package com.kostify.view;

import com.kostify.controller.KostifyController;
import com.kostify.model.KamarPenuhException;
import com.kostify.model.Kost;
import com.kostify.model.Penyewa;
import com.kostify.model.Transaksi;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;

public class MainFrame extends JFrame {
    private KostifyController controller;

    // Komponen Tabel GUI
    private JTable tabelKost, tabelTransaksi;
    private DefaultTableModel modelKost, modelTransaksi;

    // Panel penyewa (versi lengkap buatan Nadia) — dikelola sebagai kelas terpisah
    private PanelPenyewa panelPenyewa;

    // Label kartu statistik panel Kost
    private JLabel lblTotalKamar, lblTerisi, lblKosong, lblHunian;

    public MainFrame() {
        controller = new KostifyController();

        // Pengaturan Awal Window Frame 
        setTitle("Kostify Management System & POS Kasir");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Judul Aplikasi Atas — header bar navy modern dengan gradient + subtitle
        final Color navyHeader1 = new Color(13, 31, 61);   // navy gelap (#0D1F3D)
        final Color navyHeader2 = new Color(28, 58, 110);  // navy sedang (#1C3A6E)
        JPanel headerBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, navyHeader1, getWidth(), 0, navyHeader2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerBar.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel headerTeks = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTeks.setOpaque(false);
        JLabel lblHeader = new JLabel("KOSTIFY DASHBOARD MANAGEMENT");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(Color.WHITE);
        JLabel lblSubHeader = new JLabel("Sistem Manajemen Properti & POS Tagihan Kost");
        lblSubHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubHeader.setForeground(new Color(176, 196, 222)); // navy terang lembut
        headerTeks.add(lblHeader);
        headerTeks.add(lblSubHeader);

        headerBar.add(headerTeks, BorderLayout.WEST);
        add(headerBar, BorderLayout.NORTH);

        // Membuat Panel Tab (JTabbedPane)
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Masukkan masing-masing panel menu tab
        tabbedPane.addTab("Manajemen Kamar Kost", buatPanelKost());
        panelPenyewa = new PanelPenyewa();
        tabbedPane.addTab("Pendaftaran Penyewa", panelPenyewa);
        tabbedPane.addTab("POS Kasir & Transaksi", buatPanelTransaksi());
        tabbedPane.addTab("Laporan Ringkasan", new LaporanRingkasan());

        add(tabbedPane, BorderLayout.CENTER);

        // Load data awal dari database ke tabel GUI
        refreshSemuaTabel();
    }

    // TAB 1: PANEL MANAJEMEN KOST
    private JPanel buatPanelKost() {
        // Palet warna khusus panel Kost (tema navy profesional)
        final Color indigo = new Color(18, 45, 86);        // navy utama (#122D56)
        final Color merah = new Color(193, 64, 84);        // merah elegan untuk tombol hapus
        final Color bgHalaman = new Color(241, 244, 249);  // abu kebiruan lembut
        final String fontUtama = "Segoe UI";

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(bgHalaman);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ===== BAGIAN ATAS: judul + kartu statistik =====
        JPanel panelAtas = new JPanel(new BorderLayout(0, 12));
        panelAtas.setOpaque(false);

        panelAtas.add(buatHeaderBar(fontUtama, indigo), BorderLayout.NORTH);

        // Kartu statistik (dihitung otomatis dari data Kost)
        lblTotalKamar = buatLabelAngka(new Color(24, 31, 46), fontUtama);
        lblTerisi = buatLabelAngka(new Color(22, 138, 102), fontUtama);
        lblKosong = buatLabelAngka(new Color(193, 64, 84), fontUtama);
        lblHunian = buatLabelAngka(indigo, fontUtama);

        JPanel panelStat = new JPanel(new GridLayout(1, 4, 10, 0));
        panelStat.setOpaque(false);
        panelStat.add(kartuStatistik("Total Kamar", lblTotalKamar, fontUtama));
        panelStat.add(kartuStatistik("Terisi", lblTerisi, fontUtama));
        panelStat.add(kartuStatistik("Kosong", lblKosong, fontUtama));
        panelStat.add(kartuStatistik("Hunian", lblHunian, fontUtama));
        panelAtas.add(panelStat, BorderLayout.SOUTH);

        panel.add(panelAtas, BorderLayout.NORTH);

        // ===== TABEL =====
        String[] kolom = {"ID Kost", "Nama Kost", "Tipe Kost", "Harga Dasar", "Kapasitas", "Terisi"};
        modelKost = new DefaultTableModel(kolom, 0);
        tabelKost = new JTable(modelKost);

        tabelKost.setRowHeight(30);
        tabelKost.setFont(new Font(fontUtama, Font.PLAIN, 13));
        tabelKost.setSelectionBackground(new Color(214, 226, 242));
        tabelKost.setSelectionForeground(new Color(24, 31, 46));
        tabelKost.setShowVerticalLines(false);
        tabelKost.setGridColor(new Color(237, 238, 243));

        // Format Rupiah tanpa bergantung pada Locale (hindari peringatan deprecated)
        DecimalFormatSymbols simbol = new DecimalFormatSymbols();
        simbol.setGroupingSeparator('.');
        final DecimalFormat rupiah = new DecimalFormat("#,###", simbol);

        // Renderer isi tabel: format Rupiah + warna baris selang-seling + perataan
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Object tampil = value;
                if (column == 3 && value instanceof Number) {
                    tampil = "Rp " + rupiah.format(((Number) value).doubleValue());
                }
                Component c = super.getTableCellRendererComponent(table, tampil, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 248, 251));
                }
                if (column == 3) {
                    setHorizontalAlignment(RIGHT);
                } else if (column == 0 || column == 4 || column == 5) {
                    setHorizontalAlignment(CENTER);
                } else {
                    setHorizontalAlignment(LEFT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        tabelKost.setDefaultRenderer(Object.class, renderer);

        // Header tabel berwarna
        JTableHeader header = tabelKost.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                lbl.setBackground(indigo);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font(fontUtama, Font.BOLD, 13));
                lbl.setHorizontalAlignment(CENTER);
                lbl.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(tabelKost);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel wadahTabel = buatKartuBulat();
        wadahTabel.setLayout(new BorderLayout());
        wadahTabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        wadahTabel.add(scroll, BorderLayout.CENTER);
        panel.add(wadahTabel, BorderLayout.CENTER);

        // ===== KARTU FORMULIR =====
        JPanel formCard = buatKartuBulat();
        formCard.setLayout(new BorderLayout(0, 10));
        formCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel lblForm = new JLabel("Tambah Kamar Kost Baru");
        lblForm.setFont(new Font(fontUtama, Font.BOLD, 13));
        lblForm.setForeground(indigo);
        formCard.add(lblForm, BorderLayout.NORTH);

        JPanel isiForm = new JPanel(new BorderLayout(0, 8));
        isiForm.setOpaque(false);

        JPanel barisInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        barisInput.setOpaque(false);
        JTextField txtNama = new JTextField(12);
        JComboBox<String> cbTipe = new JComboBox<>(new String[]{"Ekonomis", "Elite"});
        JTextField txtHarga = new JTextField(10);
        JTextField txtKapasitas = new JTextField(5);
        styleField(txtNama, fontUtama); styleField(txtHarga, fontUtama);
        styleField(txtKapasitas, fontUtama); styleComboBox(cbTipe, fontUtama);

        barisInput.add(buatLabel("Nama:", fontUtama)); barisInput.add(txtNama);
        barisInput.add(buatLabel("Tipe:", fontUtama)); barisInput.add(cbTipe);
        barisInput.add(buatLabel("Harga:", fontUtama)); barisInput.add(txtHarga);
        barisInput.add(buatLabel("Kuota:", fontUtama)); barisInput.add(txtKapasitas);

        JPanel barisTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        barisTombol.setOpaque(false);
        JButton btnTambah = new JButton("Tambah Kost");
        JButton btnHapus = new JButton("Hapus Terpilih");
        styleButton(btnTambah, indigo, fontUtama);
        styleButton(btnHapus, merah, fontUtama);
        barisTombol.add(btnTambah);
        barisTombol.add(btnHapus);

        isiForm.add(barisInput, BorderLayout.CENTER);
        isiForm.add(barisTombol, BorderLayout.SOUTH);
        formCard.add(isiForm, BorderLayout.CENTER);
        panel.add(formCard, BorderLayout.SOUTH);

        // Event Listener Tambah Kost
        btnTambah.addActionListener(e -> {
            try {
                String nama = txtNama.getText().trim();
                String tipe = cbTipe.getSelectedItem().toString();

                // Validasi sederhana sebelum simpan
                if (nama.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nama kost wajib diisi!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double harga = Double.parseDouble(txtHarga.getText().trim());
                int kapasitas = Integer.parseInt(txtKapasitas.getText().trim());
                if (harga <= 0 || kapasitas <= 0) {
                    JOptionPane.showMessageDialog(this, "Harga dan kuota harus lebih dari 0!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

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

    // ===== HELPER STYLING (khusus tampilan panel Kost) =====

    // Kartu putih dengan sudut membulat (dipakai tabel, form, & kartu statistik)
    private JPanel buatKartuBulat() {
        JPanel kartu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(230, 232, 239));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        kartu.setOpaque(false);
        return kartu;
    }

    // Satu kartu statistik: judul kecil di atas, angka besar di bawah
    private JPanel kartuStatistik(String judul, JLabel lblAngka, String fontUtama) {
        JPanel kartu = buatKartuBulat();
        kartu.setLayout(new BorderLayout());
        kartu.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font(fontUtama, Font.PLAIN, 12));
        lblJudul.setForeground(new Color(138, 141, 155));
        kartu.add(lblJudul, BorderLayout.NORTH);
        kartu.add(lblAngka, BorderLayout.SOUTH);
        return kartu;
    }

    private JLabel buatLabelAngka(Color warna, String fontUtama) {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font(fontUtama, Font.BOLD, 22));
        lbl.setForeground(warna);
        return lbl;
    }

    private JLabel buatLabel(String teks, String fontUtama) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(new Font(fontUtama, Font.PLAIN, 13));
        lbl.setForeground(new Color(90, 92, 105));
        return lbl;
    }

    private void styleField(JComponent field, String fontUtama) {
        field.setFont(new Font(fontUtama, Font.PLAIN, 13));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 210, 220)),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)));
    }

    private void styleButton(JButton btn, Color warna, String fontUtama) {
        btn.setBackground(warna);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(fontUtama, Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMargin(new Insets(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Header bar gradien dengan badge "K" + judul (di dalam panel Kost)
    private JPanel buatHeaderBar(String fontUtama, Color indigo) {
        final Color indigo2 = new Color(32, 67, 124);
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, indigo, getWidth(), 0, indigo2));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setLayout(new BorderLayout(12, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel badge = new JLabel("K", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(42, 42));
        badge.setFont(new Font(fontUtama, Font.BOLD, 20));
        badge.setForeground(indigo);

        JPanel teks = new JPanel(new GridLayout(2, 1));
        teks.setOpaque(false);
        JLabel judul = new JLabel("Manajemen Kamar Kost");
        judul.setFont(new Font(fontUtama, Font.BOLD, 17));
        judul.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Kelola data kamar kost: tambah, lihat, dan hapus.");
        sub.setFont(new Font(fontUtama, Font.PLAIN, 12));
        sub.setForeground(new Color(186, 203, 226));
        teks.add(judul);
        teks.add(sub);

        bar.add(badge, BorderLayout.WEST);
        bar.add(teks, BorderLayout.CENTER);
        return bar;
    }

    // Dropdown (JComboBox) versi flat: panah custom, tanpa bevel kaku
    private void styleComboBox(JComboBox<?> combo, String fontUtama) {
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(new Color(120, 124, 140));
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        int[] xs = {cx - 4, cx + 4, cx};
                        int[] ys = {cy - 2, cy - 2, cy + 3};
                        g2.fillPolygon(xs, ys, 3);
                        g2.dispose();
                    }
                };
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return b;
            }
        });
        combo.setFont(new Font(fontUtama, Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(40, 42, 55));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 210, 220)),
                BorderFactory.createEmptyBorder(4, 9, 4, 4)));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
                lbl.setFont(new Font(fontUtama, Font.PLAIN, 13));
                if (isSelected) {
                    lbl.setBackground(new Color(214, 226, 242));
                    lbl.setForeground(new Color(24, 31, 46));
                }
                return lbl;
            }
        });
    }

    // Hitung & tampilkan statistik dari data tabel Kost
    private void updateStatistikKost() {
        if (modelKost == null || lblTotalKamar == null) return;
        int totalKapasitas = 0, totalTerisi = 0;
        for (int i = 0; i < modelKost.getRowCount(); i++) {
            totalKapasitas += ((Number) modelKost.getValueAt(i, 4)).intValue();
            totalTerisi += ((Number) modelKost.getValueAt(i, 5)).intValue();
        }
        int kosong = totalKapasitas - totalTerisi;
        int persen = totalKapasitas == 0 ? 0 : Math.round(totalTerisi * 100f / totalKapasitas);
        lblTotalKamar.setText(String.valueOf(totalKapasitas));
        lblTerisi.setText(String.valueOf(totalTerisi));
        lblKosong.setText(String.valueOf(kosong));
        lblHunian.setText(persen + "%");
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

        // Refresh Tabel Penyewa (didelegasikan ke PanelPenyewa milik Nadia)
        if (panelPenyewa != null) {
            panelPenyewa.muatDataPenyewa();
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

        // Perbarui kartu statistik panel Kost
        updateStatistikKost();
    }

    // =====================================================================
    //  INNER CLASS: LaporanRingkasan
    //  Tugas Zaidan (Integrasi & Dashboard).
    //  Menampilkan ringkasan data dari SEMUA fitur (Kost, Penyewa, Transaksi)
    //  dengan cara mengambil list dari controller lalu menjumlahkannya.
    //
    //  Disebut "inner class" karena didefinisikan DI DALAM kelas MainFrame.
    //  Keuntungannya: bisa langsung mengakses 'controller' milik MainFrame
    //  tanpa harus membuat objek controller baru.
    // =====================================================================
    private class LaporanRingkasan extends JPanel {

        // Label angka yang akan diisi hasil perhitungan
        private JLabel lblTotalKamar, lblTotalPenyewa, lblTotalPemasukan, lblTotalDenda;

        public LaporanRingkasan() {
            // ---- Pengaturan dasar panel ----
            setLayout(new BorderLayout());
            setBackground(new Color(241, 244, 249));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // ---- Header navy (senada dengan tab lain) ----
            JPanel header = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, new Color(18, 45, 86),
                                                  getWidth(), 0, new Color(32, 67, 124)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
            };
            header.setOpaque(false);
            header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            JLabel judul = new JLabel("Laporan Ringkasan");
            judul.setFont(new Font("Segoe UI", Font.BOLD, 17));
            judul.setForeground(Color.WHITE);
            JLabel sub = new JLabel("Rangkuman data kost, penyewa, dan transaksi");
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            sub.setForeground(new Color(186, 203, 226));
            JPanel teksHeader = new JPanel(new GridLayout(2, 1, 0, 2));
            teksHeader.setOpaque(false);
            teksHeader.add(judul);
            teksHeader.add(sub);
            header.add(teksHeader, BorderLayout.WEST);
            add(header, BorderLayout.NORTH);

            // ---- Grid 4 kartu ringkasan ----
            JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
            grid.setOpaque(false);
            grid.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

            lblTotalKamar    = new JLabel("0");
            lblTotalPenyewa  = new JLabel("0");
            lblTotalPemasukan = new JLabel("Rp 0");
            lblTotalDenda    = new JLabel("Rp 0");

            grid.add(buatKartu("Total Kapasitas Kamar", lblTotalKamar, new Color(24, 31, 46)));
            grid.add(buatKartu("Total Penyewa Aktif", lblTotalPenyewa, new Color(22, 138, 102)));
            grid.add(buatKartu("Total Pemasukan", lblTotalPemasukan, new Color(18, 45, 86)));
            grid.add(buatKartu("Total Denda Terkumpul", lblTotalDenda, new Color(193, 64, 84)));

            add(grid, BorderLayout.CENTER);

            // ---- Tombol refresh di bawah ----
            JButton btnRefresh = new JButton("Muat Ulang Laporan");
            btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnRefresh.setBackground(new Color(18, 45, 86));
            btnRefresh.setForeground(Color.WHITE);
            btnRefresh.setFocusPainted(false);
            btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btnRefresh.addActionListener(e -> hitungLaporan());
            JPanel bawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bawah.setOpaque(false);
            bawah.add(btnRefresh);
            add(bawah, BorderLayout.SOUTH);

            // Hitung pertama kali saat panel dibuka
            hitungLaporan();
        }

        // ------------------------------------------------------------------
        //  INTI LAPORAN: mengambil data dari controller lalu menjumlahkan.
        //  Pola tiap angka sama: ambil list -> loop -> akumulasi.
        // ------------------------------------------------------------------
        private void hitungLaporan() {
            // 1) TOTAL KAPASITAS KAMAR — dari fitur Kost (Maulina)
            int totalKamar = 0;
            for (Kost k : controller.getAllKost()) {
                totalKamar += k.getKapasitas();
            }

            // 2) TOTAL PENYEWA AKTIF — dari fitur Penyewa (Nadia)
            List<Penyewa> daftarPenyewa = controller.getAllPenyewa();
            int totalPenyewa = daftarPenyewa.size();

            // 3) TOTAL PEMASUKAN & 4) TOTAL DENDA — dari fitur Transaksi (Aul)
            double totalPemasukan = 0;
            double totalDenda = 0;
            for (Transaksi t : controller.getAllTransaksi()) {
                totalPemasukan += t.getTotalBayar();
                totalDenda += t.getDenda();
            }

            // Format angka uang biar rapi: Rp 1.300.000
            DecimalFormatSymbols simbol = new DecimalFormatSymbols();
            simbol.setGroupingSeparator('.');
            DecimalFormat format = new DecimalFormat("#,###", simbol);

            // Tampilkan hasil ke label
            lblTotalKamar.setText(String.valueOf(totalKamar));
            lblTotalPenyewa.setText(String.valueOf(totalPenyewa));
            lblTotalPemasukan.setText("Rp " + format.format(totalPemasukan));
            lblTotalDenda.setText("Rp " + format.format(totalDenda));
        }

        // Helper: membuat satu kartu putih berisi judul kecil + angka besar
        private JPanel buatKartu(String judulKartu, JLabel labelAngka, Color warnaAngka) {
            JPanel kartu = new JPanel(new BorderLayout(0, 8));
            kartu.setBackground(Color.WHITE);
            kartu.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 232, 239)),
                    BorderFactory.createEmptyBorder(18, 20, 18, 20)));

            JLabel jdl = new JLabel(judulKartu);
            jdl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            jdl.setForeground(new Color(138, 141, 155));

            labelAngka.setFont(new Font("Segoe UI", Font.BOLD, 26));
            labelAngka.setForeground(warnaAngka);

            kartu.add(jdl, BorderLayout.NORTH);
            kartu.add(labelAngka, BorderLayout.CENTER);
            return kartu;
        }
    }
}