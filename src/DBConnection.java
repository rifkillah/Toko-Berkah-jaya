import java.sql.*;

/**
 * DBConnection.java
 * Kelas koneksi ke database MySQL menggunakan JDBC.
 * Toko Berkah Jaya - Sistem Informasi Penjualan
 */
public class DBConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "db_berkah_jaya";
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // sesuaikan password MySQL Anda

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true";

    /**
     * Mendapatkan koneksi ke database.
     * @return objek Connection
     * @throws SQLException jika koneksi gagal
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL tidak ditemukan! Pastikan mysql-connector-java.jar sudah ditambahkan ke project.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Menutup koneksi, statement, dan resultset secara aman.
     */
    public static void close(Connection con, Statement st, ResultSet rs) {
        try { if (rs  != null) rs.close();  } catch (SQLException ignored) {}
        try { if (st  != null) st.close();  } catch (SQLException ignored) {}
        try { if (con != null) con.close(); } catch (SQLException ignored) {}
    }

    /**
     * Test koneksi.
     * @return true jika berhasil
     */
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}