package com.library.gui;

import com.library.client.LibraryClient;
import com.library.common.Message;
import com.library.common.SearchCriteria;
import com.library.model.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class BookManagementPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final LibraryClient client;
    private DefaultTableModel tableModel;
    private JTable bookTable;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private JButton addButton, editButton, deleteButton, refreshButton;
    
    private final String[] columnNames = {
        "ID", "Tiêu đề", "Tác giả", "ISBN", "Thể loại", 
        "Nhà xuất bản", "Năm XB", "Tổng số", "Còn lại", "Mô tả"
    };
    
    public BookManagementPanel(LibraryClient client) {
        this.client = client;
        initializeUI();
        setupEventHandlers();
        refreshData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create toolbar
        createToolbar();
        
        // Create table
        createTable();
        
        // Create button panel
        createButtonPanel();
    }
    
    private void createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(BorderFactory.createEtchedBorder());
        
        // Search components
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        searchTypeCombo = new JComboBox<>(new String[]{
            "Tất cả", "Tiêu đề", "Tác giả", "ISBN", "Thể loại", "Nhà xuất bản"
        });
        searchTypeCombo.setPreferredSize(new Dimension(120, 25));
        
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 25));
        
        JButton searchButton = new JButton("🔍 Tìm");
        searchButton.setPreferredSize(new Dimension(80, 25));
        searchButton.addActionListener(e -> performSearch());
        
        // Add Enter key support for search field
        searchField.addActionListener(e -> performSearch());
        
        toolbar.add(searchLabel);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(searchTypeCombo);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(searchField);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(searchButton);
        
        add(toolbar, BorderLayout.NORTH);
    }
    
    private void createTable() {
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setRowHeight(25);
        bookTable.setFont(new Font("Arial", Font.PLAIN, 12));
        bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Hide ID column
        bookTable.getColumnModel().getColumn(0).setMinWidth(0);
        bookTable.getColumnModel().getColumn(0).setMaxWidth(0);
        bookTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Title
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Author
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(100); // ISBN
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Category
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Publisher
        bookTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Year
        bookTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Total
        bookTable.getColumnModel().getColumn(8).setPreferredWidth(70);  // Available
        bookTable.getColumnModel().getColumn(9).setPreferredWidth(200); // Description
        
        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        
        addButton = new JButton("➕ Thêm sách");
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        addButton.setBackground(new Color(76, 175, 80));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        
        editButton = new JButton("✏️ Sửa sách");
        editButton.setFont(new Font("Arial", Font.BOLD, 12));
        editButton.setBackground(new Color(255, 193, 7));
        editButton.setForeground(Color.BLACK);
        editButton.setFocusPainted(false);
        editButton.setEnabled(false);
        
        deleteButton = new JButton("🗑️ Xóa sách");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 12));
        deleteButton.setBackground(new Color(244, 67, 54));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setEnabled(false);
        
        refreshButton = new JButton("🔄 Làm mới");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setBackground(new Color(33, 150, 243));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Table selection listener
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = bookTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });
        
        // Double-click to edit
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    editBook();
                }
            }
        });
        
        // Button listeners
        addButton.addActionListener(e -> addBook());
        editButton.addActionListener(e -> editBook());
        deleteButton.addActionListener(e -> deleteBook());
        refreshButton.addActionListener(e -> refreshData());
    }
    
    private void performSearch() {
        String searchText = searchField.getText().trim();
        String searchType = (String) searchTypeCombo.getSelectedItem();
        
        SearchCriteria criteria = new SearchCriteria();
        
        if (!searchText.isEmpty()) {
            switch (searchType) {
                case "Tiêu đề" -> criteria.setTitle(searchText);
                case "Tác giả" -> criteria.setAuthor(searchText);
                case "ISBN" -> criteria.setIsbn(searchText);
                case "Thể loại" -> criteria.setCategory(searchText);
                case "Nhà xuất bản" -> criteria.setPublisher(searchText);
                default -> {
                    // Search all fields
                    criteria.setTitle(searchText);
                    criteria.setAuthor(searchText);
                    criteria.setIsbn(searchText);
                    criteria.setCategory(searchText);
                    criteria.setPublisher(searchText);
                }
            }
        }
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.SEARCH_BOOKS, criteria);
                Message response = client.sendRequest(request);
                
                if (response.isSuccess()) {
                    return (List<Book>) response.getData();
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    updateTableData(books);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        BookManagementPanel.this,
                        "Lỗi tìm kiếm: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private void addBook() {
        BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Book book = dialog.getBook();
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Message request = new Message(Message.MessageType.ADD_BOOK, book);
                    Message response = client.sendRequest(request);
                    
                    if (response.isSuccess()) {
                        return true;
                    } else {
                        throw new Exception(response.getErrorMessage());
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(
                                BookManagementPanel.this,
                                "Thêm sách thành công!",
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshData();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                            BookManagementPanel.this,
                            "Lỗi thêm sách: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void editBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        // Get book ID from hidden column
        String bookId = (String) tableModel.getValueAt(selectedRow, 0);
        
        // Get full book details
        SwingWorker<Book, Void> worker = new SwingWorker<Book, Void>() {
            @Override
            protected Book doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.GET_BOOK, bookId);
                Message response = client.sendRequest(request);
                
                if (response.isSuccess()) {
                    return (Book) response.getData();
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    Book book = get();
                    BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(BookManagementPanel.this), book);
                    dialog.setVisible(true);
                    
                    if (dialog.isConfirmed()) {
                        Book updatedBook = dialog.getBook();
                        updateBook(updatedBook);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        BookManagementPanel.this,
                        "Lỗi lấy thông tin sách: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateBook(Book book) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.UPDATE_BOOK, book);
                Message response = client.sendRequest(request);
                
                if (response.isSuccess()) {
                    return true;
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(
                            BookManagementPanel.this,
                            "Cập nhật sách thành công!",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        refreshData();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        BookManagementPanel.this,
                        "Lỗi cập nhật sách: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa sách \"" + bookTitle + "\"?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            String bookId = (String) tableModel.getValueAt(selectedRow, 0);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Message request = new Message(Message.MessageType.DELETE_BOOK, bookId);
                    Message response = client.sendRequest(request);
                    
                    if (response.isSuccess()) {
                        return true;
                    } else {
                        throw new Exception(response.getErrorMessage());
                    }
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(
                                BookManagementPanel.this,
                                "Xóa sách thành công!",
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshData();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                            BookManagementPanel.this,
                            "Lỗi xóa sách: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    @Override
    public void refreshData() {
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.GET_ALL_BOOKS, null);
                Message response = client.sendRequest(request);
                
                if (response.isSuccess()) {
                    return (List<Book>) response.getData();
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    updateTableData(books);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        BookManagementPanel.this,
                        "Lỗi tải dữ liệu: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTableData(List<Book> books) {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add new data
        for (Book book : books) {
            Object[] row = {
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCategory(),
                book.getPublisher(),
                book.getPublishYear(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getDescription()
            };
            tableModel.addRow(row);
        }
        
        // Clear selection
        bookTable.clearSelection();
    }
}
