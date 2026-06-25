import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.BaseFont;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Desktop;
import java.io.*;
import java.sql.*;

public class LaporanPanel extends JPanel {

    private JTable            tabel;
    private DefaultTableModel model;
    private JLabel            lblTotal, lblJmlFaktur;
    private JTextField        txtTgl;

    public LaporanPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(20, 22, 20, 22));
        add(MainFrame.buatHeader(
                "Laporan Penjualan",
                "Riwayat lengkap semua transaksi & detail item"),
                BorderLayout.NORTH);

        JPanel card = MainFrame.glassCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── Toolbar ──
        JPanel tb = new JPanel(new BorderLayout(10, 0));
        tb.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lf = new JLabel("Filter Tanggal:");
        lf.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lf.setForeground(MainFrame.C_TEXT_DIM);

        txtTgl = MainFrame.glassField();
        txtTgl.setPreferredSize(new Dimension(140, 34));
        txtTgl.setMaximumSize(new Dimension(140, 34));
        txtTgl.setToolTipText("Format: YYYY-MM-DD");

        JButton bFilter = MainFrame.colorBtn("Filter",
                MainFrame.C_ACCENT,
                e -> loadLaporan(txtTgl.getText().trim()));
        bFilter.setPreferredSize(new Dimension(80, 34));

        JButton bAll = MainFrame.colorBtn("Semua",
                MainFrame.C_SUCCESS,
                e -> { txtTgl.setText(""); loadLaporan(); });
        bAll.setPreferredSize(new Dimension(80, 34));

        // ── Tombol Export PDF ──
        JButton bPdf = new JButton("Export PDF") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int alpha = getModel().isPressed() ? 180
                        : getModel().isRollover() ? 150 : 110;
                g2.setColor(new Color(200, 50, 50, alpha));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(200, 50, 50, 200));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(2, 2, getWidth()-4, (getHeight()-4)/2, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bPdf.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        bPdf.setForeground(Color.WHITE);
        bPdf.setOpaque(false);
        bPdf.setContentAreaFilled(false);
        bPdf.setBorderPainted(false);
        bPdf.setFocusPainted(false);
        bPdf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bPdf.setBorder(new EmptyBorder(8, 14, 8, 14));
        bPdf.setPreferredSize(new Dimension(130, 34));
        bPdf.addActionListener(e -> exportPdf());

        left.add(lf);
        left.add(txtTgl);
        left.add(bFilter);
        left.add(bAll);
        left.add(bPdf);

        // Info kanan
        JPanel rightInfo = new JPanel();
        rightInfo.setOpaque(false);
        rightInfo.setLayout(new BoxLayout(rightInfo, BoxLayout.Y_AXIS));

        lblJmlFaktur = new JLabel("Jumlah Faktur: 0");
        lblJmlFaktur.setFont(
                new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        lblJmlFaktur.setForeground(MainFrame.C_TEXT_DIM);
        lblJmlFaktur.setAlignmentX(RIGHT_ALIGNMENT);

        lblTotal = new JLabel("Total Pendapatan: Rp 0");
        lblTotal.setFont(
                new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblTotal.setForeground(MainFrame.C_WARNING);
        lblTotal.setAlignmentX(RIGHT_ALIGNMENT);

        rightInfo.add(lblJmlFaktur);
        rightInfo.add(Box.createVerticalStrut(3));
        rightInfo.add(lblTotal);

        tb.add(left,      BorderLayout.WEST);
        tb.add(rightInfo, BorderLayout.EAST);

        // ── Tabel ──
        String[] cols = {
            "No.Faktur","Tanggal","Customer",
            "Barang","Harga Satuan","Jumlah",
            "Subtotal","Total Faktur","Petugas","Hapus"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // tidak ada yang editable
            }
        };
        tabel = MainFrame.glassTable(model);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidths = {120, 90, 120, 140, 105, 55, 105, 115, 105, 65};
        for (int i = 0; i < colWidths.length; i++)
            tabel.getColumnModel().getColumn(i)
                    .setPreferredWidth(colWidths[i]);

        tabel.getColumnModel().getColumn(4)
                .setCellRenderer(MainFrame.rupiahRenderer());
        tabel.getColumnModel().getColumn(6)
                .setCellRenderer(MainFrame.rupiahRenderer());
        tabel.getColumnModel().getColumn(7)
                .setCellRenderer(MainFrame.rupiahRenderer());

        // ========== PERBAIKAN BUG HAPUS ==========
        // Renderer untuk kolom Hapus (cukup teks merah, tanpa button editor)
        tabel.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, "Hapus", isSelected, hasFocus, row, column);
                setForeground(new Color(255, 100, 100));
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setHorizontalAlignment(SwingConstants.CENTER);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return this;
            }
        });

        // Mouse listener untuk menangkap klik di kolom Hapus
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = tabel.columnAtPoint(e.getPoint());
                int row = tabel.rowAtPoint(e.getPoint());
                if (col == 9 && row >= 0) {
                    hapusFaktur(row);
                }
            }
        });
        // ========================================

        card.add(tb,                           BorderLayout.NORTH);
        card.add(MainFrame.glassScroll(tabel), BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
    }

    // ================================================================
    // LOAD DATA
    // ================================================================
    public void loadLaporan() { loadLaporan(""); }

    public void loadLaporan(String tgl) {
        model.setRowCount(0);
        double grandTotal = 0;
        int    jmlFaktur  = 0;

        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            String sql =
                "SELECT p.id_jual, p.no_faktur, p.tgl_transaksi, "
                + "c.nama_customer, b.nama_barang, d.harga_satuan, "
                + "d.jumlah_beli, d.subtotal, p.total_bayar, u.nama_lengkap "
                + "FROM tb_penjualan p "
                + "JOIN tb_customer c         ON p.id_customer=c.id_customer "
                + "JOIN tb_user u             ON p.id_user=u.id_user "
                + "JOIN tb_detail_penjualan d ON d.id_jual=p.id_jual "
                + "JOIN tb_barang b           ON d.id_barang=b.id_barang "
                + (tgl.isEmpty() ? "" : "WHERE p.tgl_transaksi=? ")
                + "ORDER BY p.id_jual DESC, d.id_detail ASC";

            ps = con.prepareStatement(sql);
            if (!tgl.isEmpty()) ps.setString(1, tgl);
            rs = ps.executeQuery();

            String lastFaktur = "";
            while (rs.next()) {
                String noFak  = rs.getString("no_faktur");
                int    idJual = rs.getInt("id_jual");
                boolean isNew = !noFak.equals(lastFaktur);
                if (isNew) {
                    grandTotal += rs.getDouble("total_bayar");
                    jmlFaktur++;
                    lastFaktur = noFak;
                }
                model.addRow(new Object[]{
                    isNew ? noFak : "",
                    rs.getString("tgl_transaksi"),
                    rs.getString("nama_customer"),
                    rs.getString("nama_barang"),
                    String.format("Rp %,.0f", rs.getDouble("harga_satuan")),
                    rs.getInt("jumlah_beli"),
                    String.format("Rp %,.0f", rs.getDouble("subtotal")),
                    isNew ? String.format("Rp %,.0f",
                            rs.getDouble("total_bayar")) : "",
                    rs.getString("nama_lengkap"),
                    idJual  // menyimpan id_jual di kolom tersembunyi untuk hapus
                });
            }
            lblTotal.setText("Total Pendapatan: "
                    + String.format("Rp %,.0f", grandTotal));
            lblJmlFaktur.setText("Jumlah Faktur: " + jmlFaktur);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally { DBConnection.close(con, ps, rs); }
    }

    // ================================================================
    // HAPUS FAKTUR (sudah diperbaiki)
    // ================================================================
    void hapusFaktur(int row) {
        // Ambil id_jual dari kolom 9
        Object idObj = model.getValueAt(row, 9);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Data tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int idJual;
        try {
            idJual = Integer.parseInt(idObj.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID Jual tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cari no_faktur dari kolom 0 (bisa kosong di baris detail)
        String noFak = "";
        for (int i = row; i >= 0; i--) {
            Object v = model.getValueAt(i, 0);
            if (v != null && !v.toString().trim().isEmpty()) {
                noFak = v.toString();
                break;
            }
        }
        if (noFak.isEmpty()) noFak = "?";

        int konfirm = JOptionPane.showConfirmDialog(this,
                "Hapus faktur " + noFak + " beserta semua detailnya?\n" +
                "Stok barang TIDAK akan dikembalikan otomatis.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (konfirm != JOptionPane.YES_OPTION) return;

        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            ps = con.prepareStatement("DELETE FROM tb_detail_penjualan WHERE id_jual=?");
            ps.setInt(1, idJual);
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("DELETE FROM tb_penjualan WHERE id_jual=?");
            ps.setInt(1, idJual);
            ps.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "Faktur " + noFak + " berhasil dihapus!", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            loadLaporan(txtTgl.getText().trim()); // refresh tabel

        } catch (SQLException ex) {
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(this, "Gagal hapus: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBConnection.close(con, ps, null);
        }
    }

    // ================================================================
    // EXPORT PDF 
    // ================================================================
    private void exportPdf() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Tidak ada data untuk diekspor!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan PDF");
        fc.setSelectedFile(new File("Laporan_Penjualan.pdf"));
        fc.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "PDF Files (*.pdf)", "pdf"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf"))
            file = new File(file.getAbsolutePath() + ".pdf");

        try {
            Document doc = new Document(
                    PageSize.A4.rotate(), 30, 30, 40, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // Font iText
            com.itextpdf.text.Font fJudul = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                    com.itextpdf.text.Font.BOLD,
                    new BaseColor(20, 50, 130));

            com.itextpdf.text.Font fSub = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                    com.itextpdf.text.Font.NORMAL,
                    new BaseColor(100, 110, 140));

            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9,
                    com.itextpdf.text.Font.BOLD,
                    BaseColor.WHITE);

            com.itextpdf.text.Font fCell = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                    com.itextpdf.text.Font.NORMAL,
                    new BaseColor(40, 40, 60));

            com.itextpdf.text.Font fCellBold = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                    com.itextpdf.text.Font.BOLD,
                    new BaseColor(160, 110, 0));

            com.itextpdf.text.Font fTotal = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11,
                    com.itextpdf.text.Font.BOLD,
                    new BaseColor(20, 50, 130));

            com.itextpdf.text.Font fTs = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                    com.itextpdf.text.Font.ITALIC,
                    new BaseColor(150, 150, 150));

            // Header dokumen
            Paragraph pJudul = new Paragraph(
                    "LAPORAN PENJUALAN — TOKO BERKAH JAYA", fJudul);
            pJudul.setAlignment(Element.ALIGN_CENTER);
            doc.add(pJudul);

            String filterInfo = txtTgl.getText().trim().isEmpty()
                    ? "Semua Transaksi"
                    : "Tanggal: " + txtTgl.getText().trim();
            Paragraph pSub = new Paragraph(
                    filterInfo + "  |  "
                    + lblJmlFaktur.getText()
                    + "  |  " + lblTotal.getText(), fSub);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(12f);
            doc.add(pSub);

            // Tabel PDF (9 kolom data, skip kolom id_jual)
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{
                    14f, 9f, 13f, 15f, 11f, 6f, 11f, 12f, 11f});

            BaseColor headerBg = new BaseColor(20, 50, 130);
            String[] headers = {
                "No.Faktur","Tanggal","Customer","Barang",
                "Harga Satuan","Jumlah","Subtotal",
                "Total Faktur","Petugas"
            };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(
                        new Phrase(h, fHeader));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderColor(new BaseColor(40, 70, 160));
                table.addCell(cell);
            }

            BaseColor rowEven = new BaseColor(235, 240, 255);
            BaseColor rowOdd  = new BaseColor(255, 255, 255);

            for (int r = 0; r < model.getRowCount(); r++) {
                BaseColor rowBg = r % 2 == 0 ? rowEven : rowOdd;
                for (int c = 0; c < 9; c++) {
                    Object val = model.getValueAt(r, c);
                    String txt = val == null ? "" : val.toString();
                    boolean isRupiah = (c == 4 || c == 6 || c == 7);
                    com.itextpdf.text.Font cellFont =
                            isRupiah ? fCellBold : fCell;

                    PdfPCell cell = new PdfPCell(
                            new Phrase(txt, cellFont));
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(5f);
                    cell.setBorderColor(new BaseColor(200, 210, 230));
                    if (isRupiah)
                        cell.setHorizontalAlignment(
                                Element.ALIGN_RIGHT);
                    else if (c == 5)
                        cell.setHorizontalAlignment(
                                Element.ALIGN_CENTER);
                    else
                        cell.setHorizontalAlignment(
                                Element.ALIGN_LEFT);
                    table.addCell(cell);
                }
            }
            doc.add(table);

            // Footer total
            Paragraph pTotal = new Paragraph(
                    "\n" + lblTotal.getText(), fTotal);
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pTotal);

            // Timestamp
            String waktu = new java.text.SimpleDateFormat(
                    "dd MMMM yyyy HH:mm:ss",
                    new java.util.Locale("id", "ID"))
                    .format(new java.util.Date());
            Paragraph pTs = new Paragraph(
                    "Dicetak pada: " + waktu, fTs);
            pTs.setAlignment(Element.ALIGN_LEFT);
            pTs.setSpacingBefore(6f);
            doc.add(pTs);

            doc.close();

            int buka = JOptionPane.showConfirmDialog(this,
                    "PDF berhasil disimpan!\n"
                    + file.getAbsolutePath()
                    + "\n\nBuka file sekarang?",
                    "Export Berhasil",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (buka == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal export PDF:\n" + ex.getMessage()
                    + "\n\nPastikan itextpdf-5.5.13.jar "
                    + "sudah ditambahkan ke Libraries!",
                    "Error PDF", JOptionPane.ERROR_MESSAGE);
        }
    }
}