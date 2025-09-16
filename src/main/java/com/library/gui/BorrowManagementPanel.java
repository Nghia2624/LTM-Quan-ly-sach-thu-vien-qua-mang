package com.library.gui;

import com.library.client.LibraryClient;
import com.library.common.Message;
import com.library.model.BorrowRecord;
import com.library.model.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BorrowManagementPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final LibraryClient client;
    private DefaultTableModel borrowTableModel;
    private DefaultTableModel overdueTableModel;
    private JTable borrowTable;
    private JTable overdueTable;
    private JTabbedPane tabbedPane;
    private JButton borrowBookButton, returnBookButton, refreshButton;
    
    private final String[] borrowColumnNames = {
        "ID", "Mã sách", "Tên sách", "Người mượn", "Email", "SĐT", 
        "Ngày mượn", "Hạn trả", "Trạng thái", "Ghi chú"
    };
    
    private final String[] overdueColumnNames = {
        "ID", "Mã sách", "Tên sách", "Người mượn", "Email", "SĐT", 
        "Ngày mượn", "Hạn trả", "Số ngày trễ"
    };
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public BorrowManagementPanel(LibraryClient client) {
        this.client = client;
        initializeUI();
        setupEventHandlers();
        refreshData();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create borrow records tab
        createBorrowRecordsTab();
        
        // Create overdue books tab
        createOverdueBooksTab();
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create button panel
        createButtonPanel();
    }
    
    private void createBorrowRecordsTab() {
        JPanel borrowPanel = new JPanel(new BorderLayout());
        
        // Create borrow table
        borrowTableModel = new DefaultTableModel(borrowColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        borrowTable = new JTable(borrowTableModel);
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowTable.setRowHeight(25);
        borrowTable.setFont(new Font("Arial", Font.PLAIN, 12));
        borrowTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Hide ID column
        borrowTable.getColumnModel().getColumn(0).setMinWidth(0);
        borrowTable.getColumnModel().getColumn(0).setMaxWidth(0);
        borrowTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        borrowTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Book ID
        borrowTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Book Title
        borrowTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Borrower Name
        borrowTable.getColumnModel().getColumn(4).setPreferredWidth(180); // Email
        borrowTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Phone
        borrowTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Borrow Date
        borrowTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Return Date
        borrowTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Status
        borrowTable.getColumnModel().getColumn(9).setPreferredWidth(150); // Notes
        
        // Enable sorting
        TableRowSorter<DefaultTableModel> borrowSorter = new TableRowSorter<>(borrowTableModel);
        borrowTable.setRowSorter(borrowSorter);
        
        JScrollPane borrowScrollPane = new JScrollPane(borrowTable);
        borrowPanel.add(borrowScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📋 Danh sách mượn sách", borrowPanel);
    }
    
    private void createOverdueBooksTab() {
        JPanel overduePanel = new JPanel(new BorderLayout());
        
        // Create overdue table
        overdueTableModel = new DefaultTableModel(overdueColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        overdueTable = new JTable(overdueTableModel);
        overdueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        overdueTable.setRowHeight(25);
        overdueTable.setFont(new Font("Arial", Font.PLAIN, 12));
        overdueTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Hide ID column
        overdueTable.getColumnModel().getColumn(0).setMinWidth(0);
        overdueTable.getColumnModel().getColumn(0).setMaxWidth(0);
        overdueTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        overdueTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Book ID
        overdueTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Book Title
        overdueTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Borrower Name
        overdueTable.getColumnModel().getColumn(4).setPreferredWidth(180); // Email
        overdueTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Phone
        overdueTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Borrow Date
        overdueTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Return Date
        overdueTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Days Overdue
        
        // Enable sorting
        TableRowSorter<DefaultTableModel> overdueSorter = new TableRowSorter<>(overdueTableModel);
        overdueTable.setRowSorter(overdueSorter);
        
        // Color overdue rows in red
        overdueTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(new Color(255, 235, 238)); // Light red background
                    c.setForeground(new Color(183, 28, 28)); // Dark red text
                }
                return c;
            }
        });
        
        JScrollPane overdueScrollPane = new JScrollPane(overdueTable);
        overduePanel.add(overdueScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("⚠️ Sách quá hạn", overduePanel);
    }
    
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEtchedBorder());
        
        borrowBookButton = new JButton("📚 Mượn sách");
        borrowBookButton.setFont(new Font("Arial", Font.BOLD, 12));
        borrowBookButton.setBackground(new Color(76, 175, 80));
        borrowBookButton.setForeground(Color.WHITE);
        borrowBookButton.setFocusPainted(false);
        
        returnBookButton = new JButton("↩️ Trả sách");
        returnBookButton.setFont(new Font("Arial", Font.BOLD, 12));
        returnBookButton.setBackground(new Color(33, 150, 243));
        returnBookButton.setForeground(Color.WHITE);
        returnBookButton.setFocusPainted(false);
        returnBookButton.setEnabled(false);
        
        refreshButton = new JButton("🔄 Làm mới");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setBackground(new Color(158, 158, 158));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        
        buttonPanel.add(borrowBookButton);
        buttonPanel.add(returnBookButton);
        buttonPanel.add(refreshButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Table selection listeners
        borrowTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = borrowTable.getSelectedRow();
            if (selectedRow != -1) {
                overdueTable.clearSelection();
                String status = (String) borrowTableModel.getValueAt(selectedRow, 8);
                returnBookButton.setEnabled("BORROWED".equals(status));
            }
        });
        
        overdueTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = overdueTable.getSelectedRow();
            if (selectedRow != -1) {
                borrowTable.clearSelection();
                returnBookButton.setEnabled(true);
            }
        });
        
        // Double-click to return book
        borrowTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && borrowTable.getSelectedRow() != -1) {
                    int selectedRow = borrowTable.getSelectedRow();
                    String status = (String) borrowTableModel.getValueAt(selectedRow, 8);
                    if ("BORROWED".equals(status)) {
                        returnBook();
                    }
                }
            }
        });
        
        overdueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && overdueTable.getSelectedRow() != -1) {
                    returnBook();
                }
            }
        });
        
        // Button listeners
        borrowBookButton.addActionListener(e -> borrowBook());
        returnBookButton.addActionListener(e -> returnBook());
        refreshButton.addActionListener(e -> refreshData());
    }
    
    private void borrowBook() {
        BorrowBookDialog dialog = new BorrowBookDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), client);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            BorrowRecord borrowRecord = dialog.getBorrowRecord();
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Message request = new Message(Message.MessageType.BORROW_BOOK, borrowRecord);
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
                                BorrowManagementPanel.this,
                                "Mượn sách thành công!",
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshData();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                            BorrowManagementPanel.this,
                            "Lỗi mượn sách: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void returnBook() {
        String borrowRecordId = null;
        
        // Get the selected borrow record ID
        if (borrowTable.getSelectedRow() != -1) {
            borrowRecordId = (String) borrowTableModel.getValueAt(borrowTable.getSelectedRow(), 0);
        } else if (overdueTable.getSelectedRow() != -1) {
            borrowRecordId = (String) overdueTableModel.getValueAt(overdueTable.getSelectedRow(), 0);
        }
        
        if (borrowRecordId == null) {
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Xác nhận trả sách này?",
            "Xác nhận trả sách",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            String finalBorrowRecordId = borrowRecordId;
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Message request = new Message(Message.MessageType.RETURN_BOOK, finalBorrowRecordId);
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
                                BorrowManagementPanel.this,
                                "Trả sách thành công!",
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshData();
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                            BorrowManagementPanel.this,
                            "Lỗi trả sách: " + e.getMessage(),
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
        // Refresh borrow records
        SwingWorker<List<BorrowRecord>, Void> borrowWorker = new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.GET_BORROW_RECORDS, null);
                Message response = client.sendRequest(request);
                
                if (response.isSuccess()) {
                    return (List<BorrowRecord>) response.getData();
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<BorrowRecord> records = get();
                    updateBorrowTableData(records);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        BorrowManagementPanel.this,
                        "Lỗi tải danh sách mượn sách: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        // Refresh overdue books
        SwingWorker<List<BorrowRecord>, Void> overdueWorker = new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.GET_OVERDUE_BOOKS, null);
                Message response = client.sendRequest(request);
                
                if (response.isSuccess()) {
                    return (List<BorrowRecord>) response.getData();
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<BorrowRecord> records = get();
                    updateOverdueTableData(records);
                    
                    // Update tab title with overdue count
                    int overdueCount = records.size();
                    String tabTitle = overdueCount > 0 ? 
                        String.format("⚠️ Sách quá hạn (%d)", overdueCount) : 
                        "⚠️ Sách quá hạn";
                    tabbedPane.setTitleAt(1, tabTitle);
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        BorrowManagementPanel.this,
                        "Lỗi tải danh sách sách quá hạn: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        borrowWorker.execute();
        overdueWorker.execute();
    }
    
    private void updateBorrowTableData(List<BorrowRecord> records) {
        // Clear existing data
        borrowTableModel.setRowCount(0);
        
        // Add new data
        for (BorrowRecord record : records) {
            Object[] row = {
                record.getId(),
                record.getBookId(),
                record.getBookTitle(),
                record.getBorrowerName(),
                record.getBorrowerEmail(),
                record.getBorrowerPhone(),
                record.getBorrowDate().format(dateFormatter),
                record.getExpectedReturnDate().format(dateFormatter),
                record.getStatus().toString(),
                record.getNotes()
            };
            borrowTableModel.addRow(row);
        }
        
        // Clear selection
        borrowTable.clearSelection();
        returnBookButton.setEnabled(false);
    }
    
    private void updateOverdueTableData(List<BorrowRecord> records) {
        // Clear existing data
        overdueTableModel.setRowCount(0);
        
        // Add new data
        for (BorrowRecord record : records) {
            // Calculate days overdue
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                record.getExpectedReturnDate().toLocalDate(),
                java.time.LocalDate.now()
            );
            
            Object[] row = {
                record.getId(),
                record.getBookId(),
                record.getBookTitle(),
                record.getBorrowerName(),
                record.getBorrowerEmail(),
                record.getBorrowerPhone(),
                record.getBorrowDate().format(dateFormatter),
                record.getExpectedReturnDate().format(dateFormatter),
                daysOverdue + " ngày"
            };
            overdueTableModel.addRow(row);
        }
        
        // Clear selection
        overdueTable.clearSelection();
    }
}
