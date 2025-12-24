package com.mrh.buscharter.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple in-memory EventBus untuk domain events.
 * Singleton pattern untuk akses global.
 */
public class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    
    private static EventBus instance;
    
    private final Map<Class<? extends DomainEvent>, List<EventHandler<?>>> handlers;

    private EventBus() {
        this.handlers = new ConcurrentHashMap<>();
    }

    /**
     * Mendapatkan instance singleton EventBus.
     */
    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    /**
     * Subscribe handler untuk event type tertentu.
     * 
     * @param eventType Class dari event
     * @param handler Handler yang akan dipanggil saat event di-publish
     */
    public <T extends DomainEvent> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
        logger.debug("Handler subscribed untuk event: {}", eventType.getSimpleName());
    }

    /**
     * Unsubscribe handler dari event type.
     */
    public <T extends DomainEvent> void unsubscribe(Class<T> eventType, EventHandler<T> handler) {
        List<EventHandler<?>> eventHandlers = handlers.get(eventType);
        if (eventHandlers != null) {
            eventHandlers.remove(handler);
            logger.debug("Handler unsubscribed dari event: {}", eventType.getSimpleName());
        }
    }

    /**
     * Publish event ke semua subscriber.
     * 
     * @param event Event yang akan di-publish
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void publish(T event) {
        logger.info("Publishing event: {} (id: {})", event.getEventName(), event.getEventId());
        
        List<EventHandler<?>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers == null || eventHandlers.isEmpty()) {
            logger.debug("Tidak ada handler untuk event: {}", event.getEventName());
            return;
        }
        
        for (EventHandler<?> handler : eventHandlers) {
            try {
                ((EventHandler<T>) handler).handle(event);
            } catch (Exception e) {
                logger.error("Error handling event {}: {}", event.getEventName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Clear semua handlers (untuk testing).
     */
    public void clearAllHandlers() {
        handlers.clear();
        logger.debug("Semua handlers di-clear");
    }

    /**
     * Reset instance (untuk testing).
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.clearAllHandlers();
        }
        instance = null;
    }

    /**
     * Cek apakah ada handler untuk event type tertentu.
     */
    public boolean hasHandlers(Class<? extends DomainEvent> eventType) {
        List<EventHandler<?>> eventHandlers = handlers.get(eventType);
        return eventHandlers != null && !eventHandlers.isEmpty();
    }

    /**
     * Mendapatkan jumlah handler untuk event type tertentu.
     */
    public int getHandlerCount(Class<? extends DomainEvent> eventType) {
        List<EventHandler<?>> eventHandlers = handlers.get(eventType);
        return eventHandlers != null ? eventHandlers.size() : 0;
    }
}
