package com.kostify.view;

import com.kostify.controller.KostifyController;
import com.kostify.model.KamarPenuhException;
import com.kostify.model.Kost;
import com.kostify.model.Penyewa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelPenyewa extends JPanel {

    // ── Controller ──────────────────────────────────────────────
    private final KostifyController controller = new KostifyController();

    // ── Komponen Form Input ──────────────────────────────────────
    private JTextField txtNama;
    private JTextField txtKtp;
    private JComboBox<String> cmbKost;   // menampilkan nama kost
    private List<Kost> listKost;         // data kost untuk mapping nama → id

    // ── Tabel ────────────────────────────────────────────────────
    private JTable tabelPenyewa;
    private DefaultTableModel modelTabel;

    // ── Tombol ───────────────────────────────────────────────────
    private JButton btnTambah;
    private JButton btnUpdate;
    private JButton btnHapus;
    private JButton btnBersihkan;

    // ── State: baris yang sedang dipilih ─────────────────────────
    private int idPenyewaTerpilih = -1;

    // ════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════
    public PanelPenyewa() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buatPanelJudul(),  BorderLayout.NORTH);
        add(buatPanelForm(),   BorderLayout.WEST);
        add(buatPanelTabel(),  BorderLayout.CENTER);

        muatDataKostKeComboBox();
        muatDataPenyewa();
    }

    // ════════════════════════════════════════════════════════════
    //  BUILDER PANEL
    // ════════════════════════════════════════════════════════════

    /** Panel judul di bagian atas */
    private JPanel buatPanelJudul() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Manajemen Data Penyewa");
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(label);
        return panel;
    }

    /** Panel form input + tombol di sebelah kiri */
    private JPanel buatPanelForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Form Penyewa"));
        panel.setPreferredSize(new Dimension(280, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        // ── Baris 0: Nama Penyewa ──
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Nama Penyewa:"), gbc);

        gbc.gridy = 1;
        txtNama = new JTextField(18);
        panel.add(txtNama, gbc);

        // ── Baris 2: Nomor KTP ──
        gbc.gridy = 2;
        panel.add(new JLabel("Nomor KTP (16 digit):"), gbc);

        gbc.gridy = 3;
        txtKtp = new JTextField(18);
        panel.add(txtKtp, gbc);

        // ── Baris 4: Pilih Kost ──
        gbc.gridy = 4;
        panel.add(new JLabel("Pilih Kost:"), gbc);

        gbc.gridy = 5;
        cmbKost = new JComboBox<>();
        panel.add(cmbKost, gbc);

        // ── Separator ──
        gbc.gridy = 6;
        panel.add(new JSeparator(), gbc);

        // ── Baris 7-10: Tombol ──
        gbc.gridwidth = 1;
        gbc.weightx   = 0.5;

        btnTambah = new JButton("Tambah");
        btnTambah.setBackground(new Color(46, 139, 87));
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(btnTambah, gbc);

        btnUpdate = new JButton("Update");
        btnUpdate.setBackground(new Color(30, 144, 255));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setEnabled(false);
        gbc.gridx = 1;
        panel.add(btnUpdate, gbc);

        btnHapus = new JButton("Hapus");
        btnHapus.setBackground(new Color(220, 53, 69));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFocusPainted(false);
        btnHapus.setEnabled(false);
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(btnHapus, gbc);

        btnBersihkan = new JButton("Bersihkan");
        btnBersihkan.setFocusPainted(false);
        gbc.gridx = 1;
        panel.add(btnBersihkan, gbc);

        // ── Daftarkan action listener ──
        btnTambah.addActionListener(e -> tambahPenyewa());
        btnUpdate.addActionListener(e -> updatePenyewa());
        btnHapus.addActionListener(e -> hapusPenyewa());
        btnBersihkan.addActionListener(e -> bersihkanForm());

        return panel;
    }

    /** Panel tabel di tengah/kanan */
    private JPanel buatPanelTabel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Daftar Penyewa"));

        // ── Definisi kolom ──
        String[] kolom = {"ID", "Nama Penyewa", "Nomor KTP", "Nama Kost"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tabelPenyewa = new JTable(modelTabel);
        tabelPenyewa.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelPenyewa.setRowHeight(26);
        tabelPenyewa.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // ── Sembunyikan kolom ID (tetap ada untuk referensi) ──
        tabelPenyewa.getColumnModel().getColumn(0).setMinWidth(0);
        tabelPenyewa.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelPenyewa.getColumnModel().getColumn(0).setWidth(0);

        // ── Listener: klik baris → isi form ──
        tabelPenyewa.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFormDariBaris();
        });

        JScrollPane scroll = new JScrollPane(tabelPenyewa);
        panel.add(scroll, BorderLayout.CENTER);

        // ── Tombol refresh ──
        JButton btnRefresh = new JButton("↻ Refresh");
        btnRefresh.addActionListener(e -> muatDataPenyewa());
        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBawah.add(btnRefresh);
        panel.add(panelBawah, BorderLayout.SOUTH);

        return panel;
    }

    // ════════════════════════════════════════════════════════════
    //  LOAD DATA
    // ════════════════════════════════════════════════════════════

    /** Isi ComboBox dengan nama kost dari database */
    private void muatDataKostKeComboBox() {
        listKost = controller.getAllKost();
        cmbKost.removeAllItems();
        for (Kost k : listKost) {
            cmbKost.addItem(k.getNamaKost());
        }
    }

    /** Muat ulang seluruh data penyewa ke tabel */
    public void muatDataPenyewa() {
        modelTabel.setRowCount(0); // kosongkan tabel
        List<Penyewa> listPenyewa = controller.getAllPenyewa();

        for (Penyewa p : listPenyewa) {
            String namaKost = getNamaKostById(p.getIdKost());
            modelTabel.addRow(new Object[]{
                p.getIdPenyewa(),
                p.getNamaPenyewa(),
                p.getNomorKtp(),
                namaKost
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  EVENT HANDLER
    // ════════════════════════════════════════════════════════════

    /** Tambah penyewa baru ke database */
    private void tambahPenyewa() {
        if (!validasiForm()) return;

        String nama = txtNama.getText().trim();
        String ktp  = txtKtp.getText().trim();
        int idKost  = getIdKostDariComboBox();

        try {
            boolean berhasil = controller.tambahPenyewa(nama, ktp, idKost);
            if (berhasil) {
                JOptionPane.showMessageDialog(this,
                    "Penyewa berhasil didaftarkan!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
                muatDataPenyewa();
                muatDataKostKeComboBox(); // update sisa kamar di combobox
                bersihkanForm();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal mendaftarkan penyewa. Coba lagi.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (KamarPenuhException ex) {
            // Tampilkan pesan KamarPenuhException dengan dialog khusus
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Kamar Penuh!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Update data penyewa yang dipilih */
    private void updatePenyewa() {
        if (idPenyewaTerpilih == -1) {
            JOptionPane.showMessageDialog(this, "Pilih penyewa dari tabel terlebih dahulu.");
            return;
        }
        if (!validasiForm()) return;

        String nama = txtNama.getText().trim();
        String ktp  = txtKtp.getText().trim();
        int idKost  = getIdKostDariComboBox();

        boolean berhasil = controller.updatePenyewa(idPenyewaTerpilih, nama, ktp, idKost);
        if (berhasil) {
            JOptionPane.showMessageDialog(this,
                "Data penyewa berhasil diperbarui!",
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            muatDataPenyewa();
            bersihkanForm();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal memperbarui data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Hapus penyewa yang dipilih */
    private void hapusPenyewa() {
        if (idPenyewaTerpilih == -1) {
            JOptionPane.showMessageDialog(this, "Pilih penyewa dari tabel terlebih dahulu.");
            return;
        }

        int baris = tabelPenyewa.getSelectedRow();
        String namaPenyewa = (String) modelTabel.getValueAt(baris, 1);

        int konfirmasi = JOptionPane.showConfirmDialog(this,
            "Hapus penyewa \"" + namaPenyewa + "\"?\nKuota kamar akan berkurang 1.",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            int idKost = getIdKostDariComboBox();
            boolean berhasil = controller.hapusPenyewa(idPenyewaTerpilih, idKost);
            if (berhasil) {
                JOptionPane.showMessageDialog(this,
                    "Penyewa berhasil dihapus.",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
                muatDataPenyewa();
                muatDataKostKeComboBox();
                bersihkanForm();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal menghapus penyewa.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Isi form dari baris tabel yang diklik */
    private void isiFormDariBaris() {
        int baris = tabelPenyewa.getSelectedRow();
        if (baris < 0) return;

        idPenyewaTerpilih = (int) modelTabel.getValueAt(baris, 0);
        txtNama.setText((String) modelTabel.getValueAt(baris, 1));
        txtKtp.setText((String)  modelTabel.getValueAt(baris, 2));

        String namaKost = (String) modelTabel.getValueAt(baris, 3);
        cmbKost.setSelectedItem(namaKost);

        // Aktifkan tombol update & hapus
        btnUpdate.setEnabled(true);
        btnHapus.setEnabled(true);
        btnTambah.setEnabled(false);
    }

    /** Reset form ke kondisi awal */
    private void bersihkanForm() {
        txtNama.setText("");
        txtKtp.setText("");
        if (cmbKost.getItemCount() > 0) cmbKost.setSelectedIndex(0);
        idPenyewaTerpilih = -1;
        tabelPenyewa.clearSelection();
        btnTambah.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER / VALIDASI
    // ════════════════════════════════════════════════════════════

    /** Validasi input form sebelum dikirim ke controller */
    private boolean validasiForm() {
        String nama = txtNama.getText().trim();
        String ktp  = txtKtp.getText().trim();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama penyewa tidak boleh kosong.");
            txtNama.requestFocus();
            return false;
        }
        if (ktp.isEmpty() || ktp.length() != 16 || !ktp.matches("\\d+")) {
            JOptionPane.showMessageDialog(this,
                "Nomor KTP harus 16 digit angka.");
            txtKtp.requestFocus();
            return false;
        }
        if (cmbKost.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Pilih kost terlebih dahulu.");
            return false;
        }
        return true;
    }

    /** Ambil id_kost berdasarkan item yang dipilih di ComboBox */
    private int getIdKostDariComboBox() {
        int index = cmbKost.getSelectedIndex();
        if (index < 0 || index >= listKost.size()) return -1;
        return listKost.get(index).getIdKost();
    }

    /** Cari nama kost berdasarkan id (untuk kolom tabel) */
    private String getNamaKostById(int idKost) {
        for (Kost k : listKost) {
            if (k.getIdKost() == idKost) return k.getNamaKost();
        }
        return "Kost ID " + idKost; // fallback
    }
}