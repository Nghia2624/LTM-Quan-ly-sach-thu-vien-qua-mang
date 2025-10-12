package com.dainam.library.client;

import com.dainam.library.util.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import java.util.Map;
import java.util.HashMap;

/**
 * Quản lý kết nối TCP đến server
 */
public class ServerConnection {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int RECONNECT_DELAY = 3000; // 3 seconds
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    
    private static ServerConnection instance;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ObjectMapper objectMapper;
    private boolean connected = false;
    private String sessionId;
    
    private ServerConnection() {
        this.objectMapper = new ObjectMapper();
    }
    
    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }
    
    /**
     * Kết nối đến server với retry logic
     */
    public boolean connect() {
        int attempts = 0;
        
        while (attempts < MAX_RECONNECT_ATTEMPTS && !connected) {
            try {
                LoggerUtil.info("Đang kết nối đến server " + SERVER_HOST + ":" + SERVER_PORT + " (lần thử " + (attempts + 1) + ")");
                
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                
                connected = true;
                LoggerUtil.info("Kết nối TCP server thành công!");
                return true;
                
            } catch (ConnectException e) {
                attempts++;
                LoggerUtil.warn("Không thể kết nối đến server (lần thử " + attempts + "): " + e.getMessage());
                
                if (attempts < MAX_RECONNECT_ATTEMPTS) {
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi kết nối server: " + e.getMessage());
                break;
            }
        }
        
        LoggerUtil.error("Không thể kết nối đến server sau " + MAX_RECONNECT_ATTEMPTS + " lần thử");
        return false;
    }
    
    /**
     * Ngắt kết nối
     */
    public void disconnect() {
        try {
            connected = false;
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            LoggerUtil.info("Đã ngắt kết nối TCP server");
        } catch (Exception e) {
            LoggerUtil.error("Lỗi ngắt kết nối: " + e.getMessage());
        }
    }
    
    /**
     * Gửi request đến server và nhận response
     */
    public Map<String, Object> sendRequest(String action, Map<String, Object> data) {
        if (!connected && !connect()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Không thể kết nối đến server");
            return errorResponse;
        }
        
        try {
            // Tạo request object
            Map<String, Object> request = new HashMap<>();
            request.put("action", action);
            request.put("data", data);
            request.put("sessionId", sessionId);
            
            // Gửi request
            String requestJson = objectMapper.writeValueAsString(request);
            writer.println(requestJson);
              // Đọc response
            String responseJson = reader.readLine();
            if (responseJson != null) {
                LoggerUtil.debug("Raw response from server: " + responseJson);
                Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
                LoggerUtil.debug("Parsed response: success=" + response.get("success") + ", message=" + response.get("message"));
                return response;
            } else {
                throw new IOException("Server đã đóng kết nối");
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi gửi request: " + e.getMessage());
            connected = false;
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi kết nối server: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Kiểm tra trạng thái kết nối
     */
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    /**
     * Set session ID sau khi đăng nhập
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * Get session ID hiện tại
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Ping server để kiểm tra kết nối
     */
    public boolean ping() {
        Map<String, Object> response = sendRequest("ping", new HashMap<>());
        return (Boolean) response.getOrDefault("success", false);
    }
    
    /**
     * Reconnect nếu mất kết nối
     */
    public boolean reconnect() {
        disconnect();
        return connect();
    }
}
