import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class MainFrame extends JFrame {

    private int    userId;
    private String namaUser;
    private String levelUser;

    private JPanel     contentArea;
    private CardLayout cardLayout;

    private BarangPanel    barangPanel;
    private CustomerPanel  customerPanel;
    private TransaksiPanel transaksiPanel;
    private LaporanPanel   laporanPanel;

    static final Color C_BG1      = new Color(7,  11, 30);
    static final Color C_BG2      = new Color(10, 16, 42);
    static final Color C_SIDEBAR  = new Color(10, 14, 36);
    static final Color C_GLASS    = new Color(255, 255, 255, 14);
    static final Color C_GLASS_BD = new Color(255, 255, 255, 45);
    static final Color C_ACCENT   = new Color(99,  179, 237);
    static final Color C_ACCENT2  = new Color(129, 230, 217);
    static final Color C_ACCENT3  = new Color(183, 148, 244);
    static final Color C_TEXT     = new Color(226, 232, 255);
    static final Color C_TEXT_DIM = new Color(113, 128, 175);
    static final Color C_SUCCESS  = new Color(72,  199, 142);
    static final Color C_DANGER   = new Color(252, 129, 129);
    static final Color C_WARNING  = new Color(246, 214,   7);

    private float  anim = 0f;
    private Timer  bgTimer;
    private JButton activeBtn;
    private boolean isMaximized = false;
    private Rectangle normalBounds;

    public MainFrame(int userId, String namaUser, String levelUser) {
        this.userId    = userId;
        this.namaUser  = namaUser;
        this.levelUser = levelUser;

        setTitle("Toko Berkah Jaya");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        setUndecorated(true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(
                        0, 0, getWidth(), getHeight(), 16, 16));
            }
        });

        bgTimer = new Timer(35, e -> {
            anim += 0.004f;
            repaint();
        });
        bgTimer.start();

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                GradientPaint bg = new GradientPaint(0, 0, C_BG1, w, h, C_BG2);
                g2.setPaint(bg);
                g2.fillRect(0, 0, w, h);

                drawGlow(g2,
                        w * .12f + (float)(Math.sin(anim) * 90),
                        h * .20f + (float)(Math.cos(anim * .7) * 60),
                        340, new Color(30, 80, 220, 38));
                drawGlow(g2,
                        w * .88f + (float)(Math.cos(anim * .9) * 70),
                        h * .72f + (float)(Math.sin(anim) * 55),
                        280, new Color(80, 20, 190, 34));
                drawGlow(g2,
                        w * .50f + (float)(Math.sin(anim * .8) * 95),
                        h * .88f + (float)(Math.cos(anim * 1.1) * 38),
                        220, new Color(14, 160, 130, 30));
                drawGlow(g2,
                        w * .75f + (float)(Math.sin(anim * 1.3) * 50),
                        h * .10f + (float)(Math.cos(anim * .6) * 35),
                        150, new Color(200, 80, 30, 25));
                g2.dispose();
            }

            private void drawGlow(Graphics2D g2,
                    float cx, float cy, float r, Color c) {
                RadialGradientPaint p = new RadialGradientPaint(
                        cx, cy, r,
                        new float[]{0f, 1f},
                        new Color[]{c, new Color(0, 0, 0, 0)});
                g2.setPaint(p);
                g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
            }
        };
        root.setOpaque(false);
        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildContent(),  BorderLayout.CENTER);
        addDrag(root);
        setContentPane(root);
    }
    
