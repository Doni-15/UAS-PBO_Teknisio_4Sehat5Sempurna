# Teknisio Backend

Backend service untuk **Teknisio: Solusi Servis Anda**.

Backend ini menjadi server utama untuk aplikasi **JavaFX Desktop** dan client lain yang memakai REST API. Seluruh komunikasi client ke backend menggunakan **HTTP REST API** dengan format **JSON**.

---

## Status Singkat Project

```text
Status backend saat ini: MVP core workflow stable
Database aktif: H2 file-based database
API test terakhir: 382 passed, 0 failed
```

Fitur backend yang sudah stabil:

```text
✅ Auth dasar: register, login, profile
✅ Update profile user login
✅ Role-based access CUSTOMER / TECHNICIAN / ADMIN
✅ Device category public API
✅ Technician device category / skill API
✅ Customer technician discovery
✅ Customer service request create/list/detail/cancel
✅ Technician service request list/detail/accept/reject/start/complete
✅ Status transition divalidasi database trigger
✅ Status history dibuat otomatis database trigger
✅ Status history read API untuk customer dan technician
✅ Customer create review setelah request selesai
✅ Duplicate review protection
✅ Ownership protection
✅ Global ApiResponse untuk success dan error
✅ Strict API regression test: 382 passed, 0 failed
```

Fitur yang belum menjadi prioritas awal MVP:

```text
⏳ Technician availability update
⏳ Technician profile/description update
⏳ List review technician
⏳ Refresh token dan logout server-side
⏳ Chat REST/WebSocket
⏳ Notification
⏳ Admin management
```

---

## Table of Contents

