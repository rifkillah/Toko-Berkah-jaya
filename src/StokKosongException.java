/**
 * StokKosongException.java
 * Custom Exception untuk menangani error saat menghapus produk dengan stok kosong.
 * Sesuai instruksi UTS poin (e): Exception Handling.
 *
 * UTS Pemrograman II - Teknik Informatika S1
 * Universitas Pamulang - Tahun Akademik 2025/2026
 */
public class StokKosongException extends Exception {

    private String namaProduk;

    public StokKosongException() {
        super("Error: Stok produk kosong! Tidak dapat menghapus produk.");
    }

    public StokKosongException(String namaProduk) {
        super("Error: Stok produk '" + namaProduk + "' kosong! Tidak dapat menghapus dari gudang.");
        this.namaProduk = namaProduk;
    }

    public String getNamaProduk() {
        return namaProduk;
    }
}