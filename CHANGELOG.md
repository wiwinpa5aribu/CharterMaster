# Changelog

Semua perubahan penting pada proyek MRH Bus Charter Management System akan didokumentasikan di file ini.

Format berdasarkan [Keep a Changelog](https://keepachangelog.com/id-ID/1.0.0/).

---

## [Unreleased]

### Ditambahkan
- Implementasi UI Panel (Filter Tree, Data Table, Booking, Fleet, Finance, Master Data)

---

## [0.3.0] - 2025-12-24

### Ditambahkan
- **UI Framework**
  - `AppTheme.java` - Konfigurasi FlatLaf dengan tema Enterprise/Professional
  - `LoginDialog.java` - Form login dengan validasi (Email, Password, Kode Tenant)
  - `MainFrame.java` - Frame utama dengan tab navigation berdasarkan role user
  
- **Report Service**
  - `ReportService.java` - Service untuk generate PDF menggunakan JasperReports
  - `quotation_template.jrxml` - Template quotation dengan header tenant, detail trip, dan syarat pembayaran

### Diperbaiki
- Fix `StateMachinePropertyTest.randomWalkBerakhirDiFinalState` - Generator sekarang menghasilkan path yang selalu berakhir di status final
- Fix `AntiDoubleBookingPropertyTest.vehicleTidakBisaDoubleBooking` - Generator overlapping trips diperbaiki untuk selalu menghasilkan overlap yang valid
- Fix `AvailabilityEnginePropertyTest.rumusKetersediaanBenar` - Perbaikan ID vehicle unik untuk perhitungan ketersediaan yang akurat

### Diubah
- Tambah field `tipeBusDiminta` (TipeVehicle) ke model `Trip`
- Update `Main.java` untuk menggunakan komponen UI baru

---

## [0.2.0] - 2025-12-23

### Ditambahkan
- **Domain Events**
  - `EventBus.java` - Event bus sederhana dengan publish/subscribe
  - `DomainEvent.java` - Interface untuk domain events
  - `BookingConfirmedEvent.java` - Event saat booking dikonfirmasi
  - `PaymentReceivedEvent.java` - Event saat pembayaran diterima
  - `VehicleAssignedEvent.java` - Event saat armada di-assign

- **Property-Based Tests (jqwik)**
  - `AntiDoubleBookingPropertyTest.java` - Property 1: Anti-Double Booking
  - `StateMachinePropertyTest.java` - Property 2: State Machine Booking Valid
  - `GrandTotalPropertyTest.java` - Property 3: Kalkulasi Grand Total Konsisten
  - `StatusPembayaranPropertyTest.java` - Property 4: Status Pembayaran Otomatis
  - `TenantIsolationPropertyTest.java` - Property 5: Tenant Isolation
  - `AvailabilityEnginePropertyTest.java` - Property 6: Availability Engine Akurat
  - `DomainBoundaryPropertyTest.java` - Property 7: Domain Boundary Tidak Dilanggar

---

## [0.1.0] - 2025-12-22

### Ditambahkan
- **Project Setup**
  - `pom.xml` dengan dependencies: Swing, FlatLaf, Hibernate, PostgreSQL, HikariCP, JasperReports, jqwik
  - `DatabaseConfig.java` dengan HikariCP connection pool
  - `persistence.xml` untuk konfigurasi Hibernate

- **Model Layer (JPA Entities)**
  - `Tenant.java` - Data Perusahaan Otobus (PO)
  - `User.java` - Pengguna sistem
  - `Vehicle.java` - Data armada/bus
  - `Driver.java` - Data sopir
  - `Customer.java` - Data pelanggan
  - `Booking.java` - Data pesanan sewa
  - `Trip.java` - Detail perjalanan
  - `TripAssignment.java` - Penugasan armada ke trip
  - `BookingCharge.java` - Komponen harga booking
  - `Payment.java` - Data pembayaran

- **Enums**
  - `StatusBooking` - DRAFT, QUOTATION_SENT, DP_DITERIMA, LUNAS, SELESAI, BATAL
  - `TipeVehicle` - BIG_BUS, MEDIUM_BUS, HIACE, ELF, MPV
  - `StatusKepemilikan` - MILIK_SENDIRI, MITRA_VENDOR
  - `StatusAssignment` - TERJADWAL, BERJALAN, SELESAI, BATAL
  - `RoleUser` - ADMIN, SALES, OPS, SOPIR, KEUANGAN
  - `TipeCharge` - UTAMA, TAMBAHAN, DISKON
  - `TipeCustomer` - PERORANGAN, PERUSAHAAN, SEKOLAH, INSTANSI

- **Repository Layer**
  - `BaseRepository<T>` - Generic CRUD operations
  - Repository untuk setiap entity dengan query khusus
  - Availability Engine query dengan rumus irisan tanggal

- **Service Layer**
  - `AuthService.java` - Login, validasi password (SHA-256), role-based access
  - `SessionManager.java` - Singleton untuk session management
  - `FleetService.java` - Availability Engine, Assignment dengan validasi konflik
  - `BookingService.java` - Create booking, manual pricing, state machine
  - `FinanceService.java` - Pembayaran, auto-update status

---

## Legenda

- **Ditambahkan** - Fitur baru
- **Diubah** - Perubahan pada fitur yang ada
- **Diperbaiki** - Perbaikan bug
- **Dihapus** - Fitur yang dihapus
- **Keamanan** - Perbaikan keamanan
