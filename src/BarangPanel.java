import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

/**
 * BarangPanel.java — CRUD Data Barang dengan UI Glassmorphism + Fitur Hapus per baris
 */
public class BarangPanel extends JPanel {

    private JTextField txtId, txtNama, txtSatuan, txtHarga, txtStok;
    private JComboBox<String> cbKategori;
    private int[]    idKategoriList;
    private JTable   tabel;
    private DefaultTableModel model;
    private JTextField txtCari;
    private String selectedId = null;

    public BarangPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Header
        JPanel header = buatHeaderPanel("Data Barang", "Kelola informasi produk toko");
        add(header, BorderLayout.NORTH);

        // Konten
        JPanel konten = new JPanel(new BorderLayout(14, 0));
        konten.setOpaque(false);
        konten.setBorder(new EmptyBorder(14, 0, 0, 0));

        // Form kiri
        konten.add(buildForm(), BorderLayout.WEST);
        // Tabel kanan
        konten.add(buildTabel(), BorderLayout.CENTER);

        add(konten, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel wrap = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.C_GLASS);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.setColor(MainFrame.C_GLASS_BD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,18,18);
                GradientPaint shine = new GradientPaint(0,0,new Color(255,255,255,35),0,80,new Color(255,255,255,0));
                g2.setPaint(shine);
                g2.fillRoundRect(2,2,getWidth()-4,80,16,16);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(260, 0));
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(20, 18, 20, 18));

        JLabel lbl = new JLabel("Form Input Barang");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        wrap.add(lbl);
        wrap.add(Box.createVerticalStrut(16));

        txtId     = addFormField(wrap, "ID Barang",  "Contoh: B006");
        txtNama   = addFormField(wrap, "Nama Barang","Nama produk...");
        txtSatuan = addFormField(wrap, "Satuan",     "Pcs / Unit / Bungkus");
        txtHarga  = addFormField(wrap, "Harga Jual", "0");
        txtStok   = addFormField(wrap, "Stok",       "0");

        // Kategori ComboBox
        JLabel lblKat = new JLabel("Kategori");
        lblKat.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblKat.setForeground(MainFrame.C_TEXT_DIM);
        lblKat.setAlignmentX(LEFT_ALIGNMENT);
        wrap.add(lblKat);
        wrap.add(Box.createVerticalStrut(4));

        cbKategori = new JComboBox<>();
        cbKategori.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKategori.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbKategori.setAlignmentX(LEFT_ALIGNMENT);
        styleComboBox(cbKategori);
        loadKategori();
        wrap.add(cbKategori);
        wrap.add(Box.createVerticalStrut(18));

        // Tombol
        wrap.add(buatGlassBtn("Simpan",  MainFrame.C_ACCENT,   e -> simpanBarang()));
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(buatGlassBtn("Update",  new Color(50,180,130),e -> updateBarang()));
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(buatGlassBtn("Hapus",   MainFrame.C_DANGER,   e -> hapusBarang()));
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(buatGlassBtn("Bersih",  MainFrame.C_TEXT_DIM, e -> bersihForm()));

        return wrap;
    }

    private JPanel buildTabel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.C_GLASS);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.setColor(MainFrame.C_GLASS_BD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,18,18);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Cari:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSearch.setForeground(MainFrame.C_TEXT_DIM);
        txtCari = buatGlassTextField("Cari nama barang...");
        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { loadData(txtCari.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { loadData(txtCari.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData(txtCari.getText()); }
        });
        searchBar.add(lblSearch, BorderLayout.WEST);
        searchBar.add(txtCari,   BorderLayout.CENTER);

        // ========== MENAMBAHKAN KOLOM HAPUS ==========
        String[] cols = {"ID","Nama Barang","Kategori","Satuan","Harga Jual","Stok","Hapus"};
        model = new DefaultTableModel(cols, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return c == 6; // hanya kolom Hapus yang bisa diedit
            }
        };
        tabel = buildStyledTable(model);
        
        // Setting lebar kolom
        tabel.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabel.getColumnModel().getColumn(3).setPreferredWidth(70);
        tabel.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabel.getColumnModel().getColumn(5).setPreferredWidth(50);
        tabel.getColumnModel().getColumn(6).setPreferredWidth(60);
        tabel.getColumnModel().getColumn(6).setMaxWidth(70);
        
        // Renderer untuk kolom Hapus
        tabel.getColumnModel().getColumn(6).setCellRenderer(new HapusRenderer());
        // Editor untuk kolom Hapus
        tabel.getColumnModel().getColumn(6).setCellEditor(new HapusEditor(new JCheckBox(), this));
        
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFormDariTabel();
        });
        // ============================================

        JScrollPane scroll = new JScrollPane(tabel);
        styleScrollPane(scroll);

        wrap.add(searchBar,  BorderLayout.NORTH);
        wrap.add(scroll,     BorderLayout.CENTER);
        return wrap;
    }

    private void loadKategori() {
        cbKategori.removeAllItems();
        Connection con = null; Statement st = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            st  = con.createStatement();
            rs  = st.executeQuery("SELECT * FROM tb_kategori ORDER BY nama_kategori");
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            while (rs.next()) {
                cbKategori.addItem(rs.getString("nama_kategori"));
                ids.add(rs.getInt("id_kategori"));
            }
            idKategoriList = ids.stream().mapToInt(Integer::intValue).toArray();
        } catch (SQLException ex) {
            showErr("Gagal load kategori: " + ex.getMessage());
        } finally { DBConnection.close(con, st, rs); }
    }

    public void loadData() { loadData(""); }
    
    private void loadData(String kw) {
        model.setRowCount(0);
        Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement(
                "SELECT b.*, k.nama_kategori FROM tb_barang b " +
                "LEFT JOIN tb_kategori k ON b.id_kategori=k.id_kategori " +
                "WHERE b.nama_barang LIKE ? ORDER BY b.id_barang");
            ps.setString(1, "%" + kw + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("nama_kategori"),
                    rs.getString("satuan"),
                    String.format("Rp %,.0f", rs.getDouble("harga_jual")),
                    rs.getInt("stok"),
                    "Hapus"  // tombol hapus
                });
            }
        } catch (SQLException ex) {
            showErr("Gagal load data: " + ex.getMessage());
        } finally { DBConnection.close(con, ps, rs); }
    }

    // ========== HAPUS BARANG DARI TABEL (LANGSUNG) ==========
    void hapusBarangDariTabel(int row) {
        String idBarang = model.getValueAt(row, 0).toString();
        String namaBarang = model.getValueAt(row, 1).toString();
        
        int konfirm = JOptionPane.showConfirmDialog(this,
                "Hapus barang " + idBarang + " - " + namaBarang + "?\n\n" +
                "PERINGATAN: Barang yang sudah pernah dijual TIDAK BISA dihapus!",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (konfirm != JOptionPane.YES_OPTION) return;
        
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM tb_barang WHERE id_barang=?");
            ps.setString(1, idBarang);
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                JOptionPane.showMessageDialog(this,
                        "Barang " + idBarang + " berhasil dihapus!",
                        "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                loadData(txtCari.getText()); // refresh tabel
                bersihForm(); // bersihkan form
            }
        } catch (SQLException ex) {
            // Error karena foreign key (barang sudah pernah dijual)
            if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("Integrity")) {
                JOptionPane.showMessageDialog(this,
                        "Tidak dapat menghapus barang " + idBarang + "!\n\n" +
                        "Barang ini sudah pernah dijual.\n" +
                        "Hapus transaksi terkait terlebih dahulu jika ingin menghapus barang.",
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            } else {
                showErr("Gagal hapus: " + ex.getMessage());
            }
        } finally {
            DBConnection.close(con, ps, null);
        }
    }
    // ==========================================================

    private void simpanBarang() {
        if (!validasiInput()) return;
        Connection con = null; PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement(
                "INSERT INTO tb_barang VALUES (?,?,?,?,?,?)");
            ps.setString(1, txtId.getText().trim());
            ps.setInt(2, idKategoriList[cbKategori.getSelectedIndex()]);
            ps.setString(3, txtNama.getText().trim());
            ps.setString(4, txtSatuan.getText().trim());
            ps.setDouble(5, Double.parseDouble(txtHarga.getText().trim()));
            ps.setInt(6, Integer.parseInt(txtStok.getText().trim()));
            ps.executeUpdate();
            showOk("✅  Barang berhasil disimpan!");
            bersihForm(); loadData();
        } catch (SQLException ex) {
            showErr("Gagal simpan: " + ex.getMessage());
        } finally { DBConnection.close(con, ps, null); }
    }

    private void updateBarang() {
        if (selectedId == null) { showWarn("Pilih barang di tabel dulu!"); return; }
        if (!validasiInput()) return;
        Connection con = null; PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement(
                "UPDATE tb_barang SET id_kategori=?,nama_barang=?,satuan=?,harga_jual=?,stok=? WHERE id_barang=?");
            ps.setInt(1, idKategoriList[cbKategori.getSelectedIndex()]);
            ps.setString(2, txtNama.getText().trim());
            ps.setString(3, txtSatuan.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtHarga.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStok.getText().trim()));
            ps.setString(6, selectedId);
            ps.executeUpdate();
            showOk("✅  Barang berhasil diupdate!");
            bersihForm(); loadData();
        } catch (SQLException ex) {
            showErr("Gagal update: " + ex.getMessage());
        } finally { DBConnection.close(con, ps, null); }
    }

    private void hapusBarang() {
        if (selectedId == null) { showWarn("Pilih barang dulu!"); return; }
        int c = JOptionPane.showConfirmDialog(this,
                "Hapus barang ID: " + selectedId + "?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        Connection con = null; PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement("DELETE FROM tb_barang WHERE id_barang=?");
            ps.setString(1, selectedId);
            ps.executeUpdate();
            showOk("✅  Barang berhasil dihapus!");
            bersihForm(); loadData();
        } catch (SQLException ex) {
            if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("Integrity")) {
                showErr("Tidak dapat menghapus barang ini!\nBarang sudah pernah dijual.");
            } else {
                showErr("Gagal hapus: " + ex.getMessage());
            }
        } finally { DBConnection.close(con, ps, null); }
    }

    private void isiFormDariTabel() {
        int row = tabel.getSelectedRow();
        if (row < 0) return;
        selectedId = model.getValueAt(row, 0).toString();
        txtId.setText(selectedId);
        txtNama.setText(model.getValueAt(row, 1).toString());
        txtSatuan.setText(model.getValueAt(row, 3).toString());
        String hStr = model.getValueAt(row, 4).toString()
                .replace("Rp ", "").replace(",", "").replace(" ", "");
        txtHarga.setText(hStr);
        txtStok.setText(model.getValueAt(row, 5).toString());
        String kat = model.getValueAt(row, 2).toString();
        for (int i = 0; i < cbKategori.getItemCount(); i++) {
            if (cbKategori.getItemAt(i).equals(kat)) { cbKategori.setSelectedIndex(i); break; }
        }
    }

    private void bersihForm() {
        selectedId = null;
        txtId.setText(""); txtNama.setText(""); txtSatuan.setText("");
        txtHarga.setText(""); txtStok.setText("");
        if (cbKategori.getItemCount() > 0) cbKategori.setSelectedIndex(0);
        tabel.clearSelection();
    }

    private boolean validasiInput() {
        if (txtId.getText().trim().isEmpty() || txtNama.getText().trim().isEmpty()) {
            showWarn("ID Barang dan Nama tidak boleh kosong!"); return false;
        }
        try { Double.parseDouble(txtHarga.getText().trim()); } catch (NumberFormatException e) {
            showWarn("Harga harus berupa angka!"); return false;
        }
        try { Integer.parseInt(txtStok.getText().trim()); } catch (NumberFormatException e) {
            showWarn("Stok harus berupa angka bulat!"); return false;
        }
        return true;
    }

    // ---- UI Helper Methods ----
    private JTextField addFormField(JPanel parent, String label, String hint) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(MainFrame.C_TEXT_DIM);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));
        JTextField tf = buatGlassTextField(hint);
        parent.add(tf);
        parent.add(Box.createVerticalStrut(10));
        return tf;
    }

    static JTextField buatGlassTextField(String hint) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,18));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(255,255,255,45));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(new EmptyBorder(8, 10, 8, 10));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        return tf;
    }

    static void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(new Color(30, 50, 100));
        cb.setForeground(Color.black);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBorder(new EmptyBorder(4, 6, 4, 6));
        ((JComponent) cb.getRenderer()).setOpaque(true);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object val,
                    int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, val, idx, sel, foc);
                setBackground(sel ? new Color(60, 100, 200) : new Color(20, 35, 75));
                setForeground(Color.WHITE);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    static JButton buatGlassBtn(String text, Color color, ActionListener al) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = color;
                if (getModel().isPressed()) g2.setColor(base.darker());
                else if (getModel().isRollover()) g2.setColor(new Color(base.getRed(),base.getGreen(),base.getBlue(),180));
                else g2.setColor(new Color(base.getRed(),base.getGreen(),base.getBlue(),100));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(base.getRed(),base.getGreen(),base.getBlue(),180));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(new Color(255,255,255,30));
                g2.fillRoundRect(2,2,getWidth()-4,getHeight()/2-2,8,8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.addActionListener(al);
        return btn;
    }

    static JTable buildStyledTable(DefaultTableModel model) {
        JTable tabel = new JTable(model);
        tabel.setOpaque(false);
        tabel.setBackground(new Color(0,0,0,0));
        tabel.setForeground(MainFrame.C_TEXT);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabel.setRowHeight(34);
        tabel.setShowGrid(false);
        tabel.setIntercellSpacing(new Dimension(0, 4));
        tabel.setSelectionBackground(new Color(60, 140, 255, 80));
        tabel.setSelectionForeground(Color.WHITE);
        tabel.setFillsViewportHeight(true);

        JTableHeader header = tabel.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(255,255,255,15));
        header.setForeground(MainFrame.C_ACCENT2);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(new EmptyBorder(0,0,0,0));
        header.setPreferredSize(new Dimension(0, 38));

        tabel.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t,val,sel,foc,row,col);
                if (sel) {
                    setBackground(new Color(60,140,255,80));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row%2==0 ? new Color(255,255,255,8) : new Color(255,255,255,3));
                    setForeground(MainFrame.C_TEXT);
                }
                setBorder(new EmptyBorder(0,12,0,12));
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                return this;
            }
        });
        return tabel;
    }

    static void styleScrollPane(JScrollPane sp) {
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createLineBorder(new Color(255,255,255,30), 1));
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
    }

    static JPanel buatHeaderPanel(String judul, String sub) {
        JPanel h = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.C_GLASS);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(MainFrame.C_GLASS_BD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                GradientPaint gp = new GradientPaint(0,0,new Color(60,140,255,40),
                        getWidth(),0,new Color(100,220,180,20));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.dispose();
            }
        };
        h.setOpaque(false);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(new EmptyBorder(14, 20, 14, 20));
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lJudul.setForeground(Color.WHITE);
        lJudul.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSub = new JLabel(sub);
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSub.setForeground(MainFrame.C_TEXT_DIM);
        lSub.setAlignmentX(LEFT_ALIGNMENT);

        h.add(lJudul);
        h.add(lSub);
        return h;
    }

    private void showOk(String msg)   { JOptionPane.showMessageDialog(this,msg,"Info",JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn(String msg) { JOptionPane.showMessageDialog(this,msg,"Peringatan",JOptionPane.WARNING_MESSAGE); }
    private void showErr(String msg)  { JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE); }

    // ================================================================
    // INNER CLASS — tombol Hapus di tabel Barang
    // ================================================================
    static class HapusRenderer extends JButton implements TableCellRenderer {
        HapusRenderer() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(new Color(255, 100, 100));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setText("Hapus");
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            return this;
        }
    }

    static class HapusEditor extends DefaultCellEditor {
        private JButton btn;
        private BarangPanel owner;
        private int editRow;

        HapusEditor(JCheckBox cb, BarangPanel owner) {
            super(cb);
            this.owner = owner;
            btn = new JButton("Hapus");
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(new Color(255, 100, 100));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                fireEditingStopped();
                owner.hapusBarangDariTabel(editRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            editRow = row;
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "Hapus";
        }
    }
}