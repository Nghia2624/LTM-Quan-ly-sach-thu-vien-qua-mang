package com.dainam.library.server;

import com.dainam.library.util.LoggerUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP Server cho hệ thống quản lý thư viện
 */
public class LibraryServer {
    
    private static final int PORT = 8888;
    private static final int MAX_CLIENTS = 100;
    
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private AtomicBoolean isRunning;
    
    public LibraryServer() {
        this.clientThreadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
        this.isRunning = new AtomicBoolean(false);
    }
    
    /**
     * Khởi chạy server
     */
    public void start() throws IOException {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning.set(true);
            
            LoggerUtil.info("Library Management Server đã khởi chạy trên port " + PORT);
            LoggerUtil.info("Đang chờ kết nối từ client...");
            
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    LoggerUtil.info("Client kết nối từ: " + clientSocket.getInetAddress().getHostAddress());
                    
                    // Xử lý client trong thread riêng
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientThreadPool.submit(clientHandler);
                    
                } catch (IOException e) {
                    if (isRunning.get()) {
                        LoggerUtil.error("Lỗi chấp nhận kết nối: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            LoggerUtil.error("Lỗi khởi chạy server: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Dừng server
     */
    public void stop() {
        isRunning.set(false);
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LoggerUtil.error("Lỗi đóng server socket: " + e.getMessage());
        }
        
        clientThreadPool.shutdown();
        LoggerUtil.info("Server đã dừng");
    }
    
    /**
     * Kiểm tra server có đang chạy không
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * Main method để chạy server độc lập
     */
    public static void main(String[] args) {
        LibraryServer server = new LibraryServer();
        
        // Add shutdown hook để đóng server một cách graceful
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerUtil.info("Shutting down server...");
            server.stop();
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            LoggerUtil.error("Không thể khởi chạy server: " + e.getMessage());
            System.exit(1);
        }
    }
}
