import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class CustomerPanel extends JPanel {

    private JTextField txtId, txtNama, txtAlamat, txtTelepon, txtCari;
    private JTable tabel;
    private DefaultTableModel model;
    private String selId = null;

    public CustomerPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(20, 22, 20, 22));
        add(MainFrame.buatHeader(
                "Data Customer", "Kelola identitas pelanggan tetap"),
                BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.add(buildForm(),  BorderLayout.WEST);
        body.add(buildTabel(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        loadData();
    }

    // ── FORM ──────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel card = MainFrame.glassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 18, 20, 18));
        card.setPreferredSize(new Dimension(260, 0));

        JLabel ttl = new JLabel("Form Input Customer");
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ttl.setForeground(MainFrame.C_TEXT);
        ttl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(ttl);
        card.add(Box.createVerticalStrut(16));

        card.add(MainFrame.fieldLabel("ID Customer"));
        card.add(Box.createVerticalStrut(5));
        txtId = MainFrame.glassField();
        card.add(txtId);
        card.add(Box.createVerticalStrut(10));

        card.add(MainFrame.fieldLabel("Nama Customer"));
        card.add(Box.createVerticalStrut(5));
        txtNama = MainFrame.glassField();
        card.add(txtNama);
        card.add(Box.createVerticalStrut(10));

        card.add(MainFrame.fieldLabel("Alamat"));
        card.add(Box.createVerticalStrut(5));
        txtAlamat = MainFrame.glassField();
        card.add(txtAlamat);
        card.add(Box.createVerticalStrut(10));

        card.add(MainFrame.fieldLabel("No. Telepon"));
        card.add(Box.createVerticalStrut(5));
        txtTelepon = MainFrame.glassField();
        
        // Membatasi input hanya angka (0-9) dan maksimal 15 digit
        ((AbstractDocument) txtTelepon.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                    throws BadLocationException {
                if (string == null) return;
                if (string.matches("\\d+")) {
                    String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
                    if (newText.length() <= 15) {
                        super.insertString(fb, offset, string, attr);
                    }
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                    throws BadLocationException {
                if (text == null) return;
                if (text.matches("\\d+")) {
                    String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                    if (newText.length() <= 15) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            }
        });
        
        txtTelepon.setToolTipText("Hanya angka (maksimal 15 digit)");
        card.add(txtTelepon);
        card.add(Box.createVerticalStrut(18));

        card.add(MainFrame.colorBtn("Simpan", MainFrame.C_ACCENT, e -> simpan()));
        card.add(Box.createVerticalStrut(8));
        card.add(MainFrame.colorBtn("Update", MainFrame.C_SUCCESS, e -> update()));
        card.add(Box.createVerticalStrut(8));
        card.add(MainFrame.colorBtn("Hapus", MainFrame.C_DANGER, e -> hapus()));
        card.add(Box.createVerticalStrut(8));
        card.add(MainFrame.colorBtn("Bersih", MainFrame.C_TEXT_DIM, e -> bersih()));
        return card;
    }

    // ── TABEL dengan kolom HAPUS ─────────────────────────────────────
    private JPanel buildTabel() {
        JPanel card = MainFrame.glassCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel sb = new JPanel(new BorderLayout(8, 0));
        sb.setOpaque(false);
        JLabel ico = new JLabel("🔍 Cari:");
        ico.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ico.setForeground(MainFrame.C_TEXT_DIM);
        txtCari = MainFrame.glassField();
        txtCari.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { loadData(txtCari.getText()); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { loadData(txtCari.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData(txtCari.getText()); }
        });
        sb.add(ico,    BorderLayout.WEST);
        sb.add(txtCari, BorderLayout.CENTER);

        // ========== MENAMBAHKAN KOLOM HAPUS ==========
        String[] cols = {"ID", "Nama Customer", "Alamat", "No. Telepon", "Hapus"};
        model = new DefaultTableModel(cols, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return c == 4; // hanya kolom Hapus yang bisa diedit (untuk tombol)
            }
        };
        tabel = MainFrame.glassTable(model);
        
        // Setting lebar kolom
        tabel.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabel.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabel.getColumnModel().getColumn(4).setPreferredWidth(60);
        tabel.getColumnModel().getColumn(4).setMaxWidth(70);
        
        // Renderer untuk kolom Hapus
        tabel.getColumnModel().getColumn(4).setCellRenderer(new HapusRenderer());
        // Editor untuk kolom Hapus
        tabel.getColumnModel().getColumn(4).setCellEditor(new HapusEditor(new JCheckBox(), this));
        
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiForms();
        });
        // ============================================

        card.add(sb,                          BorderLayout.NORTH);
        card.add(MainFrame.glassScroll(tabel), BorderLayout.CENTER);
        return card;
    }

    // ── DATA ──────────────────────────────────────────────────────
    public void loadData() { loadData(""); }

    private void loadData(String kw) {
        model.setRowCount(0);
        Connection con=null; PreparedStatement ps=null; ResultSet rs=null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement(
                "SELECT * FROM tb_customer "
                + "WHERE nama_customer LIKE ? ORDER BY id_customer");
            ps.setString(1, "%"+kw+"%");
            rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("id_customer"),
                    rs.getString("nama_customer"),
                    rs.getString("alamat"),
                    rs.getString("telepon"),
                    "Hapus"  // tombol hapus
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal load: "+ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally { DBConnection.close(con, ps, rs); }
    }

    // ========== VALIDASI TELEPON ==========
    private boolean validasiTelepon() {
        String telepon = txtTelepon.getText().trim();
        if (telepon.isEmpty()) {
            return true;
        }
        if (!telepon.matches("\\d+")) {
            JOptionPane.showMessageDialog(this,
                    "No. Telepon hanya boleh berisi ANGKA!\nContoh: 081234567890",
                    "Validasi Error", JOptionPane.ERROR_MESSAGE);
            txtTelepon.requestFocus();
            return false;
        }
        if (telepon.length() > 15) {
            JOptionPane.showMessageDialog(this,
                    "No. Telepon maksimal 15 digit!",
                    "Validasi Error", JOptionPane.ERROR_MESSAGE);
            txtTelepon.requestFocus();
            return false;
        }
        return true;
    }

    // ========== HAPUS CUSTOMER DARI TABEL (LANGSUNG) ==========
    void hapusCustomerDariTabel(int row) {
        String idCustomer = model.getValueAt(row, 0).toString();
        String namaCustomer = model.getValueAt(row, 1).toString();
        
        int konfirm = JOptionPane.showConfirmDialog(this,
                "Hapus customer " + idCustomer + " - " + namaCustomer + "?\n\n" +
                "PERINGATAN: Customer yang memiliki transaksi TIDAK BISA dihapus!",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (konfirm != JOptionPane.YES_OPTION) return;
        
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM tb_customer WHERE id_customer=?");
            ps.setString(1, idCustomer);
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                JOptionPane.showMessageDialog(this,
                        "Customer " + idCustomer + " berhasil dihapus!",
                        "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                loadData(txtCari.getText()); // refresh tabel
                bersih(); // bersihkan form
            }
        } catch (SQLException ex) {
            // Error karena foreign key (customer punya transaksi)
            if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("Integrity")) {
                JOptionPane.showMessageDialog(this,
                        "Tidak dapat menghapus customer " + idCustomer + "!\n\n" +
                        "Customer ini memiliki riwayat transaksi.\n" +
                        "Hapus transaksi terlebih dahulu jika ingin menghapus customer.",
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal hapus: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            DBConnection.close(con, ps, null);
        }
    }
    // ==========================================================

    private void simpan() {
        if (txtId.getText().trim().isEmpty() || txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ID & Nama wajib diisi!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validasiTelepon()) return;
        
        Connection con=null; PreparedStatement ps=null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement("INSERT INTO tb_customer VALUES (?,?,?,?)");
            ps.setString(1, txtId.getText().trim());
            ps.setString(2, txtNama.getText().trim());
            ps.setString(3, txtAlamat.getText().trim());
            ps.setString(4, txtTelepon.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Customer berhasil disimpan!",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            bersih(); loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal simpan: "+ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally { DBConnection.close(con, ps, null); }
    }

    private void update() {
        if (selId == null) {
            JOptionPane.showMessageDialog(this,
                    "Pilih customer dulu!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validasiTelepon()) return;
        
        Connection con=null; PreparedStatement ps=null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement(
                "UPDATE tb_customer SET nama_customer=?, alamat=?, telepon=? WHERE id_customer=?");
            ps.setString(1, txtNama.getText().trim());
            ps.setString(2, txtAlamat.getText().trim());
            ps.setString(3, txtTelepon.getText().trim());
            ps.setString(4, selId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Customer diupdate!",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            bersih(); loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal update: "+ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally { DBConnection.close(con, ps, null); }
    }

    private void hapus() {
        if (selId == null) {
            JOptionPane.showMessageDialog(this,
                    "Pilih customer dulu!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int konfirm = JOptionPane.showConfirmDialog(this,
                "Hapus customer "+selId+"?","Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (konfirm != JOptionPane.YES_OPTION) return;
        
        Connection con=null; PreparedStatement ps=null;
        try {
            con = DBConnection.getConnection();
            ps  = con.prepareStatement("DELETE FROM tb_customer WHERE id_customer=?");
            ps.setString(1, selId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                    "Customer dihapus!",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            bersih(); loadData();
        } catch (SQLException ex) {
            if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("Integrity")) {
                JOptionPane.showMessageDialog(this,
                        "Tidak dapat menghapus customer " + selId + "!\n\n" +
                        "Customer ini memiliki riwayat transaksi.\n" +
                        "Hapus transaksi terlebih dahulu jika ingin menghapus customer.",
                        "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal hapus: "+ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally { DBConnection.close(con, ps, null); }
    }

    private void isiForms() {
        int r = tabel.getSelectedRow();
        if (r < 0) return;
        selId = model.getValueAt(r,0).toString();
        txtId.setText(selId);
        txtNama.setText(model.getValueAt(r,1).toString());
        txtAlamat.setText(model.getValueAt(r,2).toString());
        txtTelepon.setText(model.getValueAt(r,3).toString());
    }

    private void bersih() {
        selId = null;
        txtId.setText("");
        txtNama.setText("");
        txtAlamat.setText("");
        txtTelepon.setText("");
        tabel.clearSelection();
    }

    // ================================================================
    // INNER CLASS — tombol Hapus di tabel Customer
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
        private CustomerPanel owner;
        private int editRow;

        HapusEditor(JCheckBox cb, CustomerPanel owner) {
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
                owner.hapusCustomerDariTabel(editRow);
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