private JPanel buildTitleBar() {
    JPanel bar = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(6, 9, 24));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255,255,255,15));
            g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            g2.dispose();
        }
    };
    bar.setOpaque(false);
    bar.setPreferredSize(new Dimension(0, 34));

    JLabel lblApp = new JLabel("   Toko Berkah Jaya");
    lblApp.setFont(new Font("Segoe UI", Font.BOLD, 12));
    lblApp.setForeground(new Color(150, 170, 220));

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    btnPanel.setOpaque(false);

    // Tombol minimize — gambar manual pakai Graphics2D (bukan karakter)
    JButton btnMin = new JButton() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(new Color(70, 70, 90));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            // Garis minimize (—)
            g2.setColor(new Color(200, 200, 210));
            g2.setStroke(new BasicStroke(1.5f));
            int cy = getHeight() / 2 + 3;
            g2.drawLine(14, cy, getWidth()-14, cy);
            g2.dispose();
        }
    };
    styleWinBtn(btnMin);
    btnMin.addActionListener(e -> setState(ICONIFIED));

    // Tombol maximize — gambar kotak manual
    JButton btnMax = new JButton() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(new Color(70, 70, 90));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.setColor(new Color(200, 200, 210));
            g2.setStroke(new BasicStroke(1.5f));
            if (!isMaximized) {
                // Kotak tunggal (restore)
                int m = 11;
                g2.drawRect(m, m, getWidth()-m*2, getHeight()-m*2);
            } else {
                // Dua kotak bertumpuk (maximized)
                int m = 10, s = 10;
                g2.drawRect(m+3, m,   s, s);
                g2.drawRect(m,   m+3, s, s);
                // Tutupi bagian bawah kotak depan supaya terlihat bertumpuk
                g2.setColor(new Color(6, 9, 24));
                g2.fillRect(m+1, m+4, s-1, s-1);
                g2.setColor(new Color(200, 200, 210));
                g2.drawRect(m,   m+3, s, s);
            }
            g2.dispose();
        }
    };
    styleWinBtn(btnMax);
    btnMax.addActionListener(e -> {
        if (!isMaximized) {
            normalBounds = getBounds();
            GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screen = ge.getMaximumWindowBounds();
            setBounds(screen);
            setShape(null);
            isMaximized = true;
        } else {
            setBounds(normalBounds);
            setShape(new RoundRectangle2D.Double(
                    0, 0, getWidth(), getHeight(), 16, 16));
            isMaximized = false;
        }
        btnMax.repaint();
    });

    // Tombol close — gambar X manual
    JButton btnClose = new JButton() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(new Color(196, 43, 28));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            // X
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int m = 13;
            g2.drawLine(m, m, getWidth()-m, getHeight()-m);
            g2.drawLine(getWidth()-m, m, m, getHeight()-m);
            g2.dispose();
        }
    };
    styleWinBtn(btnClose);
    btnClose.addActionListener(e -> System.exit(0));

    btnPanel.add(btnMin);
    btnPanel.add(btnMax);
    btnPanel.add(btnClose);

    bar.add(lblApp,   BorderLayout.WEST);
    bar.add(btnPanel, BorderLayout.EAST);

    addDrag(bar);

    bar.addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) btnMax.doClick();
        }
    });

    addResizeSupport();
    return bar;
}

// Helper styling tombol window
private void styleWinBtn(JButton btn) {
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getDefaultCursor());
    btn.setPreferredSize(new Dimension(46, 34));
}

