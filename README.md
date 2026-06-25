# 🏪 Toko Berkah Jaya — Desktop Point of Sale (POS) Application

[![Java Version](https://img.shields.io/badge/Java-SE%208%20%2F%2011-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Database](https://img.shields.io/badge/MySQL-MariaDB-blue?style=for-the-badge&logo=mysql&logoColor=white)](https://localhost/)
[![UI Style](https://img.shields.io/badge/UI%20Style-Glassmorphism%20%2F%20Modern%20Dark-purple?style=for-the-badge&logo=java&logoColor=white)]()
[![Build Tool](https://img.shields.io/badge/Build%20Tool-Apache%20Ant-red?style=for-the-badge&logo=apache-ant&logoColor=white)](https://ant.apache.org/)

**Toko Berkah Jaya** adalah sistem kasir berbasis desktop pintar yang dirancang menggunakan ekosistem **Java Swing** dengan pendekatan visual modern **Glassmorphism (Dark Theme)**. Aplikasi ini mempermudah proses manajemen stok barang, pencatatan transaksi penjualan secara real-time, validasi data otomatis terintegrasi, hingga pelaporan berkala secara komprehensif.

---

## ✨ Fitur Utama

* 📊 **Dashboard Analytics & Real-Time Counter** — Menyajikan metrik ringkasan jumlah produk, pelanggan terdaftar, total transaksi, dan total pendapatan secara instan saat aplikasi dibuka.
* 📦 **Manajemen Master Data (CRUD)** — Pengelolaan data Barang, Kategori, dan Pelanggan secara dinamis dengan filter pencarian instan.
* 💸 **Sistem Transaksi Kasir Pintar** — Pembuatan nomor faktur penjualan otomatis (*Flexible Invoice Counter Generator*) berbasis data kronologis terakhir, kalkulasi subtotal instan, fitur keranjang belanja interaktif dengan aksi hapus item, dan proteksi batasan stok produk.
* 🛑 **Validasi Data Berlapis (Input Guard)** — Validasi input ketat guna mencegah ketidaksesuaian tipe data, seperti pemblokiran karakter abjad otomatis (*Key Filtering*) khusus untuk nomor telepon dan nominal pembayaran kasir.
* 📈 **Laporan Penjualan Terintegrasi** — Riwayat data transaksi yang rapi dilengkapi detail item terjual untuk efisiensi audit toko.
* 🔐 **Multi-Level Session Security** — Pembatasan hak akses sistem berdasarkan peran pengguna login (`Admin` dan `Petugas`).

---

## 🎨 Tampilan Antarmuka (UI/UX)

Aplikasi ini menggunakan konsep estetika **Glassmorphic Design Layer** dengan skema warna gelap (*Premium Dark Mode*) demi kenyamanan visual pengguna jangka panjang.

### 🖼️ Screenshot Aplikasi
| Halaman Utama & Sesi Transaksi | Manajemen Data Master |
| --- | --- |
| <img src="https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=600&q=80" width="100%" alt="Dashboard Preview"/> | <img src="https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=600&q=80" width="100%" alt="Master Data Preview"/> |

---

## 🛠️ Teknologi & Arsitektur Sistem

Aplikasi dibangun menggunakan arsitektur berlapis berorientasi objek (**Object-Oriented Architecture**):

* **Front-End / UI:** `Java Swing` Custom Component, `Graphics2D` rendering (efek semi-transparan, rounded corners, komponen *GlassCard*).
* **Back-End Core:** `Java SE (Standard Edition)`.
* **Database & Driver:** `MariaDB / MySQL`, `JDBC (Java Database Connectivity)`.
* **Transaction Integrity:** Menerapkan fitur `Database Transaction Control (Auto-Commit: False, Manual Commit & Rollback)` demi menjamin konsistensi data stok barang apabila terjadi kegagalan jaringan saat transaksi disimpan.

---

## 🚀 Panduan Instalasi & Menjalankan Aplikasi

### Prerequisites (Persyaratan Sistem)
1. **Java Development Kit (JDK)** versi 8 atau yang lebih baru telah terinstal di komputer Anda.
2. **XAMPP / Laragon** (untuk menjalankan server database MySQL/MariaDB).
3. **Apache Ant** (opsional, jika ingin melakukan kompilasi lewat CLI/Terminal).

### Langkah-Langkah Pengaturan

1. **Clone Repositori Ini:**
   ```bash
   git clone [https://github.com/username-kamu/TokoBerkahjaya_revisi.git](https://github.com/username-kamu/TokoBerkahjaya_revisi.git)
   cd TokoBerkahjaya_revisi
