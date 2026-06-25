import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * StackProduk.java
 * Implementasi struktur data Stack menggunakan LinkedList untuk menyimpan Produk.
 * Stack bersifat LIFO (Last In, First Out).
 *
 * Fitur:
 *  - push()           : Tambah produk ke stack (poin a)
 *  - pop()            : Hapus produk teratas dari stack (poin a)
 *  - peek()           : Lihat produk teratas tanpa hapus (poin a)
 *  - searchByNama()   : Cari produk berdasarkan nama (poin d)
 *  - sortByHarga()    : Urutkan berdasarkan harga - Bubble Sort (poin c)
 *  - sortByKategori() : Urutkan berdasarkan kategori - Bubble Sort (poin c)
 *  - Logger           : Debug setiap operasi (poin f)
 *
 * UTS Pemrograman II - Teknik Informatika S1
 * Universitas Pamulang - Tahun Akademik 2025/2026
 */
public class StackProduk {

    // Logger untuk Debugging (poin f & catatan no.4)
    private static final Logger logger = Logger.getLogger(StackProduk.class.getName());

    // LinkedList sebagai backing structure Stack (catatan no.3)
    private LinkedList<Produk> stack;
    private int maxSize;

    public StackProduk(int maxSize) {
        this.stack   = new LinkedList<>();
        this.maxSize = maxSize;
        logger.info("StackProduk diinisialisasi dengan kapasitas: " + maxSize);
    }

    // ----------------------------------------------------------------
    // PUSH - Tambahkan produk ke atas stack
    // ----------------------------------------------------------------
    public void push(Produk produk) {
        if (isFull()) {
            logger.warning("PUSH GAGAL - Stack penuh! Produk: " + produk.getNama());
            throw new IllegalStateException("Gudang penuh! Kapasitas maksimal: " + maxSize);
        }
        stack.push(produk);
        logger.info("PUSH - Produk ditambahkan: " + produk + " | Total: " + stack.size());
    }

    // ----------------------------------------------------------------
    // POP - Ambil dan hapus produk teratas dari stack
    // ----------------------------------------------------------------
    public Produk pop() throws StokKosongException {
        if (isEmpty()) {
            logger.severe("POP GAGAL - Stack kosong!");
            throw new StokKosongException("Gudang");
        }
        Produk produk = stack.pop();
        logger.info("POP - Produk dihapus: " + produk + " | Sisa: " + stack.size());
        return produk;
    }

    // ----------------------------------------------------------------
    // PEEK - Lihat produk teratas tanpa menghapus
    // ----------------------------------------------------------------
    public Produk peek() throws StokKosongException {
        if (isEmpty()) {
            logger.warning("PEEK GAGAL - Stack kosong!");
            throw new StokKosongException("Gudang");
        }
        return stack.peek();
    }

    // ----------------------------------------------------------------
    // HAPUS produk tertentu berdasarkan ID
    // ----------------------------------------------------------------
    public Produk hapusProdukById(String id) throws StokKosongException {
        Produk target = null;
        for (Produk p : stack) {
            if (p.getId().equals(id)) {
                target = p;
                break;
            }
        }
        if (target == null) {
            logger.severe("HAPUS GAGAL - ID '" + id + "' tidak ditemukan.");
            throw new StokKosongException(id);
        }
        if (target.getStok() == 0) {
            logger.severe("HAPUS GAGAL - Stok '" + target.getNama() + "' = 0!");
            throw new StokKosongException(target.getNama());
        }
        stack.remove(target);
        logger.info("HAPUS - Produk dihapus: " + target);
        return target;
    }

    // ----------------------------------------------------------------
    // SEARCH - Cari produk berdasarkan nama (poin d)
    // ----------------------------------------------------------------
    public LinkedList<Produk> searchByNama(String keyword) {
        LinkedList<Produk> hasil = new LinkedList<>();
        String kw = keyword.toLowerCase().trim();
        for (Produk p : stack) {
            if (p.getNama().toLowerCase().contains(kw)) {
                hasil.add(p);
            }
        }
        logger.info("SEARCH - Keyword: '" + keyword + "' | Hasil: " + hasil.size());
        return hasil;
    }

    // ----------------------------------------------------------------
    // SORT by HARGA - Bubble Sort Ascending (poin c)
    // ----------------------------------------------------------------
    public LinkedList<Produk> sortByHarga() {
        LinkedList<Produk> sorted = new LinkedList<>(stack);
        int n = sorted.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (sorted.get(j).getHarga() > sorted.get(j + 1).getHarga()) {
                    Produk temp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, temp);
                }
            }
        }
        logger.info("SORT BY HARGA - Selesai. Total: " + sorted.size());
        return sorted;
    }

    // ----------------------------------------------------------------
    // SORT by KATEGORI - Bubble Sort A-Z (poin c)
    // ----------------------------------------------------------------
    public LinkedList<Produk> sortByKategori() {
        LinkedList<Produk> sorted = new LinkedList<>(stack);
        int n = sorted.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (sorted.get(j).getKategori()
                        .compareToIgnoreCase(sorted.get(j + 1).getKategori()) > 0) {
                    Produk temp = sorted.get(j);
                    sorted.set(j, sorted.get(j + 1));
                    sorted.set(j + 1, temp);
                }
            }
        }
        logger.info("SORT BY KATEGORI - Selesai. Total: " + sorted.size());
        return sorted;
    }

    // ----------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------
    public LinkedList<Produk> getAllProduk() { return new LinkedList<>(stack); }
    public boolean isEmpty()                 { return stack.isEmpty(); }
    public boolean isFull()                  { return stack.size() >= maxSize; }
    public int size()                        { return stack.size(); }
    public int getMaxSize()                  { return maxSize; }
}