package com.mrh.buscharter.service;

import com.mrh.buscharter.model.*;
import com.mrh.buscharter.model.enums.StatusBooking;
import com.mrh.buscharter.model.enums.TipeCharge;
import com.mrh.buscharter.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service untuk modul Booking.
 * Menangani pembuatan booking, pricing, dan state machine.
 */
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final TripRepository tripRepository;
    private final BookingChargeRepository bookingChargeRepository;
    private final PaymentRepository paymentRepository;

    public BookingService() {
        this.bookingRepository = new BookingRepository();
        this.customerRepository = new CustomerRepository();
        this.tripRepository = new TripRepository();
        this.bookingChargeRepository = new BookingChargeRepository();
        this.paymentRepository = new PaymentRepository();
    }

    // ==================== CREATE BOOKING ====================

    /**
     * Buat booking baru.
     * 
     * @param tenant Tenant yang membuat booking
     * @param customer Customer penyewa
     * @param salesPic User sales yang menangani
     * @param trips List trip dalam booking
     * @return Booking yang dibuat
     */
    public Booking buatBookingBaru(Tenant tenant, Customer customer, User salesPic, List<Trip> trips) {
        logger.info("Membuat booking baru untuk customer: {}", customer.getNama());
        
        // Validasi
        if (customer == null) {
            throw new IllegalArgumentException("Customer wajib diisi");
        }
        if (trips == null || trips.isEmpty()) {
            throw new IllegalArgumentException("Minimal harus ada 1 trip");
        }
        
        // Generate kode booking
        String kodeBooking = bookingRepository.generateKodeBooking(tenant.getId());
        
        // Buat booking
        Booking booking = new Booking(tenant, customer, kodeBooking);
        booking.setStatus(StatusBooking.DRAFT);
        booking.setSalesPic(salesPic);
        booking.setTanggalBooking(LocalDateTime.now());
        
        // Simpan booking dulu
        Booking savedBooking = bookingRepository.save(booking);
        
        // Tambahkan trips
        for (Trip trip : trips) {
            trip.setBooking(savedBooking);
            tripRepository.save(trip);
            savedBooking.getTrips().add(trip);
        }
        
        logger.info("Booking berhasil dibuat: {}", kodeBooking);
        return savedBooking;
    }

    /**
     * Buat booking dengan customer baru.
     */
    public Booking buatBookingDenganCustomerBaru(Tenant tenant, String namaCustomer, 
            String teleponCustomer, User salesPic, List<Trip> trips) {
        
        // Buat customer baru
        Customer customer = new Customer(tenant, namaCustomer, teleponCustomer);
        customer = customerRepository.save(customer);
        
        return buatBookingBaru(tenant, customer, salesPic, trips);
    }

    // ==================== MANUAL PRICING ====================

    /**
     * Tambah komponen harga ke booking.
     * 
     * @param bookingId ID booking
     * @param deskripsi Deskripsi biaya
     * @param kuantitas Jumlah
     * @param hargaSatuan Harga per unit
     * @param tipeCharge Tipe: UTAMA, TAMBAHAN, atau DISKON
     * @return BookingCharge yang dibuat
     */
    public BookingCharge tambahKomponenHarga(Long bookingId, String deskripsi, 
            Integer kuantitas, BigDecimal hargaSatuan, TipeCharge tipeCharge) {
        
        logger.info("Tambah komponen harga ke booking {}: {}", bookingId, deskripsi);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
        
        // Validasi
        if (deskripsi == null || deskripsi.isBlank()) {
            throw new IllegalArgumentException("Deskripsi wajib diisi");
        }
        if (kuantitas == null || kuantitas <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0");
        }
        if (hargaSatuan == null || hargaSatuan.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Harga satuan tidak boleh negatif");
        }
        
        BookingCharge charge = new BookingCharge(booking, deskripsi, kuantitas, hargaSatuan, tipeCharge);
        return bookingChargeRepository.save(charge);
    }

    /**
     * Hitung grand total booking.
     * Rumus: Sum(UTAMA + TAMBAHAN) - Sum(DISKON)
     */
    public BigDecimal hitungGrandTotal(Long bookingId) {
        return bookingChargeRepository.hitungGrandTotal(bookingId);
    }

    /**
     * Ambil semua komponen harga untuk booking.
     */
    public List<BookingCharge> getKomponenHarga(Long bookingId) {
        return bookingChargeRepository.findByBookingId(bookingId);
    }

    /**
     * Hapus komponen harga.
     */
    public void hapusKomponenHarga(Long chargeId) {
        bookingChargeRepository.deleteById(chargeId);
    }

    /**
     * Update komponen harga.
     */
    public BookingCharge updateKomponenHarga(Long chargeId, String deskripsi, 
            Integer kuantitas, BigDecimal hargaSatuan, TipeCharge tipeCharge) {
        
        BookingCharge charge = bookingChargeRepository.findById(chargeId)
            .orElseThrow(() -> new IllegalArgumentException("Charge tidak ditemukan: " + chargeId));
        
        charge.setDeskripsi(deskripsi);
        charge.setKuantitas(kuantitas);
        charge.setHargaSatuan(hargaSatuan);
        charge.setTipeCharge(tipeCharge);
        charge.hitungTotalHarga();
        
        return bookingChargeRepository.save(charge);
    }

    // ==================== STATE MACHINE ====================

    /**
     * Update status booking dengan validasi transisi.
     * 
     * State Machine:
     * DRAFT → QUOTATION_SENT → DP_DITERIMA → LUNAS → SELESAI
     *              ↓              ↓
     *            BATAL          BATAL
     * 
     * @param bookingId ID booking
     * @param statusBaru Status baru yang diinginkan
     * @return Booking yang diupdate
     * @throws IllegalStateException jika transisi tidak valid
     */
    public Booking updateStatusBooking(Long bookingId, StatusBooking statusBaru) {
        logger.info("Update status booking {} ke {}", bookingId, statusBaru);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
        
        StatusBooking statusLama = booking.getStatus();
        
        // Validasi transisi
        if (!statusLama.bisaTransisiKe(statusBaru)) {
            throw new IllegalStateException(
                String.format("Transisi tidak valid: %s → %s", statusLama, statusBaru));
        }
        
        // Validasi tambahan untuk LUNAS
        if (statusBaru == StatusBooking.LUNAS) {
            BigDecimal outstanding = hitungOutstanding(bookingId);
            if (outstanding.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalStateException(
                    "Tidak bisa set LUNAS, masih ada outstanding: " + outstanding);
            }
        }
        
        booking.setStatus(statusBaru);
        Booking updated = bookingRepository.save(booking);
        
        logger.info("Status booking {} berhasil diupdate: {} → {}", 
            bookingId, statusLama, statusBaru);
        
        return updated;
    }

    /**
     * Hitung outstanding (sisa yang harus dibayar).
     * Rumus: GrandTotal - Sum(Payments)
     */
    public BigDecimal hitungOutstanding(Long bookingId) {
        BigDecimal grandTotal = hitungGrandTotal(bookingId);
        BigDecimal totalPembayaran = paymentRepository.sumPembayaranByBookingId(bookingId);
        return grandTotal.subtract(totalPembayaran);
    }

    /**
     * Kirim quotation (update status ke QUOTATION_SENT).
     */
    public Booking kirimQuotation(Long bookingId) {
        return updateStatusBooking(bookingId, StatusBooking.QUOTATION_SENT);
    }

    /**
     * Batalkan booking.
     */
    public Booking batalkanBooking(Long bookingId) {
        return updateStatusBooking(bookingId, StatusBooking.BATAL);
    }

    /**
     * Selesaikan booking.
     */
    public Booking selesaikanBooking(Long bookingId) {
        return updateStatusBooking(bookingId, StatusBooking.SELESAI);
    }

    // ==================== QUERY ====================

    /**
     * Ambil booking dengan detail lengkap.
     */
    public Booking getBookingDetail(Long bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
    }

    /**
     * Ambil semua booking untuk tenant.
     */
    public List<Booking> getAllBooking(Long tenantId) {
        return bookingRepository.findAllByTenantId(tenantId);
    }

    /**
     * Ambil booking berdasarkan status.
     */
    public List<Booking> getBookingByStatus(Long tenantId, StatusBooking status) {
        return bookingRepository.findByStatusAndTenantId(status, tenantId);
    }

    /**
     * Ambil booking berdasarkan customer.
     */
    public List<Booking> getBookingByCustomer(Long tenantId, Long customerId) {
        return bookingRepository.findByCustomerIdAndTenantId(customerId, tenantId);
    }

    /**
     * Cari booking berdasarkan kode.
     */
    public Booking findByKodeBooking(Long tenantId, String kodeBooking) {
        return bookingRepository.findByKodeBookingAndTenantId(kodeBooking, tenantId)
            .orElse(null);
    }

    /**
     * Ambil booking yang perlu di-assign armada.
     */
    public List<Booking> getBookingPerluAssignment(Long tenantId) {
        return bookingRepository.findPerluAssignmentByTenantId(tenantId);
    }

    /**
     * Ambil booking yang belum lunas.
     */
    public List<Booking> getBookingBelumLunas(Long tenantId) {
        return bookingRepository.findBelumLunasByTenantId(tenantId);
    }

    // ==================== TRIP MANAGEMENT ====================

    /**
     * Tambah trip ke booking yang sudah ada.
     */
    public Trip tambahTrip(Long bookingId, LocalDateTime waktuMulai, LocalDateTime waktuSelesai,
                           String lokasiJemput, String lokasiTujuan, String deskripsiRute,
                           Integer estimasiPenumpang) {
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
        
        Trip trip = new Trip(booking, waktuMulai, waktuSelesai, lokasiJemput, lokasiTujuan);
        trip.setDeskripsiRute(deskripsiRute);
        trip.setEstimasiPenumpang(estimasiPenumpang);
        
        return tripRepository.save(trip);
    }

    /**
     * Ambil semua trip untuk booking.
     */
    public List<Trip> getTripsForBooking(Long bookingId) {
        return tripRepository.findByBookingId(bookingId);
    }
}
