package com.dainam.library.ui;

import com.dainam.library.model.Book;
import com.dainam.library.model.BorrowRecord;
import com.dainam.library.model.User;
import com.dainam.library.service.BookService;
import com.dainam.library.service.BorrowService;
import com.dainam.library.service.FineService;
import com.dainam.library.service.UserService;
import com.dainam.library.util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel Dashboard cho Admin
 */
public class AdminDashboardPanel extends JPanel {
    
    private JLabel totalBooksLabel;
    private JLabel totalUsersLabel;
    private JLabel totalBorrowsLabel;
    private JLabel totalFinesLabel;
    private JLabel availableBooksLabel;
    private JLabel borrowedBooksLabel;
    private JLabel overdueBooksLabel;
    private JLabel pendingUsersLabel;
    
    private JTable recentBorrowsTable;
    private JTable recentUsersTable;
    
    private BookService bookService;
    private UserService userService;
    private BorrowService borrowService;
    private FineService fineService;
    
    public AdminDashboardPanel() {
        this.bookService = new BookService();
        this.userService = new UserService();
        this.borrowService = new BorrowService();
        this.fineService = new FineService();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refresh();
    }
    
    private void initializeComponents() {
        // Statistics labels
        totalBooksLabel = createStatLabel("0", "Tổng số sách");
        totalUsersLabel = createStatLabel("0", "Tổng số người dùng");
        totalBorrowsLabel = createStatLabel("0", "Tổng số lượt mượn");
        totalFinesLabel = createStatLabel("0", "Tổng số phạt");
        availableBooksLabel = createStatLabel("0", "Sách có sẵn");
        borrowedBooksLabel = createStatLabel("0", "Sách đang mượn");
        overdueBooksLabel = createStatLabel("0", "Sách quá hạn");
        pendingUsersLabel = createStatLabel("0", "Người dùng chờ duyệt");
        
        // Recent borrows table
        String[] borrowColumns = {"ID", "Người mượn", "Sách", "Ngày mượn", "Hạn trả", "Trạng thái"};
        recentBorrowsTable = new JTable(new Object[0][6], borrowColumns);
        recentBorrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentBorrowsTable.getTableHeader().setReorderingAllowed(false);
        
        // Recent users table
        String[] userColumns = {"ID", "Họ tên", "Email", "Mã SV", "Khoa", "Trạng thái"};
        recentUsersTable = new JTable(new Object[0][6], userColumns);
        recentUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentUsersTable.getTableHeader().setReorderingAllowed(false);
    }
    
    private JLabel createStatLabel(String value, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setPreferredSize(new Dimension(150, 80));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(new Color(0, 120, 215));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.SOUTH);
        
