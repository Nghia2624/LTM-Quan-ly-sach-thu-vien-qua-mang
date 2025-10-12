package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.service.UserService;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.EventBus;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện chính cho Admin với real-time updates
 */
public class AdminFrame extends JFrame {
    
    private User currentUser;
    private UserService userService;
    private JTabbedPane tabbedPane;
    private Timer refreshTimer;
    private EventBus eventBus;
    
    // Panels
    private AdminDashboardPanel dashboardPanel;
    private AdminBookManagementPanel bookManagementPanel;
    private AdminUserManagementPanel userManagementPanel;
    private AdminBorrowManagementPanel borrowManagementPanel;
    private AdminReportPanel reportPanel;
    public AdminFrame(User user) {
        this.currentUser = user;
        this.userService = new UserService();
        this.eventBus = EventBus.getInstance();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupEventSubscriptions();
        startAutoRefresh();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Hệ thống quản lý thư viện - Admin Panel");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
      private void initializeComponents() {
        // Create modern tabbed pane with FlatLaf styling
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(255, 255, 255));
        tabbedPane.setForeground(new Color(52, 73, 94));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Ensure tabs are focusable and clickable
        tabbedPane.setFocusable(true);
        tabbedPane.setRequestFocusEnabled(true);
        
        // Apply modern styling to tabbed pane
        UIManager.put("TabbedPane.selectedBackground", new Color(52, 152, 219));
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
        UIManager.put("TabbedPane.background", new Color(248, 249, 250));
        UIManager.put("TabbedPane.foreground", new Color(52, 73, 94));
        UIManager.put("TabbedPane.borderHightlightColor", new Color(52, 152, 219));
        UIManager.put("TabbedPane.focus", new Color(52, 152, 219));
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 12, 8, 12));
        UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(2, 2, 2, 2));
        
        // Create panels
        dashboardPanel = new AdminDashboardPanel();
        bookManagementPanel = new AdminBookManagementPanel();
        userManagementPanel = new AdminUserManagementPanel();
        borrowManagementPanel = new AdminBorrowManagementPanel();
        reportPanel = new AdminReportPanel();        // Add tabs with modern icons and styling
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Quản lý sách", bookManagementPanel);
        tabbedPane.addTab("Quản lý người dùng", userManagementPanel);
        tabbedPane.addTab("Mượn/Trả", borrowManagementPanel);
        tabbedPane.addTab("Báo cáo", reportPanel);
        
        // Set tab tooltips
        tabbedPane.setToolTipTextAt(0, "Tổng quan hệ thống");
        tabbedPane.setToolTipTextAt(1, "Quản lý sách và bản sao");
        tabbedPane.setToolTipTextAt(2, "Quản lý tài khoản người dùng");
        tabbedPane.setToolTipTextAt(3, "Quản lý mượn trả sách");
        tabbedPane.setToolTipTextAt(4, "Báo cáo và thống kê");
        
        // Ensure proper tab dimensions for clickability
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setTabComponentAt(i, null); // Use default rendering for better clickability
        }
    }
      private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Menu bar
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        
        // Main content - Xóa toolbar không cần thiết
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = createStatusBar();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("Tệp");
        fileMenu.setMnemonic('T');
        
        JMenuItem refreshItem = new JMenuItem("Làm mới");
        refreshItem.setMnemonic('L');
        refreshItem.addActionListener(e -> refreshAllPanels());
        
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        logoutItem.setMnemonic('D');
        logoutItem.addActionListener(e -> logout());
        
        JMenuItem exitItem = new JMenuItem("Thoát");
        exitItem.setMnemonic('T');
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Tools menu
        JMenu toolsMenu = new JMenu("Công cụ");
        toolsMenu.setMnemonic('C');
        
        JMenuItem settingsItem = new JMenuItem("Cài đặt");
        settingsItem.setMnemonic('C');
        settingsItem.addActionListener(e -> showSettings());
        
        JMenuItem backupItem = new JMenuItem("Sao lưu dữ liệu");
        backupItem.setMnemonic('S');
        backupItem.addActionListener(e -> performBackup());
        
        toolsMenu.add(settingsItem);
        toolsMenu.add(backupItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Trợ giúp");
        helpMenu.setMnemonic('T');
        
        JMenuItem aboutItem = new JMenuItem("Giới thiệu");
        aboutItem.setMnemonic('G');
        aboutItem.addActionListener(e -> showAbout());
        
        JMenuItem helpItem = new JMenuItem("Hướng dẫn sử dụng");
        helpItem.setMnemonic('H');
        helpItem.addActionListener(e -> showHelp());
        
        helpMenu.add(aboutItem);
        helpMenu.add(helpItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
          return menuBar;
    }
    
    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(248, 249, 250));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // User info
        JLabel userLabel = new JLabel("Đăng nhập: " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(new Color(52, 73, 94));
        
        // Status
        JLabel statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(39, 174, 96));
        
        statusPanel.add(userLabel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.EAST);
        
        return statusPanel;
    }  private void setupEventHandlers() {
        // Tab change listener - Removed auto refresh to prevent data reset
        tabbedPane.addChangeListener(e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    Component selectedComponent = tabbedPane.getSelectedComponent();
                    if (selectedComponent != null) {
                        // Only force repaint, don't refresh data automatically
                        selectedComponent.revalidate();
                        selectedComponent.repaint();
                        tabbedPane.revalidate();
                        tabbedPane.repaint();
                    }
                } catch (Exception ex) {
                    LoggerUtil.error("Tab change error: " + ex.getMessage());
                }
            });
        });
        
        // Window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int result = JOptionPane.showConfirmDialog(
                    AdminFrame.this,
                    "Bạn có chắc chắn muốn thoát?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    private void refreshAllPanels() {
        try {
            dashboardPanel.refresh();
            bookManagementPanel.refresh();
            userManagementPanel.refresh();
            borrowManagementPanel.refresh();
            reportPanel.refresh();
            
            JOptionPane.showMessageDialog(this, "Đã làm mới dữ liệu thành công!", 
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới dữ liệu: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi làm mới dữ liệu: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
      private void logout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // Cleanup session
                userService.logout(currentUser.getUserId());
                
                // Stop refresh timer
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
                
                // Close current frame
                dispose();
                
                // Show login frame
                SwingUtilities.invokeLater(() -> {
                    new LoginFrame().setVisible(true);
                });
                
                LoggerUtil.info("Admin đăng xuất thành công: " + currentUser.getEmail());
                
            } catch (Exception e) {
                LoggerUtil.error("Lỗi đăng xuất: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi đăng xuất: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showSettings() {
        JOptionPane.showMessageDialog(this, "Tính năng cài đặt đang được phát triển", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void performBackup() {
        JOptionPane.showMessageDialog(this, "Tính năng sao lưu đang được phát triển", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAbout() {
        String aboutText = "Hệ thống quản lý thư viện\n" +
                          "Phiên bản: 1.0.0\n" +
                          "Phát triển bởi: Đại Nam University\n" +
                          "Công nghệ: Java Swing, MongoDB, TCP Socket";
        
        JOptionPane.showMessageDialog(this, aboutText, "Giới thiệu", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showHelp() {
        JOptionPane.showMessageDialog(this, "Tài liệu hướng dẫn đang được cập nhật", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void switchToBookManagement() {
        tabbedPane.setSelectedComponent(bookManagementPanel);
    }
    
    public void switchToUserManagement() {
        tabbedPane.setSelectedComponent(userManagementPanel);
    }
      public void switchToReports() {
        tabbedPane.setSelectedComponent(reportPanel);
    }
      private void setupEventSubscriptions() {
        // Subscribe to real-time events
        eventBus.subscribe(EventBus.Events.BOOK_ADDED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.BOOK_UPDATED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.BOOK_DELETED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.BOOK_BORROWED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.BOOK_RETURNED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.USER_ADDED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.USER_UPDATED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.USER_DELETED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.BORROW_RECORD_UPDATED, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        eventBus.subscribe(EventBus.Events.DATA_REFRESH, data -> SwingUtilities.invokeLater(this::refreshAllPanels));
        
        // Window closing
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int result = JOptionPane.showConfirmDialog(
                    AdminFrame.this,
                    "Bạn có chắc chắn muốn thoát?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                  if (result == JOptionPane.YES_OPTION) {
                    try {
                        // Cleanup user session first
                        userService.logout(currentUser.getUserId());
                        
                        // Stop refresh timer
                        if (refreshTimer != null) {
                            refreshTimer.stop();
                        }
                        
                        LoggerUtil.info("Admin thoát ứng dụng: " + currentUser.getEmail());
                    } catch (Exception e) {
                        LoggerUtil.error("Lỗi cleanup session khi thoát: " + e.getMessage());
                    }
                    
                    System.exit(0);
                }
            }
        });
    }
      private void startAutoRefresh() {
        // Auto refresh every 5 minutes instead of 30 seconds to prevent frequent resets
        refreshTimer = new Timer(300000, e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Only refresh dashboard for statistics, not all panels
                    dashboardPanel.refresh();
                    // Don't auto-refresh other panels to prevent data reset while user is working
                } catch (Exception ex) {
                    LoggerUtil.error("Auto refresh error: " + ex.getMessage());
                }
            });
        });
        refreshTimer.start();
    }
}
