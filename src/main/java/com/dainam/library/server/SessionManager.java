package com.dainam.library.server;

import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.EventBus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.List;

/**
 * Quản lý session và broadcasting cho multiple clients
 */
public class SessionManager {
    
    private static SessionManager instance;
    private final Map<String, ClientSession> activeSessions;
    private final List<ClientHandler> connectedClients;
    private final EventBus eventBus;
    
    private SessionManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.connectedClients = new CopyOnWriteArrayList<>();
        this.eventBus = EventBus.getInstance();
        setupEventSubscriptions();
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Đăng ký client handler mới
     */
    public void registerClient(ClientHandler clientHandler) {
        connectedClients.add(clientHandler);
        LoggerUtil.info("Client registered. Total clients: " + connectedClients.size());
    }
    
    /**
     * Hủy đăng ký client handler
     */
    public void unregisterClient(ClientHandler clientHandler) {
        connectedClients.remove(clientHandler);
        // Remove associated sessions
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getClientHandler().equals(clientHandler));
        LoggerUtil.info("Client unregistered. Total clients: " + connectedClients.size());
    }
    
    /**
     * Tạo session mới cho user
     */
    public void createSession(String userId, String sessionId, ClientHandler clientHandler) {
        ClientSession session = new ClientSession(userId, sessionId, clientHandler);
        activeSessions.put(sessionId, session);
        LoggerUtil.info("Session created for user: " + userId + ", Session ID: " + sessionId);
    }
    
    /**
     * Xóa session
     */
    public void removeSession(String sessionId) {
        ClientSession session = activeSessions.remove(sessionId);
        if (session != null) {
            LoggerUtil.info("Session removed for user: " + session.getUserId() + ", Session ID: " + sessionId);
        }
    }
    
    /**
     * Lấy session theo ID
     */
    public ClientSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * Broadcast message tới tất cả clients
     */
    public void broadcastToAll(String message) {
        for (ClientHandler client : connectedClients) {
            try {
                client.sendMessage(message);
            } catch (Exception e) {
                LoggerUtil.error("Error broadcasting to client: " + e.getMessage());
            }
        }
    }
    
    /**
     * Broadcast message tới clients cụ thể (trừ sender)
     */
    public void broadcastToOthers(ClientHandler sender, String message) {
        for (ClientHandler client : connectedClients) {
            if (!client.equals(sender)) {
                try {
                    client.sendMessage(message);
                } catch (Exception e) {
                    LoggerUtil.error("Error broadcasting to client: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Broadcast event về thay đổi dữ liệu
     */
    public void broadcastDataChange(String eventType, Object data) {
        String message = String.format("{\"type\":\"data_change\",\"eventType\":\"%s\",\"data\":%s}", 
            eventType, data != null ? data.toString() : "null");
        broadcastToAll(message);
    }
    
    /**
     * Lấy số lượng user đang online
     */
    public int getActiveUserCount() {
        return activeSessions.size();
    }
    
    /**
     * Lấy số lượng client đang kết nối
     */
    public int getConnectedClientCount() {
        return connectedClients.size();
    }
    
    /**
     * Kiểm tra user có đang online không
     */
    public boolean isUserOnline(String userId) {
        return activeSessions.values().stream()
            .anyMatch(session -> session.getUserId().equals(userId));
    }
    
    /**
     * Setup event subscriptions cho real-time updates
     */
    private void setupEventSubscriptions() {
        eventBus.subscribe(EventBus.Events.BOOK_ADDED, data -> 
            broadcastDataChange("BOOK_ADDED", data));
        eventBus.subscribe(EventBus.Events.BOOK_UPDATED, data -> 
            broadcastDataChange("BOOK_UPDATED", data));
        eventBus.subscribe(EventBus.Events.BOOK_DELETED, data -> 
            broadcastDataChange("BOOK_DELETED", data));
        eventBus.subscribe(EventBus.Events.BOOK_BORROWED, data -> 
            broadcastDataChange("BOOK_BORROWED", data));
        eventBus.subscribe(EventBus.Events.BOOK_RETURNED, data -> 
            broadcastDataChange("BOOK_RETURNED", data));
        eventBus.subscribe(EventBus.Events.USER_ADDED, data -> 
            broadcastDataChange("USER_ADDED", data));
        eventBus.subscribe(EventBus.Events.USER_UPDATED, data -> 
            broadcastDataChange("USER_UPDATED", data));
        eventBus.subscribe(EventBus.Events.USER_DELETED, data -> 
            broadcastDataChange("USER_DELETED", data));
        eventBus.subscribe(EventBus.Events.BORROW_RECORD_UPDATED, data -> 
            broadcastDataChange("BORROW_RECORD_UPDATED", data));
        eventBus.subscribe(EventBus.Events.DATA_REFRESH, data -> 
            broadcastDataChange("DATA_REFRESH", data));
    }
    
    /**
     * Cleanup inactive sessions
     */
    public void cleanupInactiveSessions() {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> {
            ClientSession session = entry.getValue();
            boolean inactive = (currentTime - session.getLastActivity()) > 300000; // 5 minutes
            if (inactive) {
                LoggerUtil.info("Removing inactive session: " + entry.getKey());
            }
            return inactive;
        });
    }
    
    /**
     * Class đại diện cho session của client
     */
    public static class ClientSession {
        private final String userId;
        private final String sessionId;
        private final ClientHandler clientHandler;
        private final long createdTime;
        private volatile long lastActivity;
        
        public ClientSession(String userId, String sessionId, ClientHandler clientHandler) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.clientHandler = clientHandler;
            this.createdTime = System.currentTimeMillis();
            this.lastActivity = createdTime;
        }
        
        public void updateActivity() {
            this.lastActivity = System.currentTimeMillis();
        }
        
        // Getters
        public String getUserId() { return userId; }
        public String getSessionId() { return sessionId; }
        public ClientHandler getClientHandler() { return clientHandler; }
        public long getCreatedTime() { return createdTime; }
        public long getLastActivity() { return lastActivity; }
    }
}
