package com.dainam.library.ui;

import com.dainam.library.model.Book;
import com.dainam.library.service.BookService;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog thêm sách mới
 */
public class AddBookDialog extends JDialog {
    
    private JTextField titleField;
    private JTextField authorField;
    private JTextField isbnField;
    private JTextField publisherField;
    private JSpinner yearSpinner;
    private JComboBox<String> categoryComboBox;
    private JTextArea descriptionArea;
    private JTextField languageField;
    private JSpinner pageCountSpinner;
    private JSpinner priceSpinner;
    private JTextField coverImageField;
    
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    private BookService bookService;
    private boolean bookAdded = false;
    
    public AddBookDialog(JFrame parent) {
        super(parent, "Thêm sách mới", true);
        this.bookService = new BookService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setSize(650, 700);
        setLocationRelativeTo(parent);
        setResizable(true);
    }
    
    private void initializeComponents() {
        // Text fields
        titleField = new JTextField(30);
        authorField = new JTextField(30);
        isbnField = new JTextField(30);
        publisherField = new JTextField(30);
        languageField = new JTextField(30);
        coverImageField = new JTextField(30);
        
        // Spinners
        yearSpinner = new JSpinner(new SpinnerNumberModel(2024, 1900, 2030, 1));
        pageCountSpinner = new JSpinner(new SpinnerNumberModel(200, 1, 10000, 1));
        priceSpinner = new JSpinner(new SpinnerNumberModel(50000, 0, 10000000, 1000));
        
        // Combo box
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem("Công nghệ thông tin");
        categoryComboBox.addItem("Khoa học máy tính");
        categoryComboBox.addItem("Toán học");
        categoryComboBox.addItem("Vật lý");
        categoryComboBox.addItem("Hóa học");
        categoryComboBox.addItem("Sinh học");
        categoryComboBox.addItem("Kinh tế");
        categoryComboBox.addItem("Quản trị kinh doanh");
        categoryComboBox.addItem("Ngoại ngữ");
        categoryComboBox.addItem("Văn học");
        categoryComboBox.addItem("Lịch sử");
        categoryComboBox.addItem("Địa lý");
        categoryComboBox.addItem("Tâm lý học");
        categoryComboBox.addItem("Triết học");
        categoryComboBox.addItem("Luật");
        
        // Text area
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        // Buttons
        saveButton = new JButton("Lưu");
        saveButton.setPreferredSize(new Dimension(100, 35));
        
        cancelButton = new JButton("Hủy");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel with scroll
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Title
        JLabel titleLabel = new JLabel("Thông tin sách mới");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Tiêu đề *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(titleField, gbc);
        row++;
        
        // Author
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Tác giả *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(authorField, gbc);
        row++;
        
        // ISBN
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("ISBN *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(isbnField, gbc);
        row++;
        
        // Publisher
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Nhà xuất bản *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(publisherField, gbc);
        row++;
        
        // Year
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Năm xuất bản *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(yearSpinner, gbc);
        row++;
        
        // Category
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Thể loại *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(categoryComboBox, gbc);
        row++;
        
        // Language
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Ngôn ngữ:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(languageField, gbc);
        row++;
        
        // Page count
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Số trang:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(pageCountSpinner, gbc);
        row++;
        
        // Price
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Giá (VND) *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(priceSpinner, gbc);
        row++;
        
        // Cover image
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Hình ảnh bìa:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(coverImageField, gbc);
        row++;
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(new JScrollPane(descriptionArea), gbc);
        row++;
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(buttonPanel, gbc);
        row++;
        
        // Status label
        gbc.gridy = row;
        gbc.insets = new Insets(5, 5, 0, 5);
        mainPanel.add(statusLabel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBook();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
      private void saveBook() {
        // Get input values
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String publisher = publisherField.getText().trim();
        int year = (Integer) yearSpinner.getValue();
        String category = (String) categoryComboBox.getSelectedItem();
        String language = languageField.getText().trim();
        int pageCount = (Integer) pageCountSpinner.getValue();
        double price = (Double) priceSpinner.getValue();
        String coverImage = coverImageField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        // Validate required fields
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || publisher.isEmpty()) {
            showStatus("Vui lòng điền đầy đủ các trường bắt buộc (*)", true);
            return;
        }
        
        // Validate input format
        String validationError = ValidationUtil.validateBook(title, author, isbn, publisher, year, price);
        if (validationError != null) {
            showStatus(validationError, true);
            return;
        }
          // Check for duplicate ISBN
        try {
            Book existingBook = bookService.getBookByISBN(isbn);
            if (existingBook != null) {
                showStatus("ISBN đã tồn tại: " + existingBook.getTitle(), true);
                return;
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi kiểm tra ISBN trùng lặp: " + e.getMessage());
        }
        
        // Show loading
        saveButton.setEnabled(false);
        saveButton.setText("Đang lưu...");
        statusLabel.setText("Đang lưu sách...");
        statusLabel.setForeground(Color.BLUE);
        
        // Save book in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create book object
                Book book = new Book();
                book.setTitle(title);
                book.setAuthor(author);
                book.setIsbn(isbn);
                book.setPublisher(publisher);
                book.setPublicationYear(year);
                book.setCategory(category);
                book.setLanguage(language.isEmpty() ? "Tiếng Việt" : language);
                book.setPageCount(pageCount);
                book.setPrice(price);
                book.setCoverImage(coverImage);
                book.setDescription(description);
                book.setTotalCopies(0);
                book.setAvailableCopies(0);
                
                boolean success = bookService.addBook(book);
                
                if (success) {
                    showStatus("Thêm sách thành công!", false);
                    bookAdded = true;
                    
                    // Close dialog after 1 second
                    Timer timer = new Timer(1000, e -> dispose());
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    showStatus("Thêm sách thất bại. Vui lòng thử lại.", true);
                }
            } catch (Exception ex) {
                LoggerUtil.error("Lỗi thêm sách: " + ex.getMessage());
                showStatus("Lỗi thêm sách: " + ex.getMessage(), true);
            } finally {
                saveButton.setEnabled(true);
                saveButton.setText("Lưu");
            }
        });
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : Color.GREEN);
    }
    
    public boolean isBookAdded() {
        return bookAdded;
    }
}