private JButton winBtn(String text, Color fg, Color hoverBg) {
    JButton btn = new JButton(text) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (getModel().isRollover()) {
                g2.setColor(hoverBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    };
    btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    btn.setForeground(fg);
    btn.setOpaque(false); btn.setContentAreaFilled(false);
    btn.setBorderPainted(false); btn.setFocusPainted(false);
    btn.setCursor(Cursor.getDefaultCursor());
    btn.setPreferredSize(new Dimension(46, 34));
    return btn;
}

private void addResizeSupport() {
    int BORDER = 6;
    addMouseMotionListener(new MouseMotionAdapter() {
        @Override public void mouseMoved(MouseEvent e) {
            if (isMaximized) { setCursor(Cursor.getDefaultCursor()); return; }
            int x=e.getX(), y=e.getY(), w=getWidth(), h=getHeight();
            if      (x<BORDER && y<BORDER)         setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            else if (x>w-BORDER && y<BORDER)       setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
            else if (x<BORDER && y>h-BORDER)       setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            else if (x>w-BORDER && y>h-BORDER)     setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
            else if (x<BORDER)                     setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            else if (x>w-BORDER)                   setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            else if (y<BORDER)                     setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            else if (y>h-BORDER)                   setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
            else                                   setCursor(Cursor.getDefaultCursor());
        }
        @Override public void mouseDragged(MouseEvent e) {
            if (isMaximized) return;
            int cx=getCursor().getType(), x=e.getXOnScreen(), y=e.getYOnScreen();
            Rectangle r = getBounds();
            int minW=1100, minH=680;
            switch(cx){
                case Cursor.SE_RESIZE_CURSOR:
                    setSize(Math.max(minW,x-r.x),Math.max(minH,y-r.y)); break;
                case Cursor.S_RESIZE_CURSOR:
                    setSize(r.width,Math.max(minH,y-r.y)); break;
                case Cursor.E_RESIZE_CURSOR:
                    setSize(Math.max(minW,x-r.x),r.height); break;
                case Cursor.N_RESIZE_CURSOR:
                    int nh=Math.max(minH,r.y+r.height-y);
                    setBounds(r.x,y,r.width,nh); break;
                case Cursor.W_RESIZE_CURSOR:
                    int nw=Math.max(minW,r.x+r.width-x);
                    setBounds(x,r.y,nw,r.height); break;
                case Cursor.NW_RESIZE_CURSOR:
                    int nw2=Math.max(minW,r.x+r.width-x);
                    int nh2=Math.max(minH,r.y+r.height-y);
                    setBounds(x,y,nw2,nh2); break;
                case Cursor.NE_RESIZE_CURSOR:
                    int nh3=Math.max(minH,r.y+r.height-y);
                    setSize(Math.max(minW,x-r.x),nh3);
                    setLocation(r.x,y); break;
                case Cursor.SW_RESIZE_CURSOR:
                    int nw3=Math.max(minW,r.x+r.width-x);
                    setSize(nw3,Math.max(minH,y-r.y));
                    setLocation(x,r.y); break;
            }
            if (!isMaximized)
                setShape(new RoundRectangle2D.Double(
                        0,0,getWidth(),getHeight(),16,16));
        }
    });
}

    private JPanel buildSidebar() {
        JPanel sb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(C_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 0),
                        0, getHeight() / 2, new Color(255, 255, 255, 35));
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sb.setOpaque(false);
        sb.setPreferredSize(new Dimension(210, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));

        // Logo
        JPanel logo = new JPanel();
        logo.setOpaque(false);
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBorder(new EmptyBorder(24, 20, 20, 20));
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JPanel ikonToko = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 36, x = (getWidth() - s) / 2, y = (getHeight() - s) / 2;
                GradientPaint gp = new GradientPaint(
                        x, y, C_ACCENT, x + s, y + s, C_ACCENT3);
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, s, s, 10, 10);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(x, y, s, s / 2, 10, 10);
                // Atap
                int[] rx = {x + 3, x + s / 2, x + s - 3};
                int[] ry = {y + 15, y + 4, y + 15};
                g2.setColor(Color.WHITE);
                g2.fillPolygon(rx, ry, 3);
                // Badan
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(x + 6, y + 14, s - 12, s - 14, 3, 3);
                // Pintu
                g2.setColor(new Color(
                        C_ACCENT.getRed(), C_ACCENT.getGreen(),
                        C_ACCENT.getBlue(), 180));
                g2.fillRoundRect(x + s / 2 - 5, y + 24, 10, s - 24, 2, 2);
                // Jendela
                g2.setColor(new Color(180, 220, 255, 200));
                g2.fillRect(x + 8,      y + 17, 8, 6);
                g2.fillRect(x + s - 16, y + 17, 8, 6);
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
            @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 40); }
            @Override public Dimension getMinimumSize()   { return new Dimension(40, 40); }
        };
        ikonToko.setOpaque(false);
        ikonToko.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblNama = new JLabel("Berkah Jaya");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblNama.setForeground(C_TEXT);
        lblNama.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Sistem Penjualan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(C_TEXT_DIM);
        lblSub.setAlignmentX(LEFT_ALIGNMENT);

        logo.add(ikonToko);
        logo.add(Box.createVerticalStrut(10));
        logo.add(lblNama);
        logo.add(Box.createVerticalStrut(2));
        logo.add(lblSub);

        sb.add(logo);
        sb.add(makeDivider());
        sb.add(Box.createVerticalStrut(8));

        JButton b1 = navBtn("Data Barang",   "barang");
        JButton b2 = navBtn("Data Customer", "customer");
        JButton b3 = navBtn("Transaksi",     "transaksi");
        JButton b4 = navBtn("Laporan",       "laporan");

        sb.add(b1); sb.add(Box.createVerticalStrut(4));
        sb.add(b2); sb.add(Box.createVerticalStrut(4));
        sb.add(b3); sb.add(Box.createVerticalStrut(4));
        sb.add(b4);
        sb.add(Box.createVerticalGlue());
        sb.add(makeDivider());

        // User info
        JPanel uInfo = new JPanel();
        uInfo.setOpaque(false);
        uInfo.setLayout(new BoxLayout(uInfo, BoxLayout.Y_AXIS));
        uInfo.setBorder(new EmptyBorder(12, 16, 10, 16));
        uInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        uInfo.setAlignmentX(LEFT_ALIGNMENT);

        JPanel uRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        uRow.setOpaque(false);
        uRow.setAlignmentX(LEFT_ALIGNMENT);
        uRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, C_ACCENT, getWidth(), getHeight(), C_ACCENT3);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String ini = namaUser.length() > 0
                        ? String.valueOf(namaUser.charAt(0)).toUpperCase() : "U";
                g2.drawString(ini,
                        (28 - fm.stringWidth(ini)) / 2,
                        (28 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() { return new Dimension(28, 28); }
            @Override public Dimension getMaximumSize()   { return new Dimension(28, 28); }
            @Override public Dimension getMinimumSize()   { return new Dimension(28, 28); }
        };
        avatar.setOpaque(false);

        JPanel uText = new JPanel();
        uText.setOpaque(false);
        uText.setLayout(new BoxLayout(uText, BoxLayout.Y_AXIS));

        JLabel lNama = new JLabel(namaUser);
        lNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lNama.setForeground(C_TEXT);

        JLabel lLvl = new JLabel(levelUser);
        lLvl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lLvl.setForeground(C_ACCENT2);

        uText.add(lNama);
        uText.add(lLvl);
        uRow.add(avatar);
        uRow.add(uText);
        uInfo.add(uRow);
        sb.add(uInfo);

        sb.add(sideBtn("Keluar", C_DANGER, e -> {
            bgTimer.stop();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }));
        sb.add(Box.createVerticalStrut(4));
        sb.add(sideBtn("Tutup", C_TEXT_DIM, e -> System.exit(0)));
        sb.add(Box.createVerticalStrut(16));

        activeBtn = b1;
        setNavActive(b1);
        return sb;
    }

    private JButton navBtn(String label, String panel) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (this == activeBtn) {
                    GradientPaint gp = new GradientPaint(
                            0, 0, new Color(99, 179, 237, 50),
                            getWidth(), 0, new Color(183, 148, 244, 30));
                    g2.setPaint(gp);
                    g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
                    GradientPaint ind = new GradientPaint(
                            0, 0, C_ACCENT, 0, getHeight(), C_ACCENT3);
                    g2.setPaint(ind);
                    g2.setStroke(new BasicStroke(3f,
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(4, 8, 4, getHeight() - 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(C_TEXT_DIM);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> {
            if (activeBtn != null) {
                activeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                activeBtn.setForeground(C_TEXT_DIM);
                activeBtn.repaint();
            }
            activeBtn = btn;
            setNavActive(btn);
            cardLayout.show(contentArea, panel);
            if (panel.equals("transaksi")) transaksiPanel.refreshData();
            if (panel.equals("laporan"))   laporanPanel.loadLaporan();
        });
        return btn;
    }

    private void setNavActive(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.repaint();
    }

    private JButton sideBtn(String label, Color color, ActionListener al) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 40)
                        : new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 8, 8);
                g2.setColor(new Color(
                        color.getRed(), color.getGreen(), color.getBlue(), 80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(8, 2, getWidth() - 17, getHeight() - 5, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(color);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(al);
        return btn;
    }

    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        barangPanel    = new BarangPanel();
        customerPanel  = new CustomerPanel();
        transaksiPanel = new TransaksiPanel(userId);
        laporanPanel   = new LaporanPanel();

        contentArea.add(barangPanel,    "barang");
        contentArea.add(customerPanel,  "customer");
        contentArea.add(transaksiPanel, "transaksi");
        contentArea.add(laporanPanel,   "laporan");
        return contentArea;
    }

    private void addDrag(JPanel p) {
        final Point[] s = {null};
        p.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { s[0] = e.getPoint(); }
            @Override public void mouseReleased(MouseEvent e) { s[0] = null; }
        });
        p.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (s[0] != null) {
                    Point l = getLocation();
                    setLocation(l.x + e.getX() - s[0].x,
                                l.y + e.getY() - s[0].y);
                }
            }
        });
    }

    // ================================================================
    // STATIC HELPERS — dipakai semua panel anak
    // ================================================================

    static JPanel makeDivider() {
        JPanel d = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint l = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 0),
                        getWidth() / 2, 0, new Color(255, 255, 255, 30));
                GradientPaint r = new GradientPaint(
                        getWidth() / 2, 0, new Color(255, 255, 255, 30),
                        getWidth(), 0, new Color(255, 255, 255, 0));
                g2.setPaint(l);
                g2.fillRect(0, getHeight() / 2, getWidth() / 2, 1);
                g2.setPaint(r);
                g2.fillRect(getWidth() / 2, getHeight() / 2, getWidth() / 2, 1);
                g2.dispose();
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        d.setAlignmentX(LEFT_ALIGNMENT);
        return d;
    }

    static JPanel buatHeader(String judul, String sub) {
        JPanel h = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_GLASS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(99, 179, 237, 30),
                        getWidth(), 0, new Color(183, 148, 244, 15));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(C_GLASS_BD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        h.setOpaque(false);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel lj = new JLabel(judul);
        lj.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lj.setForeground(C_TEXT);
        lj.setAlignmentX(LEFT_ALIGNMENT);

        JLabel ls = new JLabel(sub);
        ls.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ls.setForeground(C_TEXT_DIM);
        ls.setAlignmentX(LEFT_ALIGNMENT);

        h.add(lj);
        h.add(Box.createVerticalStrut(3));
        h.add(ls);
        return h;
    }

    static JPanel glassCard() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_GLASS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                GradientPaint shine = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 22),
                        0, 60, new Color(255, 255, 255, 0));
                g2.setPaint(shine);
                g2.fillRoundRect(0, 0, getWidth(), 60, 16, 16);
                g2.setColor(C_GLASS_BD);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    static JTextField glassField() {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus()
                        ? new Color(255, 255, 255, 24)
                        : new Color(255, 255, 255, 12));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(hasFocus()
                        ? new Color(99, 179, 237, 160)
                        : new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(hasFocus() ? 1.6f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(C_TEXT);
        tf.setCaretColor(C_ACCENT);
        tf.setBorder(new EmptyBorder(8, 11, 8, 11));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.repaint(); }
            @Override public void focusLost(FocusEvent e)   { tf.repaint(); }
        });
        return tf;
    }

    static JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(160, 185, 230));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    static JButton colorBtn(String text, Color color, ActionListener al) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int alpha = getModel().isPressed() ? 160
                        : getModel().isRollover() ? 130 : 90;
                g2.setColor(new Color(
                        color.getRed(), color.getGreen(),
                        color.getBlue(), alpha));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(
                        color.getRed(), color.getGreen(),
                        color.getBlue(), 160));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(2, 2, getWidth() - 4, (getHeight() - 4) / 2, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(al);
        return btn;
    }

