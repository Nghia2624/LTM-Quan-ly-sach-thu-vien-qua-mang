package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.service.UserService;
import com.dainam.library.util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel quản lý người dùng cho Admin
 */
public class AdminUserManagementPanel extends JPanel {
    
    private UserService userService;
    private JTable usersTable;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> roleFilter;
    private JLabel statusLabel;
    private JButton refreshButton;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton lockUserButton;
    private JButton unlockUserButton;
    private JButton resetPasswordButton;
    
    public AdminUserManagementPanel() {
        this.userService = new UserService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refresh();
    }
    
    private void initializeComponents() {
        // Search components with better styling
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.setToolTipText("Tìm kiếm theo tên, email, hoặc mã sinh viên");
        
        statusFilter = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE", "LOCKED"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        roleFilter = new JComboBox<>(new String[]{"Tất cả", "ADMIN", "USER"});
        roleFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
          // Table
        String[] columns = {"ID", "Email", "Họ tên", "Mã SV", "Khoa", "Năm học", "Vai trò", "Trạng thái", "Số sách mượn", "Tổng phạt", "Ngày đăng ký"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        usersTable = new JTable(model);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getTableHeader().setReorderingAllowed(false);
        usersTable.setRowHeight(25);
        
        // Buttons
        refreshButton = createStyledButton("Làm mới", new Color(52, 152, 219));
        addUserButton = createStyledButton("Thêm người dùng", new Color(39, 174, 96));
        editUserButton = createStyledButton("Sửa thông tin", new Color(241, 196, 15));
        deleteUserButton = createStyledButton("Xóa người dùng", new Color(231, 76, 60));
        lockUserButton = createStyledButton("Khóa tài khoản", new Color(230, 126, 34));
        unlockUserButton = createStyledButton("Mở khóa tài khoản", new Color(46, 204, 113));
        resetPasswordButton = createStyledButton("Đặt lại mật khẩu", new Color(155, 89, 182));
        
        // Status label
        statusLabel = new JLabel("Quản lý người dùng hệ thống");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(52, 73, 94));
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 35));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Tìm kiếm và bộ lọc"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        panel.add(statusLabel, BorderLayout.WEST);
        
        // Search panel with better spacing
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchLabel.setForeground(new Color(52, 73, 94));
        
        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
        
        JLabel roleLabel = new JLabel("Vai trò:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(52, 73, 94));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(statusLabel);
        searchPanel.add(statusFilter);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(roleLabel);
        searchPanel.add(roleFilter);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(refreshButton);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Danh sách người dùng"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        panel.add(addUserButton);
        panel.add(editUserButton);
        panel.add(deleteUserButton);
        panel.add(lockUserButton);
        panel.add(unlockUserButton);
        panel.add(resetPasswordButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> refresh());
        addUserButton.addActionListener(e -> showAddUserDialog());
        editUserButton.addActionListener(e -> showEditUserDialog());
        deleteUserButton.addActionListener(e -> deleteUser());
        lockUserButton.addActionListener(e -> lockUser());
        unlockUserButton.addActionListener(e -> unlockUser());
        resetPasswordButton.addActionListener(e -> resetPassword());
        
        searchField.addActionListener(e -> performSearch());
        statusFilter.addActionListener(e -> performSearch());
        roleFilter.addActionListener(e -> performSearch());
        
        // Table selection listener
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = usersTable.getSelectedRow() >= 0;
            editUserButton.setEnabled(hasSelection);
            deleteUserButton.setEnabled(hasSelection);
            lockUserButton.setEnabled(hasSelection);
            unlockUserButton.setEnabled(hasSelection);
            resetPasswordButton.setEnabled(hasSelection);
        });
    }
    
    public void refresh() {
        try {
            loadUsers();
            updateStatus();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới danh sách người dùng: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi làm mới danh sách: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
      private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        model.setRowCount(0);
        
        List<User> users = userService.getAllUsers(); // Get all users
        
        // Sắp xếp: Admin trước, sau đó Users theo userId
        users.sort((u1, u2) -> {
            // Admin luôn đầu tiên
            if (u1.getRole() == User.Role.ADMIN && u2.getRole() != User.Role.ADMIN) {
                return -1;
            }
            if (u1.getRole() != User.Role.ADMIN && u2.getRole() == User.Role.ADMIN) {
                return 1;
            }
            // Cùng role thì sắp xếp theo userId
            return u1.getUserId().compareTo(u2.getUserId());
        });
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (User user : users) {
            Object[] row;
            if (user.getRole() == User.Role.ADMIN) {
                // Admin: không hiển thị Mã SV, Khoa, Năm học, Số sách mượn, Tổng phạt
                row = new Object[]{
                    user.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    "N/A", // Mã SV
                    "N/A", // Khoa
                    "N/A", // Năm học
                    "ADMIN",
                    user.getStatus().toString(),
                    "N/A", // Số sách mượn
                    "N/A", // Tổng phạt
                    user.getRegistrationDate().format(formatter)
                };
            } else {
                // User: hiển thị đầy đủ thông tin
                row = new Object[]{
                    user.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getStudentId() != null ? user.getStudentId() : "N/A",
                    user.getFaculty() != null ? user.getFaculty() : "N/A",
                    user.getYearOfStudy() != null ? user.getYearOfStudy() : "N/A",
                    "USER",
                    user.getStatus().toString(),
                    String.valueOf(user.getCurrentBorrowed()),
                    String.format("%.0f VND", user.getTotalFines()),
                    user.getRegistrationDate().format(formatter)
                };
            }
            model.addRow(row);
        }
    }
      private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String statusFilterValue = (String) statusFilter.getSelectedItem();
        String roleFilterValue = (String) roleFilter.getSelectedItem();
        
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        model.setRowCount(0);
        
        List<User> users = userService.getAllUsers();
        
        // Sắp xếp: Admin trước, sau đó Users theo userId
        users.sort((u1, u2) -> {
            // Admin luôn đầu tiên
            if (u1.getRole() == User.Role.ADMIN && u2.getRole() != User.Role.ADMIN) {
                return -1;
            }
            if (u1.getRole() != User.Role.ADMIN && u2.getRole() == User.Role.ADMIN) {
                return 1;
            }
            // Cùng role thì sắp xếp theo userId
            return u1.getUserId().compareTo(u2.getUserId());
        });
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (User user : users) {
            // Apply filters
            if (!statusFilterValue.equals("Tất cả") && !user.getStatus().toString().equals(statusFilterValue)) {
                continue;
            }
            if (!roleFilterValue.equals("Tất cả") && !user.getRole().toString().equals(roleFilterValue)) {
                continue;
            }
            if (!query.isEmpty()) {
                String searchText = (user.getFullName() + " " + user.getEmail() + " " + 
                    (user.getStudentId() != null ? user.getStudentId() : "")).toLowerCase();
                if (!searchText.contains(query)) {
                    continue;
                }
            }
            
            Object[] row;
            if (user.getRole() == User.Role.ADMIN) {
                // Admin: không hiển thị Mã SV, Khoa, Năm học, Số sách mượn, Tổng phạt
                row = new Object[]{
                    user.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    "N/A", // Mã SV
                    "N/A", // Khoa
                    "N/A", // Năm học
                    "ADMIN",
                    user.getStatus().toString(),
                    "N/A", // Số sách mượn
                    "N/A", // Tổng phạt
                    user.getRegistrationDate().format(formatter)
                };
            } else {
                // User: hiển thị đầy đủ thông tin
                row = new Object[]{
                    user.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getStudentId() != null ? user.getStudentId() : "N/A",
                    user.getFaculty() != null ? user.getFaculty() : "N/A",
                    user.getYearOfStudy() != null ? user.getYearOfStudy() : "N/A",
                    "USER",
                    user.getStatus().toString(),
                    String.valueOf(user.getCurrentBorrowed()),
                    String.format("%.0f VND", user.getTotalFines()),
                    user.getRegistrationDate().format(formatter)
                };
            }
            model.addRow(row);
        }
    }
    
    public void showAddUserDialog() {
        AddUserDialog dialog = new AddUserDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        refresh();
    }
    
    private void showEditUserDialog() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần sửa", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String userId = (String) usersTable.getValueAt(selectedRow, 0);
        User user = userService.getUserById(userId);
        
        if (user != null) {
            EditUserDialog dialog = new EditUserDialog((JFrame) SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);
            refresh();
        }
    }
    
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần xóa", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String userId = (String) usersTable.getValueAt(selectedRow, 0);
        String userName = (String) usersTable.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa người dùng '" + userName + "'?", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                boolean success = userService.deleteUser(userId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa người dùng thành công", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa người dùng", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi xóa người dùng: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi xóa người dùng: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void lockUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần khóa", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String userId = (String) usersTable.getValueAt(selectedRow, 0);
        String userName = (String) usersTable.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn khóa tài khoản '" + userName + "'?", 
            "Xác nhận khóa", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                User user = userService.getUserById(userId);
                if (user != null) {
                    user.setStatus(User.Status.LOCKED);
                    boolean success = userService.updateUser(user);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Khóa tài khoản thành công", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Không thể khóa tài khoản", 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi khóa tài khoản: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi khóa tài khoản: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void unlockUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần mở khóa", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String userId = (String) usersTable.getValueAt(selectedRow, 0);
        String userName = (String) usersTable.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn mở khóa tài khoản '" + userName + "'?", 
            "Xác nhận mở khóa", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                User user = userService.getUserById(userId);
                if (user != null) {
                    user.setStatus(User.Status.ACTIVE);
                    boolean success = userService.updateUser(user);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Mở khóa tài khoản thành công", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Không thể mở khóa tài khoản", 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi mở khóa tài khoản: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi mở khóa tài khoản: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void resetPassword() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng cần đặt lại mật khẩu", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String userId = (String) usersTable.getValueAt(selectedRow, 0);
        String userName = (String) usersTable.getValueAt(selectedRow, 2);
        
        String newPassword = JOptionPane.showInputDialog(this, 
            "Nhập mật khẩu mới cho '" + userName + "':", 
            "Đặt lại mật khẩu", JOptionPane.QUESTION_MESSAGE);
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            try {
                User user = userService.getUserById(userId);
                if (user != null) {
                    user.setPassword(com.dainam.library.util.PasswordUtil.hashPassword(newPassword));
                    boolean success = userService.updateUser(user);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thành công", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Không thể đặt lại mật khẩu", 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi đặt lại mật khẩu: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi đặt lại mật khẩu: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateStatus() {
        int totalUsers = usersTable.getRowCount();
        int selectedRow = usersTable.getSelectedRow();
        
        if (selectedRow >= 0) {
            String userName = (String) usersTable.getValueAt(selectedRow, 2);
            statusLabel.setText("Đã chọn: " + userName + " | Tổng: " + totalUsers + " người dùng");
        } else {
            statusLabel.setText("Tổng: " + totalUsers + " người dùng");
        }
    }
}