package com.mrh.buscharter.service;

import com.mrh.buscharter.model.enums.StatusBooking;
import net.jqwik.api.*;

import java.util.*;

/**
 * Property-based test untuk State Machine Booking.
 * 
 * **Property 2: State Machine Booking Valid**
 * **Validates: Requirements 10.2, 10.3, 10.4, 10.5, 10.6, 10.7**
 * 
 * State Machine:
 * DRAFT → QUOTATION_SENT → DP_DITERIMA → LUNAS → SELESAI
 *              ↓              ↓
 *            BATAL          BATAL
 */
public class StateMachinePropertyTest {

    // Valid transitions map
    private static final Map<StatusBooking, Set<StatusBooking>> VALID_TRANSITIONS = new EnumMap<>(StatusBooking.class);
    
    static {
        VALID_TRANSITIONS.put(StatusBooking.DRAFT, Set.of(StatusBooking.QUOTATION_SENT));
        VALID_TRANSITIONS.put(StatusBooking.QUOTATION_SENT, Set.of(StatusBooking.DP_DITERIMA, StatusBooking.BATAL));
        VALID_TRANSITIONS.put(StatusBooking.DP_DITERIMA, Set.of(StatusBooking.LUNAS, StatusBooking.BATAL));
        VALID_TRANSITIONS.put(StatusBooking.LUNAS, Set.of(StatusBooking.SELESAI));
        VALID_TRANSITIONS.put(StatusBooking.SELESAI, Set.of()); // Final state
        VALID_TRANSITIONS.put(StatusBooking.BATAL, Set.of()); // Final state
    }

    /**
     * Property: Hanya transisi yang valid yang diizinkan.
     * 
     * For any status S and target status T,
     * bisaTransisiKe(T) should return true if and only if T is in VALID_TRANSITIONS[S].
     */
    @Property(tries = 100)
    void hanyaTransisiValidYangDiizinkan(
            @ForAll StatusBooking statusAwal,
            @ForAll StatusBooking statusTujuan) {
        
        boolean expected = VALID_TRANSITIONS.get(statusAwal).contains(statusTujuan);
        boolean actual = statusAwal.bisaTransisiKe(statusTujuan);
        
        assert expected == actual 
            : String.format("Transisi %s → %s: expected %s, got %s", 
                statusAwal, statusTujuan, expected, actual);
    }

    /**
     * Property: DRAFT hanya bisa ke QUOTATION_SENT.
     */
    @Property(tries = 50)
    void draftHanyaBisaKeQuotationSent(@ForAll StatusBooking statusTujuan) {
        boolean canTransition = StatusBooking.DRAFT.bisaTransisiKe(statusTujuan);
        
        if (statusTujuan == StatusBooking.QUOTATION_SENT) {
            assert canTransition : "DRAFT harus bisa ke QUOTATION_SENT";
        } else {
            assert !canTransition : "DRAFT tidak boleh ke " + statusTujuan;
        }
    }

    /**
     * Property: Tidak bisa loncat dari DRAFT ke LUNAS.
     */
    @Property
    void tidakBisaLoncatDraftKeLunas() {
        assert !StatusBooking.DRAFT.bisaTransisiKe(StatusBooking.LUNAS) 
            : "Tidak boleh loncat dari DRAFT ke LUNAS";
        assert !StatusBooking.DRAFT.bisaTransisiKe(StatusBooking.DP_DITERIMA) 
            : "Tidak boleh loncat dari DRAFT ke DP_DITERIMA";
        assert !StatusBooking.DRAFT.bisaTransisiKe(StatusBooking.SELESAI) 
            : "Tidak boleh loncat dari DRAFT ke SELESAI";
    }

    /**
     * Property: Status final (SELESAI, BATAL) tidak bisa berubah.
     */
    @Property(tries = 50)
    void statusFinalTidakBisaBerubah(@ForAll StatusBooking statusTujuan) {
        // SELESAI tidak bisa ke mana-mana
        assert !StatusBooking.SELESAI.bisaTransisiKe(statusTujuan) 
            : "SELESAI tidak boleh berubah ke " + statusTujuan;
        
        // BATAL tidak bisa ke mana-mana
        assert !StatusBooking.BATAL.bisaTransisiKe(statusTujuan) 
            : "BATAL tidak boleh berubah ke " + statusTujuan;
    }