static void styleCombo(JComboBox<?> cb) {
    cb.setBackground(new Color(15, 25, 55));
    cb.setForeground(C_TEXT);
    cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                    new Color(255, 255, 255, 40), 1, true),
            new EmptyBorder(4, 8, 4, 8)));

    // Fix: paksa warna teks di selected item yang tampil di kotak
    cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton("▾") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(15, 25, 55));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(C_TEXT_DIM);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("▾",
                            (getWidth()  - fm.stringWidth("▾")) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setOpaque(true);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            return btn;
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Paksa background & foreground saat render teks terpilih
            ListCellRenderer renderer = comboBox.getRenderer();
            Component c = renderer.getListCellRendererComponent(
                    new JList<>(), comboBox.getSelectedItem(),
                    -1, false, false);
            c.setFont(comboBox.getFont());
            c.setForeground(C_TEXT);           // teks putih
            c.setBackground(new Color(15, 25, 55)); // bg gelap
            if (c instanceof JComponent) {
                ((JComponent) c).setBorder(new EmptyBorder(0, 4, 0, 0));
            }
            javax.swing.CellRendererPane pane =
                    new javax.swing.CellRendererPane();
            pane.paintComponent(g, c, comboBox,
                    bounds.x, bounds.y,
                    bounds.width, bounds.height, true);
        }

        @Override
        public void paintCurrentValueBackground(
                Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(new Color(15, 25, 55));
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    });

    cb.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object val, int idx,
                boolean sel, boolean foc) {
            super.getListCellRendererComponent(
                    list, val, idx, sel, foc);
            setBackground(sel
                    ? new Color(50, 90, 180)
                    : new Color(12, 22, 50));
            setForeground(C_TEXT);   // teks selalu putih
            setBorder(new EmptyBorder(7, 10, 7, 10));
            return this;
        }
    });

    cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    cb.setAlignmentX(LEFT_ALIGNMENT);
}

    static JTable glassTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setOpaque(false);
        t.setBackground(new Color(0, 0, 0, 0));
        t.setForeground(C_TEXT);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 2));
        t.setSelectionBackground(new Color(99, 179, 237, 55));
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);

        JTableHeader hdr = t.getTableHeader();
        hdr.setOpaque(false);
        hdr.setBackground(new Color(255, 255, 255, 12));
        hdr.setForeground(C_ACCENT);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hdr.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, new Color(255, 255, 255, 25)));
        hdr.setPreferredSize(new Dimension(0, 38));

        t.setDefaultRenderer(Object.class,
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        tbl, val, sel, foc, row, col);
                if (sel) {
                    setBackground(new Color(99, 179, 237, 55));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0
                            ? new Color(255, 255, 255, 7)
                            : new Color(0, 0, 0, 0));
                    setForeground(C_TEXT);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                return this;
            }
        });
        return t;
    }

    static JScrollPane glassScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(new Color(0, 0, 0, 0));
        sp.setBorder(BorderFactory.createLineBorder(
                new Color(255, 255, 255, 25), 1, true));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 6));
        return sp;
    }

    static DefaultTableCellRenderer rupiahRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel,
                    boolean foc, int row, int col) {
                super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                setForeground(sel ? Color.WHITE : C_WARNING);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBackground(sel
                        ? new Color(99, 179, 237, 55)
                        : (row % 2 == 0
                                ? new Color(255, 255, 255, 7)
                                : new Color(0, 0, 0, 0)));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(RIGHT);
                return this;
            }
        };
    }
}