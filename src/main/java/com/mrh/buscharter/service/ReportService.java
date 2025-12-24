package com.mrh.buscharter.service;

import com.mrh.buscharter.model.*;
import com.mrh.buscharter.repository.BookingRepository;
import com.mrh.buscharter.repository.BookingChargeRepository;
import com.mrh.buscharter.repository.TripRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service untuk generate laporan PDF menggunakan JasperReports.
 * Termasuk: Quotation, SPJ, Invoice, dll.
 */
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private static final String TEMPLATE_PATH = "/reports/quotation_template.jrxml";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final BookingRepository bookingRepository;
    private final BookingChargeRepository chargeRepository;
    private final TripRepository tripRepository;
    private final BookingService bookingService;
    
    // Syarat pembayaran default
    private static final String SYARAT_PEMBAYARAN_DEFAULT = 
        "1. Pembayaran DP minimal 50% dari total harga untuk konfirmasi booking.\n" +
        "2. Pelunasan dilakukan maksimal H-3 sebelum keberangkatan.\n" +
        "3. Pembatalan H-7 dikenakan biaya 25%, H-3 dikenakan biaya 50%.\n" +
        "4. Harga sudah termasuk BBM, driver, dan parkir tol.\n" +
        "5. Harga belum termasuk tiket masuk wisata dan makan.";
    
    public ReportService(BookingRepository bookingRepository, 
                         BookingChargeRepository chargeRepository,
                         TripRepository tripRepository,
                         BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.chargeRepository = chargeRepository;
        this.tripRepository = tripRepository;
        this.bookingService = bookingService;
    }
    
    /**
     * Generate Quotation PDF untuk booking tertentu.
     * 
     * @param bookingId ID booking
     * @param outputPath Path file output PDF
     * @return true jika berhasil generate
     */
    public boolean generateQuotationPDF(Long bookingId, String outputPath) {
        try {
            logger.info("Generating quotation PDF untuk booking ID: {}", bookingId);
            
            // Ambil data booking
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
            
            // Ambil trips
            List<Trip> trips = tripRepository.findByBookingId(bookingId);
            
            // Hitung grand total
            BigDecimal grandTotal = bookingService.hitungGrandTotal(bookingId);
            
            // Siapkan parameters
            Map<String, Object> parameters = buildParameters(booking, grandTotal);
            
            // Siapkan data source untuk trips
            List<Map<String, Object>> tripDataList = buildTripDataList(trips);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(tripDataList);
            
            // Compile dan generate report
            JasperReport jasperReport = compileReport();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            // Export ke PDF
            exportToPdf(jasperPrint, outputPath);
            
            logger.info("Quotation PDF berhasil digenerate: {}", outputPath);
            return true;
            
        } catch (Exception e) {
            logger.error("Gagal generate quotation PDF untuk booking {}: {}", bookingId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Generate Quotation PDF dan return sebagai byte array.
     * Berguna untuk preview atau kirim via email.
     */
    public byte[] generateQuotationPDFBytes(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan: " + bookingId));
            
            List<Trip> trips = tripRepository.findByBookingId(bookingId);
            BigDecimal grandTotal = bookingService.hitungGrandTotal(bookingId);
            
            Map<String, Object> parameters = buildParameters(booking, grandTotal);
            List<Map<String, Object>> tripDataList = buildTripDataList(trips);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(tripDataList);
            
            JasperReport jasperReport = compileReport();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            return JasperExportManager.exportReportToPdf(jasperPrint);
            
        } catch (Exception e) {
            logger.error("Gagal generate quotation PDF bytes: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal generate PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate nama file quotation berdasarkan kode booking.
     */
    public String generateNamaFileQuotation(Booking booking) {
        String kodeBooking = booking.getKodeBooking().replace("/", "-");
        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("Quotation_%s_%s.pdf", kodeBooking, tanggal);
    }
    
    // ==================== Private Helper Methods ====================
    
    private Map<String, Object> buildParameters(Booking booking, BigDecimal grandTotal) {
        Map<String, Object> params = new HashMap<>();
        
        Tenant tenant = booking.getTenant();
        Customer customer = booking.getCustomer();
        
        // Info Tenant
        params.put("NAMA_TENANT", tenant != null ? tenant.getNama() : "PT. MANDIRI RAJAWALI HUTAMA");
        params.put("ALAMAT_TENANT", tenant != null ? tenant.getAlamat() : "-");
        params.put("TELEPON_TENANT", tenant != null ? tenant.getTelepon() : "-");
        params.put("LOGO_PATH", tenant != null && tenant.getLogoUrl() != null ? tenant.getLogoUrl() : "");
        
        // Info Booking
        params.put("KODE_BOOKING", booking.getKodeBooking());
        params.put("TANGGAL_QUOTATION", LocalDateTime.now().format(DATE_FORMATTER));
        
        // Info Customer
        params.put("NAMA_CUSTOMER", customer != null ? customer.getNama() : "-");
        params.put("TELEPON_CUSTOMER", customer != null ? customer.getTelepon() : "-");
        params.put("EMAIL_CUSTOMER", customer != null && customer.getEmail() != null ? customer.getEmail() : "-");
        params.put("ALAMAT_CUSTOMER", customer != null && customer.getAlamat() != null ? customer.getAlamat() : "-");
        
        // Catatan dan Total
        params.put("CATATAN", booking.getCatatanInternal() != null ? booking.getCatatanInternal() : "");
        params.put("GRAND_TOTAL", grandTotal);
        params.put("SYARAT_PEMBAYARAN", SYARAT_PEMBAYARAN_DEFAULT);
        
        return params;
    }
    
    private List<Map<String, Object>> buildTripDataList(List<Trip> trips) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        int nomor = 1;
        
        for (Trip trip : trips) {
            Map<String, Object> row = new HashMap<>();
            row.put("nomorTrip", nomor++);
            row.put("tanggalMulai", formatDateTime(trip.getWaktuMulai()));
            row.put("tanggalSelesai", formatDateTime(trip.getWaktuSelesai()));
            row.put("lokasiJemput", trip.getLokasiJemput());
            row.put("lokasiTujuan", trip.getLokasiTujuan());
            row.put("tipeBus", trip.getTipeBusDiminta() != null ? 
                trip.getTipeBusDiminta().getDeskripsi() : "-");
            dataList.add(row);
        }
        
        return dataList;
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    private JasperReport compileReport() throws JRException {
        try (InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new JRException("Template tidak ditemukan: " + TEMPLATE_PATH);
            }
            return JasperCompileManager.compileReport(templateStream);
        } catch (IOException e) {
            throw new JRException("Gagal membaca template: " + e.getMessage(), e);
        }
    }
    
    private void exportToPdf(JasperPrint jasperPrint, String outputPath) throws JRException {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputPath));
        exporter.exportReport();
    }
}
