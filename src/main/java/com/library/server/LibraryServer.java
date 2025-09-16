package com.library.server;

import com.library.database.DatabaseConnection;
import com.library.database.DatabaseInitializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LibraryServer {
    private static final int PORT = 8888;
    private static final int MAX_CLIENTS = 50;
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean isRunning = false;
    private AtomicInteger activeConnections = new AtomicInteger(0);
    private AtomicInteger totalConnections = new AtomicInteger(0);
    
    public LibraryServer() {
        threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    }    public void start() {
        isRunning = true;
        
        try {
            // Initialize database and check connection
            if (!initializeDatabase()) {
                System.err.println("❌ Failed to initialize database. Server shutting down.");
                return;
            }
            
            System.out.println("✅ Database connection established successfully!");
            
            // Start server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("🚀 Library Management Server started on port " + PORT);
            System.out.println("🔄 Server is ready and waiting for client connections...");
            System.out.println("📊 Press Ctrl+C to gracefully shutdown the server");
            System.out.println();
            
            // Main server loop
            while (isRunning && !serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    totalConnections.incrementAndGet();
                    activeConnections.incrementAndGet();
                    
                    String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                    System.out.println("🔗 New client connected: " + clientInfo + 
                                     " (Active: " + activeConnections.get() + 
                                     ", Total: " + totalConnections.get() + ")");
                    
                    // Create and submit client handler
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    threadPool.execute(clientHandler);
                    
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("❌ Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    private boolean initializeDatabase() {
        try {
            System.out.println("🔧 Initializing database connection...");
            
            if (!DatabaseConnection.testConnection()) {
                System.err.println("❌ Cannot connect to MongoDB. Please ensure MongoDB is running on localhost:27017");
                return false;
            }
            
            System.out.println("✅ MongoDB connection successful");
            
            // Initialize database with proper indexes
            System.out.println("🔧 Setting up database indexes...");
            DatabaseInitializer.initializeDatabase();
            
            // Check data consistency
            System.out.println("🔍 Checking data consistency...");
            DatabaseInitializer.checkDataConsistency();
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void clientDisconnected() {
        int remaining = activeConnections.decrementAndGet();
        System.out.println("🔌 Client disconnected. Active connections: " + remaining);
    }
      public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        System.out.println("\n🛑 Initiating server shutdown...");
        
        cleanup();
        
        System.out.println("✅ Server stopped successfully.");
        System.out.println("📊 Final statistics:");
        System.out.println("   • Total connections served: " + totalConnections.get());
        System.out.println("   • Server uptime: " + getUptime());
    }
    
    private void cleanup() {
        try {
            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("🔒 Server socket closed");
            }
            
            // Shutdown thread pool
            if (threadPool != null) {
                threadPool.shutdown();
                System.out.println("🧵 Thread pool shutdown initiated");
                
                // Wait for existing tasks to complete
                if (!threadPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    System.out.println("⚡ Forced thread pool shutdown");
                }
            }
            
            // Close database connection
            DatabaseConnection.closeConnection();
            System.out.println("🗄️ Database connection closed");
            
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
        }
    }
    
    private String getUptime() {
        // Simple uptime calculation - in a real app you'd track start time
        return "Session ended";
    }
      public static void main(String[] args) {
        LibraryServer server = null;
        
        try {
            server = new LibraryServer();
            
            // Add graceful shutdown hook
            final LibraryServer finalServer = server;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Shutdown signal received...");
                finalServer.stop();
            }));
            
            // Start the server
            server.start();
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected server error: " + e.getMessage());
            e.printStackTrace();
            if (server != null) {
                server.stop();
            }
            System.exit(1);
        }
    }
}
