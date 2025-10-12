package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.service.UserService;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.PasswordUtil;
import com.dainam.library.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

/**
 * Dialog thêm người dùng mới
 */
public class AddUserDialog extends JDialog {
    
    private UserService userService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JTextField firstNameField;
    private JTextField lastNameField;    private JTextField studentIdField;    private JTextField facultyField;
    private JComboBox<Integer> yearOfStudyComboBox;
    private JComboBox<User.Role> roleComboBox;
    private JComboBox<User.Status> statusComboBox;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextArea notesArea;
    private JButton saveButton;
    private JButton cancelButton;
    
    public AddUserDialog(JFrame parent) {
        super(parent, "Thêm người dùng mới", true);
        this.userService = new UserService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 700);
        setLocationRelativeTo(parent);
        setResizable(true);
    }
    
    private void initializeComponents() {
        // Text fields with better styling and proper sizing
        emailField = new JTextField(25);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        passwordField = new JPasswordField(25);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        fullNameField = new JTextField(25);
        fullNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fullNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        firstNameField = new JTextField(25);
        firstNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        firstNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        lastNameField = new JTextField(25);
        lastNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        studentIdField = new JTextField(25);
        studentIdField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentIdField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        facultyField = new JTextField(25);
        facultyField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        facultyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)        ));
        
        phoneField = new JTextField(25);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)        ));
        
        // Combo box for year of study
        Integer[] years = {2021, 2022, 2023, 2024, 2025, 2026};
        yearOfStudyComboBox = new JComboBox<>(years);
        yearOfStudyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearOfStudyComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Combo boxes
        roleComboBox = new JComboBox<>(User.Role.values());
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        statusComboBox = new JComboBox<>(User.Status.values());
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Text areas
        addressArea = new JTextArea(3, 25);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        notesArea = new JTextArea(3, 25);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Buttons with better styling
        saveButton = new JButton("Lưu");
        saveButton.setPreferredSize(new Dimension(100, 35));
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveButton.setBackground(new Color(39, 174, 96));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cancelButton = new JButton("Hủy");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Thêm người dùng mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content with scroll pane
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create labels with consistent styling
        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Color labelColor = new Color(52, 73, 94);
        
        // Row 1: Email
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel emailLabel = new JLabel("Email *:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(labelColor);
        mainPanel.add(emailLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(emailField, gbc);
        
        // Row 2: Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("Mật khẩu *:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(labelColor);
        mainPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(passwordField, gbc);
        
        // Row 3: Full Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel fullNameLabel = new JLabel("Họ và tên *:");
        fullNameLabel.setFont(labelFont);
        fullNameLabel.setForeground(labelColor);
        mainPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(fullNameField, gbc);
        
        // Row 4: First Name
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel firstNameLabel = new JLabel("Tên:");
        firstNameLabel.setFont(labelFont);
        firstNameLabel.setForeground(labelColor);
        mainPanel.add(firstNameLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(firstNameField, gbc);
        
        // Row 5: Last Name
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel lastNameLabel = new JLabel("Họ:");
        lastNameLabel.setFont(labelFont);
        lastNameLabel.setForeground(labelColor);
        mainPanel.add(lastNameLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(lastNameField, gbc);
        
        // Row 6: Student ID
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel studentIdLabel = new JLabel("Mã sinh viên *:");
        studentIdLabel.setFont(labelFont);
        studentIdLabel.setForeground(labelColor);
        mainPanel.add(studentIdLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(studentIdField, gbc);
        
        // Row 7: Faculty
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel facultyLabel = new JLabel("Khoa:");
        facultyLabel.setFont(labelFont);
        facultyLabel.setForeground(labelColor);
        mainPanel.add(facultyLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;        mainPanel.add(facultyField, gbc);
        
        // Row 8: Year of Study
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel yearLabel = new JLabel("Năm học:");
        yearLabel.setFont(labelFont);
        yearLabel.setForeground(labelColor);
        mainPanel.add(yearLabel, gbc);        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(yearOfStudyComboBox, gbc);
        
        // Row 9: Role
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel roleLabel = new JLabel("Vai trò *:");
        roleLabel.setFont(labelFont);
        roleLabel.setForeground(labelColor);
        mainPanel.add(roleLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(roleComboBox, gbc);
          // Row 10: Status
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel statusLabel = new JLabel("Trạng thái *:");
        statusLabel.setFont(labelFont);
        statusLabel.setForeground(labelColor);
        mainPanel.add(statusLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(statusComboBox, gbc);
        
        // Row 11: Phone
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(labelFont);
        phoneLabel.setForeground(labelColor);
        mainPanel.add(phoneLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(phoneField, gbc);
          // Row 12: Address
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(labelFont);
        addressLabel.setForeground(labelColor);
        mainPanel.add(addressLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(new JScrollPane(addressArea), gbc);
        
        // Row 13: Notes
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel notesLabel = new JLabel("Ghi chú:");
        notesLabel.setFont(labelFont);
        notesLabel.setForeground(labelColor);
        mainPanel.add(notesLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(new JScrollPane(notesArea), gbc);
        
        // Add scroll pane for main content
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Footer with buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(new Color(248, 249, 250));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        footerPanel.add(saveButton);
        footerPanel.add(Box.createHorizontalStrut(10));
        footerPanel.add(cancelButton);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveUser();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void saveUser() {
        try {
            // Validate required fields
            if (emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập email", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (fullNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập họ và tên", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (studentIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã sinh viên", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validate email format
            if (!ValidationUtil.isValidEmail(emailField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Email không hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if email already exists
            if (userService.getUserByEmail(emailField.getText().trim()) != null) {
                JOptionPane.showMessageDialog(this, "Email đã tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if student ID already exists
            if (userService.getUserByStudentId(studentIdField.getText().trim()) != null) {
                JOptionPane.showMessageDialog(this, "Mã sinh viên đã tồn tại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create user
            User user = new User();
            user.setUserId("user_" + System.currentTimeMillis());
            user.setEmail(emailField.getText().trim());
            user.setPassword(PasswordUtil.hashPassword(new String(passwordField.getPassword())));
            user.setFullName(fullNameField.getText().trim());
            user.setFirstName(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setStudentId(studentIdField.getText().trim());            user.setFaculty(facultyField.getText().trim());
            user.setYearOfStudy(String.valueOf((Integer) yearOfStudyComboBox.getSelectedItem()));
            user.setRole((User.Role) roleComboBox.getSelectedItem());
            user.setStatus((User.Status) statusComboBox.getSelectedItem());
            user.setPhone(phoneField.getText().trim());
            user.setAddress(addressArea.getText().trim());
            user.setNotes(notesArea.getText().trim());
            user.setTotalBorrowed(0);
            user.setCurrentBorrowed(0);
            user.setTotalFines(0.0);
            user.setDateOfBirth(LocalDate.of(2000, 1, 1)); // Default date
            user.setRegistrationDate(LocalDate.now());
            user.setLastLogin(LocalDate.now());
            
            // Save user
            boolean success = userService.addUser(user);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Thêm người dùng thành công", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể thêm người dùng", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi thêm người dùng: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi thêm người dùng: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
