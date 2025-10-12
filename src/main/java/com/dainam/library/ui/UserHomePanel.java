package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.model.Book;
import com.dainam.library.model.BorrowRecord;
import com.dainam.library.service.BookService;
import com.dainam.library.service.BorrowService;
import com.dainam.library.util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel trang chủ cho User
 */
public class UserHomePanel extends JPanel {
    
    private User currentUser;
    private JLabel welcomeLabel;
    private JLabel statsLabel;
    private JTable recentBooksTable;
    private JTable currentBorrowsTable;
    
    public UserHomePanel(User user) {
        this.currentUser = user;
        initializeComponents();
        setupLayout();
        refresh();
    }
    
    private void initializeComponents() {
        // Welcome label
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Stats label
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Recent books table
        String[] bookColumns = {"Tiêu đề", "Tác giả", "Thể loại", "Năm xuất bản", "Trạng thái"};
        recentBooksTable = new JTable(new Object[0][5], bookColumns);
        recentBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentBooksTable.getTableHeader().setReorderingAllowed(false);
        
        // Current borrows table
        String[] borrowColumns = {"Sách", "Ngày mượn", "Hạn trả", "Trạng thái", "Phạt"};
        currentBorrowsTable = new JTable(new Object[0][5], borrowColumns);
        currentBorrowsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentBorrowsTable.getTableHeader().setReorderingAllowed(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel - Welcome and stats
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Tables
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Quick actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Welcome message
        String welcomeText = "Chào mừng " + currentUser.getFullName() + " đến với hệ thống thư viện!";
        welcomeLabel.setText(welcomeText);
          // Stats - only show for regular users, not for admin
        if (currentUser.getRole() == User.Role.USER) {
            String statsText = String.format("Sách đang mượn: %d | Tổng lượt mượn: %d | Phạt: %.0f VND", 
                currentUser.getCurrentBorrowed(), currentUser.getTotalBorrowed(), currentUser.getTotalFines());
            statsLabel.setText(statsText);
        } else {
            statsLabel.setText("Quản trị viên hệ thống - Quản lý thư viện");
        }
        
        panel.add(welcomeLabel, BorderLayout.NORTH);
        panel.add(statsLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Recent books
        JPanel booksPanel = new JPanel(new BorderLayout());
        booksPanel.setBorder(BorderFactory.createTitledBorder("Sách mới"));
        
        JScrollPane booksScrollPane = new JScrollPane(recentBooksTable);
        booksScrollPane.setPreferredSize(new Dimension(400, 200));
        booksPanel.add(booksScrollPane, BorderLayout.CENTER);
        
        JButton viewAllBooksButton = new JButton("Xem tất cả sách");
        viewAllBooksButton.addActionListener(e -> showAllBooks());
        booksPanel.add(viewAllBooksButton, BorderLayout.SOUTH);
        
        // Current borrows
        JPanel borrowsPanel = new JPanel(new BorderLayout());
        borrowsPanel.setBorder(BorderFactory.createTitledBorder("Sách đang mượn"));
        
        JScrollPane borrowsScrollPane = new JScrollPane(currentBorrowsTable);
        borrowsScrollPane.setPreferredSize(new Dimension(400, 200));
        borrowsPanel.add(borrowsScrollPane, BorderLayout.CENTER);
        
        JButton manageBorrowsButton = new JButton("Quản lý mượn sách");
        manageBorrowsButton.addActionListener(e -> showBorrowManagement());
        borrowsPanel.add(manageBorrowsButton, BorderLayout.SOUTH);
        
        panel.add(booksPanel);
        panel.add(borrowsPanel);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Thao tác nhanh"));
        
        JButton searchBooksButton = new JButton("Tìm kiếm sách");
        searchBooksButton.addActionListener(e -> showBookSearch());
        
        JButton borrowBookButton = new JButton("Mượn sách");
        borrowBookButton.addActionListener(e -> showBorrowBook());
        
        JButton viewProfileButton = new JButton("Thông tin cá nhân");
        viewProfileButton.addActionListener(e -> showProfile());
        
        JButton viewHistoryButton = new JButton("Lịch sử mượn");
        viewHistoryButton.addActionListener(e -> showBorrowHistory());
        
        panel.add(searchBooksButton);
        panel.add(borrowBookButton);
        panel.add(viewProfileButton);
        panel.add(viewHistoryButton);
        
        return panel;
    }
    
    public void refresh() {
        try {
            // Update welcome message
            String welcomeText = "Chào mừng " + currentUser.getFullName() + " đến với hệ thống thư viện!";
            welcomeLabel.setText(welcomeText);
              // Update stats - only show for regular users, not for admin
            if (currentUser.getRole() == User.Role.USER) {
                String statsText = String.format("Sách đang mượn: %d | Tổng lượt mượn: %d | Phạt: %.0f VND", 
                    currentUser.getCurrentBorrowed(), currentUser.getTotalBorrowed(), currentUser.getTotalFines());
                statsLabel.setText(statsText);
            } else {
                statsLabel.setText("Quản trị viên hệ thống - Quản lý thư viện");
            }
            
            // Update tables
            refreshRecentBooks();
            refreshCurrentBorrows();
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới trang chủ: " + e.getMessage());
        }
    }
    
    private void refreshRecentBooks() {
        try {
            // Lấy dữ liệu thực tế từ MongoDB
            BookService bookService = new BookService();
            List<Book> recentBooks = bookService.getBooks(1, 5, null); // Lấy 5 sách gần nhất
            
            String[] columns = {"Tiêu đề", "Tác giả", "Thể loại", "Năm xuất bản", "Trạng thái"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            for (Book book : recentBooks) {
                String status = book.getAvailableCopies() > 0 ? "Có sẵn" : "Không có sẵn";
                Object[] row = {
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPublicationYear(),
                    status
                };
                model.addRow(row);
            }
            
            recentBooksTable.setModel(model);
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới danh sách sách: " + e.getMessage());
        }
    }
    
    private void refreshCurrentBorrows() {
        try {
            // Lấy dữ liệu thực tế từ MongoDB
            BorrowService borrowService = new BorrowService();
            BookService bookService = new BookService();
            List<BorrowRecord> currentBorrows = borrowService.getCurrentBorrows(currentUser.getUserId());
            
            String[] columns = {"Sách", "Ngày mượn", "Hạn trả", "Trạng thái", "Phạt"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (BorrowRecord record : currentBorrows) {
                Book book = bookService.getBookById(record.getBookId());
                String bookTitle = book != null ? book.getTitle() : "Không xác định";
                
                String status = getStatusText(record.getStatus());
                String fine = String.format("%.0f VND", record.getFineAmount());
                
                Object[] row = {
                    bookTitle,
                    record.getBorrowDate().format(formatter),
                    record.getExpectedReturnDate().format(formatter),
                    status,
                    fine
                };
                model.addRow(row);
            }
            
            currentBorrowsTable.setModel(model);
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới danh sách mượn: " + e.getMessage());
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
    
    private void showAllBooks() {
        // Tìm UserFrame parent để chuyển đến tab tìm kiếm sách
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof UserFrame) {
            UserFrame userFrame = (UserFrame) parentFrame;
            userFrame.switchToBookSearch();
        }
    }
    
    private void showBorrowManagement() {
        // Tìm UserFrame parent để chuyển đến tab mượn sách
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof UserFrame) {
            UserFrame userFrame = (UserFrame) parentFrame;
            userFrame.switchToBorrow();
        }
    }
    
    private void showBookSearch() {
        // Tìm UserFrame parent để chuyển đến tab tìm kiếm sách
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof UserFrame) {
            UserFrame userFrame = (UserFrame) parentFrame;
            userFrame.switchToBookSearch();
        }
    }
    
    private void showBorrowBook() {
        // Tìm UserFrame parent để chuyển đến tab mượn sách
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof UserFrame) {
            UserFrame userFrame = (UserFrame) parentFrame;
            userFrame.switchToBorrow();
        }
    }
    
    private void showProfile() {
        // Tìm UserFrame parent để chuyển đến tab thông tin cá nhân
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof UserFrame) {
            UserFrame userFrame = (UserFrame) parentFrame;
            userFrame.switchToProfile();
        }
    }
    
    private void showBorrowHistory() {
        // Tìm UserFrame parent để chuyển đến tab mượn sách (có lịch sử)
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame instanceof UserFrame) {
            UserFrame userFrame = (UserFrame) parentFrame;
            userFrame.switchToBorrow();
        }
    }
}
