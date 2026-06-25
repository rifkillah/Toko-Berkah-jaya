/**
 * Produk.java
 * Class model untuk merepresentasikan sebuah produk.
 * Menerapkan konsep OOP: Encapsulation.
 *
 * UTS Pemrograman II - Teknik Informatika S1
 * Universitas Pamulang - Tahun Akademik 2025/2026
 */
public class Produk {

    // Atribut private (Encapsulation)
    private String id;
    private String nama;
    private String kategori;
    private double harga;
    private int stok;

    /**
     * Constructor untuk membuat objek Produk.
     * @throws IllegalArgumentException jika harga atau stok negatif
     */
    public Produk(String id, String nama, String kategori, double harga, int stok) {
        if (harga < 0) throw new IllegalArgumentException("Harga tidak boleh negatif!");
        if (stok < 0)  throw new IllegalArgumentException("Stok tidak boleh negatif!");
        this.id       = id;
        this.nama     = nama;
        this.kategori = kategori;
        this.harga    = harga;
        this.stok     = stok;
    }

    // Getter & Setter (Encapsulation)
    public String getId()       { return id; }

    public String getNama()     { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public double getHarga()    { return harga; }
    public void setHarga(double harga) {
        if (harga < 0) throw new IllegalArgumentException("Harga tidak boleh negatif!");
        this.harga = harga;
    }

    public int getStok()        { return stok; }
    public void setStok(int stok) {
        if (stok < 0) throw new IllegalArgumentException("Stok tidak boleh negatif!");
        this.stok = stok;
    }

    @Override
    public String toString() {
        return String.format("Produk{id='%s', nama='%s', kategori='%s', harga=%.2f, stok=%d}",
                id, nama, kategori, harga, stok);
    }
}