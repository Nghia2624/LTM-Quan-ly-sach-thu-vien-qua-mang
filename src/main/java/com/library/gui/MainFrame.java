package com.library.gui;

import com.library.client.LibraryClient;
import com.library.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private final LibraryClient client;
    private final User currentUser;
    private JTabbedPane tabbedPane;
    
    // Panels
    private BookManagementPanel bookManagementPanel;
    private BorrowManagementPanel borrowManagementPanel;
    private StatisticsPanel statisticsPanel;
    
    public MainFrame(LibraryClient client, User currentUser) {
        this.client = client;
        this.currentUser = currentUser;
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setTitle("Hệ thống Quản lý Sách Thư viện - " + currentUser.getFullName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Create menu bar
        createMenuBar();
        
        // Create main content
        createMainContent();
        
        // Create status bar
        createStatusBar();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem refreshItem = new JMenuItem("Làm mới");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshCurrentPanel());
        fileMenu.add(refreshItem);
        
        fileMenu.addSeparator();
        
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        logoutItem.addActionListener(e -> logout());
        fileMenu.add(logoutItem);
        
        JMenuItem exitItem = new JMenuItem("Thoát");
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Trợ giúp");
        
        JMenuItem aboutItem = new JMenuItem("Về chương trình");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Book Management Panel
        bookManagementPanel = new BookManagementPanel(client);
        tabbedPane.addTab("📚 Quản lý Sách", null, bookManagementPanel, "Quản lý thông tin sách");
        
        // Borrow Management Panel
        borrowManagementPanel = new BorrowManagementPanel(client);
        tabbedPane.addTab("📖 Quản lý Mượn/Trả", null, borrowManagementPanel, "Quản lý mượn trả sách");
        
        // Statistics Panel
        statisticsPanel = new StatisticsPanel(client);
        tabbedPane.addTab("📊 Thống kê", null, statisticsPanel, "Xem thống kê và báo cáo");
        
        // Tab change listener to refresh data
        tabbedPane.addChangeListener(e -> {
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent instanceof RefreshablePanel) {
                ((RefreshablePanel) selectedComponent).refreshData();
            }
        });
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));
        
        JLabel userLabel = new JLabel("Đăng nhập: " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        userLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        
        JLabel connectionLabel = new JLabel("Kết nối: Đã kết nối");
        connectionLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        connectionLabel.setForeground(new Color(0, 128, 0));
        
        statusBar.add(userLabel, BorderLayout.WEST);
        statusBar.add(connectionLabel, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void refreshCurrentPanel() {
        Component selectedComponent = tabbedPane.getSelectedComponent();
        if (selectedComponent instanceof RefreshablePanel) {
            ((RefreshablePanel) selectedComponent).refreshData();
        }
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Send logout request to server
            try {
                client.sendRequest(new com.library.common.Message(
                    com.library.common.Message.MessageType.LOGOUT, null));
            } catch (Exception e) {
                // Ignore logout errors
            }
            
            client.disconnect();
            
            SwingUtilities.invokeLater(() -> {
                new LoginForm().setVisible(true);
                dispose();
            });
        }
    }
    
    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn thoát ứng dụng?",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                client.sendRequest(new com.library.common.Message(
                    com.library.common.Message.MessageType.LOGOUT, null));
                client.disconnect();
            } catch (Exception e) {
                // Ignore errors during shutdown
            }
            
            System.exit(0);
        }
    }
    
    private void showAbout() {
        String aboutText = """
            Hệ thống Quản lý Sách Thư viện
            Version: 1.0.0
            
            Phát triển bởi: Đại Nam
            Công nghệ: Java, TCP Socket, MongoDB, Swing
            
            Chức năng:
            • Quản lý thông tin sách
            • Quản lý mượn/trả sách
            • Thống kê và báo cáo
            • Giao diện thân thiện người dùng
            
            © 2025 Library Management System
            """;
        
        JOptionPane.showMessageDialog(
            this,
            aboutText,
            "Về chương trình",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    // Interface for panels that can be refreshed
    public interface RefreshablePanel {
        void refreshData();
    }
}
