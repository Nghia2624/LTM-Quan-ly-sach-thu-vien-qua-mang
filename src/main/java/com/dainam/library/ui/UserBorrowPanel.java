package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.model.BorrowRecord;
import com.dainam.library.model.Book;
import com.dainam.library.service.BorrowService;
import com.dainam.library.service.BookService;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.EventBus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Panel quản lý mượn sách cho User với real-time updates
 */
public class UserBorrowPanel extends JPanel {
    
    private User currentUser;
    private BorrowService borrowService;
    private BookService bookService;
    private JTable currentBorrowsTable;
    private JTable borrowHistoryTable;
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JButton returnButton;
    private JButton extendButton;
    private JButton refreshButton;
    private Timer refreshTimer;
    private EventBus eventBus;
    
    // Table models
    private DefaultTableModel currentBorrowsModel;
    private DefaultTableModel historyModel;
    
    public UserBorrowPanel(User user) {
        this.currentUser = user;
        this.borrowService = new BorrowService();
        this.bookService = new BookService();
        this.eventBus = EventBus.getInstance();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupEventSubscriptions();
        startAutoRefresh();
        refresh();
    }
    
    private void initializeComponents() {
        // Status labels with modern styling
        statusLabel = new JLabel("Đang tải dữ liệu...", JLabel.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(102, 102, 102));
        
        statsLabel = new JLabel("", JLabel.CENTER);
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statsLabel.setForeground(new Color(51, 51, 51));
        
        // Buttons with modern styling
        returnButton = createStyledButton("Trả sách", new Color(34, 139, 34), Color.WHITE);
        extendButton = createStyledButton("Gia hạn", new Color(30, 144, 255), Color.WHITE);
        refreshButton = createStyledButton("Làm mới", new Color(128, 128, 128), Color.WHITE);
        
        // Current borrows table
        String[] currentColumns = {"Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Còn lại", "Trạng thái"};
        currentBorrowsModel = new DefaultTableModel(currentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        currentBorrowsTable = new JTable(currentBorrowsModel);
        setupTable(currentBorrowsTable);
        
        // History table
        String[] historyColumns = {"Mã sách", "Tên sách", "Ngày mượn", "Ngày trả", "Trạng thái", "Ghi chú"};
        historyModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowHistoryTable = new JTable(historyModel);
        setupTable(borrowHistoryTable);
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void setupTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(245, 245, 245));
        table.getTableHeader().setForeground(new Color(51, 51, 51));
        
        // Custom cell renderer for status coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    // Color coding based on status
                    String status = table.getModel().getValueAt(row, table.getColumnCount() - 1).toString();
                    if (status.contains("Quá hạn")) {
                        c.setBackground(new Color(255, 240, 240)); // Light red
                    } else if (status.contains("Đang mượn")) {
                        c.setBackground(new Color(240, 255, 240)); // Light green
                    } else if (status.contains("Đã trả")) {
                        c.setBackground(new Color(248, 248, 255)); // Light blue
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                
                return c;
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);
        
        // Top panel with title and stats
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Quản lý mượn sách", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(51, 51, 51));
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.add(statsLabel);
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(returnButton);
        buttonPanel.add(extendButton);
        buttonPanel.add(refreshButton);
        
        // Main content panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Current borrows tab
        JPanel currentPanel = new JPanel(new BorderLayout(5, 5));
        currentPanel.setBackground(Color.WHITE);
        currentPanel.add(new JScrollPane(currentBorrowsTable), BorderLayout.CENTER);
        tabbedPane.addTab("Sách đang mượn", currentPanel);
        
        // History tab
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBackground(Color.WHITE);
        historyPanel.add(new JScrollPane(borrowHistoryTable), BorderLayout.CENTER);
        tabbedPane.addTab("Lịch sử mượn", historyPanel);
        
        // Add components to main panel
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        returnButton.addActionListener(e -> returnSelectedBook());
        extendButton.addActionListener(e -> extendSelectedBook());
        refreshButton.addActionListener(e -> refresh());
        
        // Table selection listeners
        currentBorrowsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }
    
    private void setupEventSubscriptions() {
        // Subscribe to real-time events
        eventBus.subscribe(EventBus.Events.BOOK_BORROWED, data -> SwingUtilities.invokeLater(this::refresh));
        eventBus.subscribe(EventBus.Events.BOOK_RETURNED, data -> SwingUtilities.invokeLater(this::refresh));
        eventBus.subscribe(EventBus.Events.BORROW_RECORD_UPDATED, data -> SwingUtilities.invokeLater(this::refresh));
        eventBus.subscribe(EventBus.Events.DATA_REFRESH, data -> SwingUtilities.invokeLater(this::refresh));
    }
    
    private void startAutoRefresh() {
        // Auto refresh every 60 seconds
        refreshTimer = new Timer(60000, e -> refresh());
        refreshTimer.start();
    }
    
