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

/**
 * Dialog chỉnh sửa thông tin người dùng
 */
public class EditUserDialog extends JDialog {
    
    private UserService userService;
    private User user;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField studentIdField;
    private JComboBox<String> facultyComboBox;
    private JComboBox<Integer> yearOfStudyComboBox;
    private JComboBox<User.Role> roleComboBox;
    private JComboBox<User.Status> statusComboBox;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JTextArea notesArea;
    private JButton saveButton;
    private JButton cancelButton;
    private JCheckBox changePasswordCheckBox;
    
    public EditUserDialog(JFrame parent, User user) {
        super(parent, "Chỉnh sửa thông tin người dùng", true);
        this.userService = new UserService();
        this.user = user;
        initializeComponents();
        populateFields();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(750, 800);
        setLocationRelativeTo(parent);
        setResizable(true);
    }
    
    private void initializeComponents() {
        // Text fields with modern styling
        emailField = new JTextField(25);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        emailField.setEditable(false);
        emailField.setBackground(new Color(248, 249, 250));
        
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
        
        // Combo boxes for standardized data
        String[] faculties = {
            "Công nghệ thông tin",
            "Kinh tế",
            "Ngoại ngữ", 
            "Kỹ thuật",
            "Y học",
            "Khoa học tự nhiên",
            "Khoa học xã hội",
            "Nghệ thuật"
        };
        facultyComboBox = new JComboBox<>(faculties);
        facultyComboBox.setEditable(true);
        facultyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
          // Year of study combo box
        Integer[] years = {2021, 2022, 2023, 2024, 2025, 2026};
        yearOfStudyComboBox = new JComboBox<>(years);
        yearOfStudyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        phoneField = new JTextField(25);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Combo boxes with modern styling
        roleComboBox = new JComboBox<>(User.Role.values());
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        statusComboBox = new JComboBox<>(User.Status.values());
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Text areas with modern styling
        addressArea = new JTextArea(3, 25);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        notesArea = new JTextArea(3, 25);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Checkbox for password change
        changePasswordCheckBox = new JCheckBox("Thay đổi mật khẩu");
        changePasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Buttons
        saveButton = new JButton("Lưu thay đổi");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveButton.setPreferredSize(new Dimension(120, 35));
        
        cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setPreferredSize(new Dimension(120, 35));
    }
    
    private void populateFields() {
        emailField.setText(user.getEmail());
        fullNameField.setText(user.getFullName());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        studentIdField.setText(user.getStudentId());
        facultyComboBox.setSelectedItem(user.getFaculty());
        yearOfStudyComboBox.setSelectedItem(user.getYearOfStudy());
        roleComboBox.setSelectedItem(user.getRole());
        statusComboBox.setSelectedItem(user.getStatus());
        phoneField.setText(user.getPhone());
        addressArea.setText(user.getAddress());
        notesArea.setText(user.getNotes());
        
        // Disable password field initially
        passwordField.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Chỉnh sửa thông tin: " + user.getFullName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
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
        
        // Row 2: Password with checkbox
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(labelColor);
        mainPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(passwordField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(changePasswordCheckBox, gbc);
        
        // Row 3: Full Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel fullNameLabel = new JLabel("Họ và tên *:");
        fullNameLabel.setFont(labelFont);
        fullNameLabel.setForeground(labelColor);
        mainPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(fullNameField, gbc);
        
        // Row 4: First Name and Last Name
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel firstNameLabel = new JLabel("Tên:");
        firstNameLabel.setFont(labelFont);
        firstNameLabel.setForeground(labelColor);
        mainPanel.add(firstNameLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(firstNameField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel lastNameLabel = new JLabel("Họ:");
        lastNameLabel.setFont(labelFont);
        lastNameLabel.setForeground(labelColor);
        mainPanel.add(lastNameLabel, gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(lastNameField, gbc);
        
        // Row 5: Student ID
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel studentIdLabel = new JLabel("Mã sinh viên *:");
        studentIdLabel.setFont(labelFont);
        studentIdLabel.setForeground(labelColor);
        mainPanel.add(studentIdLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(studentIdField, gbc);
        
        // Row 6: Faculty
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel facultyLabel = new JLabel("Khoa:");
        facultyLabel.setFont(labelFont);
        facultyLabel.setForeground(labelColor);
        mainPanel.add(facultyLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(facultyComboBox, gbc);
        
        // Row 7: Year of Study and Role
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel yearLabel = new JLabel("Năm học:");
        yearLabel.setFont(labelFont);
        yearLabel.setForeground(labelColor);
        mainPanel.add(yearLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(yearOfStudyComboBox, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel roleLabel = new JLabel("Vai trò *:");
        roleLabel.setFont(labelFont);
        roleLabel.setForeground(labelColor);
        mainPanel.add(roleLabel, gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(roleComboBox, gbc);
        
        // Row 8: Status and Phone
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel statusLabel = new JLabel("Trạng thái *:");
        statusLabel.setFont(labelFont);
        statusLabel.setForeground(labelColor);
        mainPanel.add(statusLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(statusComboBox, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(labelFont);
        phoneLabel.setForeground(labelColor);
        mainPanel.add(phoneLabel, gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(phoneField, gbc);
        
        // Row 9: Address
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(labelFont);
        addressLabel.setForeground(labelColor);
        mainPanel.add(addressLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(addressArea), gbc);
        
        // Row 10: Notes
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel notesLabel = new JLabel("Ghi chú:");
        notesLabel.setFont(labelFont);
        notesLabel.setForeground(labelColor);
        mainPanel.add(notesLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(notesArea), gbc);
        
        // Wrap main panel in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottomPanel.setBackground(new Color(248, 249, 250));
        bottomPanel.add(cancelButton);
        bottomPanel.add(saveButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
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
        
        changePasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.setEnabled(changePasswordCheckBox.isSelected());
                if (!changePasswordCheckBox.isSelected()) {
                    passwordField.setText("");
                }
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
            
            // Update user
            user.setEmail(emailField.getText().trim());
            user.setFullName(fullNameField.getText().trim());
            user.setFirstName(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());            user.setStudentId(studentIdField.getText().trim());
            user.setFaculty((String) facultyComboBox.getSelectedItem());
            user.setYearOfStudy(String.valueOf((Integer) yearOfStudyComboBox.getSelectedItem()));
            user.setRole((User.Role) roleComboBox.getSelectedItem());
            user.setStatus((User.Status) statusComboBox.getSelectedItem());
            user.setPhone(phoneField.getText().trim());
            user.setAddress(addressArea.getText().trim());
            user.setNotes(notesArea.getText().trim());
            
            // Update password if changing
            if (changePasswordCheckBox.isSelected()) {
                user.setPassword(PasswordUtil.hashPassword(new String(passwordField.getPassword())));
            }
            
            // Save user
            boolean success = userService.updateUser(user);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể cập nhật thông tin", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật người dùng: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật người dùng: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
