import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

/**
 * LoginFrame.java — Redesign Premium Glassmorphism
 * Fix: tidak ada emoji (diganti shape custom), konten tidak terpotong,
 * ukuran window cukup, font terbaca, animasi halus.
 */
public class LoginFrame extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblStatus;
    private float          animOffset = 0f;
    private Timer          animTimer;

    public LoginFrame() {
        setTitle("Login - Toko Berkah Jaya");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 620);           // lebih tinggi supaya tidak terpotong
        setMinimumSize(new Dimension(420, 580));
        setUndecorated(true);
        setLocationRelativeTo(null);

        // Rounded window shape
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), 28, 28));
            }
        });

        animTimer = new Timer(25, e -> {
            animOffset += 0.006f;
            if (animOffset > (float)(2 * Math.PI)) animOffset = 0f;
            repaint();
        });
        animTimer.start();

        buildUI();
    }

    // ================================================================
    // BUILD UI
    // ================================================================
    private void buildUI() {
        AnimatedBgPanel root = new AnimatedBgPanel();
        root.setLayout(new GridBagLayout());

        JPanel card = buildCard();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30);
        gbc.fill   = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        root.add(card, gbc);

        // Drag window support
        addDrag(root);
        setContentPane(root);
    }

    // ================================================================
    // CARD KACA
    // ================================================================
    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Isi kaca
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(0, 0, w, h, 22, 22);

                // Kilap atas (highlight)
                GradientPaint shine = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 55),
                        0, h / 4, new Color(255, 255, 255, 0));
                g2.setPaint(shine);
                g2.fillRoundRect(0, 0, w, h / 4, 22, 22);

                // Border kaca
                g2.setColor(new Color(255, 255, 255, 65));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 22, 22);

                // Border bawah lebih gelap (kedalaman)
                GradientPaint bdr = new GradientPaint(
                        0, 0, new Color(255,255,255,0),
                        0, h, new Color(120, 160, 255, 40));
                g2.setPaint(bdr);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 21, 21);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());

        // Inner panel dengan BoxLayout
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(8, 32, 24, 32));

        // --- Tombol tutup (X) di sudut kanan atas ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JButton btnClose = buatCloseBtn();
        topBar.add(btnClose, BorderLayout.EAST);
        inner.add(topBar);

        // --- Ikon custom (bukan emoji) ---
        inner.add(Box.createVerticalStrut(4));
        inner.add(buatIkon());
        inner.add(Box.createVerticalStrut(16));

        // --- Judul ---
        JLabel lblTitle = new JLabel("Berkah Jaya");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        inner.add(lblTitle);
        inner.add(Box.createVerticalStrut(6));

        JLabel lblSub = new JLabel("Sistem Informasi Penjualan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(160, 195, 255, 200));
        lblSub.setAlignmentX(CENTER_ALIGNMENT);
        inner.add(lblSub);
        inner.add(Box.createVerticalStrut(24));

        // --- Garis separator ---
        inner.add(buatDivider());
        inner.add(Box.createVerticalStrut(24));

        // --- Field Username ---
        inner.add(buatFieldLabel("Username"));
        inner.add(Box.createVerticalStrut(6));
        txtUsername = buatGlassField(false);
        inner.add(txtUsername);
        inner.add(Box.createVerticalStrut(16));

        // --- Field Password ---
        inner.add(buatFieldLabel("Password"));
        inner.add(Box.createVerticalStrut(6));
        txtPassword = (JPasswordField) buatGlassField(true);
        inner.add(txtPassword);
        inner.add(Box.createVerticalStrut(28));

        // --- Tombol Login ---
        btnLogin = buatLoginBtn();
        inner.add(btnLogin);
        inner.add(Box.createVerticalStrut(14));

        // --- Status ---
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(255, 110, 110));
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);
        lblStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        inner.add(lblStatus);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        card.add(inner, gbc);
        return card;
    }

    // ================================================================
    // IKON TOKO (digambar manual, bukan emoji)
    // ================================================================
    private JPanel buatIkon() {
        JPanel icon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int r  = 30;

                // Lingkaran latar
                GradientPaint bg = new GradientPaint(
                        cx - r, cy - r, new Color(60, 130, 255, 200),
                        cx + r, cy + r, new Color(40, 200, 180, 200));
                g2.setPaint(bg);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                // Border lingkaran
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                // Gambar ikon toko (bangunan)
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Badan toko
                int bx = cx - 14, by = cy - 4, bw = 28, bh = 16;
                g2.fillRoundRect(bx, by, bw, bh, 3, 3);

                // Atap (segitiga)
                int[] rx = {cx - 18, cx, cx + 18};
                int[] ry = {cy - 4, cy - 18, cy - 4};
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillPolygon(rx, ry, 3);

                // Pintu
                g2.setColor(new Color(60, 130, 255));
                g2.fillRoundRect(cx - 5, cy + 2, 10, 10, 2, 2);

                // Jendela kiri
                g2.setColor(new Color(180, 220, 255));
                g2.fillRect(bx + 4, by + 4, 7, 6);

                // Jendela kanan
                g2.fillRect(bx + bw - 11, by + 4, 7, 6);

                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(80, 80); }
            @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 80); }
        };
        icon.setOpaque(false);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        return icon;
    }

    // ================================================================
    // GLASS TEXT FIELD
    // ================================================================
    private JTextField buatGlassField(boolean isPassword) {
        JTextField tf = isPassword ? new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                paintGlass(g, this); super.paintComponent(g);
            }
        } : new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                paintGlass(g, this); super.paintComponent(g);
            }
        };

        tf.setOpaque(false);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(new Color(100, 200, 255));
        tf.setBorder(new EmptyBorder(11, 14, 11, 14));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        // Focus glow effect
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.repaint(); }
            @Override public void focusLost(FocusEvent e)   { tf.repaint(); }
        });
        return tf;
    }

    private void paintGlass(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean focused = c.hasFocus();
        // Isi
        g2.setColor(focused ? new Color(255,255,255,30) : new Color(255,255,255,16));
        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
        // Border
        if (focused) {
            g2.setColor(new Color(80, 160, 255, 200));
            g2.setStroke(new BasicStroke(1.8f));
        } else {
            g2.setColor(new Color(255, 255, 255, 55));
            g2.setStroke(new BasicStroke(1.2f));
        }
        g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, 12, 12);
        // Kilap atas
        g2.setColor(new Color(255, 255, 255, focused ? 40 : 25));
        g2.fillRoundRect(2, 2, c.getWidth()-4, (c.getHeight()-4)/2, 10, 10);
        g2.dispose();
    }

    // ================================================================
    // TOMBOL LOGIN
    // ================================================================
    private JButton buatLoginBtn() {
        JButton btn = new JButton("MASUK") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                if (getModel().isPressed()) {
                    g2.setColor(new Color(20, 90, 200));
                    g2.fillRoundRect(0,0,w,h,14,14);
                } else {
                    // Gradient biru premium
                    GradientPaint gp = new GradientPaint(
                            0, 0,   new Color(70, 150, 255),
                            0, h,   new Color(30,  90, 220));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 14, 14);

                    if (getModel().isRollover()) {
                        g2.setColor(new Color(255, 255, 255, 25));
                        g2.fillRoundRect(0, 0, w, h, 14, 14);
                    }
                }

                // Kilap atas tombol
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(3, 3, w - 6, h / 2 - 3, 11, 11);

                // Shadow bawah
                g2.setColor(new Color(20, 60, 180, 80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 2, w - 1, h - 2, 14, 14);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(0, 0, 0, 0));
        btn.addActionListener(e -> doLogin());
        return btn;
    }

    // ================================================================
    // TOMBOL CLOSE
    // ================================================================
    private JButton buatCloseBtn() {
        JButton btn = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 80, 80, 100));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(180, 190, 220));
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(28, 28));
        btn.addActionListener(e -> System.exit(0));
        return btn;
    }

    // ================================================================
    // HELPER KOMPONEN
    // ================================================================
    private JLabel buatFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(160, 195, 255));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buatDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255,255,255,0),
                        getWidth()/2, 0, new Color(255,255,255,60));
                GradientPaint gp2 = new GradientPaint(
                        getWidth()/2, 0, new Color(255,255,255,60),
                        getWidth(), 0, new Color(255,255,255,0));
                g2.setPaint(gp);
                g2.fillRect(0, getHeight()/2-1, getWidth()/2, 1);
                g2.setPaint(gp2);
                g2.fillRect(getWidth()/2, getHeight()/2-1, getWidth()/2, 1);
                g2.dispose();
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        d.setAlignmentX(LEFT_ALIGNMENT);
        return d;
    }

    // ================================================================
    // ANIMASI BACKGROUND
    // ================================================================
    private class AnimatedBgPanel extends JPanel {
        AnimatedBgPanel() { setOpaque(true); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();

            // Background dasar gradient gelap
            GradientPaint bg = new GradientPaint(
                    0, 0, new Color(8, 14, 40),
                    w, h, new Color(14, 22, 55));
            g2.setPaint(bg);
            g2.fillRect(0, 0, w, h);

            // Bola cahaya bergerak
            buatGlow(g2,
                    w * 0.15f + (float)(Math.sin(animOffset) * 70),
                    h * 0.25f + (float)(Math.cos(animOffset * 0.7) * 50),
                    280, new Color(30, 80, 220, 55));
            buatGlow(g2,
                    w * 0.85f + (float)(Math.cos(animOffset * 1.1) * 60),
                    h * 0.70f + (float)(Math.sin(animOffset * 0.8) * 55),
                    240, new Color(90, 20, 190, 50));
            buatGlow(g2,
                    w * 0.50f + (float)(Math.sin(animOffset * 0.9) * 80),
                    h * 0.85f + (float)(Math.cos(animOffset * 1.2) * 35),
                    180, new Color(15, 160, 130, 45));
            // Bola kecil tambahan
            buatGlow(g2,
                    w * 0.70f + (float)(Math.sin(animOffset * 1.4) * 40),
                    h * 0.15f + (float)(Math.cos(animOffset * 0.6) * 30),
                    130, new Color(200, 80, 30, 35));

            g2.dispose();
        }

        private void buatGlow(Graphics2D g2, float cx, float cy, float r, Color c) {
            if (r <= 0) return;
            RadialGradientPaint rp = new RadialGradientPaint(
                    cx, cy, r,
                    new float[]{0f, 1f},
                    new Color[]{c, new Color(0, 0, 0, 0)});
            g2.setPaint(rp);
            g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        }
    }

    // ================================================================
    // DRAG SUPPORT
    // ================================================================
    private void addDrag(JPanel panel) {
        final Point[] start = {null};
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { start[0] = e.getPoint(); }
            @Override public void mouseReleased(MouseEvent e) { start[0] = null; }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (start[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - start[0].x,
                                loc.y + e.getY() - start[0].y);
                }
            }
        });
    }

    // ================================================================
    // LOGIKA LOGIN
    // ================================================================
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Username dan password tidak boleh kosong!", false);
            animShake();
            return;
        }

        btnLogin.setEnabled(false);
        setStatus("Memeriksa kredensial...", true);

        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                Connection con = null; PreparedStatement ps = null; ResultSet rs = null;
                try {
                    con = DBConnection.getConnection();
                    ps  = con.prepareStatement(
                            "SELECT * FROM tb_user WHERE username=? AND password=?");
                    ps.setString(1, username);
                    ps.setString(2, password);
                    rs  = ps.executeQuery();
                    if (rs.next()) return new Object[]{
                        rs.getInt("id_user"),
                        rs.getString("nama_lengkap"),
                        rs.getString("level")
                    };
                    return null;
                } finally { DBConnection.close(con, ps, rs); }
            }
            @Override protected void done() {
                btnLogin.setEnabled(true);
                try {
                    Object[] user = (Object[]) get();
                    if (user != null) {
                        setStatus("Login berhasil! Membuka aplikasi...", true);
                        animTimer.stop();
                        Timer delay = new Timer(700, ev -> {
                            dispose();
                            new MainFrame(
                                (int)    user[0],
                                (String) user[1],
                                (String) user[2]
                            ).setVisible(true);
                        });
                        delay.setRepeats(false);
                        delay.start();
                    } else {
                        setStatus("Username atau password salah!", false);
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        animShake();
                    }
                } catch (Exception ex) {
                    setStatus("Koneksi database gagal! Periksa DBConnection.java", false);
                }
            }
        }.execute();
    }

    private void setStatus(String msg, boolean ok) {
        lblStatus.setText(msg);
        lblStatus.setForeground(ok
                ? new Color(80, 220, 150)
                : new Color(255, 100, 100));
    }

    /** Animasi goyang saat login salah */
    private void animShake() {
        final int[]   step = {0};
        final Point   orig = getLocation();
        final int[]   dx   = {10, -10, 8, -8, 5, -5, 3, -3, 0};
        Timer t = new Timer(40, null);
        t.addActionListener(e -> {
            if (step[0] < dx.length) {
                setLocation(orig.x + dx[step[0]], orig.y);
                step[0]++;
            } else {
                setLocation(orig);
                ((Timer) e.getSource()).stop();
            }
        });
        t.start();
    }

    // ================================================================
    // MAIN
    // ================================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        // Matikan log verbose AWT
        java.util.logging.Logger.getLogger("java.awt").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("javax.swing").setLevel(java.util.logging.Level.OFF);
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}