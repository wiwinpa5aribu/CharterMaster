package com.mrh.buscharter.event;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Property-based test untuk Domain Boundary.
 * 
 * **Property 7: Domain Boundary Tidak Dilanggar**
 * **Validates: Requirements 14.1, 14.2, 15.1, 15.2**
 * 
 * Memastikan bahwa komunikasi antar modul hanya melalui events.
 */
public class DomainBoundaryPropertyTest {

    /**
     * Property: Event yang di-publish akan diterima oleh semua subscriber.
     * 
     * For any event E and N subscribers,
     * publishing E should invoke all N handlers exactly once.
     */
    @Property(tries = 100)
    void eventDiterimaOlehSemuaSubscriber(
            @ForAll @IntRange(min = 1, max = 10) int jumlahSubscriber) {
        
        EventBus.resetInstance();
        EventBus eventBus = EventBus.getInstance();
        
        AtomicInteger handlerCount = new AtomicInteger(0);
        
        // Subscribe multiple handlers
        for (int i = 0; i < jumlahSubscriber; i++) {
            eventBus.subscribe(TestEvent.class, event -> handlerCount.incrementAndGet());
        }
        
        // Publish event
        TestEvent event = new TestEvent(1L, "Test");
        eventBus.publish(event);
        
        // Property: semua handler harus dipanggil
        assert handlerCount.get() == jumlahSubscriber 
            : String.format("Expected %d handlers called, got %d", jumlahSubscriber, handlerCount.get());
        
        EventBus.resetInstance();
    }

    /**
     * Property: Event tanpa subscriber tidak menyebabkan error.
     */
    @Property(tries = 50)
    void eventTanpaSubscriberTidakError(@ForAll @LongRange(min = 1, max = 1000) long tenantId) {
        EventBus.resetInstance();
        EventBus eventBus = EventBus.getInstance();
        
        // Publish tanpa subscriber - tidak boleh throw exception
        TestEvent event = new TestEvent(tenantId, "Test");
        
        try {
            eventBus.publish(event);
            assert true; // Berhasil tanpa error
        } catch (Exception e) {
            assert false : "Event tanpa subscriber tidak boleh throw exception";
        }
        
        EventBus.resetInstance();
    }

    /**
     * Property: Subscriber hanya menerima event yang di-subscribe.
     */
    @Property(tries = 50)
    void subscriberHanyaMenerimaEventYangDiSubscribe() {
        EventBus.resetInstance();
        EventBus eventBus = EventBus.getInstance();
        
        AtomicInteger testEventCount = new AtomicInteger(0);
        AtomicInteger otherEventCount = new AtomicInteger(0);
        
        // Subscribe ke TestEvent
        eventBus.subscribe(TestEvent.class, event -> testEventCount.incrementAndGet());
        
        // Subscribe ke OtherTestEvent
        eventBus.subscribe(OtherTestEvent.class, event -> otherEventCount.incrementAndGet());
        
        // Publish TestEvent
        eventBus.publish(new TestEvent(1L, "Test"));
        
        // Property: hanya TestEvent handler yang dipanggil
        assert testEventCount.get() == 1 : "TestEvent handler harus dipanggil sekali";
        assert otherEventCount.get() == 0 : "OtherTestEvent handler tidak boleh dipanggil";
        
        EventBus.resetInstance();
    }

    /**
     * Property: Unsubscribe menghentikan penerimaan event.
     */
    @Property(tries = 50)
    void unsubscribeMenghentikanPenerimaanEvent() {
        EventBus.resetInstance();
        EventBus eventBus = EventBus.getInstance();
        
        AtomicInteger handlerCount = new AtomicInteger(0);
        EventHandler<TestEvent> handler = event -> handlerCount.incrementAndGet();
        
        // Subscribe
        eventBus.subscribe(TestEvent.class, handler);
        
        // Publish pertama
        eventBus.publish(new TestEvent(1L, "Test1"));
        assert handlerCount.get() == 1 : "Handler harus dipanggil sekali";
        
        // Unsubscribe
        eventBus.unsubscribe(TestEvent.class, handler);
        
        // Publish kedua
        eventBus.publish(new TestEvent(1L, "Test2"));
        
        // Property: handler tidak dipanggil lagi setelah unsubscribe
        assert handlerCount.get() == 1 : "Handler tidak boleh dipanggil setelah unsubscribe";
        
        EventBus.resetInstance();
    }

