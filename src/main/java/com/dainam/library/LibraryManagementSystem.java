package com.dainam.library;

import com.formdev.flatlaf.FlatLightLaf;
import com.dainam.library.ui.LoginFrame;
import com.dainam.library.server.LibraryServer;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.DataInitializer;

import javax.swing.*;
import java.util.concurrent.Executors;

/**
 * Main class để khởi chạy hệ thống quản lý thư viện
 * Hỗ trợ chạy ở chế độ Server hoặc Client
 */
public class LibraryManagementSystem {
    
    public static void main(String[] args) {
        // Thiết lập look and feel
        setupLookAndFeel();
        
        // Khởi tạo logger
        LoggerUtil.initialize();
        
        // Kiểm tra arguments
        if (args.length > 0 && "server".equals(args[0])) {
            startServer();
        } else {
            startClient();
        }
    }
    
    /**
     * Thiết lập giao diện hiện đại với FlatLaf
     */
    private static void setupLookAndFeel() {
        try {
            // Sử dụng theme sáng làm mặc định
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Thiết lập UI properties cho giao diện hiện đại
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("ScrollBar.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 12);
            UIManager.put("ScrollBar.trackArc", 12);
            UIManager.put("TabbedPane.arc", 12);
            UIManager.put("Table.arc", 12);
            UIManager.put("Panel.arc", 12);
            
            // Thiết lập bảng màu hiện đại
            UIManager.put("Button.background", new java.awt.Color(0x4F46E5)); // Indigo
            UIManager.put("Button.foreground", java.awt.Color.WHITE);
            UIManager.put("Button.hoverBackground", new java.awt.Color(0x4338CA));
            UIManager.put("Button.pressedBackground", new java.awt.Color(0x3730A3));
            UIManager.put("Button.focusedBackground", new java.awt.Color(0x4338CA));
            
            UIManager.put("TextComponent.focusedBorderColor", new java.awt.Color(0x4F46E5));
            UIManager.put("TextComponent.selectionBackground", new java.awt.Color(0x4F46E5));
            UIManager.put("TextComponent.selectionForeground", java.awt.Color.WHITE);
            
            UIManager.put("Table.selectionBackground", new java.awt.Color(0x4F46E5));
            UIManager.put("Table.selectionForeground", java.awt.Color.WHITE);
            UIManager.put("Table.gridColor", new java.awt.Color(0xE5E7EB));
            UIManager.put("Table.background", new java.awt.Color(0xFAFAFA));
            UIManager.put("Table.alternateRowColor", new java.awt.Color(0xF9FAFB));
            
            UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(0x4F46E5));
            UIManager.put("TabbedPane.selectedForeground", java.awt.Color.WHITE);
            UIManager.put("TabbedPane.hoverBackground", new java.awt.Color(0xE0E7FF));
            UIManager.put("TabbedPane.hoverForeground", new java.awt.Color(0x4F46E5));
            
            UIManager.put("Panel.background", new java.awt.Color(0xFAFAFA));
            UIManager.put("Label.foreground", new java.awt.Color(0x374151));
            UIManager.put("TitledBorder.titleColor", new java.awt.Color(0x4F46E5));
            UIManager.put("TitledBorder.borderColor", new java.awt.Color(0xE5E7EB));
            
            UIManager.put("ComboBox.background", java.awt.Color.WHITE);
            UIManager.put("ComboBox.selectionBackground", new java.awt.Color(0x4F46E5));
            UIManager.put("ComboBox.selectionForeground", java.awt.Color.WHITE);
            
            UIManager.put("ScrollBar.background", new java.awt.Color(0xF3F4F6));
            UIManager.put("ScrollBar.thumb", new java.awt.Color(0xD1D5DB));
            UIManager.put("ScrollBar.hoverThumb", new java.awt.Color(0x9CA3AF));
            
            UIManager.put("MenuBar.background", java.awt.Color.WHITE);
            UIManager.put("Menu.background", java.awt.Color.WHITE);
            UIManager.put("Menu.foreground", new java.awt.Color(0x374151));
            UIManager.put("MenuItem.background", java.awt.Color.WHITE);
            UIManager.put("MenuItem.foreground", new java.awt.Color(0x374151));
            UIManager.put("MenuItem.selectionBackground", new java.awt.Color(0x4F46E5));
            UIManager.put("MenuItem.selectionForeground", java.awt.Color.WHITE);
            
            UIManager.put("ToolBar.background", java.awt.Color.WHITE);
            UIManager.put("ToolBar.borderColor", new java.awt.Color(0xE5E7EB));
            
            UIManager.put("SplitPane.background", new java.awt.Color(0xFAFAFA));
            UIManager.put("SplitPane.dividerColor", new java.awt.Color(0xE5E7EB));
            
            UIManager.put("Tree.background", new java.awt.Color(0xFAFAFA));
            UIManager.put("Tree.selectionBackground", new java.awt.Color(0x4F46E5));
            UIManager.put("Tree.selectionForeground", java.awt.Color.WHITE);
            
            UIManager.put("List.background", new java.awt.Color(0xFAFAFA));
            UIManager.put("List.selectionBackground", new java.awt.Color(0x4F46E5));
            UIManager.put("List.selectionForeground", java.awt.Color.WHITE);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Khởi chạy server
     */
    private static void startServer() {
        System.out.println("Đang khởi chạy Library Management Server...");
        
        try {
            // Khởi tạo database connection và dữ liệu
            DataInitializer.initialize();
            
            // Khởi chạy server trong thread riêng
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    LibraryServer server = new LibraryServer();
                    server.start();
                } catch (Exception e) {
                    LoggerUtil.error("Lỗi khởi chạy server: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            System.out.println("Server đã khởi chạy thành công!");
            System.out.println("Nhấn Ctrl+C để dừng server");
            
            // Giữ main thread chạy
            Thread.currentThread().join();
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi khởi chạy server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Khởi chạy client (giao diện người dùng)
     */
    private static void startClient() {
        System.out.println("Đang khởi chạy Library Management Client...");
        
        // Thiết lập EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Hiển thị màn hình đăng nhập
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                
            } catch (Exception e) {
                LoggerUtil.error("Lỗi khởi chạy client: " + e.getMessage());
                e.printStackTrace();
                
                JOptionPane.showMessageDialog(
                    null,
                    "Lỗi khởi chạy ứng dụng: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