        return valueLabel;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel - Statistics
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.NORTH);
        
        // Center panel - Charts and tables
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Quick actions
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        
        // Create stat panels
        panel.add(createStatPanel(totalBooksLabel, "Tổng số sách"));
        panel.add(createStatPanel(totalUsersLabel, "Tổng số người dùng"));
        panel.add(createStatPanel(totalBorrowsLabel, "Tổng số lượt mượn"));
        panel.add(createStatPanel(totalFinesLabel, "Tổng số phạt"));
        panel.add(createStatPanel(availableBooksLabel, "Sách có sẵn"));
        panel.add(createStatPanel(borrowedBooksLabel, "Sách đang mượn"));
        panel.add(createStatPanel(overdueBooksLabel, "Sách quá hạn"));
        panel.add(createStatPanel(pendingUsersLabel, "Người dùng chờ duyệt"));
        
        return panel;
    }
    
    private JPanel createStatPanel(JLabel valueLabel, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setPreferredSize(new Dimension(150, 80));
        panel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(titleLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Recent borrows
        JPanel borrowsPanel = new JPanel(new BorderLayout());
        borrowsPanel.setBorder(BorderFactory.createTitledBorder("Lượt mượn gần đây"));
        
        JScrollPane borrowsScrollPane = new JScrollPane(recentBorrowsTable);
        borrowsScrollPane.setPreferredSize(new Dimension(400, 200));
        borrowsPanel.add(borrowsScrollPane, BorderLayout.CENTER);
        
        JButton refreshBorrowsButton = new JButton("Làm mới");
        refreshBorrowsButton.addActionListener(e -> refreshRecentBorrows());
        borrowsPanel.add(refreshBorrowsButton, BorderLayout.SOUTH);
        
        // Recent users
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBorder(BorderFactory.createTitledBorder("Người dùng mới"));
        
        JScrollPane usersScrollPane = new JScrollPane(recentUsersTable);
        usersScrollPane.setPreferredSize(new Dimension(400, 200));
        usersPanel.add(usersScrollPane, BorderLayout.CENTER);
        
        JButton refreshUsersButton = new JButton("Làm mới");
        refreshUsersButton.addActionListener(e -> refreshRecentUsers());
        usersPanel.add(refreshUsersButton, BorderLayout.SOUTH);
        
        panel.add(borrowsPanel);
        panel.add(usersPanel);
        
        return panel;
    }
    
    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Thao tác nhanh"));
        
        JButton addBookButton = new JButton("Thêm sách mới");
        addBookButton.addActionListener(e -> showAddBookDialog());
        
        JButton addUserButton = new JButton("Thêm người dùng");
        addUserButton.addActionListener(e -> showAddUserDialog());
        
        JButton viewReportsButton = new JButton("Xem báo cáo");
        viewReportsButton.addActionListener(e -> showReportsDialog());
        
        JButton systemSettingsButton = new JButton("Cài đặt hệ thống");
        systemSettingsButton.addActionListener(e -> showSystemSettings());
        
        panel.add(addBookButton);
        panel.add(addUserButton);
        panel.add(viewReportsButton);
        panel.add(systemSettingsButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Table double-click handlers
        recentBorrowsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showBorrowDetails();
                }
            }
        });
        
        recentUsersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showUserDetails();
                }
            }
        });
    }
    
    public void refresh() {
        try {
            // Update statistics
            updateStatistics();
            
            // Update tables
            refreshRecentBorrows();
            refreshRecentUsers();
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới dashboard: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi làm mới dữ liệu: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatistics() {        try {
            // Lấy dữ liệu thực tế từ MongoDB
            int totalBooks = bookService.getTotalBooks();
            int totalUsers = userService.getTotalUsers();
            long totalBorrows = borrowService.getTotalBorrows();
            double totalFines = fineService.getTotalFines();
            int availableBooks = bookService.getAvailableBooks();
            long borrowedBooks = borrowService.getBorrowedBooks();
            long overdueBooks = borrowService.getOverdueBooks();
            int pendingUsers = userService.getPendingUsers();
            
            // Cập nhật labels với dữ liệu thực tế
            totalBooksLabel.setText(String.valueOf(totalBooks));
            totalUsersLabel.setText(String.valueOf(totalUsers));
            totalBorrowsLabel.setText(String.valueOf(totalBorrows));
            totalFinesLabel.setText(String.format("%.0f VND", totalFines));
            availableBooksLabel.setText(String.valueOf(availableBooks));
            borrowedBooksLabel.setText(String.valueOf(borrowedBooks));
            overdueBooksLabel.setText(String.valueOf(overdueBooks));
            pendingUsersLabel.setText(String.valueOf(pendingUsers));
            
            LoggerUtil.info("Đã cập nhật thống kê: " + totalBooks + " sách, " + totalUsers + " users, " + totalBorrows + " mượn");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật thống kê: " + e.getMessage());
            // Set default values if error
            totalBooksLabel.setText("0");
            totalUsersLabel.setText("0");
            totalBorrowsLabel.setText("0");
            totalFinesLabel.setText("0 VND");
            availableBooksLabel.setText("0");
            borrowedBooksLabel.setText("0");
            overdueBooksLabel.setText("0");
            pendingUsersLabel.setText("0");
        }
    }
    
    private void refreshRecentBorrows() {
        try {
            // Lấy dữ liệu thực tế từ MongoDB
            List<BorrowRecord> recentBorrows = borrowService.getAllBorrowRecords(1, 10);
            UserService userService = new UserService();
            BookService bookService = new BookService();
            
            String[] columns = {"ID", "Người mượn", "Sách", "Ngày mượn", "Hạn trả", "Trạng thái"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (BorrowRecord record : recentBorrows) {
                User user = userService.getUserById(record.getUserId());
                Book book = bookService.getBookById(record.getBookId());
                
                String userName = user != null ? user.getFullName() : "Không xác định";
                String bookTitle = book != null ? book.getTitle() : "Không xác định";
                String status = getStatusText(record.getStatus());
                
                Object[] row = {
                    record.getRecordId(),
                    userName,
                    bookTitle,
                    record.getBorrowDate().format(formatter),
                    record.getExpectedReturnDate().format(formatter),
                    status
                };
                model.addRow(row);
            }
            
            recentBorrowsTable.setModel(model);
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới danh sách mượn: " + e.getMessage());
        }
    }
    
    private void refreshRecentUsers() {
        try {
            // Lấy dữ liệu thực tế từ MongoDB
            List<User> recentUsers = userService.getAllUsers();
            if (recentUsers.size() > 10) {
                recentUsers = recentUsers.subList(0, 10); // Lấy 10 user gần nhất
            }
            
            String[] columns = {"ID", "Họ tên", "Email", "Mã SV", "Khoa", "Trạng thái"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (User user : recentUsers) {
                String status = getUserStatusText(user.getStatus());
                
                Object[] row = {
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getStudentId(),
                    user.getFaculty(),
                    status
                };
                model.addRow(row);
            }
            
            recentUsersTable.setModel(model);
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới danh sách người dùng: " + e.getMessage());
        }
    }
    
    private String getStatusText(BorrowRecord.Status status) {
        switch (status) {
            case BORROWED: return "Đang mượn";
            case RETURNED: return "Đã trả";
            case OVERDUE: return "Quá hạn";
            case LOST: return "Bị mất";
            case DAMAGED: return "Bị hỏng";
            default: return "Không xác định";
        }
    }
    
    private String getUserStatusText(User.Status status) {
        switch (status) {
            case ACTIVE: return "Hoạt động";
            case PENDING: return "Chờ duyệt";
            case LOCKED: return "Bị khóa";
            default: return "Không xác định";
        }
    }
    
    private void showBorrowDetails() {
        int selectedRow = recentBorrowsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String recordId = (String) recentBorrowsTable.getValueAt(selectedRow, 0);
            JOptionPane.showMessageDialog(this, "Chi tiết bản ghi mượn: " + recordId, 
                "Thông tin", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showUserDetails() {
        int selectedRow = recentUsersTable.getSelectedRow();
        if (selectedRow >= 0) {
            String userId = (String) recentUsersTable.getValueAt(selectedRow, 0);
            JOptionPane.showMessageDialog(this, "Chi tiết người dùng: " + userId, 
                "Thông tin", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showAddBookDialog() {
        // Tìm AdminFrame parent để chuyển đến tab quản lý sách
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof AdminFrame) {
            AdminFrame adminFrame = (AdminFrame) parentFrame;
            adminFrame.switchToBookManagement();
        }
    }
    
    private void showAddUserDialog() {
        // Tìm AdminFrame parent để chuyển đến tab quản lý người dùng
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof AdminFrame) {
            AdminFrame adminFrame = (AdminFrame) parentFrame;
            adminFrame.switchToUserManagement();
        }
    }
    
    private void showReportsDialog() {
        // Tìm AdminFrame parent để chuyển đến tab báo cáo
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof AdminFrame) {
            AdminFrame adminFrame = (AdminFrame) parentFrame;
            adminFrame.switchToReports();
        }
    }
    
    private void showSystemSettings() {
        JOptionPane.showMessageDialog(this, "Tính năng cài đặt hệ thống đang được phát triển", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
