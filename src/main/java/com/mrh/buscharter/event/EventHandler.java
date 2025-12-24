package com.mrh.buscharter.event;

/**
 * Interface untuk event handler.
 * 
 * @param <T> Tipe event yang di-handle
 */
@FunctionalInterface
public interface EventHandler<T extends DomainEvent> {
    
    /**
     * Handle event.
     * 
     * @param event Event yang akan di-handle
     */
    void handle(T event);
}
