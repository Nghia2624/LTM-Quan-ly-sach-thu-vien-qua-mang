package com.library.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.library.client.LibraryClient;
import com.library.common.LoginRequest;
import com.library.common.Message;
import com.library.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame {
    private final LibraryClient client;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton connectButton;
    private JLabel statusLabel;
    
    public LoginForm() {
        this.client = new LibraryClient();
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setTitle("Hệ thống Quản lý Sách Thư viện - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(74, 144, 226),
                    0, getHeight(), new Color(80, 170, 237)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("QUẢN LÝ SÁCH THƯ VIỆN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // Login panel
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setOpaque(false);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Email label and field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(emailLabel, gbc);
        
        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setText("dainam@gmail.com"); // Default value
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(emailField, gbc);
        
        // Password label and field
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setText("dainam"); // Default value
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        connectButton = new JButton("Kết nối Server");
        connectButton.setFont(new Font("Arial", Font.BOLD, 12));
        connectButton.setBackground(new Color(46, 125, 50));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.setBorderPainted(false);
        buttonPanel.add(connectButton);
        
        loginButton = new JButton("Đăng nhập");
        loginButton.setFont(new Font("Arial", Font.BOLD, 12));
        loginButton.setBackground(new Color(25, 118, 210));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setEnabled(false);
        buttonPanel.add(loginButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(buttonPanel, gbc);
        
        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        statusLabel = new JLabel("Nhấn 'Kết nối Server' để bắt đầu", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.WHITE);
        statusPanel.add(statusLabel);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void setupEventHandlers() {
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Enter key for login
        Action loginAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loginButton.isEnabled()) {
                    performLogin();
                }
            }
        };
        
        emailField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
    }
    
    private void connectToServer() {
        connectButton.setEnabled(false);
        connectButton.setText("Đang kết nối...");
        statusLabel.setText("Đang kết nối đến server...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.connect();
            }
            
            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        statusLabel.setText("Kết nối thành công! Hãy đăng nhập.");
                        connectButton.setText("Đã kết nối");
                        connectButton.setBackground(new Color(76, 175, 80));
                        loginButton.setEnabled(true);
                        emailField.requestFocus();
                    } else {
                        statusLabel.setText("Không thể kết nối đến server. Thử lại.");
                        connectButton.setText("Kết nối Server");
                        connectButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Lỗi kết nối: " + e.getMessage());
                    connectButton.setText("Kết nối Server");
                    connectButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ email và mật khẩu!");
            return;
        }
        
        loginButton.setEnabled(false);
        loginButton.setText("Đang đăng nhập...");
        statusLabel.setText("Đang xác thực...");
        
        SwingWorker<Message, Void> worker = new SwingWorker<Message, Void>() {
            @Override
            protected Message doInBackground() throws Exception {
                LoginRequest loginRequest = new LoginRequest(email, password);
                Message request = new Message(Message.MessageType.LOGIN, loginRequest);
                return client.sendRequest(request);
            }
            
            @Override
            protected void done() {
                try {
                    Message response = get();
                    if (response.isSuccess()) {
                        User user = (User) response.getData();
                        statusLabel.setText("Đăng nhập thành công!");
                        
                        // Open main application window
                        SwingUtilities.invokeLater(() -> {
                            new MainFrame(client, user).setVisible(true);
                            dispose();
                        });
                    } else {
                        showError("Đăng nhập thất bại: " + response.getErrorMessage());
                        loginButton.setText("Đăng nhập");
                        loginButton.setEnabled(true);
                        statusLabel.setText("Đăng nhập thất bại. Thử lại.");
                    }
                } catch (Exception e) {
                    showError("Lỗi đăng nhập: " + e.getMessage());
                    loginButton.setText("Đăng nhập");
                    loginButton.setEnabled(true);
                    statusLabel.setText("Lỗi đăng nhập. Thử lại.");
                }
            }
        };
        
        worker.execute();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
