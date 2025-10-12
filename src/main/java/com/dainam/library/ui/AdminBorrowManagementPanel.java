package com.dainam.library.ui;

import com.dainam.library.model.BorrowRecord;
import com.dainam.library.model.User;
import com.dainam.library.model.Book;
import com.dainam.library.service.BorrowService;
import com.dainam.library.service.UserService;
import com.dainam.library.service.BookService;
import com.dainam.library.util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel quản lý mượn/trả sách cho Admin
 */
public class AdminBorrowManagementPanel extends JPanel {
    
    private BorrowService borrowService;
    private UserService userService;
    private BookService bookService;
    
    private JTable borrowTable;
    private JTextField searchField;
    private JComboBox<String> statusComboBox;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton returnBookButton;
    private JButton markLostButton;
    private JButton markDamagedButton;
    private JButton forceReturnButton;
    private JLabel statusLabel;
    
    public AdminBorrowManagementPanel() {
        this.borrowService = new BorrowService();
        this.userService = new UserService();
        this.bookService = new BookService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        refresh();
    }
    
    private void initializeComponents() {
        // Search components
        searchField = new JTextField(20);
        searchField.setToolTipText("Tìm kiếm theo tên người dùng, tên sách, mã mượn");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        statusComboBox = new JComboBox<>();
        statusComboBox.addItem("Tất cả trạng thái");
        statusComboBox.addItem("Đang mượn");
        statusComboBox.addItem("Đã trả");
        statusComboBox.addItem("Quá hạn");
        statusComboBox.addItem("Bị mất");
        statusComboBox.addItem("Bị hỏng");
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Buttons
        searchButton = createStyledButton("Tìm kiếm", new Color(52, 152, 219));
        refreshButton = createStyledButton("Làm mới", new Color(52, 73, 94));
        returnBookButton = createStyledButton("Trả sách", new Color(46, 204, 113));
        markLostButton = createStyledButton("Đánh dấu mất", new Color(231, 76, 60));
        markDamagedButton = createStyledButton("Đánh dấu hỏng", new Color(230, 126, 34));
        forceReturnButton = createStyledButton("Bắt buộc trả", new Color(155, 89, 182));
        
        // Table
        String[] columns = {"Mã mượn", "Người dùng", "Sách", "Ngày mượn", "Hạn trả", "Ngày trả", "Trạng thái", "Phạt (VND)"};
        borrowTable = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowTable.getTableHeader().setReorderingAllowed(false);
        borrowTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        borrowTable.setRowHeight(25);
        borrowTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        borrowTable.getTableHeader().setBackground(new Color(52, 73, 94));
        borrowTable.getTableHeader().setForeground(Color.WHITE);
        
        // Status label
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
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
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel - Search and controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Actions and status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm và bộ lọc"));
        
        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Từ khóa:"));
        searchRow.add(searchField);
        searchRow.add(new JLabel("Trạng thái:"));
        searchRow.add(statusComboBox);
        searchRow.add(searchButton);
        searchRow.add(refreshButton);
        
        panel.add(searchRow, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách mượn/trả"));
        
        JScrollPane scrollPane = new JScrollPane(borrowTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(returnBookButton);
        buttonPanel.add(markLostButton);
        buttonPanel.add(markDamagedButton);
        buttonPanel.add(forceReturnButton);
        
        // Status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        searchButton.addActionListener(e -> performSearch());
        refreshButton.addActionListener(e -> refresh());
        returnBookButton.addActionListener(e -> returnBook());
        markLostButton.addActionListener(e -> markAsLost());
        markDamagedButton.addActionListener(e -> markAsDamaged());
        forceReturnButton.addActionListener(e -> forceReturn());
        
        // Double-click to return book
        borrowTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    returnBook();
                }
            }
        });
    }
    
    public void refresh() {
        try {
            loadBorrowRecords();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới danh sách mượn/trả: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi làm mới dữ liệu: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadBorrowRecords() {
        try {
            DefaultTableModel model = (DefaultTableModel) borrowTable.getModel();
            model.setRowCount(0);
            
            List<BorrowRecord> records = borrowService.getAllBorrowRecords(1, 100);
            // Sắp xếp theo ngày mượn gần nhất
            records.sort((r1, r2) -> r2.getBorrowDate().compareTo(r1.getBorrowDate()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (BorrowRecord record : records) {
                User user = userService.getUserById(record.getUserId());
                Book book = bookService.getBookById(record.getBookId());
                
                String userName = user != null ? user.getFullName() : "Không xác định";
                String bookTitle = book != null ? book.getTitle() : "Không xác định";
                
                Object[] row = {
                    record.getRecordId(),
                    userName,
                    bookTitle,
                    record.getBorrowDate().format(formatter),
                    record.getExpectedReturnDate().format(formatter),
                    record.getActualReturnDate() != null ? record.getActualReturnDate().format(formatter) : "",
                    getStatusText(record.getStatus()),
                    String.format("%.0f", record.getFineAmount())
                };
                model.addRow(row);
            }
            
            statusLabel.setText("Đã tải " + records.size() + " bản ghi");
            statusLabel.setForeground(Color.GREEN);
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tải danh sách mượn/trả: " + e.getMessage());
            statusLabel.setText("Lỗi tải dữ liệu: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
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
    
    private void performSearch() {
        try {
            String query = searchField.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();
            
            DefaultTableModel model = (DefaultTableModel) borrowTable.getModel();
            model.setRowCount(0);
            
            List<BorrowRecord> records = borrowService.searchBorrowRecords(query, status);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (BorrowRecord record : records) {
                User user = userService.getUserById(record.getUserId());
                Book book = bookService.getBookById(record.getBookId());
                
                String userName = user != null ? user.getFullName() : "Không xác định";
                String bookTitle = book != null ? book.getTitle() : "Không xác định";
                
                Object[] row = {
                    record.getRecordId(),
                    userName,
                    bookTitle,
                    record.getBorrowDate().format(formatter),
                    record.getExpectedReturnDate().format(formatter),
                    record.getActualReturnDate() != null ? record.getActualReturnDate().format(formatter) : "",
                    getStatusText(record.getStatus()),
                    String.format("%.0f", record.getFineAmount())
                };
                model.addRow(row);
            }
            
            statusLabel.setText("Tìm thấy " + records.size() + " kết quả");
            statusLabel.setForeground(Color.GREEN);
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tìm kiếm mượn/trả: " + e.getMessage());
            statusLabel.setText("Lỗi tìm kiếm: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void returnBook() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi cần trả", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String recordId = (String) borrowTable.getValueAt(selectedRow, 0);
        
        try {
            boolean success = borrowService.returnBook(recordId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Trả sách thành công", 
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Trả sách thất bại", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi trả sách: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi trả sách: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void markAsLost() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi cần đánh dấu mất", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String recordId = (String) borrowTable.getValueAt(selectedRow, 0);
        String notes = JOptionPane.showInputDialog(this, "Nhập ghi chú về việc mất sách:", 
            "Đánh dấu sách bị mất", JOptionPane.QUESTION_MESSAGE);
        
        if (notes != null && !notes.trim().isEmpty()) {
            try {
                boolean success = borrowService.markAsLost(recordId, notes);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Đánh dấu sách bị mất thành công", 
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Đánh dấu sách bị mất thất bại", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi đánh dấu sách bị mất: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi đánh dấu sách bị mất: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void markAsDamaged() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi cần đánh dấu hỏng", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String recordId = (String) borrowTable.getValueAt(selectedRow, 0);
        
        // Get damage percentage
        String damageInput = JOptionPane.showInputDialog(this, "Nhập % hư hỏng (0-100):", 
            "Đánh dấu sách bị hỏng", JOptionPane.QUESTION_MESSAGE);
        
        if (damageInput != null && !damageInput.trim().isEmpty()) {
            try {
                double damagePercentage = Double.parseDouble(damageInput);
                if (damagePercentage < 0 || damagePercentage > 100) {
                    JOptionPane.showMessageDialog(this, "Phần trăm hư hỏng phải từ 0-100", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String notes = JOptionPane.showInputDialog(this, "Nhập ghi chú về tình trạng hỏng:", 
                    "Đánh dấu sách bị hỏng", JOptionPane.QUESTION_MESSAGE);
                
                if (notes != null) {
                    boolean success = borrowService.markAsDamaged(recordId, notes, damagePercentage);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Đánh dấu sách bị hỏng thành công", 
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(this, "Đánh dấu sách bị hỏng thất bại", 
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Phần trăm hư hỏng không hợp lệ", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                LoggerUtil.error("Lỗi đánh dấu sách bị hỏng: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi đánh dấu sách bị hỏng: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void forceReturn() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi cần bắt buộc trả", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String recordId = (String) borrowTable.getValueAt(selectedRow, 0);
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn bắt buộc trả sách này?",
            "Xác nhận bắt buộc trả", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                boolean success = borrowService.forceReturn(recordId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Bắt buộc trả sách thành công", 
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Bắt buộc trả sách thất bại", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi bắt buộc trả sách: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi bắt buộc trả sách: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}