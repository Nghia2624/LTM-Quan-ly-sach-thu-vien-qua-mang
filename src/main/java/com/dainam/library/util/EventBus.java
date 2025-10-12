package com.dainam.library.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Event Bus để đồng bộ dữ liệu real-time giữa các giao diện
 */
public class EventBus {
    
    private static final EventBus INSTANCE = new EventBus();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Consumer<Object>>> listeners = new ConcurrentHashMap<>();
    
    private EventBus() {}
    
    public static EventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * Đăng ký listener cho một event type
     */
    public void subscribe(String eventType, Consumer<Object> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }
    
    /**
     * Hủy đăng ký listener
     */
    public void unsubscribe(String eventType, Consumer<Object> listener) {
        CopyOnWriteArrayList<Consumer<Object>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }
    
    /**
     * Phát sự kiện đến tất cả listeners
     */
    public void publish(String eventType, Object data) {
        CopyOnWriteArrayList<Consumer<Object>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (Consumer<Object> listener : eventListeners) {
                try {
                    listener.accept(data);
                } catch (Exception e) {
                    LoggerUtil.error("Error in event listener: " + e.getMessage());
                }
            }
        }
    }
      /**
     * Event types constants
     */
    public static class Events {
        // Local events
        public static final String BOOK_ADDED = "BOOK_ADDED";
        public static final String BOOK_UPDATED = "BOOK_UPDATED";
        public static final String BOOK_DELETED = "BOOK_DELETED";
        public static final String BOOK_BORROWED = "BOOK_BORROWED";
        public static final String BOOK_RETURNED = "BOOK_RETURNED";
        public static final String USER_ADDED = "USER_ADDED";
        public static final String USER_UPDATED = "USER_UPDATED";
        public static final String USER_DELETED = "USER_DELETED";
        public static final String USER_STATUS_CHANGED = "USER_STATUS_CHANGED";
        public static final String BORROW_RECORD_UPDATED = "BORROW_RECORD_UPDATED";
        public static final String DATA_REFRESH = "DATA_REFRESH";
        
        // Network events for multi-user support
        public static final String NETWORK_DATA_CHANGE = "NETWORK_DATA_CHANGE";
        public static final String USER_CONNECTED = "USER_CONNECTED";
        public static final String USER_DISCONNECTED = "USER_DISCONNECTED";
        public static final String SERVER_MESSAGE = "SERVER_MESSAGE";
    }
}
