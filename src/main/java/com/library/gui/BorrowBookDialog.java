package com.library.gui;

import com.library.client.LibraryClient;
import com.library.common.Message;
import com.library.model.Book;
import com.library.model.BorrowRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.List;

public class BorrowBookDialog extends JDialog {
    private final LibraryClient client;
    private BorrowRecord borrowRecord;
    private boolean confirmed = false;
    
    // Form fields
    private JComboBox<BookItem> bookComboBox;
    private JTextField borrowerNameField;
    private JTextField borrowerEmailField;
    private JTextField borrowerPhoneField;
    private JSpinner daysToReturnSpinner;
    private JTextArea notesArea;
    private JButton borrowButton;
    private JButton cancelButton;
    private JButton refreshBooksButton;
    
    // Helper class for book items in combobox
    private static class BookItem {
        private final Book book;
        
        public BookItem(Book book) {
            this.book = book;
        }
        
        public Book getBook() {
            return book;
        }
        
        @Override
        public String toString() {
            return String.format("%s - %s (Còn: %d)", 
                book.getTitle(), book.getAuthor(), book.getAvailableCopies());
        }
    }
    
    public BorrowBookDialog(Frame parent, LibraryClient client) {
        super(parent, "Mượn sách", true);
        this.client = client;
        initializeUI();
        setupEventHandlers();
        loadAvailableBooks();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(500, 400);
        
        // Create form panel
        createFormPanel();
        
        // Create button panel
        createButtonPanel();
    }
    
    private void createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Book selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Chọn sách: *"), gbc);
        
        JPanel bookPanel = new JPanel(new BorderLayout());
        bookComboBox = new JComboBox<>();
        bookComboBox.setPreferredSize(new Dimension(300, 25));
        refreshBooksButton = new JButton("🔄");
        refreshBooksButton.setPreferredSize(new Dimension(30, 25));
        refreshBooksButton.setToolTipText("Làm mới danh sách sách");
        
        bookPanel.add(bookComboBox, BorderLayout.CENTER);
        bookPanel.add(refreshBooksButton, BorderLayout.EAST);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(bookPanel, gbc);
        
        // Borrower name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tên người mượn: *"), gbc);
        
        borrowerNameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(borrowerNameField, gbc);
        
        // Borrower email
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email: *"), gbc);
        
        borrowerEmailField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(borrowerEmailField, gbc);
        
        // Borrower phone
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Số điện thoại:"), gbc);
        
        borrowerPhoneField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(borrowerPhoneField, gbc);
          // Days to return
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Số ngày mượn: *"), gbc);
        
        int defaultDays = com.library.common.Configuration.getDefaultBorrowDays();
        int maxDays = com.library.common.Configuration.getMaxBorrowDays();
        daysToReturnSpinner = new JSpinner(new SpinnerNumberModel(defaultDays, 1, maxDays, 1));
        daysToReturnSpinner.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(daysToReturnSpinner, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Ghi chú:"), gbc);
        
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        formPanel.add(notesScrollPane, gbc);
        
        // Required fields note
        JLabel noteLabel = new JLabel("* Trường bắt buộc");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        noteLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        formPanel.add(noteLabel, gbc);
        
        add(formPanel, BorderLayout.CENTER);
    }
    
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        borrowButton = new JButton("📚 Mượn sách");
        borrowButton.setFont(new Font("Arial", Font.BOLD, 12));
        borrowButton.setBackground(new Color(76, 175, 80));
        borrowButton.setForeground(Color.WHITE);
        borrowButton.setFocusPainted(false);
        
        cancelButton = new JButton("❌ Hủy");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(borrowButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        borrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    createBorrowRecord();
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
        
        refreshBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAvailableBooks();
            }
        });
        
        // ESC to cancel
        getRootPane().registerKeyboardAction(
            e -> {
                confirmed = false;
                dispose();
            },
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void loadAvailableBooks() {
        refreshBooksButton.setEnabled(false);
        refreshBooksButton.setText("⏳");
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                Message request = new Message(Message.MessageType.GET_ALL_BOOKS, null);
                Message response = client.sendRequest(request);
                  if (response.isSuccess()) {
                    @SuppressWarnings("unchecked")
                    List<Book> bookList = (List<Book>) response.getData();
                    return bookList;
                } else {
                    throw new Exception(response.getErrorMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    
                    // Clear existing items
                    bookComboBox.removeAllItems();
                    
                    // Add only books with available copies
                    for (Book book : books) {
                        if (book.getAvailableCopies() > 0) {
                            bookComboBox.addItem(new BookItem(book));
                        }
                    }
                    
                    if (bookComboBox.getItemCount() == 0) {
                        JOptionPane.showMessageDialog(
                            BorrowBookDialog.this,
                            "Không có sách nào có sẵn để mượn!",
                            "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        BorrowBookDialog.this,
                        "Lỗi tải danh sách sách: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    refreshBooksButton.setEnabled(true);
                    refreshBooksButton.setText("🔄");
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (bookComboBox.getSelectedItem() == null) {
            errors.append("- Vui lòng chọn sách\n");
        }
        
        if (borrowerNameField.getText().trim().isEmpty()) {
            errors.append("- Tên người mượn không được để trống\n");
        }
        
        if (borrowerEmailField.getText().trim().isEmpty()) {
            errors.append("- Email không được để trống\n");
        } else {
            String email = borrowerEmailField.getText().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errors.append("- Email không hợp lệ\n");
            }
        }
        
        int daysToReturn = (Integer) daysToReturnSpinner.getValue();
        if (daysToReturn < 1) {
            errors.append("- Số ngày mượn phải lớn hơn 0\n");
        }
        
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng sửa các lỗi sau:\n" + errors.toString(),
                "Lỗi nhập liệu",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        return true;
    }
    
    private void createBorrowRecord() {
        BookItem selectedBookItem = (BookItem) bookComboBox.getSelectedItem();
        Book selectedBook = selectedBookItem.getBook();
        
        int daysToReturn = (Integer) daysToReturnSpinner.getValue();
        LocalDateTime expectedReturnDate = LocalDateTime.now().plusDays(daysToReturn);
        
        borrowRecord = new BorrowRecord(
            selectedBook.getId(),
            selectedBook.getTitle(),
            borrowerNameField.getText().trim(),
            borrowerEmailField.getText().trim(),
            borrowerPhoneField.getText().trim(),
            expectedReturnDate
        );
        
        borrowRecord.setNotes(notesArea.getText().trim());
    }
    
    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
