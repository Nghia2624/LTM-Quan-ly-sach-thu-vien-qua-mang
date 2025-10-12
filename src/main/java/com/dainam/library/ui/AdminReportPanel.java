package com.dainam.library.ui;

import com.dainam.library.model.Book;
import com.dainam.library.model.BookCopy;
import com.dainam.library.model.User;
import com.dainam.library.model.BorrowRecord;
import com.dainam.library.service.BookService;
import com.dainam.library.service.UserService;
import com.dainam.library.service.BorrowService;
import com.dainam.library.util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Panel báo cáo và thống kê cho Admin
 */
public class AdminReportPanel extends JPanel {
    
    private BookService bookService;
    private UserService userService;
    private BorrowService borrowService;
    
    private JTabbedPane tabbedPane;
    private JTable booksReportTable;
    private JTable usersReportTable;
    private JTable borrowReportTable;
    private JTable fineReportTable;
    
    private JComboBox<String> periodComboBox;
    private JButton generateReportButton;
    private JButton exportButton;
    private JLabel statusLabel;
    
    public AdminReportPanel() {
        this.bookService = new BookService();
        this.userService = new UserService();
        this.borrowService = new BorrowService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        generateReports();
    }
    
    private void initializeComponents() {
        // Period selection
        periodComboBox = new JComboBox<>();
        periodComboBox.addItem("Hôm nay");
        periodComboBox.addItem("Tuần này");
        periodComboBox.addItem("Tháng này");
        periodComboBox.addItem("Quý này");
        periodComboBox.addItem("Năm nay");
        periodComboBox.addItem("Tất cả");
        
        // Buttons
        generateReportButton = new JButton("Tạo báo cáo");
        exportButton = new JButton("Xuất Excel");
        
        // Tables
        booksReportTable = createBooksReportTable();
        usersReportTable = createUsersReportTable();
        borrowReportTable = createBorrowReportTable();
        fineReportTable = createFineReportTable();
        
        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Báo cáo sách", new JScrollPane(booksReportTable));
        tabbedPane.addTab("Báo cáo người dùng", new JScrollPane(usersReportTable));
        tabbedPane.addTab("Báo cáo mượn/trả", new JScrollPane(borrowReportTable));
        tabbedPane.addTab("Báo cáo phạt", new JScrollPane(fineReportTable));
        
        // Status label
        statusLabel = new JLabel("Sẵn sàng tạo báo cáo");
        statusLabel.setForeground(Color.BLUE);
    }
    
    private JTable createBooksReportTable() {
        String[] columns = {"Thể loại", "Tổng sách", "Có sẵn", "Đang mượn", "Bị mất", "Bị hỏng", "Tỷ lệ mượn (%)"};
        return new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }
    
    private JTable createUsersReportTable() {
        String[] columns = {"Khoa", "Tổng người dùng", "Hoạt động", "Bị khóa", "Sách đang mượn", "Tổng phạt (VND)"};
        return new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }
    
    private JTable createBorrowReportTable() {
        String[] columns = {"Ngày", "Số lượt mượn", "Số lượt trả", "Số lượt quá hạn", "Tỷ lệ trả đúng hạn (%)"};
        return new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }
    
