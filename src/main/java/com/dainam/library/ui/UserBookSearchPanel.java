package com.dainam.library.ui;

import com.dainam.library.model.User;
import com.dainam.library.model.Book;
import com.dainam.library.model.BookCopy;
import com.dainam.library.model.BorrowRecord;
import com.dainam.library.service.BookService;
import com.dainam.library.service.BorrowService;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.EventBus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel tìm kiếm và mượn sách cho User với real-time updates
 */
public class UserBookSearchPanel extends JPanel {
    
    private User currentUser;
    private BookService bookService;
    private BorrowService borrowService;
    private JTable booksTable;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton borrowButton;
    private JButton viewDetailsButton;
    private Timer refreshTimer;
    
    public UserBookSearchPanel(User user) {
        this.currentUser = user;
        this.bookService = new BookService();
        this.borrowService = new BorrowService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        startAutoRefresh();
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
        searchField.setToolTipText("Tìm kiếm theo tên sách, tác giả, hoặc ISBN");
        
        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryFilter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        categoryFilter.addItem("Tất cả thể loại");
        
        // Table with updated columns
        String[] columns = {"Tên sách", "Tác giả", "Thể loại", "Năm xuất bản", "Số trang", "Giá (VND)", "Có sẵn", "Tổng số"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        booksTable = new JTable(model);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.getTableHeader().setReorderingAllowed(false);
        booksTable.setRowHeight(28);
        booksTable.setGridColor(new Color(230, 230, 230));
        booksTable.setShowGrid(true);
        
        // Buttons
        searchButton = createStyledButton("Tìm kiếm", new Color(52, 152, 219));
        refreshButton = createStyledButton("Làm mới", new Color(46, 204, 113));
        borrowButton = createStyledButton("Mượn sách", new Color(241, 196, 15));
        viewDetailsButton = createStyledButton("Xem chi tiết", new Color(155, 89, 182));
        
        // Status label
        statusLabel = new JLabel("Tìm kiếm và mượn sách");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(52, 73, 94));
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 35));
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
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));
        
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
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        // Title
        panel.add(statusLabel, BorderLayout.WEST);
        
        // Search panel with better spacing
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchLabel.setForeground(new Color(52, 73, 94));
        
        JLabel categoryLabel = new JLabel("Thể loại:");
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryLabel.setForeground(new Color(52, 73, 94));
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(categoryLabel);
        searchPanel.add(categoryFilter);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Danh sách sách",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(52, 73, 94)
            ),
            BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        
        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        panel.add(borrowButton);
        panel.add(viewDetailsButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        searchButton.addActionListener(e -> performSearch());
        refreshButton.addActionListener(e -> refresh());
        borrowButton.addActionListener(e -> borrowBook());
        viewDetailsButton.addActionListener(e -> viewBookDetails());
        
        // Search on Enter key
        searchField.addActionListener(e -> performSearch());
        
        // Filter on category change
        categoryFilter.addActionListener(e -> performSearch());
        
        // Table selection listener
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Subscribe to real-time events
        EventBus.getInstance().subscribe(EventBus.Events.BOOK_BORROWED, data -> SwingUtilities.invokeLater(this::refresh));
        EventBus.getInstance().subscribe(EventBus.Events.BOOK_RETURNED, data -> SwingUtilities.invokeLater(this::refresh));
        EventBus.getInstance().subscribe(EventBus.Events.BOOK_ADDED, data -> SwingUtilities.invokeLater(this::refresh));
        EventBus.getInstance().subscribe(EventBus.Events.BOOK_UPDATED, data -> SwingUtilities.invokeLater(this::refresh));
        EventBus.getInstance().subscribe(EventBus.Events.DATA_REFRESH, data -> SwingUtilities.invokeLater(this::refresh));
    }
    
    private void startAutoRefresh() {
        // Auto refresh every 60 seconds
        refreshTimer = new Timer(60000, e -> refresh());
        refreshTimer.start();
    }
    
    private void updateButtonStates() {
        boolean hasSelection = booksTable.getSelectedRow() >= 0;
        borrowButton.setEnabled(hasSelection);
        viewDetailsButton.setEnabled(hasSelection);
        
        if (hasSelection) {
            int selectedRow = booksTable.getSelectedRow();
            int availableCopies = (Integer) booksTable.getValueAt(selectedRow, 6);
            borrowButton.setEnabled(availableCopies > 0);
        }
    }
    
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                loadCategories();
                loadBooks();
                updateStatus();
                LoggerUtil.info("UserBookSearchPanel refreshed successfully");
            } catch (Exception e) {
                LoggerUtil.error("Error refreshing UserBookSearchPanel: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Lỗi làm mới danh sách sách: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void loadCategories() {
        try {
            List<String> categories = bookService.getCategories();
            String selectedCategory = (String) categoryFilter.getSelectedItem();
            
            categoryFilter.removeAllItems();
            categoryFilter.addItem("Tất cả thể loại");
            for (String category : categories) {
                categoryFilter.addItem(category);
            }
            
            // Restore selected category if it still exists
            if (selectedCategory != null && categories.contains(selectedCategory)) {
                categoryFilter.setSelectedItem(selectedCategory);
            }
        } catch (Exception e) {
            LoggerUtil.error("Error loading categories: " + e.getMessage());
        }
    }
    
    private void loadBooks() {
        try {            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
            model.setRowCount(0); // Clear existing data
            
            List<Book> books = bookService.getAllBooks(0, 1000);
            
            // Sort by title (newest/updated first)
            books.sort((a, b) -> {
                if (a.getUpdatedAt() != null && b.getUpdatedAt() != null) {
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                }
                return a.getTitle().compareTo(b.getTitle());
            });
            
            for (Book book : books) {
                Object[] row = {
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPublicationYear(),
                    book.getPageCount(),
                    String.format("%,.0f", book.getPrice()),
                    book.getAvailableCopies(),
                    book.getTotalCopies()
                };
                model.addRow(row);
            }
            
            updateButtonStates();
        } catch (Exception e) {
            LoggerUtil.error("Error loading books: " + e.getMessage());
        }
    }
    
    private void performSearch() {
        try {
            String query = searchField.getText().trim();
            String selectedCategory = (String) categoryFilter.getSelectedItem();
            
            if (selectedCategory == null) {
                selectedCategory = "Tất cả thể loại";
            }
            
            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
            model.setRowCount(0);
              List<Book> books;
            if (query.isEmpty()) {
                books = bookService.getAllBooks(0, 1000);
            } else {
                books = bookService.searchBooks(query);
            }
            
            // Sort by relevance/update time
            books.sort((a, b) -> {
                if (a.getUpdatedAt() != null && b.getUpdatedAt() != null) {
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                }
                return a.getTitle().compareTo(b.getTitle());
            });
            
            for (Book book : books) {
                // Apply category filter
                if (!selectedCategory.equals("Tất cả thể loại") && 
                    !book.getCategory().equals(selectedCategory)) {
                    continue;
                }
                
                Object[] row = {
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getPublicationYear(),
                    book.getPageCount(),
                    String.format("%,.0f", book.getPrice()),
                    book.getAvailableCopies(),
                    book.getTotalCopies()
                };
                model.addRow(row);
            }
            
            updateStatus();
            updateButtonStates();
            
        } catch (Exception e) {
            LoggerUtil.error("Error performing search: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Lỗi tìm kiếm: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void borrowBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn sách cần mượn!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
            String bookTitle = (String) model.getValueAt(selectedRow, 0);
            String author = (String) model.getValueAt(selectedRow, 1);
            int availableCount = (Integer) model.getValueAt(selectedRow, 6);
            
            if (availableCount <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "Sách này hiện không có sẵn để mượn!", 
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
              // Find the book by title and author
            List<Book> books = bookService.getAllBooks(0, 1000);
            Book selectedBook = books.stream()
                .filter(b -> b.getTitle().equals(bookTitle) && b.getAuthor().equals(author))
                .findFirst()
                .orElse(null);
                
            if (selectedBook == null) {
                JOptionPane.showMessageDialog(this, 
                    "Không tìm thấy thông tin sách!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get available copies for this book
            List<BookCopy> availableCopies = bookService.getBookCopies(selectedBook.getBookId()).stream()
                .filter(copy -> copy.getStatus() == BookCopy.Status.AVAILABLE)
                .collect(java.util.stream.Collectors.toList());
            
            if (availableCopies.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Không có bản sao nào có sẵn!", 
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Show copy selection dialog
            String[] copyOptions = availableCopies.stream()
                .map(copy -> String.format("Bản sao %s - Kệ: %s - Vị trí: %s", 
                    copy.getCopyId(), copy.getShelf(), copy.getLocation()))
                .toArray(String[]::new);
            
            String selectedCopyOption = (String) JOptionPane.showInputDialog(this,
                String.format("Chọn bản sao để mượn:\n\nSách: %s\nTác giả: %s", bookTitle, author),
                "Chọn bản sao sách",
                JOptionPane.QUESTION_MESSAGE,
                null,
                copyOptions,
                copyOptions[0]);
            
            if (selectedCopyOption == null) {
                return; // User cancelled
            }
            
            // Find the selected copy
            int selectedCopyIndex = java.util.Arrays.asList(copyOptions).indexOf(selectedCopyOption);
            BookCopy selectedCopy = availableCopies.get(selectedCopyIndex);
            
            // Confirm borrowing
            int result = JOptionPane.showConfirmDialog(this, 
                String.format("Bạn có chắc chắn muốn mượn sách?\n\n" +
                    "• Tiêu đề: %s\n" +
                    "• Tác giả: %s\n" +
                    "• Bản sao: %s\n" +
                    "• Vị trí: Kệ %s - %s\n" +
                    "• Thời hạn mượn: 14 ngày\n" +
                    "• Hạn trả: %s", 
                    bookTitle, author, selectedCopy.getCopyId(), 
                    selectedCopy.getShelf(), selectedCopy.getLocation(),
                    LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 
                "Xác nhận mượn sách", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                // Use BorrowService to borrow the book
                BorrowRecord borrowRecord = borrowService.borrowBook(
                    currentUser.getUserId(), 
                    selectedBook.getBookId(), 
                    selectedCopy.getCopyId()
                );
                
                if (borrowRecord != null) {
                    JOptionPane.showMessageDialog(this, 
                        String.format("Mượn sách thành công!\n\n" +
                            "• Sách: %s\n" +
                            "• Bản sao: %s\n" +
                            "• Vị trí: Kệ %s - %s\n" +
                            "• Ngày mượn: %s\n" +
                            "• Hạn trả: %s\n\n" +
                            "Lưu ý: Mỗi sách có thể gia hạn 1 lần (7 ngày thêm)", 
                            bookTitle, selectedCopy.getCopyId(), 
                            selectedCopy.getShelf(), selectedCopy.getLocation(),
                            borrowRecord.getBorrowDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            borrowRecord.getExpectedReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 
                        "Mượn sách thành công", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Publish event for real-time updates
                    EventBus.getInstance().publish(EventBus.Events.BOOK_BORROWED, borrowRecord);
                    
                    // Refresh data
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Không thể mượn sách. Vui lòng kiểm tra lại!", 
                        "Lỗi mượn sách", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Error borrowing book: " + e.getMessage());
            String errorMessage = e.getMessage();
            if (errorMessage.contains("đã mượn tối đa")) {
                JOptionPane.showMessageDialog(this, 
                    "Bạn đã mượn tối đa 5 quyển sách!\nVui lòng trả sách trước khi mượn mới.", 
                    "Giới hạn mượn sách", 
                    JOptionPane.WARNING_MESSAGE);
            } else if (errorMessage.contains("quá hạn")) {
                JOptionPane.showMessageDialog(this, 
                    "Bạn có sách quá hạn!\nVui lòng trả sách quá hạn trước khi mượn mới.", 
                    "Sách quá hạn", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi mượn sách: " + errorMessage, 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewBookDetails() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn sách cần xem chi tiết!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
            String bookTitle = (String) model.getValueAt(selectedRow, 0);
            String author = (String) model.getValueAt(selectedRow, 1);
              // Find the book
            List<Book> books = bookService.getAllBooks(0, 1000);
            Book book = books.stream()
                .filter(b -> b.getTitle().equals(bookTitle) && b.getAuthor().equals(author))
                .findFirst()
                .orElse(null);
            
            if (book != null) {
                showBookDetailsDialog(book);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Không tìm thấy thông tin chi tiết sách!", 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Error viewing book details: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Lỗi xem chi tiết sách: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showBookDetailsDialog(Book book) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
            "Chi tiết sách: " + book.getTitle(), true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Book info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add book details
        addDetailRow(infoPanel, gbc, 0, "Tên sách:", book.getTitle());
        addDetailRow(infoPanel, gbc, 1, "Tác giả:", book.getAuthor());
        addDetailRow(infoPanel, gbc, 2, "ISBN:", book.getIsbn());
        addDetailRow(infoPanel, gbc, 3, "Thể loại:", book.getCategory());
        addDetailRow(infoPanel, gbc, 4, "Năm xuất bản:", String.valueOf(book.getPublicationYear()));
        addDetailRow(infoPanel, gbc, 5, "Số trang:", String.valueOf(book.getPageCount()));
        addDetailRow(infoPanel, gbc, 6, "Giá:", String.format("%,.0f VND", book.getPrice()));
        addDetailRow(infoPanel, gbc, 7, "Có sẵn/Tổng:", String.format("%d/%d quyển", 
            book.getAvailableCopies(), book.getTotalCopies()));
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Description panel
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            JPanel descPanel = new JPanel(new BorderLayout());
            descPanel.setBorder(BorderFactory.createTitledBorder("Mô tả"));
            
            JTextArea descriptionArea = new JTextArea(book.getDescription(), 5, 40);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(descriptionArea);
            descPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(descPanel, BorderLayout.CENTER);
        }
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = createStyledButton("Đóng", new Color(108, 117, 125));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lblValue, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }
    
    private void updateStatus() {
        int totalBooks = booksTable.getRowCount();
        statusLabel.setText(String.format("Tìm thấy %d quyển sách", totalBooks));
    }
    
    /**
     * Cleanup when panel is disposed
     */
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        LoggerUtil.info("UserBookSearchPanel disposed");
    }
}