    /**
     * Property: Event memiliki ID unik.
     */
    @Property(tries = 100)
    void eventMemilikiIdUnik(@ForAll @Size(min = 2, max = 20) List<@LongRange(min = 1, max = 100) Long> tenantIds) {
        Set<String> eventIds = new HashSet<>();
        
        for (Long tenantId : tenantIds) {
            TestEvent event = new TestEvent(tenantId, "Test");
            
            // Property: setiap event harus punya ID unik
            assert !eventIds.contains(event.getEventId()) 
                : "Event ID harus unik";
            
            eventIds.add(event.getEventId());
        }
    }

    /**
     * Property: Event menyimpan tenant ID dengan benar.
     */
    @Property(tries = 100)
    void eventMenyimpanTenantIdDenganBenar(@ForAll @LongRange(min = 1, max = 1000) long tenantId) {
        TestEvent event = new TestEvent(tenantId, "Test");
        
        // Property: tenant ID harus sama dengan yang diberikan
        assert event.getTenantId().equals(tenantId) 
            : "Tenant ID harus sama dengan yang diberikan";
    }

    /**
     * Property: Handler error tidak menghentikan handler lain.
     */
    @Property(tries = 50)
    void handlerErrorTidakMenghentikanHandlerLain() {
        EventBus.resetInstance();
        EventBus eventBus = EventBus.getInstance();
        
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Handler yang throw exception
        eventBus.subscribe(TestEvent.class, event -> {
            throw new RuntimeException("Test error");
        });
        
        // Handler yang sukses
        eventBus.subscribe(TestEvent.class, event -> successCount.incrementAndGet());
        
        // Publish event
        eventBus.publish(new TestEvent(1L, "Test"));
        
        // Property: handler sukses tetap dipanggil meskipun ada handler yang error
        assert successCount.get() == 1 
            : "Handler sukses harus tetap dipanggil meskipun ada handler error";
        
        EventBus.resetInstance();
    }

    /**
     * Property: Clear handlers menghapus semua subscriber.
     */
    @Property(tries = 50)
    void clearHandlersMenghapusSemuaSubscriber(
            @ForAll @IntRange(min = 1, max = 10) int jumlahSubscriber) {
        
        EventBus.resetInstance();
        EventBus eventBus = EventBus.getInstance();
        
        AtomicInteger handlerCount = new AtomicInteger(0);
        
        // Subscribe multiple handlers
        for (int i = 0; i < jumlahSubscriber; i++) {
            eventBus.subscribe(TestEvent.class, event -> handlerCount.incrementAndGet());
        }
        
        // Clear all handlers
        eventBus.clearAllHandlers();
        
        // Publish event
        eventBus.publish(new TestEvent(1L, "Test"));
        
        // Property: tidak ada handler yang dipanggil setelah clear
        assert handlerCount.get() == 0 
            : "Tidak ada handler yang boleh dipanggil setelah clear";
        
        EventBus.resetInstance();
    }

    // ==================== Test Events ====================

    public static class TestEvent extends DomainEvent {
        private final String data;

        public TestEvent(Long tenantId, String data) {
            super(tenantId);
            this.data = data;
        }

        @Override
        public String getEventName() {
            return "TestEvent";
        }

        public String getData() {
            return data;
        }
    }

    public static class OtherTestEvent extends DomainEvent {
        public OtherTestEvent(Long tenantId) {
            super(tenantId);
        }

        @Override
        public String getEventName() {
            return "OtherTestEvent";
        }
    }
}
