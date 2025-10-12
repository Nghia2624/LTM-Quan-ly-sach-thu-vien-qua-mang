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
 * Dialog đăng ký tài khoản mới
 */
public class RegisterDialog extends JDialog {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;    private JTextField studentIdField;
    private JComboBox<String> facultyComboBox;
    private JComboBox<Integer> yearOfStudyComboBox;
    private JTextArea addressArea;    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    private UserService userService;

    public RegisterDialog(JFrame parent) {
        super(parent, "Đăng ký tài khoản", true);
        this.userService = new UserService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setSize(550, 700);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initializeComponents() {
        // Text fields
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        phoneField = new JTextField(20);
        studentIdField = new JTextField(20);

        // Combo boxes for standardized data
        String[] faculties = {
                "Công nghệ thông tin",
                "Kinh tế",
                "Ngoại ngữ",
                "Kỹ thuật",
                "Y học",
                "Khoa học tự nhiên",
                "Khoa học xã hội",
                "Nghệ thuật"        };
        facultyComboBox = new JComboBox<>(faculties);
        facultyComboBox.setEditable(true);        // Year of study combo box
        Integer[] years = {2021, 2022, 2023, 2024, 2025, 2026};
        yearOfStudyComboBox = new JComboBox<>(years);

        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);        // Buttons
        registerButton = new JButton("Đăng ký");
        registerButton.setPreferredSize(new Dimension(100, 35));

        cancelButton = new JButton("Hủy");
        cancelButton.setPreferredSize(new Dimension(100, 35));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
        JLabel titleLabel = new JLabel("Thông tin đăng ký");
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

        // Email
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Email *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);
        row++;

        // Password
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Mật khẩu *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);
        row++;

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Xác nhận mật khẩu *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);
        row++;

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

        // Phone
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Số điện thoại *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(phoneField, gbc);
        row++;        // Student ID with hint
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Mã sinh viên *:"), gbc);
        gbc.gridx = 1;
        
        JPanel studentIdPanel = new JPanel(new BorderLayout());
        studentIdPanel.add(studentIdField, BorderLayout.CENTER);
        
        JLabel hintLabel = new JLabel("(1671020000 - 1671029999)");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        hintLabel.setForeground(Color.GRAY);
        studentIdPanel.add(hintLabel, BorderLayout.SOUTH);
        
        mainPanel.add(studentIdPanel, gbc);
        row++;        // Faculty
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Khoa:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(facultyComboBox, gbc);
        row++;

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
        buttonPanel.add(registerButton);
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
    }

    private void setupEventHandlers() {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegister();
            }
        });        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void performRegister() {
        // Get input values
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();        String phone = phoneField.getText().trim();        String studentId = studentIdField.getText().trim();
        String faculty = (String) facultyComboBox.getSelectedItem();
        String yearOfStudy = String.valueOf((Integer) yearOfStudyComboBox.getSelectedItem());
        String address = addressArea.getText().trim();

        // Validate required fields
        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() ||
                lastName.isEmpty() || phone.isEmpty() || studentId.isEmpty()) {
            showStatus("Vui lòng điền đầy đủ các trường bắt buộc (*)", true);
            return;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            showStatus("Mật khẩu xác nhận không khớp", true);
            return;
        }

        // Validate input format
        String validationError = ValidationUtil.validateUserRegistration(
                email, password, firstName, lastName, phone, studentId);

        if (validationError != null) {
            showStatus(validationError, true);
            return;
        }

        // Show loading
        registerButton.setEnabled(false);
        registerButton.setText("Đang đăng ký...");
        statusLabel.setText("Đang tạo tài khoản...");

        // Perform registration in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create user object
                User user = new User();
                user.setEmail(email);
                user.setPassword(password);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setPhone(phone);                user.setStudentId(studentId);
                user.setFaculty(faculty);
                user.setYearOfStudy(yearOfStudy);
                user.setAddress(address);

                boolean success = userService.register(user);

                if (success) {
                    showStatus("Đăng ký thành công! Vui lòng chờ admin duyệt tài khoản.", false);

                    // Clear form
                    clearForm();

                    // Close dialog after 2 seconds
                    Timer timer = new Timer(2000, e -> dispose());
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    showStatus("Đăng ký thất bại. Vui lòng thử lại.", true);
                }
            } catch (Exception ex) {
                LoggerUtil.error("Lỗi đăng ký: " + ex.getMessage());
                showStatus("Lỗi đăng ký: " + ex.getMessage(), true);
            } finally {
                registerButton.setEnabled(true);
                registerButton.setText("Đăng ký");
            }
        });
    }

    private void clearForm() {
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        phoneField.setText("");        studentIdField.setText("");
        facultyComboBox.setSelectedIndex(0);
        yearOfStudyComboBox.setSelectedIndex(0);
        addressArea.setText("");
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : Color.GREEN);    }
}
