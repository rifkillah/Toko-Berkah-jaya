import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransaksiPanel extends JPanel {

    private int userId;

    private JTextField        txtNoFaktur, txtTgl, txtTotalBayar;
    private JComboBox<String> cbCustomer;
    private String[]          custIds;

    private JComboBox<String> cbBarang;
    private String[]          barangIds;
    private JTextField        txtHargaSat, txtJumlah, txtSubtotal;
    private double            hargaSat = 0;
    private int               stokAda  = 0;

    private JTable            tblKeranjang;
    private DefaultTableModel mdlKeranjang;
    private List<Object[]>    keranjang = new ArrayList<>();

    private JTable            tblRiwayat;
    private DefaultTableModel mdlRiwayat;

    public TransaksiPanel(int userId) {
        this.userId = userId;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(20, 22, 20, 22));

        add(MainFrame.buatHeader(
                "Transaksi Penjualan",
                "Buat faktur multi-item | stok otomatis berkurang"),
                BorderLayout.NORTH);

        // Gunakan GridBagLayout untuk form area agar lebih fleksibel
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        body.add(buildFormArea(), gbc);
        
        gbc.weightx = 0.6;
        gbc.gridx = 1;
        body.add(buildRiwayat(), gbc);
        
        add(body, BorderLayout.CENTER);
        
        refreshData();
    }

    private JPanel buildFormArea() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);
        container.add(buildHeaderFaktur(), BorderLayout.NORTH);
        container.add(buildKeranjang(), BorderLayout.CENTER);
        return container;
    }

    private JPanel buildHeaderFaktur() {
        JPanel card = MainFrame.glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel ttl = new JLabel("Header Faktur");
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ttl.setForeground(MainFrame.C_ACCENT);
        ttl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(ttl);
        card.add(Box.createVerticalStrut(12));

        card.add(MainFrame.fieldLabel("No. Faktur (Otomatis)"));
        card.add(Box.createVerticalStrut(4));
        txtNoFaktur = MainFrame.glassField();
        txtNoFaktur.setEditable(false);
        txtNoFaktur.setForeground(MainFrame.C_ACCENT2);
        txtNoFaktur.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(txtNoFaktur);
        card.add(Box.createVerticalStrut(8));

        card.add(MainFrame.fieldLabel("Tanggal Transaksi"));
        card.add(Box.createVerticalStrut(4));
        txtTgl = MainFrame.glassField();
        txtTgl.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        txtTgl.setEditable(false);
        txtTgl.setForeground(MainFrame.C_ACCENT2);
        txtTgl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(txtTgl);
        card.add(Box.createVerticalStrut(8));

        card.add(MainFrame.fieldLabel("Pilih Customer"));
        card.add(Box.createVerticalStrut(4));
        cbCustomer = new JComboBox<>();
        MainFrame.styleCombo(cbCustomer);
        cbCustomer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(cbCustomer);
        return card;
    }

    private JPanel buildKeranjang() {
        JPanel card = MainFrame.glassCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // ==================== FORM TAMBAH ITEM ====================
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Judul
        JLabel ttl2 = new JLabel("Tambah Item ke Keranjang");
        ttl2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ttl2.setForeground(MainFrame.C_ACCENT);
        gbc.gridy = 0;
        formPanel.add(ttl2, gbc);

        // Pilih Barang
        JLabel lblBarang = new JLabel("Pilih Barang");
        lblBarang.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBarang.setForeground(new Color(160, 185, 230));
        gbc.gridy = 1;
        formPanel.add(lblBarang, gbc);

        cbBarang = new JComboBox<>();
        cbBarang.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        cbBarang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        
        cbBarang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    setToolTipText(value.toString());
                }
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
        
        cbBarang.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                @SuppressWarnings("unchecked")
                JComboBox<String> combo = (JComboBox<String>) e.getSource();
                Object child = combo.getAccessibleContext().getAccessibleChild(0);
                if (child instanceof javax.swing.plaf.basic.BasicComboPopup) {
                    javax.swing.plaf.basic.BasicComboPopup popup = (javax.swing.plaf.basic.BasicComboPopup) child;
                    JScrollPane sp = (JScrollPane) popup.getComponent(0);
                    
                    int maxWidth = combo.getWidth();
                    FontMetrics fm = combo.getFontMetrics(combo.getFont());
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        String item = combo.getItemAt(i);
                        if (item != null) {
                            int w = fm.stringWidth(item) + 40;
                            if (w > maxWidth) maxWidth = w;
                        }
                    }
                    maxWidth = Math.min(maxWidth, 500);
                    sp.setPreferredSize(new Dimension(maxWidth, sp.getPreferredSize().height));
                    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    popup.pack();
                }
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
        
        cbBarang.addActionListener(e -> updateHarga());
        gbc.gridy = 2;
        formPanel.add(cbBarang, gbc);

        // Harga Satuan dan Jumlah (2 kolom)
        JPanel rowHJ = new JPanel(new GridLayout(1, 2, 12, 0));
        rowHJ.setOpaque(false);

        JPanel colH = new JPanel();
        colH.setOpaque(false);
        colH.setLayout(new BoxLayout(colH, BoxLayout.Y_AXIS));
        JLabel lblHarga = new JLabel("Harga Satuan");
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHarga.setForeground(new Color(160, 185, 230));
        lblHarga.setAlignmentX(LEFT_ALIGNMENT);
        colH.add(lblHarga);
        colH.add(Box.createVerticalStrut(4));
        txtHargaSat = MainFrame.glassField();
        txtHargaSat.setEditable(false);
        txtHargaSat.setForeground(MainFrame.C_ACCENT2);
        txtHargaSat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtHargaSat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        colH.add(txtHargaSat);

        JPanel colJ = new JPanel();
        colJ.setOpaque(false);
        colJ.setLayout(new BoxLayout(colJ, BoxLayout.Y_AXIS));
        JLabel lblJumlah = new JLabel("Jumlah");
        lblJumlah.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblJumlah.setForeground(new Color(160, 185, 230));
        lblJumlah.setAlignmentX(LEFT_ALIGNMENT);
        colJ.add(lblJumlah);
        colJ.add(Box.createVerticalStrut(4));
        txtJumlah = MainFrame.glassField();
        txtJumlah.setText("1");
        txtJumlah.setHorizontalAlignment(JTextField.CENTER);
        txtJumlah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtJumlah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { hitungSubtotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { hitungSubtotal(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { hitungSubtotal(); }
        });
        colJ.add(txtJumlah);

        rowHJ.add(colH);
        rowHJ.add(colJ);
        gbc.gridy = 3;
        formPanel.add(rowHJ, gbc);

        // Subtotal Item
        JLabel lblSubtotal = new JLabel("Subtotal Item");
        lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSubtotal.setForeground(new Color(160, 185, 230));
        gbc.gridy = 4;
        formPanel.add(lblSubtotal, gbc);

        txtSubtotal = MainFrame.glassField();
        txtSubtotal.setEditable(false);
        txtSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSubtotal.setForeground(MainFrame.C_WARNING);
        txtSubtotal.setHorizontalAlignment(JTextField.RIGHT);
        txtSubtotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        gbc.gridy = 5;
        formPanel.add(txtSubtotal, gbc);

        // Tombol Tambah
        JButton btnTambah = new JButton("+ Tambah ke Keranjang") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = MainFrame.C_ACCENT;
                int alpha = getModel().isPressed() ? 160 : getModel().isRollover() ? 130 : 90;
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(2, 2, getWidth()-4, (getHeight()-4)/2, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnTambah.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setOpaque(false);
        btnTambah.setContentAreaFilled(false);
        btnTambah.setBorderPainted(false);
        btnTambah.setFocusPainted(false);
        btnTambah.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTambah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        gbc.gridy = 6;
        gbc.insets = new Insets(8, 0, 0, 0);
        formPanel.add(btnTambah, gbc);

        // ==================== TABEL KERANJANG ====================
        String[] kols = {"Barang", "Harga", "Jml", "Subtotal", ""};
        mdlKeranjang = new DefaultTableModel(kols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        tblKeranjang = MainFrame.glassTable(mdlKeranjang);
        tblKeranjang.setRowHeight(34);
        tblKeranjang.getColumnModel().getColumn(4).setCellRenderer(new BtnRenderer());
        tblKeranjang.getColumnModel().getColumn(4).setCellEditor(new BtnEditor(new JCheckBox(), this));
        tblKeranjang.getColumnModel().getColumn(4).setMaxWidth(60);
        tblKeranjang.getColumnModel().getColumn(4).setPreferredWidth(60);
        tblKeranjang.getColumnModel().getColumn(0).setPreferredWidth(140);
        tblKeranjang.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblKeranjang.getColumnModel().getColumn(2).setPreferredWidth(45);
        tblKeranjang.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblKeranjang.getColumnModel().getColumn(1).setCellRenderer(MainFrame.rupiahRenderer());
        tblKeranjang.getColumnModel().getColumn(3).setCellRenderer(MainFrame.rupiahRenderer());

        JScrollPane scrK = MainFrame.glassScroll(tblKeranjang);
        scrK.setPreferredSize(new Dimension(0, 150));

        // ==================== TOTAL & TOMBOL SIMPAN ====================
        JPanel botPanel = new JPanel(new BorderLayout(10, 0));
        botPanel.setOpaque(false);
        botPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        totalPanel.setOpaque(false);
        JLabel lblTotal = new JLabel("Total:");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(MainFrame.C_ACCENT);
        txtTotalBayar = MainFrame.glassField();
        txtTotalBayar.setEditable(false);
        txtTotalBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtTotalBayar.setForeground(MainFrame.C_WARNING);
        txtTotalBayar.setHorizontalAlignment(JTextField.RIGHT);
        txtTotalBayar.setText("Rp 0");
        txtTotalBayar.setPreferredSize(new Dimension(160, 38));
        totalPanel.add(lblTotal);
        totalPanel.add(txtTotalBayar);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnBatal = MainFrame.colorBtn("Batal", MainFrame.C_DANGER, e -> resetSemua());
        JButton btnSimpan = MainFrame.colorBtn("SIMPAN", MainFrame.C_SUCCESS, e -> simpanFaktur());
        btnBatal.setPreferredSize(new Dimension(80, 38));
        btnSimpan.setPreferredSize(new Dimension(100, 38));
        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);

        botPanel.add(totalPanel, BorderLayout.WEST);
        botPanel.add(btnPanel, BorderLayout.EAST);

        // Gabungkan semua ke card
        JPanel northContent = new JPanel(new BorderLayout());
        northContent.setOpaque(false);
        northContent.add(formPanel, BorderLayout.NORTH);
        
        card.add(northContent, BorderLayout.NORTH);
        card.add(scrK, BorderLayout.CENTER);
        card.add(botPanel, BorderLayout.SOUTH);
        
        return card;
    }

    private JPanel buildRiwayat() {
        JPanel card = MainFrame.glassCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lbl = new JLabel("Riwayat Faktur (klik untuk lihat detail)");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(MainFrame.C_ACCENT);

        String[] cols = {"No.Faktur", "Tgl", "Customer", "Total", "Petugas"};
        mdlRiwayat = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRiwayat = MainFrame.glassTable(mdlRiwayat);
        tblRiwayat.getColumnModel().getColumn(3).setCellRenderer(MainFrame.rupiahRenderer());
        tblRiwayat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) lihatDetail();
        });
        
        // Set column widths
        tblRiwayat.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblRiwayat.getColumnModel().getColumn(1).setPreferredWidth(70);
        tblRiwayat.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblRiwayat.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblRiwayat.getColumnModel().getColumn(4).setPreferredWidth(90);

        JScrollPane scrR = MainFrame.glassScroll(tblRiwayat);
        scrR.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrR.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        card.add(lbl, BorderLayout.NORTH);
        card.add(scrR, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        loadCustomers();
        loadBarangs();
        loadRiwayat();
        generateNoFaktur();
    }

    private void loadCustomers() {
        cbCustomer.removeAllItems();
        Connection con = null; Statement st = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM tb_customer ORDER BY nama_customer");
            List<String> ids = new ArrayList<>();
            while (rs.next()) {
                cbCustomer.addItem(rs.getString("id_customer") + " - " + rs.getString("nama_customer"));
                ids.add(rs.getString("id_customer"));
            }
            custIds = ids.toArray(new String[0]);
        } catch (SQLException ex) { err(ex.getMessage()); }
        finally { DBConnection.close(con, st, rs); }
    }

    private void loadBarangs() {
        cbBarang.removeAllItems();
        Connection con = null; Statement st = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM tb_barang ORDER BY nama_barang");
            List<String> ids = new ArrayList<>();
            while (rs.next()) {
                String display = rs.getString("id_barang") + " - " + rs.getString("nama_barang") + "  [Stok: " + rs.getInt("stok") + "]";
                cbBarang.addItem(display);
                ids.add(rs.getString("id_barang"));
            }
            barangIds = ids.toArray(new String[0]);
        } catch (SQLException ex) { err(ex.getMessage()); }
        finally { DBConnection.close(con, st, rs); }
        if (cbBarang.getItemCount() > 0) updateHarga();
    }

    private void generateNoFaktur() {
        String tglStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String tglDB = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT COUNT(*) FROM tb_penjualan WHERE tgl_transaksi=?");
            ps.setString(1, tglDB);
            rs = ps.executeQuery();
            int urut = rs.next() ? rs.getInt(1) + 1 : 1;
            txtNoFaktur.setText(String.format("INV-%s-%03d", tglStr, urut));
        } catch (SQLException ex) {
            txtNoFaktur.setText("INV-" + tglStr + "-001");
        } finally { DBConnection.close(con, ps, rs); }
    }

    private void updateHarga() {
        if (barangIds == null || cbBarang.getSelectedIndex() < 0) return;
        String id = barangIds[cbBarang.getSelectedIndex()];
        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("SELECT harga_jual,stok FROM tb_barang WHERE id_barang=?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                hargaSat = rs.getDouble("harga_jual");
                stokAda = rs.getInt("stok");
                txtHargaSat.setText(String.format("Rp %,.0f", hargaSat));
                hitungSubtotal();
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        finally { DBConnection.close(con, ps, rs); }
    }

    private void hitungSubtotal() {
        try {
            int j = Integer.parseInt(txtJumlah.getText().trim());
            txtSubtotal.setText(String.format("Rp %,.0f", hargaSat * j));
        } catch (NumberFormatException e) {
            txtSubtotal.setText("Rp 0");
        }
    }

    private void tambahKeKeranjang() {
        if (barangIds == null || cbBarang.getSelectedIndex() < 0) {
            warn("Pilih barang terlebih dahulu!");
            return;
        }
        int jumlah;
        try {
            jumlah = Integer.parseInt(txtJumlah.getText().trim());
            if (jumlah <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            warn("Jumlah harus angka positif!");
            return;
        }
        if (stokAda == 0) {
            warn("Stok barang KOSONG!");
            return;
        }
        if (jumlah > stokAda) {
            warn("Stok tidak cukup!\nTersedia: " + stokAda);
            return;
        }

        String idBrg = barangIds[cbBarang.getSelectedIndex()];
        String fullText = cbBarang.getSelectedItem().toString();
        String nmBrg = fullText.split(" - ", 2)[1].split("  \\[")[0];
        double subtot = hargaSat * jumlah;

        for (int i = 0; i < keranjang.size(); i++) {
            if (keranjang.get(i)[0].equals(idBrg)) {
                int jLama = (int) keranjang.get(i)[3];
                int jBaru = jLama + jumlah;
                if (jBaru > stokAda) {
                    warn("Total melebihi stok!\nStok: " + stokAda + "\nDi keranjang: " + jLama);
                    return;
                }
                double stBaru = hargaSat * jBaru;
                keranjang.get(i)[3] = jBaru;
                keranjang.get(i)[4] = stBaru;
                mdlKeranjang.setValueAt(jBaru, i, 2);
                mdlKeranjang.setValueAt(String.format("Rp %,.0f", stBaru), i, 3);
                updateGrandTotal();
                txtJumlah.setText("1");
                return;
            }
        }

        keranjang.add(new Object[]{idBrg, nmBrg, hargaSat, jumlah, subtot});
        mdlKeranjang.addRow(new Object[]{nmBrg, String.format("Rp %,.0f", hargaSat), jumlah, String.format("Rp %,.0f", subtot), "Hapus"});
        updateGrandTotal();
        txtJumlah.setText("1");
    }

    void hapusDariKeranjang(int row) {
        if (row < 0 || row >= keranjang.size()) return;
        keranjang.remove(row);
        mdlKeranjang.removeRow(row);
        updateGrandTotal();
    }

    private void updateGrandTotal() {
        double total = keranjang.stream().mapToDouble(item -> (double) item[4]).sum();
        txtTotalBayar.setText(String.format("Rp %,.0f", total));
    }

    private void simpanFaktur() {
        if (keranjang.isEmpty()) {
            warn("Keranjang masih kosong!");
            return;
        }
        if (custIds == null || cbCustomer.getSelectedIndex() < 0) {
            warn("Pilih customer terlebih dahulu!");
            return;
        }

        String noFaktur = txtNoFaktur.getText();
        String tgl = txtTgl.getText();
        String idCust = custIds[cbCustomer.getSelectedIndex()];
        double grandTotal = keranjang.stream().mapToDouble(item -> (double) item[4]).sum();

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            PreparedStatement psH = con.prepareStatement(
                "INSERT INTO tb_penjualan (no_faktur,tgl_transaksi,id_customer,total_bayar,id_user) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            psH.setString(1, noFaktur);
            psH.setString(2, tgl);
            psH.setString(3, idCust);
            psH.setDouble(4, grandTotal);
            psH.setInt(5, userId);
            psH.executeUpdate();

            ResultSet gk = psH.getGeneratedKeys();
            int idJual = gk.next() ? gk.getInt(1) : -1;
            psH.close();

            PreparedStatement psD = con.prepareStatement(
                "INSERT INTO tb_detail_penjualan (id_jual,id_barang,harga_satuan,jumlah_beli,subtotal) VALUES (?,?,?,?,?)");
            PreparedStatement psS = con.prepareStatement("UPDATE tb_barang SET stok=stok-? WHERE id_barang=?");

            for (Object[] item : keranjang) {
                psD.setInt(1, idJual);
                psD.setString(2, (String) item[0]);
                psD.setDouble(3, (double) item[2]);
                psD.setInt(4, (int) item[3]);
                psD.setDouble(5, (double) item[4]);
                psD.addBatch();
                psS.setInt(1, (int) item[3]);
                psS.setString(2, (String) item[0]);
                psS.addBatch();
            }
            psD.executeBatch();
            psS.executeBatch();
            psD.close();
            psS.close();
            con.commit();

            JOptionPane.showMessageDialog(this,
                "Faktur " + noFaktur + " berhasil disimpan!\n" + keranjang.size() + " item  |  Total: " + String.format("Rp %,.0f", grandTotal),
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            resetSemua();
            loadBarangs();
            loadRiwayat();

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ignored) {}
            err("Gagal simpan: " + ex.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    private void loadRiwayat() {
        mdlRiwayat.setRowCount(0);
        Connection con = null; Statement st = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery(
                "SELECT p.no_faktur,p.tgl_transaksi,c.nama_customer,p.total_bayar,u.nama_lengkap " +
                "FROM tb_penjualan p " +
                "JOIN tb_customer c ON p.id_customer=c.id_customer " +
                "JOIN tb_user u     ON p.id_user=u.id_user " +
                "ORDER BY p.id_jual DESC LIMIT 100");
            while (rs.next()) {
                mdlRiwayat.addRow(new Object[]{
                    rs.getString("no_faktur"),
                    rs.getString("tgl_transaksi"),
                    rs.getString("nama_customer"),
                    String.format("Rp %,.0f", rs.getDouble("total_bayar")),
                    rs.getString("nama_lengkap")
                });
            }
        } catch (SQLException ex) { err(ex.getMessage()); }
        finally { DBConnection.close(con, st, rs); }
    }

    private void lihatDetail() {
        int row = tblRiwayat.getSelectedRow();
        if (row < 0) return;
        String noFak = mdlRiwayat.getValueAt(row, 0).toString();

        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement(
                "SELECT b.nama_barang,d.harga_satuan,d.jumlah_beli,d.subtotal " +
                "FROM tb_detail_penjualan d " +
                "JOIN tb_barang b    ON d.id_barang=b.id_barang " +
                "JOIN tb_penjualan p ON d.id_jual=p.id_jual " +
                "WHERE p.no_faktur=?");
            ps.setString(1, noFak);
            rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("  Faktur : ").append(noFak).append("\n");
            sb.append("  ").append("─".repeat(52)).append("\n");
            sb.append(String.format("  %-24s %12s %5s %13s%n", "Barang", "Harga", "Qty", "Subtotal"));
            sb.append("  ").append("─".repeat(52)).append("\n");
            double tot = 0;
            while (rs.next()) {
                double sub = rs.getDouble("subtotal");
                tot += sub;
                sb.append(String.format("  %-24s %12s %5d %13s%n",
                    trunc(rs.getString("nama_barang"), 23),
                    String.format("Rp%,.0f", rs.getDouble("harga_satuan")),
                    rs.getInt("jumlah_beli"),
                    String.format("Rp%,.0f", sub)));
            }
            sb.append("  ").append("─".repeat(52)).append("\n");
            sb.append(String.format("  %43s %13s%n", "TOTAL:", String.format("Rp%,.0f", tot)));

            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new Font("Consolas", Font.PLAIN, 12));
            ta.setEditable(false);
            ta.setBackground(new Color(12, 22, 50));
            ta.setForeground(new Color(220, 230, 255));
            ta.setBorder(new EmptyBorder(10, 10, 10, 10));
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(500, 240));
            JOptionPane.showMessageDialog(this, sp, "Detail — " + noFak, JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) { err(ex.getMessage()); }
        finally { DBConnection.close(con, ps, rs); }
    }

    private void resetSemua() {
        keranjang.clear();
        mdlKeranjang.setRowCount(0);
        txtTotalBayar.setText("Rp 0");
        txtJumlah.setText("1");
        generateNoFaktur();
        if (cbCustomer.getItemCount() > 0) cbCustomer.setSelectedIndex(0);
        if (cbBarang.getItemCount() > 0) {
            cbBarang.setSelectedIndex(0);
            updateHarga();
        }
    }

    private String trunc(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    // INNER CLASS — tombol Hapus di keranjang
    // ================================================================
    static class BtnRenderer extends JButton implements TableCellRenderer {
        BtnRenderer() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(new Color(255, 120, 120));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setText("Hapus");
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            return this;
        }
    }

    static class BtnEditor extends DefaultCellEditor {
        private JButton btn;
        private TransaksiPanel owner;
        private int editRow;

        BtnEditor(JCheckBox cb, TransaksiPanel owner) {
            super(cb);
            this.owner = owner;
            btn = new JButton("Hapus");
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(new Color(255, 120, 120));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                fireEditingStopped();
                owner.hapusDariKeranjang(editRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            editRow = row;
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "✕";
        }
    }
}