    private JTable createFineReportTable() {
        String[] columns = {"Người dùng", "Số phạt", "Tổng tiền phạt (VND)", "Đã thanh toán (VND)", "Còn nợ (VND)", "Trạng thái"};
        return new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel - Controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Reports
        add(tabbedPane, BorderLayout.CENTER);
        
        // Bottom panel - Status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tùy chọn báo cáo"));
        
        // Controls row
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsRow.add(new JLabel("Khoảng thời gian:"));
        controlsRow.add(periodComboBox);
        controlsRow.add(generateReportButton);
        controlsRow.add(exportButton);
        
        panel.add(controlsRow, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(statusLabel);
        return panel;
    }
    
    private void setupEventHandlers() {
        generateReportButton.addActionListener(e -> generateReports());
        exportButton.addActionListener(e -> exportReports());
    }
    
    public void generateReports() {
        try {
            statusLabel.setText("Đang tạo báo cáo...");
            statusLabel.setForeground(Color.BLUE);
            
            SwingUtilities.invokeLater(() -> {
                try {
                    generateBooksReport();
                    generateUsersReport();
                    generateBorrowReport();
                    generateFineReport();
                    
                    statusLabel.setText("Báo cáo đã được tạo thành công");
                    statusLabel.setForeground(Color.GREEN);
                } catch (Exception e) {
                    LoggerUtil.error("Lỗi tạo báo cáo: " + e.getMessage());
                    statusLabel.setText("Lỗi tạo báo cáo: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            });
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo báo cáo: " + e.getMessage());
            statusLabel.setText("Lỗi tạo báo cáo: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void generateBooksReport() {
        try {
            DefaultTableModel model = (DefaultTableModel) booksReportTable.getModel();
            model.setRowCount(0);
            
            List<String> categories = bookService.getCategories();
            for (String category : categories) {
                List<Book> books = bookService.getBooks(1, 1000, category);
                
                int totalBooks = 0;
                int availableBooks = 0;
                int borrowedBooks = 0;
                int lostBooks = 0;
                int damagedBooks = 0;
                
                for (Book book : books) {
                    // Lấy thông tin thực tế từ database
                    List<BookCopy> copies = bookService.getBookCopies(book.getBookId());
                    
                    for (BookCopy copy : copies) {
                        totalBooks++;
                        switch (copy.getStatus()) {
                            case AVAILABLE:
                                availableBooks++;
                                break;
                            case BORROWED:
                                borrowedBooks++;
                                break;
                            case LOST:
                                lostBooks++;
                                break;
                            case DAMAGED:
                                damagedBooks++;
                                break;
                        }
                    }
                }
                
                double borrowRate = totalBooks > 0 ? (double) borrowedBooks / totalBooks * 100 : 0;
                
                Object[] row = {
                    category,
                    totalBooks,
                    availableBooks,
                    borrowedBooks,
                    lostBooks,
                    damagedBooks,
                    String.format("%.1f", borrowRate)
                };
                model.addRow(row);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo báo cáo sách: " + e.getMessage());
        }
    }
      private void generateUsersReport() {
        try {
            DefaultTableModel model = (DefaultTableModel) usersReportTable.getModel();
            model.setRowCount(0);
            
            // Lấy thống kê theo khoa từ UserService
            List<UserService.FacultyStatistics> facultyStats = userService.getFacultyStatistics();
            
            if (facultyStats.isEmpty()) {
                // Nếu không có dữ liệu, thêm một dòng thông báo
                Object[] row = {
                    "Không có dữ liệu",
                    0, 0, 0, 0, "0"
                };
                model.addRow(row);
            } else {
                for (UserService.FacultyStatistics stats : facultyStats) {
                    Object[] row = {
                        stats.facultyName != null ? stats.facultyName : "Chưa phân khoa",
                        stats.totalUsers,
                        stats.activeUsers,
                        stats.inactiveUsers,
                        stats.totalCurrentBorrowed,
                        String.format("%.0f", stats.totalFines)
                    };
                    model.addRow(row);
                }
            }
            
            LoggerUtil.info("Tạo báo cáo người dùng thành công với " + facultyStats.size() + " khoa");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo báo cáo người dùng: " + e.getMessage());
            e.printStackTrace();
            
            // Thêm dòng lỗi vào bảng
            DefaultTableModel model = (DefaultTableModel) usersReportTable.getModel();
            model.setRowCount(0);
            Object[] errorRow = {
                "Lỗi tải dữ liệu",
                0, 0, 0, 0, "0"
            };
            model.addRow(errorRow);
        }
    }
      private void generateBorrowReport() {
        try {
            DefaultTableModel model = (DefaultTableModel) borrowReportTable.getModel();
            model.setRowCount(0);
            
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            // Generate report for last 7 days để giảm tải
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                
                try {
                    List<BorrowRecord> dayRecords = borrowService.getBorrowRecordsByDate(date);
                    
                    int borrowed = 0;
                    int returned = 0;
                    int overdue = 0;
                    
                    for (BorrowRecord record : dayRecords) {
                        if (record.getBorrowDate() != null && record.getBorrowDate().equals(date)) {
                            borrowed++;
                        }
                        if (record.getActualReturnDate() != null && record.getActualReturnDate().equals(date)) {
                            returned++;
                        }
                        if (record.getStatus() == BorrowRecord.Status.OVERDUE) {
                            overdue++;
                        }
                    }
                    
                    double onTimeRate = returned > 0 ? (double) (returned - overdue) / returned * 100 : 100;
                    
                    Object[] row = {
                        date.format(formatter),
                        borrowed,
                        returned,
                        overdue,
                        String.format("%.1f", onTimeRate)
                    };
                    model.addRow(row);
                } catch (Exception e) {
                    // Nếu có lỗi với ngày cụ thể, thêm dòng 0
                    Object[] row = {
                        date.format(formatter),
                        0, 0, 0, "100.0"
                    };
                    model.addRow(row);
                }
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo báo cáo mượn/trả: " + e.getMessage());
            e.printStackTrace();
        }
    }
      private void generateFineReport() {
        try {
            DefaultTableModel model = (DefaultTableModel) fineReportTable.getModel();
            model.setRowCount(0);
            
            List<User> users = userService.getAllUsers();
            
            for (User user : users) {
                if (user.getTotalFines() > 0) {
                    try {
                        // Get fine details for this user
                        List<BorrowRecord> userRecords = borrowService.getBorrowRecordsByUser(user.getUserId());
                        double totalFines = 0;
                        double paidFines = 0;
                        int fineCount = 0;
                        
                        for (BorrowRecord record : userRecords) {
                            if (record.getFineAmount() > 0) {
                                totalFines += record.getFineAmount();
                                if (record.isFinePaid()) {
                                    paidFines += record.getFineAmount();
                                }
                                fineCount++;
                            }
                        }
                        
                        double remainingFines = totalFines - paidFines;
                        String status = remainingFines > 0 ? "Còn nợ" : "Đã thanh toán";
                        
                        Object[] row = {
                            user.getFullName() != null ? user.getFullName() : "Không xác định",
                            fineCount,
                            String.format("%.0f", totalFines),
                            String.format("%.0f", paidFines),
                            String.format("%.0f", remainingFines),
                            status
                        };
                        model.addRow(row);
                    } catch (Exception e) {
                        // Nếu có lỗi với user cụ thể, vẫn hiển thị thông tin cơ bản
                        Object[] row = {
                            user.getFullName() != null ? user.getFullName() : "Không xác định",
                            0,
                            String.format("%.0f", user.getTotalFines()),
                            "0",
                            String.format("%.0f", user.getTotalFines()),
                            "Chưa xác định"
                        };
                        model.addRow(row);
                    }
                }
            }
            
            if (model.getRowCount() == 0) {
                Object[] row = {
                    "Không có phạt nào",
                    0, "0", "0", "0", "Không có"
                };
                model.addRow(row);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo báo cáo phạt: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void exportReports() {
        JOptionPane.showMessageDialog(this, "Tính năng xuất Excel đang được phát triển", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
      public void refresh() {
        generateReports();
    }
}