1. [Tujuan Backend](#1-tujuan-backend)
2. [Tech Stack](#2-tech-stack)
3. [Arsitektur Backend](#3-arsitektur-backend)
4. [Struktur Folder](#4-struktur-folder)
5. [Aturan Kontrak API](#5-aturan-kontrak-api)
6. [Environment Variables](#6-environment-variables)
7. [Menjalankan Project dari Nol](#7-menjalankan-project-dari-nol)
8. [Menjalankan Project Harian](#8-menjalankan-project-harian)
9. [Database dan Flyway](#9-database-dan-flyway)
10. [H2 Console](#10-h2-console)
11. [Endpoint Summary](#11-endpoint-summary)
12. [Response Format](#12-response-format)
13. [Security Contract](#13-security-contract)
14. [Testing](#14-testing)
15. [Gradle Commands](#15-gradle-commands)
16. [Git Rules](#16-git-rules)
17. [Troubleshooting](#17-troubleshooting)
18. [Catatan Developer](#18-catatan-developer)
19. [Commit Convention](#19-commit-convention)

---

# 1. Tujuan Backend

Backend Teknisio menangani proses utama aplikasi:

- Autentikasi customer dan technician.
- Manajemen data user dan technician profile.
- Master data kategori alat elektronik atau `deviceCategory`.
- Manajemen keahlian technician berdasarkan device category.
- Pencarian technician oleh customer.
- Pembuatan service request oleh customer.
- Proses accept, reject, start, dan complete request oleh technician.
- Tracking status layanan melalui status history.
- Review technician setelah request selesai.
- Persiapan chat, notifikasi, dan admin untuk fase berikutnya.

Untuk MVP desktop, customer **tidak wajib memakai GPS**. Customer mengisi alamat secara manual melalui field:

```text
address
addressDetail
```

Alur MVP utama:

```text
Customer register/login
↓
Customer melihat device category
↓
Customer memilih device category
↓
Customer melihat technician yang mendukung kategori tersebut
↓
Customer membuat service request
↓
Technician accept/reject
↓
Technician start
↓
Technician complete
↓
Customer memberi review
```

---

# 2. Tech Stack

| Komponen | Teknologi |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.14 |
| Build Tool | Gradle Kotlin DSL |
| Database | H2 Database file-based |
| Migration | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Token | JWT / JJWT 0.12.6 |
| Validation | Spring Validation / Jakarta Validation |
| Monitoring | Spring Boot Actuator |
| Local DB Console | H2 Console |
| Realtime planned | Spring WebSocket |
| Boilerplate helper | Lombok |

---

# 3. Arsitektur Backend

Backend memakai pendekatan:

```text
Spring MVC + Layered Architecture
```

Alur request:

```text
JavaFX Desktop / Client lain
        ↓
Controller
        ↓
Service
        ↓
Repository
        ↓
Entity
        ↓
H2 Database
```

Tanggung jawab tiap layer:

| Layer | Tanggung Jawab |
|---|---|
| Controller | Menerima request, validasi DTO, dan mengembalikan response API |
| Service | Business logic, validasi ownership, validasi status, dan mapping response |
| Repository | Query database menggunakan Spring Data JPA |
| Entity | Mapping tabel database |
| DTO Request | Kontrak input dari frontend |
| DTO Response | Kontrak output ke frontend |
| Security | JWT, authorization, current user |
| Common | Response wrapper, exception, utility |

Catatan penting:

- Controller tidak boleh berisi business logic berat.
- Entity tidak boleh langsung dikirim ke frontend.
- Semua response harus lewat DTO response.
- Semua endpoint resmi menggunakan field berbahasa Inggris.

---

# 4. Struktur Folder

Struktur utama backend:

```text
src/main/java/com/teknisio/
├── TeknisioBackendApplication.java
├── common/
│   ├── exception/
│   ├── response/
│   └── util/
├── config/
├── controllers/
├── database/
│   └── trigger/
├── dto/
│   ├── requests/
│   └── responses/
├── model/
│   ├── entities/
│   ├── entities/base/
│   └── enums/
├── repositories/
├── security/
├── services/
└── websocket/
```

Struktur resource:

```text
src/main/resources/
├── application.yml
├── db/
│   └── migration/
├── static/
└── templates/
```

Package root aplikasi adalah:

```java
package com.teknisio;
```

Karena `TeknisioBackendApplication.java` berada di `com.teknisio`, Spring Boot otomatis melakukan component scan ke semua sub-package seperti:

```text
com.teknisio.controllers
com.teknisio.services
com.teknisio.repositories
com.teknisio.model
com.teknisio.security
com.teknisio.common
com.teknisio.database.trigger
```

---

# 5. Aturan Kontrak API

Aturan kontrak API Teknisio:

- Backend hanya satu untuk semua client.
- Jangan membuat endpoint khusus desktop atau khusus Android.
- Endpoint resmi memakai bahasa Inggris.
- Field request dan response API memakai bahasa Inggris.
- Nama internal Java/database boleh tetap bahasa Indonesia jika sudah ada.
- Jangan expose entity JPA langsung ke response.
- Gunakan DTO request dan DTO response.
- Semua response memakai `ApiResponse<T>`.
- Semua error melewati global exception handler atau security handler.
- Untuk MVP, customer memilih `deviceCategoryIds`, bukan detail `jenis_layanan`.
- Untuk MVP, customer mengisi `issueDescription`, `address`, dan optional `addressDetail`.

Mapping istilah:

| Konsep | Nama API | Nama Internal |
|---|---|---|
| User | `user` | `User`, `users` |
| Customer | `customer` | `User` role `CUSTOMER` |
| Technician | `technician` | `User` role `TECHNICIAN`, `TeknisiProfile` |
| Kategori alat elektronik | `deviceCategory` | `KategoriLayanan` |
| Keahlian technician | `technicianDeviceCategory` | `TeknisiKategoriLayanan` |
| Permintaan layanan | `serviceRequest` | `PermintaanLayanan` |
| Kategori terpilih | `selectedDeviceCategories` | `PermintaanLayananKategori` |
| Deskripsi masalah | `issueDescription` | `deskripsiMasalah` |
| Alamat | `address` | `alamat` |
| Detail alamat | `addressDetail` | `detailAlamat` |
| Riwayat status | `statusHistory` | `RiwayatStatus` |
| Catatan technician | `technicianNote` | `catatanTeknisi` |
| Alasan batal | `cancelReason` | `alasanBatal` |
| Alasan tolak | `rejectReason` | `alasanTolak` |
| Review comment | `comment` | `comment` |
| Rating | `rating` | `rating` |

---

# 6. Environment Variables

File `.env` wajib dibuat di root project, sejajar dengan:

```text
build.gradle.kts
settings.gradle.kts
```

Contoh `.env.example`:

```env
APP_NAME=teknisio-backend
SERVER_PORT=8080

JWT_SECRET=change_me_minimum_32_characters
JWT_EXPIRATION_MS=86400000

H2_URL=jdbc:h2:file:./data/teknisio_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS public\\;SET SCHEMA public
H2_USER=teknisio_db
H2_PASSWORD=change_me
```

Catatan:

- Jangan commit `.env`.
- Commit hanya `.env.example`.
- `.env.example` tidak boleh berisi password asli.
- `JWT_SECRET` untuk development boleh tetap, tetapi jangan terlalu pendek.
- `JWT_EXPIRATION_MS=86400000` berarti access token berlaku 24 jam.
- H2 database lokal akan tersimpan di folder `data/`.

Generate JWT secret random:

```bash
openssl rand -base64 64
```

---

# 7. Menjalankan Project dari Nol

## 7.1 Install tools

Pastikan sudah terinstall:

- Java 17
- Git
- curl
- jq
- IDE seperti IntelliJ IDEA atau VS Code

Cek Java:

```bash
java -version
```

Cek Git:

```bash
git --version
```

Cek curl dan jq:

```bash
curl --version
jq --version
```

Di Arch Linux, dependency test bisa diinstall dengan:

```bash
sudo pacman -S curl jq
```

## 7.2 Clone repository

```bash
git clone <url-repository>
cd teknisio_backend
```

Sesuaikan nama folder jika repository berbeda.

## 7.3 Buat `.env`

```bash
cp .env.example .env
```

Lalu isi nilai environment sesuai laptop masing-masing.

Contoh untuk development lokal:

```env
APP_NAME=teknisio-backend
SERVER_PORT=8080

JWT_SECRET=teknisio_super_secret_key_minimum_32_characters
JWT_EXPIRATION_MS=86400000

H2_URL=jdbc:h2:file:./data/teknisio_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS public\\;SET SCHEMA public
H2_USER=teknisio_db
H2_PASSWORD=change_me
```

## 7.4 Jalankan backend

Linux/macOS:

```bash
./gradlew bootRun
```

Windows:

```bash
gradlew.bat bootRun
```

Backend berjalan di:

```text
http://localhost:8080
```

## 7.5 Cek health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

## 7.6 Cek kategori awal

```bash
curl http://localhost:8080/api/device-categories
```

Expected:

```text
Response 200
success true
data berisi kategori alat elektronik
```

---

# 8. Menjalankan Project Harian

Untuk developer yang sudah setup sebelumnya:

```bash
./gradlew bootRun
```

Build sebelum commit:

```bash
./gradlew clean build
```

Jalankan strict API regression test:

```bash
bash develop/api-strict-test.sh
```

---

# 9. Database dan Flyway

Project memakai **H2 Database** dan **Flyway** untuk database migration.

Lokasi migration:

```text
src/main/resources/db/migration
```

Migration yang digunakan:

```text
V1__create_enums.sql
V2__create_core_tables.sql
V3__create_indexes.sql
V4__create_triggers.sql
V5__seed_device_categories.sql
V6__create_reviews.sql
```

Fungsi tiap migration:

| File | Fungsi |
|---|---|
| `V1__create_enums.sql` | Membuat enum yang dipakai schema aplikasi |
| `V2__create_core_tables.sql` | Membuat tabel utama dan relasi FK |
| `V3__create_indexes.sql` | Membuat index untuk query utama |
| `V4__create_triggers.sql` | Membuat trigger H2 untuk `updated_at`, status flow, dan status history |
| `V5__seed_device_categories.sql` | Seed kategori alat elektronik default |
| `V6__create_reviews.sql` | Membuat tabel review |

Aturan penting:

- Flyway menjadi sumber utama pembentukan schema.
- `spring.jpa.hibernate.ddl-auto` menggunakan `none`.
- Jangan edit migration lama jika database tim sudah pernah menjalankannya.
- Untuk perubahan schema baru, buat file migration baru.
- Data seed harus idempotent.
- Folder `data/` adalah database lokal dan tidak boleh di-commit.

Cek history Flyway dari H2 Console:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Cek tabel:

```sql
SHOW TABLES;
```

Cek seed kategori:

```sql
SELECT * FROM kategori_layanan;
```

---

# 10. H2 Console

H2 Console digunakan untuk melihat isi database saat development.

URL:

```text
http://localhost:8080/h2-console
```

Pengaturan login:

```text
Saved Settings : Generic H2 (Embedded)
Driver Class   : org.h2.Driver
JDBC URL       : jdbc:h2:file:./data/teknisio_db
User Name      : sesuai H2_USER di .env
Password       : sesuai H2_PASSWORD di .env
```

Untuk koneksi yang sama dengan aplikasi, bisa memakai URL lengkap dari `.env` dengan catatan `INIT` di form H2 Console memakai satu backslash:

```text
jdbc:h2:file:./data/teknisio_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE;INIT=CREATE SCHEMA IF NOT EXISTS public\;SET SCHEMA public
```

Query validasi yang disarankan:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
SHOW TABLES;
SELECT * FROM kategori_layanan;
```

Catatan:

- H2 Console hanya untuk development.
- JavaFX tidak perlu mengakses H2 langsung.
- JavaFX cukup mengakses REST API backend.

---

# 11. Endpoint Summary

## 11.1 Public endpoints

```text
POST /api/auth/register/customer
POST /api/auth/register/technician
POST /api/auth/login
GET  /api/device-categories
GET  /api/device-categories/{deviceCategoryId}
GET  /actuator/health
GET  /actuator/info
GET  /h2-console/**              # development only
```

## 11.2 Authenticated endpoints

```text
GET /api/auth/profile
PUT /api/users/me
```

## 11.3 Technician device category endpoints

```text
POST   /api/technicians/device-categories
GET    /api/technicians/device-categories
DELETE /api/technicians/device-categories/{deviceCategoryId}
```

## 11.4 Customer technician discovery endpoints

```text
GET /api/customers/technicians?deviceCategoryId={deviceCategoryId}
GET /api/customers/technicians/{technicianProfileId}
```

Optional query:

```text
availabilityStatus=ONLINE|OFFLINE|BUSY|ON_LEAVE
sort=rating|totalJobs|name
```

## 11.5 Customer service request endpoints

```text
POST  /api/customers/service-requests
GET   /api/customers/service-requests
GET   /api/customers/service-requests/{serviceRequestId}
GET   /api/customers/service-requests/{serviceRequestId}/status-history
PATCH /api/customers/service-requests/{serviceRequestId}/cancel
POST  /api/customers/service-requests/{serviceRequestId}/review
```

Optional query:

```text
status=WAITING|ACCEPTED|ON_PROGRESS|COMPLETED|CANCELLED|REJECTED
```

## 11.6 Technician service request endpoints

```text
GET   /api/technicians/service-requests
GET   /api/technicians/service-requests/{serviceRequestId}
GET   /api/technicians/service-requests/{serviceRequestId}/status-history
PATCH /api/technicians/service-requests/{serviceRequestId}/accept
PATCH /api/technicians/service-requests/{serviceRequestId}/reject
PATCH /api/technicians/service-requests/{serviceRequestId}/start
PATCH /api/technicians/service-requests/{serviceRequestId}/complete
```

Optional query:

```text
status=WAITING|ACCEPTED|ON_PROGRESS|COMPLETED|CANCELLED|REJECTED
sort=latest|oldest
```

## 11.7 Next endpoints

```text
PATCH /api/technicians/availability
PUT   /api/technicians/profile
GET   /api/customers/technicians/{technicianProfileId}/reviews
```

## 11.8 Deferred endpoints

```text
POST  /api/auth/refresh
POST  /api/auth/logout
POST  /api/service-requests/{serviceRequestId}/messages
GET   /api/service-requests/{serviceRequestId}/messages
GET   /api/notifications
PATCH /api/notifications/{notificationId}/read
GET   /api/admin/users
GET   /api/admin/service-requests
```

---

# 12. Response Format

Semua response API memakai wrapper:

```java
ApiResponse<T>
```

## 12.1 Success response

```json
{
  "success": true,
  "message": "Success message",
  "data": {},
  "errors": null
}
```

## 12.2 Error response

```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "errors": null
}
```

## 12.3 Validation error

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": {
    "email": "Email is required",
    "password": "Password is required"
  }
}
```

Catatan:

- `success=true` hanya untuk HTTP 2xx.
- `success=false` untuk HTTP 4xx dan 5xx.
- `errors` digunakan terutama untuk validation error.
- Error biasa boleh memakai `errors: null`.

---

# 13. Security Contract

## 13.1 Public

Endpoint berikut tidak butuh token:

```text
POST /api/auth/register/customer
POST /api/auth/register/technician
POST /api/auth/login
GET  /api/device-categories
GET  /api/device-categories/{deviceCategoryId}
GET  /actuator/health
GET  /actuator/info
GET  /h2-console/**              # development only
```

## 13.2 Authenticated

Endpoint berikut butuh token valid:

```text
GET /api/auth/profile
PUT /api/users/me
```

## 13.3 Role based

```text
/api/customers/**    -> role CUSTOMER
/api/technicians/**  -> role TECHNICIAN
/api/admin/**        -> role ADMIN
```

Expected error:

| Kondisi | HTTP | Message |
|---|---:|---|
| Tanpa token | 401 | `Unauthorized` |
| Token role salah | 403 | `Forbidden` |
| Token invalid | 401 | `Unauthorized` |

---

# 14. Testing

## 14.1 Unit/integration test Gradle

```bash
./gradlew test
```

## 14.2 Build validation

```bash
./gradlew clean build
```

## 14.3 Health check

```bash
curl http://localhost:8080/actuator/health
```

## 14.4 Strict API regression test

Script utama regression API:

```bash
bash develop/api-strict-test.sh
```

Hasil terakhir:

```text
ALL STRICT API TESTS PASSED
Passed: 382
Failed: 0
```

Cakupan strict test:

```text
Health endpoint
Public endpoint
Register customer/technician
Login benar dan salah
JWT profile
Update profile
Validasi input 400
Unauthorized 401
Forbidden 403
Not found 404
Conflict 409
Role CUSTOMER / TECHNICIAN
Device category
Technician skill category
Customer cari teknisi
Service request flow
WAITING -> ACCEPTED -> ON_PROGRESS -> COMPLETED
Cancel flow
Reject flow
Review flow
Duplicate review
Ownership protection
Status history
```

Setiap endpoint baru wajib dites minimal:

- Success case.
- Tanpa token jika protected.
- Wrong role jika role-based.
- Invalid UUID jika memakai UUID.
- Not found jika data tidak ada.
- Ownership protection jika data bukan milik user login.
- Validation error jika body tidak valid.
- Invalid enum jika memakai enum.
- Invalid status transition jika update status.
- Response sukses memakai `ApiResponse`.
- Response error memakai `ApiResponse`.

---

# 15. Gradle Commands

Menjalankan backend:

```bash
./gradlew bootRun
```

Build:

```bash
./gradlew build
```

Clean build:

```bash
./gradlew clean build
```

Test:

```bash
./gradlew test
```

Clean:

```bash
./gradlew clean
```

---

# 16. Git Rules

## 16.1 Jangan commit

```text
.env
.env.*
!.env.example
data/
*.db
*.mv.db
*.lock.db
build/
.gradle/
.idea/
.vscode/
*.log
*.session.sql
application-local.yml
application-local.properties
```

## 16.2 Boleh dan perlu commit

```text
.env.example
build.gradle.kts
settings.gradle.kts
src/main/resources/application.yml
src/main/resources/db/migration/*.sql
src/main/java/**
src/test/java/**
develop/api-strict-test.sh
README.md
Roadmap.md
docs/**
```

Catatan penting:

- Jangan ignore semua `*.sh`, karena `develop/api-strict-test.sh` perlu di-commit.
- Jangan commit file database lokal H2 dari folder `data/`.
- Jangan commit `.env`.
- `.env.example` harus berisi placeholder, bukan password asli.
- Script SQL session lokal tetap jangan di-commit.

---

# 17. Troubleshooting

## 17.1 Backend tidak membaca `.env`

Cek `application.yml`:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

Pastikan `.env` berada di root project.

## 17.2 H2 Console masih Unauthorized

Pastikan `SecurityConfig` mengizinkan:

```text
/h2-console/**
```

Pastikan juga frame options diset:

```text
sameOrigin
```

Lalu restart backend:

```bash
./gradlew bootRun
```

## 17.3 H2 Console login gagal

Cek `.env`:

```bash
grep -n "H2_" .env
```

Gunakan value yang sama di H2 Console:

```text
JDBC URL  : jdbc:h2:file:./data/teknisio_db
User Name : sesuai H2_USER
Password  : sesuai H2_PASSWORD
```

Untuk URL lengkap dengan `INIT`, gunakan satu backslash di H2 Console:

```text
public\;SET SCHEMA public
```

## 17.4 Flyway checksum mismatch

Error:

```text
Validate failed: Migration checksum mismatch
```

Penyebab:

- Migration lama yang sudah pernah dijalankan berubah.

Solusi development lokal:

```bash
rm -rf data
./gradlew bootRun
```

Solusi tim:

- Jangan edit migration lama yang sudah dipakai bersama.
- Buat migration baru.

## 17.5 Hibernate schema error

Cek:

- `spring.jpa.hibernate.ddl-auto` harus `none`.
- Flyway migration harus sukses.
- H2 URL harus benar.
- Database lokal bisa direset dengan `rm -rf data` saat development.

## 17.6 API protected return 403 padahal sudah login

Cek role token:

```text
CUSTOMER hanya boleh /api/customers/**
TECHNICIAN hanya boleh /api/technicians/**
ADMIN hanya boleh /api/admin/**
```

## 17.7 Strict test gagal karena data lama

Script strict test membuat user unik setiap run, jadi normalnya aman dijalankan berkali-kali.

Jika ingin reset total saat development:

```bash
rm -rf data
./gradlew bootRun
```

Lalu jalankan ulang:

```bash
bash develop/api-strict-test.sh
```

---

# 18. Catatan Developer

- Jalankan backend dengan `./gradlew bootRun`.
- Jangan hardcode credential di source code.
- Jangan commit `.env`.
- Jangan commit folder `data/`.
- Semua field API harus English.
- Entity dan tabel internal boleh tetap memakai bahasa Indonesia.
- Jangan expose entity langsung ke response.
- Status history jangan di-insert manual dari Java service; database trigger sudah menangani.
- Jangan pakai `JenisLayanan` untuk flow MVP stable.
- Customer memilih `deviceCategoryIds`, bukan detail servis.
- JavaFX tidak perlu akses database langsung.
- JavaFX cukup akses REST API backend.
- Setiap fitur baru harus masuk strict API regression test.
- Commit per phase supaya mudah rollback.

---

# 19. Commit Convention

Contoh commit backend phase:

```bash
git add .
git commit -m "phase 5 add technician service request workflow"
```

Contoh commit test:

```bash
git add develop/api-strict-test.sh
git commit -m "test add strict backend api regression test"
```

Contoh commit dokumentasi:

```bash
git add README.md Roadmap.md
git commit -m "docs update backend readme and roadmap"
```
