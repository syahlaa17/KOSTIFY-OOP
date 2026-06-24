package com.kostify.view;

import com.kostify.controller.KostifyController;
import com.kostify.model.KamarPenuhException;
import com.kostify.model.Kost;
import com.kostify.model.Penyewa;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class PanelPenyewa extends JPanel {

    // ── Warna tema (seragam dengan tab Kost & Laporan) ──────────
    private static final Color DARK_BLUE    = new Color(18, 45, 86);    // navy utama
    private static final Color DARK_BLUE2   = new Color(32, 67, 124);   // navy gradient kanan
    private static final Color ACCENT_RED   = new Color(193, 64, 84);   // merah elegan
    private static final Color WHITE        = Color.WHITE;
    private static final Color LIGHT_GRAY   = new Color(241, 244, 249); // abu kebiruan
    private static final Color TEXT_GRAY    = new Color(138, 141, 155);
    private static final Color TABLE_HEADER = new Color(18, 45, 86);

    // ── Controller ──────────────────────────────────────────────
    private final KostifyController controller = new KostifyController();

    // ── Komponen Form Input ──────────────────────────────────────
    private JTextField txtNama;
    private JTextField txtKtp;
    private JComboBox<String> cmbKost;
    private List<Kost> listKost;

    // ── Tabel ────────────────────────────────────────────────────
    private JTable tabelPenyewa;
    private DefaultTableModel modelTabel;

    // ── Kartu statistik ─────────────────────────────────────────
    private JLabel lblTotalPenyewa;
    private JLabel lblTotalKost;

    // ── Tombol ───────────────────────────────────────────────────
    private JButton btnTambah;
    private JButton btnUpdate;
    private JButton btnHapus;
    private JButton btnBersihkan;

    // ── State ────────────────────────────────────────────────────
    private int idPenyewaTerpilih = -1;

    // ════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════
    public PanelPenyewa() {
        setLayout(new BorderLayout());
        setBackground(LIGHT_GRAY);

        // Panel utama dengan padding
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(LIGHT_GRAY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(buatPanelHeader(), BorderLayout.NORTH);

        JPanel tengah = new JPanel(new BorderLayout(0, 15));
        tengah.setBackground(LIGHT_GRAY);
        tengah.add(buatPanelStats(), BorderLayout.NORTH);
        tengah.add(buatPanelTabel(), BorderLayout.CENTER);
        tengah.add(buatPanelForm(),  BorderLayout.SOUTH);
        mainPanel.add(tengah, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        muatDataKostKeComboBox();
        muatDataPenyewa();
    }

    // ════════════════════════════════════════════════════════════
    //  BUILDER PANEL
    // ════════════════════════════════════════════════════════════

    /** Header gradient navy dengan ikon P dan judul */
    private JPanel buatPanelHeader() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, DARK_BLUE, getWidth(), 0, DARK_BLUE2));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        kiri.setOpaque(false);

        // Ikon kotak dengan huruf P (sama gaya dengan badge K di tab Kost)
        JLabel ikon = new JLabel("P", SwingConstants.CENTER) {
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
        ikon.setOpaque(false);
        ikon.setPreferredSize(new Dimension(42, 42));
        ikon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        ikon.setForeground(DARK_BLUE);

        JPanel teksPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        teksPanel.setOpaque(false);

        JLabel judul = new JLabel("Manajemen Data Penyewa");
        judul.setFont(new Font("Segoe UI", Font.BOLD, 18));
        judul.setForeground(WHITE);

        JLabel sub = new JLabel("Kelola data penyewa kost: tambah, lihat, edit, dan hapus.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(186, 203, 226));

        teksPanel.add(judul);
        teksPanel.add(sub);

        kiri.add(ikon);
        kiri.add(teksPanel);
        panel.add(kiri, BorderLayout.CENTER);
        return panel;
    }

    /** Kartu statistik: Total Penyewa & Total Kost Tersedia */
    private JPanel buatPanelStats() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        lblTotalPenyewa = new JLabel("0");
        lblTotalKost    = new JLabel("0");

        panel.add(buatKartuStat("Total Penyewa",  lblTotalPenyewa, new Color(22, 138, 102)));
        panel.add(buatKartuStat("Kost Tersedia",  lblTotalKost,    new Color(18, 45, 86)));

        return panel;
    }

    /** Helper: buat satu kartu statistik */
    private JPanel buatKartuStat(String label, JLabel nilaiLabel, Color warnaAngka) {
        JPanel kartu = new JPanel(new GridLayout(2, 1, 0, 4));
        kartu.setBackground(WHITE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_GRAY);

        nilaiLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nilaiLabel.setForeground(warnaAngka);

        kartu.add(lbl);
        kartu.add(nilaiLabel);
        return kartu;
    }

    /** Tabel daftar penyewa dengan header gelap */
    private JPanel buatPanelTabel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        // ── Definisi kolom ──
        String[] kolom = {"ID", "Nama Penyewa", "Nomor KTP", "Nama Kost"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tabelPenyewa = new JTable(modelTabel);
        tabelPenyewa.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelPenyewa.setRowHeight(30);
        tabelPenyewa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelPenyewa.setGridColor(new Color(235, 235, 235));
        tabelPenyewa.setSelectionBackground(new Color(214, 226, 242));
        tabelPenyewa.setSelectionForeground(new Color(24, 31, 46));
        tabelPenyewa.setBackground(WHITE);
        tabelPenyewa.setShowHorizontalLines(true);
        tabelPenyewa.setShowVerticalLines(false);

        // Header gelap
        JTableHeader header = tabelPenyewa.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(TABLE_HEADER);
                setForeground(WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // Sembunyikan kolom ID
        tabelPenyewa.getColumnModel().getColumn(0).setMinWidth(0);
        tabelPenyewa.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelPenyewa.getColumnModel().getColumn(0).setWidth(0);

        // Listener klik baris
        tabelPenyewa.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFormDariBaris();
        });

        JScrollPane scroll = new JScrollPane(tabelPenyewa);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /** Form tambah/edit di bawah tabel */
    private JPanel buatPanelForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Judul form
        JLabel judulForm = new JLabel("Tambah / Edit Penyewa");
        judulForm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        judulForm.setForeground(DARK_BLUE);
        judulForm.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        wrapper.add(judulForm, BorderLayout.NORTH);

        // Grid field
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 12);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Nama
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lNama = new JLabel("Nama:");
        lNama.setFont(labelFont);
        grid.add(lNama, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNama = new JTextField(20);
        txtNama.setFont(fieldFont);
        txtNama.setPreferredSize(new Dimension(200, 30));
        grid.add(txtNama, gbc);

        // KTP
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lKtp = new JLabel("Nomor KTP:");
        lKtp.setFont(labelFont);
        grid.add(lKtp, gbc);

        gbc.gridx = 3; gbc.weightx = 1.0;
        txtKtp = new JTextField(20);
        txtKtp.setFont(fieldFont);
        txtKtp.setPreferredSize(new Dimension(200, 30));
        grid.add(txtKtp, gbc);

        // Kost
        gbc.gridx = 4; gbc.weightx = 0;
        JLabel lKost = new JLabel("Kost:");
        lKost.setFont(labelFont);
        grid.add(lKost, gbc);

        gbc.gridx = 5; gbc.weightx = 1.0;
        cmbKost = new JComboBox<>();
        cmbKost.setFont(fieldFont);
        cmbKost.setPreferredSize(new Dimension(180, 30));
        grid.add(cmbKost, gbc);

        wrapper.add(grid, BorderLayout.CENTER);

        // Tombol
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelTombol.setBackground(WHITE);
        panelTombol.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        btnBersihkan = buatTombol("Bersihkan", new Color(150, 150, 150));
        btnUpdate    = buatTombol("Update Terpilih", new Color(30, 100, 200));
        btnHapus     = buatTombol("Hapus Terpilih", ACCENT_RED);
        btnTambah    = buatTombol("Tambah Penyewa", new Color(18, 45, 85));

        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setOpaque(true);
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setOpaque(true);

        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);

        panelTombol.add(btnBersihkan);
        panelTombol.add(btnUpdate);
        panelTombol.add(btnHapus);
        panelTombol.add(btnTambah);

        wrapper.add(panelTombol, BorderLayout.SOUTH);

        // Listener
        btnTambah.addActionListener(e -> tambahPenyewa());
        btnUpdate.addActionListener(e -> updatePenyewa());
        btnHapus.addActionListener(e -> hapusPenyewa());
        btnBersihkan.addActionListener(e -> bersihkanForm());

        return wrapper;
    }

    /** Helper buat tombol bergaya */
    private JButton buatTombol(String teks, Color bg) {
        JButton btn = new JButton(teks);
        btn.setBackground(bg);
        btn.setForeground(WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(150, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ════════════════════════════════════════════════════════════
    //  LOAD DATA
    // ════════════════════════════════════════════════════════════

    private void muatDataKostKeComboBox() {
        listKost = controller.getAllKost();
        cmbKost.removeAllItems();
        for (Kost k : listKost) {
            cmbKost.addItem(k.getNamaKost());
        }
        lblTotalKost.setText(String.valueOf(listKost.size()));
    }

    public void muatDataPenyewa() {
        modelTabel.setRowCount(0);
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
        lblTotalPenyewa.setText(String.valueOf(listPenyewa.size()));
    }

    // ════════════════════════════════════════════════════════════
    //  EVENT HANDLER
    // ════════════════════════════════════════════════════════════

    private void tambahPenyewa() {
        if (!validasiForm()) return;
        String nama = txtNama.getText().trim();
        String ktp  = txtKtp.getText().trim();
        int idKost  = getIdKostDariComboBox();

        try {
            boolean berhasil = controller.tambahPenyewa(nama, ktp, idKost);
            if (berhasil) {
                JOptionPane.showMessageDialog(this,
                    "Penyewa berhasil didaftarkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                muatDataPenyewa();
                muatDataKostKeComboBox();
                bersihkanForm();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal mendaftarkan penyewa. Coba lagi.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (KamarPenuhException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Kamar Penuh!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updatePenyewa() {
        if (idPenyewaTerpilih == -1) {
            JOptionPane.showMessageDialog(this, "Pilih penyewa dari tabel terlebih dahulu.");
            return;
        }
        if (!validasiForm()) return;

        boolean berhasil = controller.updatePenyewa(
            idPenyewaTerpilih,
            txtNama.getText().trim(),
            txtKtp.getText().trim(),
            getIdKostDariComboBox()
        );
        if (berhasil) {
            JOptionPane.showMessageDialog(this,
                "Data penyewa berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            muatDataPenyewa();
            bersihkanForm();
        } else {
            JOptionPane.showMessageDialog(this,
                "Gagal memperbarui data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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
            boolean berhasil = controller.hapusPenyewa(idPenyewaTerpilih, getIdKostDariComboBox());
            if (berhasil) {
                JOptionPane.showMessageDialog(this,
                    "Penyewa berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                muatDataPenyewa();
                muatDataKostKeComboBox();
                bersihkanForm();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal menghapus penyewa.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void isiFormDariBaris() {
        int baris = tabelPenyewa.getSelectedRow();
        if (baris < 0) return;

        idPenyewaTerpilih = (int) modelTabel.getValueAt(baris, 0);
        txtNama.setText((String) modelTabel.getValueAt(baris, 1));
        txtKtp.setText((String)  modelTabel.getValueAt(baris, 2));
        cmbKost.setSelectedItem(modelTabel.getValueAt(baris, 3));

        btnUpdate.setEnabled(true);
        btnHapus.setEnabled(true);
        btnTambah.setEnabled(false);
    }

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

    private boolean validasiForm() {
        String nama = txtNama.getText().trim();
        String ktp  = txtKtp.getText().trim();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama penyewa tidak boleh kosong.");
            txtNama.requestFocus();
            return false;
        }
        if (ktp.isEmpty() || ktp.length() != 16 || !ktp.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Nomor KTP harus 16 digit angka.");
            txtKtp.requestFocus();
            return false;
        }
        if (cmbKost.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Pilih kost terlebih dahulu.");
            return false;
        }
        return true;
    }

    private int getIdKostDariComboBox() {
        int index = cmbKost.getSelectedIndex();
        if (index < 0 || index >= listKost.size()) return -1;
        return listKost.get(index).getIdKost();
    }

    private String getNamaKostById(int idKost) {
        for (Kost k : listKost) {
            if (k.getIdKost() == idKost) return k.getNamaKost();
        }
        return "Kost ID " + idKost;
    }
}