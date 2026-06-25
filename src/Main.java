/**
 * Main.java
 * Entry point aplikasi Manajemen Produk
 * UTS Pemrograman II - Teknik Informatika S1
 * Universitas Pamulang - Tahun Akademik 2025/2026
 */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Memanggil LoginFrame, bukan MainFrame
            new LoginFrame().setVisible(true); 
        });
    }
}