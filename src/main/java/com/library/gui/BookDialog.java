package com.library.gui;

import com.library.model.Book;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

public class BookDialog extends JDialog {
    private Book book;
    private boolean confirmed = false;
    
    // Form fields
    private JTextField titleField;
    private JTextField authorField;
    private JTextField isbnField;
    private JTextField categoryField;
    private JTextField publisherField;
    private JSpinner publishYearSpinner;
    private JSpinner totalCopiesSpinner;
    private JTextArea descriptionArea;
    private JButton saveButton;
    private JButton cancelButton;
    
    public BookDialog(Frame parent, Book book) {
        super(parent, book == null ? "Thêm sách mới" : "Sửa thông tin sách", true);
        this.book = book;
        initializeUI();
        setupEventHandlers();
        
        if (book != null) {
            populateFields();
        }
        
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
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tiêu đề: *"), gbc);
        
        titleField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(titleField, gbc);
        
        // Author
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tác giả: *"), gbc);
        
        authorField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(authorField, gbc);
        
        // ISBN
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("ISBN: *"), gbc);
        
        isbnField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(isbnField, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Thể loại:"), gbc);
        
        categoryField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(categoryField, gbc);
        
        // Publisher
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Nhà xuất bản:"), gbc);
        
        publisherField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(publisherField, gbc);
        
        // Publish Year
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Năm xuất bản:"), gbc);
        
        publishYearSpinner = new JSpinner(new SpinnerNumberModel(2025, 1800, 2100, 1));
        publishYearSpinner.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(publishYearSpinner, gbc);
        
        // Total Copies
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tổng số bản: *"), gbc);
        
        totalCopiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        totalCopiesSpinner.setPreferredSize(new Dimension(100, 25));
        gbc.gridx = 1; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(totalCopiesSpinner, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        gbc.gridx = 1; gbc.gridy = 7; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        formPanel.add(descScrollPane, gbc);
        
        // Required fields note
        JLabel noteLabel = new JLabel("* Trường bắt buộc");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        noteLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        formPanel.add(noteLabel, gbc);
        
        add(formPanel, BorderLayout.CENTER);
    }
    
    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        saveButton = new JButton("💾 Lưu");
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        
        cancelButton = new JButton("❌ Hủy");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    saveBook();
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
        
        // ESC to cancel
        getRootPane().registerKeyboardAction(
            e -> {
                confirmed = false;
                dispose();
            },
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Enter to save
        getRootPane().registerKeyboardAction(
            e -> {
                if (validateFields()) {
                    saveBook();
                    confirmed = true;
                    dispose();
                }
            },
            KeyStroke.getKeyStroke("ENTER"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private void populateFields() {
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn());
            categoryField.setText(book.getCategory());
            publisherField.setText(book.getPublisher());
            publishYearSpinner.setValue(book.getPublishYear());
            totalCopiesSpinner.setValue(book.getTotalCopies());
            descriptionArea.setText(book.getDescription());
            
            // Disable ISBN editing for existing books
            isbnField.setEditable(false);
        }
    }
    
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (titleField.getText().trim().isEmpty()) {
            errors.append("- Tiêu đề không được để trống\n");
        }
        
        if (authorField.getText().trim().isEmpty()) {
            errors.append("- Tác giả không được để trống\n");
        }
        
        if (isbnField.getText().trim().isEmpty()) {
            errors.append("- ISBN không được để trống\n");
        }
        
        int totalCopies = (Integer) totalCopiesSpinner.getValue();
        if (totalCopies < 1) {
            errors.append("- Tổng số bản phải lớn hơn 0\n");
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
    
    private void saveBook() {
        if (book == null) {
            book = new Book();
            book.setCreatedAt(LocalDateTime.now());
        }
        
        book.setTitle(titleField.getText().trim());
        book.setAuthor(authorField.getText().trim());
        book.setIsbn(isbnField.getText().trim());
        book.setCategory(categoryField.getText().trim());
        book.setPublisher(publisherField.getText().trim());
        book.setPublishYear((Integer) publishYearSpinner.getValue());
        book.setDescription(descriptionArea.getText().trim());
        book.setUpdatedAt(LocalDateTime.now());
        
        int newTotalCopies = (Integer) totalCopiesSpinner.getValue();
        int oldTotalCopies = book.getTotalCopies();
        int currentAvailable = book.getAvailableCopies();
        
        // Calculate new available copies
        int difference = newTotalCopies - oldTotalCopies;
        int newAvailableCopies = currentAvailable + difference;
        
        // Ensure available copies doesn't go below 0
        if (newAvailableCopies < 0) {
            newAvailableCopies = 0;
        }
        
        book.setTotalCopies(newTotalCopies);
        book.setAvailableCopies(newAvailableCopies);
    }
    
    public Book getBook() {
        return book;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
