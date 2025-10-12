package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.service.UserService;
import com.dainam.library.client.ServiceManager;
import com.dainam.library.client.RemoteServiceAdapter;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.util.Map;

/**
 * Màn hình đăng nhập với giao diện hiện đại
 */
public class LoginFrame extends JFrame {
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;
    
    private UserService userService;
      public LoginFrame() {
        this.userService = new UserService();
        
        // Thử kết nối TCP server trước
        ServiceManager serviceManager = ServiceManager.getInstance();
        boolean serverConnected = serviceManager.tryConnectToServer();
        
        if (serverConnected) {
            LoggerUtil.info("Đã kết nối TCP server, sử dụng chế độ Client-Server");
        } else {
            LoggerUtil.info("Không thể kết nối TCP server, sử dụng chế độ local");
        }
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Hệ thống quản lý thư viện - Đăng nhập (" + serviceManager.getConnectionStatus() + ")");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(createIcon());
    }
    
    private void initializeComponents() {
        // Email field with modern styling
        emailField = new JTextField(25);
        emailField.setToolTipText("Nhập email của bạn");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Password field with modern styling
        passwordField = new JPasswordField(25);
        passwordField.setToolTipText("Nhập mật khẩu của bạn");
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Show password checkbox
        showPasswordCheckBox = new JCheckBox("Hiển thị mật khẩu");
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheckBox.setForeground(new Color(100, 100, 100));
        showPasswordCheckBox.setOpaque(false);
        
        // Login button with modern styling
        loginButton = new JButton("Đăng nhập");
        loginButton.setPreferredSize(new Dimension(200, 45));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Register button with modern styling
        registerButton = new JButton("Đăng ký tài khoản mới");
        registerButton.setPreferredSize(new Dimension(200, 35));
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerButton.setBackground(new Color(236, 240, 241));
        registerButton.setForeground(new Color(52, 73, 94));
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Hệ thống Quản lý Thư viện", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Đăng nhập để tiếp tục", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel, BorderLayout.CENTER);
        centerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(centerPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Email label
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emailLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(emailLabel, gbc);
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(emailField, gbc);
        
        // Password label
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordLabel, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);
        
        // Show password checkbox
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(showPasswordCheckBox, gbc);
        
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(loginButton, gbc);
        
        // Status label
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(statusLabel, gbc);
        
        // Register button
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(registerButton, gbc);
        
        return mainPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(236, 240, 241));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel footerLabel = new JLabel("© 2025 Hệ thống quản lý thư viện - Đại Nam University");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footerLabel.setForeground(new Color(100, 100, 100));
        
        footerPanel.add(footerLabel);
        
        return footerPanel;
    }
    
    private void setupEventHandlers() {
        // Login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });
        
        // Show password checkbox
        showPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char) 0 : '•');
            }
        });
        
        // Enter key to login
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {}
        };
        
        emailField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        
        // Hover effects for buttons
        setupButtonHoverEffects();
    }
    
    private void setupButtonHoverEffects() {
        // Login button hover effect
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(52, 152, 219));
            }
        });
        
        // Register button hover effect
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(new Color(189, 195, 199));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(new Color(236, 240, 241));
            }
        });
    }
    
    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Vui lòng nhập đầy đủ thông tin", true);
            return;
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            showStatus("Email không hợp lệ", true);
            return;
        }
        
        // Show loading
        loginButton.setEnabled(false);
        loginButton.setText("Đang đăng nhập...");
        statusLabel.setText("Đang xác thực...");        // Perform login in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                User user = null;
                ServiceManager serviceManager = ServiceManager.getInstance();
                  if (serviceManager.isRemoteMode()) {
                    // Sử dụng TCP connection
                    RemoteServiceAdapter remoteAdapter = serviceManager.getRemoteAdapter();
                    Map<String, Object> response = remoteAdapter.authenticateUser(email, password);
                    
                    if ((Boolean) response.getOrDefault("success", false)) {
                        Map<String, Object> responseData = (Map<String, Object>) response.get("data");
                        if (responseData != null) {
                            Map<String, Object> userData = (Map<String, Object>) responseData.get("user");
                            if (userData != null) {
                                user = convertMapToUser(userData);
                            }
                        }
                    } else {
                        String errorMessage = (String) response.getOrDefault("message", "Đăng nhập thất bại");
                        showStatus(errorMessage, true);
                        return;
                    }
                } else {
                    // Sử dụng local connection (MongoDB trực tiếp)
                    user = userService.authenticate(email, password);
                }
                
                if (user != null) {
                    showStatus("Đăng nhập thành công!", false);
                    
                    // Open appropriate interface based on role
                    if (user.isAdmin()) {
                        openAdminInterface(user);
                    } else {
                        openUserInterface(user);
                    }
                    
                    dispose();
                } else {
                    showStatus("Email hoặc mật khẩu không đúng", true);
                }
            } catch (Exception ex) {
                LoggerUtil.error("Lỗi đăng nhập: " + ex.getMessage());
                showStatus(ex.getMessage(), true);
            } finally {
                loginButton.setEnabled(true);
                loginButton.setText("Đăng nhập");
            }
        });
    }
    
    private void showRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
    }
    
    private void openAdminInterface(User user) {
        SwingUtilities.invokeLater(() -> {
            AdminFrame adminFrame = new AdminFrame(user);
            adminFrame.setVisible(true);
        });
    }
    
    private void openUserInterface(User user) {
        SwingUtilities.invokeLater(() -> {
            UserFrame userFrame = new UserFrame(user);
            userFrame.setVisible(true);
        });
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : new Color(39, 174, 96));
    }
    
    private Image createIcon() {
        // Create a simple icon
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a simple book icon
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRect(4, 8, 24, 20);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(6, 10, 20, 16);
        g2d.setColor(new Color(52, 152, 219));
        g2d.drawLine(8, 12, 24, 12);
        g2d.drawLine(8, 14, 24, 14);
        g2d.drawLine(8, 16, 24, 16);
        g2d.drawLine(8, 18, 24, 18);
        g2d.drawLine(8, 20, 24, 20);
        g2d.drawLine(8, 22, 24, 22);
        
        g2d.dispose();
        return icon;
    }
    
    /**
     * Convert Map to User object (for TCP response)
     */
    private User convertMapToUser(Map<String, Object> data) {
        if (data == null) return null;
        
        User user = new User();
        user.setUserId((String) data.get("userId"));
        user.setEmail((String) data.get("email"));
        user.setFullName((String) data.get("fullName"));
        user.setStudentId((String) data.get("studentId"));
        user.setPhone((String) data.get("phone"));
        user.setFaculty((String) data.get("faculty"));
        
        String role = (String) data.get("role");
        if (role != null) {
            user.setRole(User.Role.valueOf(role));
        }
        
        String status = (String) data.get("status");
        if (status != null) {
            user.setStatus(User.Status.valueOf(status));
        }
        
        return user;
    }
}