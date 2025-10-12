package com.dainam.library.client;

import com.dainam.library.util.LoggerUtil;

/**
 * Quản lý chế độ hoạt động của ứng dụng (Local hoặc Remote)
 */
public class ServiceManager {
    
    public enum Mode {
        LOCAL,   // Kết nối trực tiếp đến MongoDB
        REMOTE   // Kết nối qua TCP server
    }
    
    private static ServiceManager instance;
    private Mode currentMode = Mode.LOCAL; // Mặc định là local
    private RemoteServiceAdapter remoteAdapter;
    private boolean serverAvailable = false;
    
    private ServiceManager() {
        this.remoteAdapter = new RemoteServiceAdapter();
    }
    
    public static synchronized ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }
    
    /**
     * Thử kết nối đến server và chuyển sang chế độ remote nếu thành công
     */
    public boolean tryConnectToServer() {
        try {
            ServerConnection serverConnection = ServerConnection.getInstance();
            if (serverConnection.connect()) {
                currentMode = Mode.REMOTE;
                serverAvailable = true;
                LoggerUtil.info("Đã chuyển sang chế độ Remote (TCP Client-Server)");
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.warn("Không thể kết nối đến server, sử dụng chế độ Local: " + e.getMessage());
        }
        
        currentMode = Mode.LOCAL;
        serverAvailable = false;
        LoggerUtil.info("Sử dụng chế độ Local (Direct MongoDB)");
        return false;
    }
    
    /**
     * Buộc chuyển về chế độ local
     */
    public void forceLocalMode() {
        currentMode = Mode.LOCAL;
        serverAvailable = false;
        
        // Đóng kết nối server nếu có
        ServerConnection.getInstance().disconnect();
        
        LoggerUtil.info("Đã chuyển về chế độ Local (Direct MongoDB)");
    }
    
    /**
     * Lấy chế độ hiện tại
     */
    public Mode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Kiểm tra xem có đang ở chế độ remote không
     */
    public boolean isRemoteMode() {
        return currentMode == Mode.REMOTE && serverAvailable;
    }
    
    /**
     * Kiểm tra xem có đang ở chế độ local không
     */
    public boolean isLocalMode() {
        return currentMode == Mode.LOCAL;
    }
    
    /**
     * Lấy remote service adapter
     */
    public RemoteServiceAdapter getRemoteAdapter() {
        return remoteAdapter;
    }
    
    /**
     * Kiểm tra trạng thái server
     */
    public boolean isServerAvailable() {
        if (currentMode == Mode.REMOTE) {
            serverAvailable = ServerConnection.getInstance().isConnected();
        }
        return serverAvailable;
    }
    
    /**
     * Thử kết nối lại server
     */
    public boolean reconnectToServer() {
        if (ServerConnection.getInstance().reconnect()) {
            currentMode = Mode.REMOTE;
            serverAvailable = true;
            LoggerUtil.info("Đã kết nối lại server thành công");
            return true;
        }
        
        LoggerUtil.warn("Không thể kết nối lại server");
        return false;
    }
    
    /**
     * Lấy thông tin trạng thái kết nối
     */
    public String getConnectionStatus() {
        if (isRemoteMode() && isServerAvailable()) {
            return "Kết nối TCP Server - Hoạt động bình thường";
        } else if (isRemoteMode() && !isServerAvailable()) {
            return "Kết nối TCP Server - Mất kết nối";
        } else {
            return "Chế độ Local - MongoDB trực tiếp";
        }
    }
}