    private void returnSelectedBook() {
        int selectedRow = currentBorrowsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần trả!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Get record ID from the selected row (assuming it's stored somewhere)
            // For now, we'll need to find the record by user, book, and date
            String bookId = currentBorrowsModel.getValueAt(selectedRow, 0).toString();
            String bookTitle = currentBorrowsModel.getValueAt(selectedRow, 1).toString();
            
            int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn trả sách \"" + bookTitle + "\"?",
                "Xác nhận trả sách",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                // Find the record
                List<BorrowRecord> currentBorrows = borrowService.getCurrentBorrows(currentUser.getUserId());
                BorrowRecord targetRecord = null;
                for (BorrowRecord record : currentBorrows) {
                    if (record.getBookId().equals(bookId)) {
                        targetRecord = record;
                        break;
                    }
                }
                
                if (targetRecord != null) {
                    boolean success = borrowService.returnBook(targetRecord.getRecordId());
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Trả sách thành công!", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        eventBus.publish(EventBus.Events.BOOK_RETURNED, targetRecord.getBookId());
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Lỗi khi trả sách!", 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy bản ghi mượn!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi trả sách: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi trả sách: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void extendSelectedBook() {
        int selectedRow = currentBorrowsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần gia hạn!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String bookId = currentBorrowsModel.getValueAt(selectedRow, 0).toString();
            String bookTitle = currentBorrowsModel.getValueAt(selectedRow, 1).toString();
            
            int result = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn gia hạn sách \"" + bookTitle + "\"?\n" +
                "Sách sẽ được gia hạn thêm 7 ngày.",
                "Xác nhận gia hạn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                // Find the record
                List<BorrowRecord> currentBorrows = borrowService.getCurrentBorrows(currentUser.getUserId());
                BorrowRecord targetRecord = null;
                for (BorrowRecord record : currentBorrows) {
                    if (record.getBookId().equals(bookId)) {
                        targetRecord = record;
                        break;
                    }
                }
                
                if (targetRecord != null) {
                    boolean success = borrowService.extendBorrow(targetRecord.getRecordId());
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Gia hạn sách thành công!", 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        eventBus.publish(EventBus.Events.BORROW_RECORD_UPDATED, targetRecord.getRecordId());
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Lỗi khi gia hạn sách!", 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy bản ghi mượn!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi gia hạn sách: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi gia hạn sách: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateButtonStates() {
        int selectedRow = currentBorrowsTable.getSelectedRow();
        boolean hasSelection = selectedRow != -1;
        
        returnButton.setEnabled(hasSelection);
        extendButton.setEnabled(hasSelection);
        
        if (hasSelection) {
            // Check if book can be extended (not already extended and not overdue for too long)
            try {
                String status = currentBorrowsModel.getValueAt(selectedRow, 5).toString();
                extendButton.setEnabled(!status.contains("Đã gia hạn"));
            } catch (Exception e) {
                extendButton.setEnabled(false);
            }
        }
    }
    
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Đang tải dữ liệu...");
                statusLabel.setForeground(new Color(102, 102, 102));
                
                // Clear tables
                currentBorrowsModel.setRowCount(0);
                historyModel.setRowCount(0);
                
                // Load current borrows
                List<BorrowRecord> currentBorrows = borrowService.getCurrentBorrows(currentUser.getUserId());
                for (BorrowRecord record : currentBorrows) {
                    Book book = bookService.getBookById(record.getBookId());
                    if (book != null) {
                        String[] row = new String[6];
                        row[0] = record.getBookId();
                        row[1] = book.getTitle();
                        row[2] = record.getBorrowDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        row[3] = record.getExpectedReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        
                        // Calculate days remaining
                        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), record.getExpectedReturnDate());
                        row[4] = daysRemaining > 0 ? daysRemaining + " ngày" : "Quá hạn " + Math.abs(daysRemaining) + " ngày";
                        
                        // Status with extension info
                        String status = getStatusText(record.getStatus());
                        if (record.isExtended()) {
                            status += " (Đã gia hạn)";
                        }
                        row[5] = status;
                        
                        currentBorrowsModel.addRow(row);
                    }
                }
                
                // Load borrow history
                List<BorrowRecord> history = borrowService.getBorrowHistory(currentUser.getUserId());
                for (BorrowRecord record : history) {
                    Book book = bookService.getBookById(record.getBookId());
                    if (book != null) {
                        String[] row = new String[6];
                        row[0] = record.getBookId();
                        row[1] = book.getTitle();
                        row[2] = record.getBorrowDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        row[3] = record.getActualReturnDate() != null ? 
                            record.getActualReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa trả";
                        row[4] = getStatusText(record.getStatus());
                        row[5] = record.getReturnNotes() != null ? record.getReturnNotes() : "";
                        
                        historyModel.addRow(row);
                    }
                }
                
                // Update stats
                int totalBorrowed = currentBorrows.size();
                int overdueCount = (int) currentBorrows.stream()
                    .filter(r -> r.getExpectedReturnDate().isBefore(LocalDate.now()))
                    .count();
                  statsLabel.setText(String.format("Đang mượn: %d sách | Quá hạn: %d sách | Tối đa: 5 sách", 
                    totalBorrowed, overdueCount));
                
                statusLabel.setText("Dữ liệu đã được cập nhật - " + 
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                statusLabel.setForeground(new Color(34, 139, 34));
                
                updateButtonStates();
                
            } catch (Exception e) {
                LoggerUtil.error("Lỗi làm mới dữ liệu: " + e.getMessage());
                statusLabel.setText("Lỗi tải dữ liệu: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
            }
        });
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
    
    /**
     * Cleanup when panel is disposed
     */
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        LoggerUtil.info("UserBorrowPanel disposed");
    }
}
