# Teknisio — Aplikasi Desktop Layanan Perbaikan Elektronik

Teknisio adalah project UAS Pemrograman Berbasis Objek yang menggabungkan klien desktop JavaFX dan REST API Spring Boot. Aplikasi ini memodelkan alur pemesanan servis elektronik antara pelanggan dan teknisi, mulai dari autentikasi hingga penyelesaian permintaan layanan.

Project ini dibuat secara berkelompok oleh:

- Rifki Al Sauqy
- Doni Rivaldo Simamora
- Rionaldo Benedictus Purba
- Yehezkiel Gustav Setiawan Sitanggang
- M. Farhan Prasetyo

Repository ini merupakan coursework dan snapshot pengembangan, bukan layanan production.

## Fitur yang Tersedia

- Registrasi dan login untuk peran pelanggan dan teknisi.
- Otorisasi endpoint menggunakan Spring Security, JWT, dan role-based access control.
- Pengelolaan kategori perangkat dan keahlian teknisi.
- Siklus permintaan servis: dibuat, diterima/ditolak, dimulai, dan diselesaikan.
- Riwayat status, ulasan, chat, dan pembuatan nota teks.
- Klien JavaFX dengan pencarian, sorting, dan tampilan pelacakan lokasi.
- Migrasi database menggunakan Flyway.

## Teknologi

| Bagian | Teknologi |
| --- | --- |
| Desktop | Java 17, JavaFX 22, Maven, Gson |
| Backend | Java 17, Spring Boot 3.5, Gradle |
| Security | Spring Security, BCrypt, JWT |
| Database | H2 file-based, Flyway |
| Realtime | Spring WebSocket |

## Struktur Repository

```text
teknisio/           klien desktop JavaFX
teknisio_backend/   REST API Spring Boot
teknisio_document/  dokumentasi dan rancangan database
```

## Menjalankan Backend

Persyaratan: JDK 17. Gradle Wrapper sudah tersedia.

1. Masuk ke direktori backend dan buat konfigurasi lokal:

   ```bash
   cd teknisio_backend
   cp .env.example .env
   ```

2. Isi `JWT_SECRET` dengan nilai acak minimal 32 byte. Jangan commit `.env`.

3. Jalankan backend dengan konfigurasi default:

   ```bash
   ./gradlew bootRun
   ```

Konfigurasi default menonaktifkan H2 Console dan tidak memberikan akses publik ke `/h2-console/**`.

### H2 Console untuk Development

Console hanya tersedia melalui profile `development`:

```bash
SPRING_PROFILES_ACTIVE=development ./gradlew bootRun
```

Profile tersebut mengikat aplikasi ke `127.0.0.1`, mempertahankan `web-allow-others=false`, dan membuka console lokal di `http://127.0.0.1:8080/h2-console`. Jangan aktifkan profile ini pada deployment yang dapat diakses jaringan.

## Menjalankan Klien Desktop

Persyaratan: JDK 17 dan Maven.

```bash
cd teknisio
mvn clean compile
mvn javafx:run
```

Backend perlu aktif agar fitur yang memakai REST API dapat digunakan. Pencarian lokasi berbasis IP dan geocoding hanya memakai endpoint HTTPS; jika provider tidak tersedia aplikasi menggunakan nilai lokal pengganti tanpa menurunkan koneksi ke HTTP.

## Pengujian

Backend:

```bash
cd teknisio_backend
./gradlew test
```

Klien desktop belum memiliki automated test suite. Kompilasi dapat diverifikasi dengan:

```bash
cd teknisio
mvn clean compile
```

## Catatan Keamanan

- JWT secret wajib disediakan saat runtime dan tidak memiliki fallback source-controlled.
- H2 Console nonaktif pada profile default; pengecualian development dibatasi ke loopback.
- API memakai bearer token dan tidak bergantung pada session cookie. CSRF hanya diabaikan untuk path REST `/api/**`, bukan dinonaktifkan global untuk console.
- File `.env`, database lokal, build output, dan state IDE tidak seharusnya masuk version control.

## Status Project

Project mata kuliah telah mencapai alur utama yang dapat didemonstrasikan. Pengembangan berikutnya sebaiknya berfokus pada penambahan automated test dan penyederhanaan dokumentasi internal.
