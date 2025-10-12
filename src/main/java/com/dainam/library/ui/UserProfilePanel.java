package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.service.UserService;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel thông tin cá nhân cho User
 */
public class UserProfilePanel extends JPanel {
    
    private User currentUser;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField studentIdField;    private JTextField facultyField;
    private JComboBox<Integer> yearOfStudyComboBox;
    private JTextArea addressArea;
    private JLabel statusLabel;
    private JButton saveButton;
    private JButton cancelButton;
    
    public UserProfilePanel(User user) {
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadUserData();
    }
    
    private void initializeComponents() {
        // Text fields với styling hiện đại
        firstNameField = createStyledTextField(20);
        lastNameField = createStyledTextField(20);
        emailField = createStyledTextField(20);
        phoneField = createStyledTextField(20);        studentIdField = createStyledTextField(20);
        facultyField = createStyledTextField(20);
        
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
          // Year of study combo box
        Integer[] years = {2021, 2022, 2023, 2024, 2025, 2026};
        yearOfStudyComboBox = new JComboBox<>(years);
        
        // Buttons với styling hiện đại
        saveButton = createStyledButton("Lưu thay đổi", new Color(46, 125, 50));
        cancelButton = createStyledButton("Hủy", new Color(158, 158, 158));
        
        // Status label
        statusLabel = new JLabel("Thông tin cá nhân của bạn");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(33, 150, 243));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel with scroll
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Title
        JLabel titleLabel = new JLabel("Thông tin cá nhân");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // First Name
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Tên *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(firstNameField, gbc);
        row++;
        
        // Last Name
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Họ *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(lastNameField, gbc);
        row++;
        
        // Email (read-only)
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField.setEditable(false);
        emailField.setBackground(Color.LIGHT_GRAY);
        mainPanel.add(emailField, gbc);
        row++;
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Số điện thoại *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(phoneField, gbc);
        row++;
        
        // Student ID (read-only)
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Mã sinh viên:"), gbc);
        gbc.gridx = 1;
        studentIdField.setEditable(false);
        studentIdField.setBackground(Color.LIGHT_GRAY);
        mainPanel.add(studentIdField, gbc);
        row++;
        
        // Faculty
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Khoa:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(facultyField, gbc);        row++;
        
        // Year of Study
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Năm học:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(yearOfStudyComboBox, gbc);
        row++;
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(new JScrollPane(addressArea), gbc);
        row++;
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(buttonPanel, gbc);
        row++;
        
        // Status label
        gbc.gridy = row;
        gbc.insets = new Insets(5, 5, 0, 5);
        mainPanel.add(statusLabel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }
      private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Thống kê cá nhân"));
        panel.setPreferredSize(new Dimension(0, 100));
        
        // Only show borrowing stats for regular users, not for admin
        if (currentUser.getRole() == User.Role.USER) {
            // Create stat panels for regular users
            JLabel totalBorrowsLabel = createStatLabel(String.valueOf(currentUser.getTotalBorrowed()), "Tổng lượt mượn");
            JLabel currentBorrowsLabel = createStatLabel(String.valueOf(currentUser.getCurrentBorrowed()), "Đang mượn");
            JLabel totalFinesLabel = createStatLabel(String.format("%.0f VND", currentUser.getTotalFines()), "Tổng phạt");
            JLabel statusLabel = createStatLabel(currentUser.getStatus().getDisplayName(), "Trạng thái");
            
            panel.add(totalBorrowsLabel);
            panel.add(currentBorrowsLabel);
            panel.add(totalFinesLabel);
            panel.add(statusLabel);
        } else {
            // For admin, show different stats
            JLabel roleLabel = createStatLabel("Quản trị viên", "Vai trò");
            JLabel systemLabel = createStatLabel("Quản lý thư viện", "Chức năng");
            JLabel accessLabel = createStatLabel("Toàn quyền", "Quyền hạn");
            JLabel statusLabel = createStatLabel(currentUser.getStatus().getDisplayName(), "Trạng thái");
            
            panel.add(roleLabel);
            panel.add(systemLabel);
            panel.add(accessLabel);
            panel.add(statusLabel);
        }
        
        return panel;
    }
    
    private JLabel createStatLabel(String value, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        valueLabel.setForeground(new Color(0, 120, 215));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        titleLabel.setForeground(Color.GRAY);
        
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.SOUTH);
        
        return valueLabel;
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadUserData();
            }
        });
    }
    
    private void loadUserData() {
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone());
        studentIdField.setText(currentUser.getStudentId());        facultyField.setText(currentUser.getFaculty());
        // Set year of study in combobox
        try {
            int year = Integer.parseInt(currentUser.getYearOfStudy());
            yearOfStudyComboBox.setSelectedItem(year);
        } catch (NumberFormatException e) {
            yearOfStudyComboBox.setSelectedIndex(0); // Default to first option
        }
        addressArea.setText(currentUser.getAddress());
        
        statusLabel.setText("Thông tin cá nhân của bạn");
        statusLabel.setForeground(Color.BLUE);
    }
      private void saveChanges() {
        // Get input values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();        String phone = phoneField.getText().trim();
        String faculty = facultyField.getText().trim();
        String yearOfStudy = String.valueOf((Integer) yearOfStudyComboBox.getSelectedItem());
        String address = addressArea.getText().trim();
        
        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("Vui lòng điền đầy đủ các trường bắt buộc (*)");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        // Validate input format
        if (!ValidationUtil.isValidName(firstName)) {
            statusLabel.setText("Tên không hợp lệ");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        if (!ValidationUtil.isValidName(lastName)) {
            statusLabel.setText("Họ không hợp lệ");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        if (!ValidationUtil.isValidPhone(phone)) {
            statusLabel.setText("Số điện thoại không hợp lệ");
            statusLabel.setForeground(Color.RED);
            return;
        }
        
        // Show loading
        saveButton.setEnabled(false);
        saveButton.setText("Đang lưu...");
        statusLabel.setText("Đang lưu thay đổi...");
        statusLabel.setForeground(Color.BLUE);
        
        // Save changes in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Update user object
                currentUser.setFirstName(firstName);
                currentUser.setLastName(lastName);
                currentUser.setPhone(phone);                currentUser.setFaculty(faculty);
                currentUser.setYearOfStudy(yearOfStudy);                currentUser.setAddress(address);
                
                // Cập nhật người dùng trong database
                UserService userService = new UserService();
                boolean success = userService.updateUser(currentUser);
                  if (success) {
                    statusLabel.setText("Lưu thay đổi thành công!");
                    statusLabel.setForeground(Color.GREEN);
                    
                    // Cập nhật lại hiển thị
                    SwingUtilities.invokeLater(() -> {
                        loadUserData(); // Sử dụng loadUserData thay vì updateUserInfo
                    });
                } else {
                    statusLabel.setText("Lỗi lưu thay đổi!");
                    statusLabel.setForeground(Color.RED);
                }
                
            } catch (Exception ex) {
                LoggerUtil.error("Lỗi lưu thay đổi: " + ex.getMessage());
                statusLabel.setText("Lỗi lưu thay đổi: " + ex.getMessage());
                statusLabel.setForeground(Color.RED);
            } finally {
                saveButton.setEnabled(true);
                saveButton.setText("Lưu thay đổi");
            }
        });
    }
    
    public void refresh() {
        loadUserData();
    }
    
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
}