    /**
     * Property: BATAL hanya bisa dari QUOTATION_SENT atau DP_DITERIMA.
     */
    @Property(tries = 50)
    void batalHanyaDariQuotationAtauDp(@ForAll StatusBooking statusAwal) {
        boolean canCancel = statusAwal.bisaTransisiKe(StatusBooking.BATAL);
        
        if (statusAwal == StatusBooking.QUOTATION_SENT || statusAwal == StatusBooking.DP_DITERIMA) {
            assert canCancel : statusAwal + " harus bisa ke BATAL";
        } else {
            assert !canCancel : statusAwal + " tidak boleh ke BATAL";
        }
    }

    /**
     * Property: Path valid dari DRAFT ke SELESAI.
     * 
     * The only valid path is: DRAFT → QUOTATION_SENT → DP_DITERIMA → LUNAS → SELESAI
     */
    @Property
    void pathValidDariDraftKeSelesai() {
        // Simulasi path lengkap
        StatusBooking current = StatusBooking.DRAFT;
        
        // Step 1: DRAFT → QUOTATION_SENT
        assert current.bisaTransisiKe(StatusBooking.QUOTATION_SENT);
        current = StatusBooking.QUOTATION_SENT;
        
        // Step 2: QUOTATION_SENT → DP_DITERIMA
        assert current.bisaTransisiKe(StatusBooking.DP_DITERIMA);
        current = StatusBooking.DP_DITERIMA;
        
        // Step 3: DP_DITERIMA → LUNAS
        assert current.bisaTransisiKe(StatusBooking.LUNAS);
        current = StatusBooking.LUNAS;
        
        // Step 4: LUNAS → SELESAI
        assert current.bisaTransisiKe(StatusBooking.SELESAI);
        current = StatusBooking.SELESAI;
        
        // Final: tidak bisa ke mana-mana lagi
        for (StatusBooking s : StatusBooking.values()) {
            assert !current.bisaTransisiKe(s) : "SELESAI tidak boleh berubah";
        }
    }

    /**
     * Property: Setiap status non-final memiliki minimal 1 transisi valid.
     */
    @Property
    void statusNonFinalPunyaTransisi() {
        for (StatusBooking status : StatusBooking.values()) {
            if (status != StatusBooking.SELESAI && status != StatusBooking.BATAL) {
                boolean hasValidTransition = false;
                for (StatusBooking target : StatusBooking.values()) {
                    if (status.bisaTransisiKe(target)) {
                        hasValidTransition = true;
                        break;
                    }
                }
                assert hasValidTransition 
                    : status + " harus punya minimal 1 transisi valid";
            }
        }
    }

    /**
     * Property: Transisi ke diri sendiri tidak diizinkan.
     */
    @Property(tries = 50)
    void transisiKeDiriSendiriTidakDiizinkan(@ForAll StatusBooking status) {
        assert !status.bisaTransisiKe(status) 
            : status + " tidak boleh transisi ke diri sendiri";
    }

    /**
     * Property: Random walk dari DRAFT selalu berakhir di SELESAI atau BATAL.
     * 
     * Generator menghasilkan path yang LENGKAP sampai final state.
     */
    @Property(tries = 100)
    void randomWalkBerakhirDiFinalState(@ForAll("randomPathGenerator") List<StatusBooking> path) {
        if (path.isEmpty()) return;
        
        StatusBooking finalStatus = path.get(path.size() - 1);
        
        // Path harus berakhir di status final
        boolean isFinal = finalStatus == StatusBooking.SELESAI || finalStatus == StatusBooking.BATAL;
        
        assert isFinal : "Random walk harus berakhir di status final, got: " + finalStatus;
    }

    // ==================== Generators ====================

    @Provide
    Arbitrary<List<StatusBooking>> randomPathGenerator() {
        // Generate pilihan untuk setiap langkah: 0 = pilih opsi pertama, 1 = pilih opsi kedua (jika ada)
        return Arbitraries.integers().between(0, 1).list().ofMinSize(1).ofMaxSize(10)
            .map(choices -> {
                List<StatusBooking> path = new ArrayList<>();
                StatusBooking current = StatusBooking.DRAFT;
                path.add(current);
                
                int choiceIndex = 0;
                while (current != StatusBooking.SELESAI && current != StatusBooking.BATAL) {
                    Set<StatusBooking> validTargets = VALID_TRANSITIONS.get(current);
                    if (validTargets.isEmpty()) break;
                    
                    List<StatusBooking> targetList = new ArrayList<>(validTargets);
                    // Gunakan choice dari generator, modulo jumlah opsi
                    int choice = choiceIndex < choices.size() ? choices.get(choiceIndex) : 0;
                    current = targetList.get(choice % targetList.size());
                    path.add(current);
                    choiceIndex++;
                }
                
                return path;
            });
    }
}
