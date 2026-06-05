# Teknisio Backend - Strict API Test Plan

Dokumen ini adalah rencana pengujian API strict untuk backend Teknisio setelah migrasi PostgreSQL ke H2.

## Tujuan

Menilai keandalan backend dari sisi:

1. Server hidup dan database terkoneksi.
2. Format response konsisten.
3. Endpoint public bisa diakses tanpa token.
4. Endpoint protected menolak request tanpa token.
5. Role-based access control berjalan.
6. Validasi DTO berjalan.
7. Validasi bisnis service berjalan.
8. Flow status service request benar.
9. Ownership data aman.
10. Error case tidak membuat backend crash.

## Cara menjalankan

Dari root project backend:

```bash
mkdir -p develop
cp /path/to/teknisio_api_strict_test.sh develop/api-strict-test.sh
chmod +x develop/api-strict-test.sh
./gradlew bootRun
```

Di terminal kedua:

```bash
BASE_URL=http://localhost:8080 bash develop/api-strict-test.sh
```

## Dependency test

Script membutuhkan:

```bash
curl
jq
```

Di Arch Linux:

```bash
sudo pacman -S curl jq
```

## Endpoint yang diuji

### Health

```text
GET /actuator/health
```

Expected benar:

```json
{"status":"UP"}
```

Expected salah:

```text
Connection failed / HTTP bukan 200 / status bukan UP
```

### Device Categories

```text
GET /api/device-categories
GET /api/device-categories/{deviceCategoryId}
```

Success case:

- List kategori return 200.
- Response memakai ApiResponse.
- Data array tidak kosong.
- Detail kategori by UUID return 200.

Error case:

- Invalid UUID return 400.
- UUID tidak ada return 404.

### Auth

```text
POST /api/auth/register/customer
POST /api/auth/register/technician
POST /api/auth/login
GET  /api/auth/profile
PUT  /api/users/me
```

Success case:

- Register customer return 201.
- Register technician return 201.
- Login customer return 200.
- Login technician return 200.
- Token JWT ada.
- Role user benar.
- Profile bisa diambil dengan token.
- Update profile berhasil.

Error case:

- Register body kosong return 400.
- Email tidak valid return 400.
- Phone number tidak valid return 400.
- Password terlalu pendek return 400.
- Email/phone duplicate return 409.
- Login password salah return 401.
- Profile tanpa token return 401.
- Profile fake token return 401.
- Update profile body kosong return 400.
- Update profile phone invalid return 400.

### Security Role

Kontrak role:

```text
/api/customers/**   -> CUSTOMER only
/api/technicians/** -> TECHNICIAN only
/api/admin/**       -> ADMIN only
```

Success case:

- Customer token bisa akses customer endpoint.
- Technician token bisa akses technician endpoint.

Error case:

- Endpoint protected tanpa token return 401.
- Customer token ke technician endpoint return 403.
- Technician token ke customer endpoint return 403.

### Technician Device Categories

```text
GET    /api/technicians/device-categories
POST   /api/technicians/device-categories
DELETE /api/technicians/device-categories/{deviceCategoryId}
```

Success case:

- Technician bisa melihat skill kategori.
- Technician bisa menambahkan kategori aktif.
- Technician bisa menghapus kategori secara soft-disable.
- Technician bisa re-add kategori setelah delete.

Error case:

- Empty body return 400.
- Invalid category UUID return 400.
- Category tidak ada return 404.
- Duplicate active category return 409.
- Delete category yang tidak dimiliki return 404.

### Customer Technician Discovery

```text
GET /api/customers/technicians?deviceCategoryId={deviceCategoryId}
GET /api/customers/technicians/{technicianProfileId}
```

Success case:

- Customer bisa cari technician berdasarkan kategori.
- Filter availability ONLINE bisa jalan.
- Sort name/rating/totalJobs bisa jalan.
- Detail technician return supportedDeviceCategories.

Error case:

- Missing deviceCategoryId return 400.
- Invalid deviceCategoryId return 400.
- Category tidak ada return 404.
- Invalid availabilityStatus return 400.
- Invalid sort return 400.
- Detail invalid technicianProfileId return 400.
- Detail technician tidak ada return 404.
- Technician token ke customer endpoint return 403.

### Customer Service Request

```text
POST  /api/customers/service-requests
GET   /api/customers/service-requests
GET   /api/customers/service-requests/{serviceRequestId}
GET   /api/customers/service-requests/{serviceRequestId}/status-history
PATCH /api/customers/service-requests/{serviceRequestId}/cancel
POST  /api/customers/service-requests/{serviceRequestId}/review
```

Success case:

- Customer membuat service request return 201.
- Status awal WAITING.
- Customer list request return 200.
- Customer detail request return 200.
- Customer status history return 200.
- Customer cancel request WAITING return 200 dengan status CANCELLED.
- Customer review request COMPLETED return 201.

Error case:

- Tanpa token return 401.
- Technician token return 403.
- Empty body return 400.
- Invalid technician UUID return 400.
- Technician tidak ada return 404.
- Invalid category UUID return 400.
- Duplicate category id return 400.
- Technician tidak support kategori return 400.
- Technician sedang BUSY return 409.
- Invalid status filter return 400.
- Wrong owner return 404.
- Invalid service request UUID return 400.
- Cancel tanpa alasan return 400.
- Cancel request final return 409.
- Review sebelum COMPLETED return 409.
- Rating < 1 return 400.
- Rating > 5 return 400.
- Duplicate review return 409.

### Technician Service Request

```text
GET   /api/technicians/service-requests
GET   /api/technicians/service-requests/{serviceRequestId}
GET   /api/technicians/service-requests/{serviceRequestId}/status-history
PATCH /api/technicians/service-requests/{serviceRequestId}/accept
PATCH /api/technicians/service-requests/{serviceRequestId}/reject
PATCH /api/technicians/service-requests/{serviceRequestId}/start
PATCH /api/technicians/service-requests/{serviceRequestId}/complete
```

Success case:

- Technician list request return 200.
- Technician detail request miliknya return 200.
- WAITING -> ACCEPTED berhasil.
- ACCEPTED -> ON_PROGRESS berhasil.
- ON_PROGRESS -> COMPLETED berhasil.
- Reject WAITING berhasil.
- Status history return 200.

Error case:

- Tanpa token return 401.
- Customer token return 403.
- Wrong technician owner return 404.
- Invalid status filter return 400.
- Invalid sort return 400.
- Start sebelum accept return 409.
- Complete sebelum start return 409.
- Accept dua kali return 409.
- Complete finalCost negatif return 400.
- Complete finalCost kosong return 400.
- Complete dua kali return 409.
- Start request REJECTED return 409.
- Accept request CANCELLED return 409.

## Kriteria lulus

Strict test dinyatakan lulus jika script berakhir dengan:

```text
ALL STRICT API TESTS PASSED
Failed: 0
```

Jika ada gagal, perbaiki dari error pertama terlebih dahulu karena error awal bisa menyebabkan error turunan.

## Catatan penting

Script ini membuat data baru setiap kali dijalankan memakai email unik berbasis timestamp. Script tidak menghapus database dan tidak menghapus user yang dibuat. Untuk database lokal UAS, itu aman. Kalau ingin bersih, reset H2 secara manual hanya saat development:

```bash
rm -rf data
./gradlew bootRun
```

Jangan reset database jika data sudah dipakai untuk demo final.
