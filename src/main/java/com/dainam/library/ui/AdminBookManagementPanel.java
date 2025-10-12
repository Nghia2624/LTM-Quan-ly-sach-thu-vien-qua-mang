package com.dainam.library.ui;

import com.dainam.library.model.Book;
import com.dainam.library.model.BookCopy;
import com.dainam.library.service.BookService;
import com.dainam.library.util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel quản lý sách cho Admin
 */
public class AdminBookManagementPanel extends JPanel {
    
    private BookService bookService;
    private JTable booksTable;
    private JTable copiesTable;
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JButton addBookButton;
    private JButton editBookButton;
    private JButton deleteBookButton;
    private JButton addCopyButton;
    private JButton deleteCopyButton;
    private JButton searchButton;
    private JButton refreshButton;
    
    public AdminBookManagementPanel() {
        this.bookService = new BookService();
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
        searchField.setToolTipText("Tìm kiếm theo tiêu đề, tác giả, ISBN");
        
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        categoryComboBox.addItem("Tất cả thể loại");
        categoryComboBox.addItem("Công nghệ thông tin");
        categoryComboBox.addItem("Kinh tế");
        categoryComboBox.addItem("Ngoại ngữ");
        categoryComboBox.addItem("Toán học");
        categoryComboBox.addItem("Văn học");
        
        // Buttons with better styling
        addBookButton = createStyledButton("Thêm sách", new Color(39, 174, 96));
        editBookButton = createStyledButton("Sửa sách", new Color(241, 196, 15));
        deleteBookButton = createStyledButton("Xóa sách", new Color(231, 76, 60));
        addCopyButton = createStyledButton("Thêm bản sao", new Color(52, 152, 219));
        deleteCopyButton = createStyledButton("Xóa bản sao", new Color(155, 89, 182));
        searchButton = createStyledButton("Tìm kiếm", new Color(46, 204, 113));
        refreshButton = createStyledButton("Làm mới", new Color(52, 73, 94));
        
        // Books table
        String[] bookColumns = {"ID", "Tiêu đề", "Tác giả", "ISBN", "Thể loại", "Năm XB", "Giá", "Tổng số", "Có sẵn", "Trạng thái"};
        booksTable = new JTable(new DefaultTableModel(bookColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.getTableHeader().setReorderingAllowed(false);
          // Copies table
        String[] copyColumns = {"ID", "Sách", "Trạng thái", "Vị trí", "Ghi chú"};
        copiesTable = new JTable(new DefaultTableModel(copyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        copiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        copiesTable.getTableHeader().setReorderingAllowed(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel - Search and controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Tables
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Actions
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Tìm kiếm và bộ lọc"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Search row with better spacing
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchRow.setBackground(new Color(248, 249, 250));
        
        JLabel keywordLabel = new JLabel("Từ khóa:");
        keywordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        keywordLabel.setForeground(new Color(52, 73, 94));
        
        JLabel categoryLabel = new JLabel("Thể loại:");
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryLabel.setForeground(new Color(52, 73, 94));
        
        searchRow.add(keywordLabel);
        searchRow.add(searchField);
        searchRow.add(Box.createHorizontalStrut(20));
        searchRow.add(categoryLabel);
        searchRow.add(categoryComboBox);
        searchRow.add(Box.createHorizontalStrut(20));
        searchRow.add(searchButton);
        searchRow.add(refreshButton);
        
        panel.add(searchRow, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Books panel
        JPanel booksPanel = new JPanel(new BorderLayout());
        booksPanel.setBackground(Color.WHITE);
        booksPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Danh sách sách"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane booksScrollPane = new JScrollPane(booksTable);
        booksScrollPane.setPreferredSize(new Dimension(600, 300));
        booksScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        booksPanel.add(booksScrollPane, BorderLayout.CENTER);
        
        // Books buttons with better spacing
        JPanel booksButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        booksButtonPanel.setBackground(Color.WHITE);
        booksButtonPanel.add(addBookButton);
        booksButtonPanel.add(editBookButton);
        booksButtonPanel.add(deleteBookButton);
        booksPanel.add(booksButtonPanel, BorderLayout.SOUTH);
        
        // Copies panel
        JPanel copiesPanel = new JPanel(new BorderLayout());
        copiesPanel.setBackground(Color.WHITE);
        copiesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Bản sao sách"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane copiesScrollPane = new JScrollPane(copiesTable);
        copiesScrollPane.setPreferredSize(new Dimension(400, 300));
        copiesScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        copiesPanel.add(copiesScrollPane, BorderLayout.CENTER);
        
        // Copies buttons with better spacing
        JPanel copiesButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        copiesButtonPanel.setBackground(Color.WHITE);
        copiesButtonPanel.add(addCopyButton);
        copiesButtonPanel.add(deleteCopyButton);
        copiesPanel.add(copiesButtonPanel, BorderLayout.SOUTH);
        
        panel.add(booksPanel);
        panel.add(copiesPanel);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
        panel.add(statusLabel);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        addBookButton.addActionListener(e -> showAddBookDialog());
        editBookButton.addActionListener(e -> showEditBookDialog());
        deleteBookButton.addActionListener(e -> deleteBook());
        addCopyButton.addActionListener(e -> showAddCopyDialog());
        deleteCopyButton.addActionListener(e -> deleteCopy());
        searchButton.addActionListener(e -> performSearch());
        refreshButton.addActionListener(e -> refresh());
        
        // Table selection handlers
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadBookCopies();
            }
        });
        
        // Double-click to edit
        booksTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditBookDialog();
                }
            }
        });
    }
    
    public void refresh() {
        try {
            loadBooks();
            loadBookCopies();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi làm mới danh sách sách: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi làm mới dữ liệu: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadBooks() {
        try {
            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
            model.setRowCount(0);
            
            List<Book> books = bookService.getBooks(1, 100, null);
            for (Book book : books) {
                // Lấy thông tin thực tế từ database
                List<BookCopy> copies = bookService.getBookCopies(book.getBookId());
                int totalCopies = copies.size();
                int availableCopies = 0;
                
                for (BookCopy copy : copies) {
                    if (copy.getStatus() == BookCopy.Status.AVAILABLE) {
                        availableCopies++;
                    }
                }
                
                Object[] row = {
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getCategory(),
                    book.getPublicationYear(),
                    String.format("%.0f VND", book.getPrice()),
                    totalCopies,
                    availableCopies,
                    availableCopies > 0 ? "Có sẵn" : "Không có sẵn"
                };
                model.addRow(row);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tải danh sách sách: " + e.getMessage());
        }
    }
    
    private void loadBookCopies() {
        try {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow < 0) {
                DefaultTableModel model = (DefaultTableModel) copiesTable.getModel();
                model.setRowCount(0);
                return;
            }
            
            String bookId = (String) booksTable.getValueAt(selectedRow, 0);
            
            DefaultTableModel model = (DefaultTableModel) copiesTable.getModel();
            model.setRowCount(0);
              // Lấy danh sách bản sao của sách được chọn
            List<BookCopy> copies = bookService.getBookCopies(bookId);            for (BookCopy copy : copies) {
                Object[] row = {
                    copy.getCopyId(),
                    copy.getBookId(),
                    copy.getStatus().getDisplayName(), // Sử dụng getDisplayName() thay vì name()
                    copy.getLocation(),
                    copy.getNotes()
                };
                model.addRow(row);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tải danh sách bản sao: " + e.getMessage());
        }
    }
      private void performSearch() {
        try {
            String query = searchField.getText().trim();
            String category = (String) categoryComboBox.getSelectedItem();
            
            DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
            model.setRowCount(0);
            
            List<Book> books;
            if (query.isEmpty()) {
                books = bookService.getBooks(1, 100, 
                    "Tất cả thể loại".equals(category) ? null : category);
            } else {
                books = bookService.searchBooks(query);
            }
            
            for (Book book : books) {
                // Lấy thông tin thực tế từ database
                List<BookCopy> copies = bookService.getBookCopies(book.getBookId());
                int totalCopies = copies.size();
                int availableCopies = 0;
                
                for (BookCopy copy : copies) {
                    if (copy.getStatus() == BookCopy.Status.AVAILABLE) {
                        availableCopies++;
                    }
                }
                
                Object[] row = {
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getCategory(),
                    book.getPublicationYear(),
                    String.format("%.0f VND", book.getPrice()),
                    totalCopies,
                    availableCopies,
                    availableCopies > 0 ? "Có sẵn" : "Không có sẵn"
                };
                model.addRow(row);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tìm kiếm sách: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void showAddBookDialog() {
        AddBookDialog dialog = new AddBookDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isBookAdded()) {
            refresh();
        }
    }
    
    private void showEditBookDialog() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần sửa", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookId = (String) booksTable.getValueAt(selectedRow, 0);
        Book book = bookService.getBookById(bookId);
        
        if (book != null) {
            EditBookDialog dialog = new EditBookDialog((JFrame) SwingUtilities.getWindowAncestor(this), book);
            dialog.setVisible(true);
            if (dialog.isBookUpdated()) {
                refresh();
            }
        }
    }
    
    private void deleteBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần xóa", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookId = (String) booksTable.getValueAt(selectedRow, 0);
        String bookTitle = (String) booksTable.getValueAt(selectedRow, 1);
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa sách '" + bookTitle + "'?\n" +
            "Tất cả bản sao của sách này cũng sẽ bị xóa!",
            "Xác nhận xóa sách", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                boolean success = bookService.deleteBook(bookId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa sách thành công", 
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa sách thất bại", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi xóa sách: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi xóa sách: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showAddCopyDialog() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để thêm bản sao", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookId = (String) booksTable.getValueAt(selectedRow, 0);
        Book book = bookService.getBookById(bookId);
        
        if (book != null) {
            AddCopyDialog dialog = new AddCopyDialog((JFrame) SwingUtilities.getWindowAncestor(this), book);
            dialog.setVisible(true);
            if (dialog.isCopyAdded()) {
                refresh();
            }
        }
    }
    
    private void deleteCopy() {
        int selectedRow = copiesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản sao cần xóa", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String copyId = (String) copiesTable.getValueAt(selectedRow, 0);
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa bản sao này?",
            "Xác nhận xóa bản sao", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                boolean success = bookService.deleteBookCopy(copyId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa bản sao thành công", 
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa bản sao thất bại", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                LoggerUtil.error("Lỗi xóa bản sao: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi xóa bản sao: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
}
