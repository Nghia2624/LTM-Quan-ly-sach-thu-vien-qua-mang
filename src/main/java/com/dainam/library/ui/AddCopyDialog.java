package com.dainam.library.ui;

import com.dainam.library.model.Book;
import com.dainam.library.model.BookCopy;
import com.dainam.library.service.BookService;
import com.dainam.library.util.LoggerUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

/**
 * Dialog thêm bản sao sách
 */
public class AddCopyDialog extends JDialog {
      private Book book;
    private JTextField locationField;
    private JComboBox<String> conditionComboBox;
    private JTextArea notesArea;
    
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    private BookService bookService;
    private boolean copyAdded = false;
    
    public AddCopyDialog(JFrame parent, Book book) {
        super(parent, "Thêm bản sao sách", true);
        this.book = book;
        this.bookService = new BookService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(true);
    }
      private void initializeComponents() {
        // Text fields
        locationField = new JTextField(20);
        
        // Combo box
        conditionComboBox = new JComboBox<>();
        conditionComboBox.addItem("Mới");
        conditionComboBox.addItem("Tốt");
        conditionComboBox.addItem("Khá");
        conditionComboBox.addItem("Trung bình");
        conditionComboBox.addItem("Cũ");
        
        // Text area
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        // Buttons
        saveButton = new JButton("Thêm");
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
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Title
        JLabel titleLabel = new JLabel("Thêm bản sao cho: " + book.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
          // Location
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Vị trí *:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(locationField, gbc);
        row++;
        
        // Condition
        gbc.gridx = 0;
        gbc.gridy = row;
        mainPanel.add(new JLabel("Tình trạng:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(conditionComboBox, gbc);
        row++;
        
        // Notes
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(new JScrollPane(notesArea), gbc);
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
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCopy();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }    private void addCopy() {
        // Get input values
        String location = locationField.getText().trim();
        String condition = (String) conditionComboBox.getSelectedItem();
        String notes = notesArea.getText().trim();
        
        // Validate required fields
        if (location.isEmpty()) {
            showStatus("Vui lòng nhập vị trí bản sao", true);
            return;
        }
        
        // Show loading
        saveButton.setEnabled(false);
        saveButton.setText("Đang thêm...");
        statusLabel.setText("Đang thêm bản sao...");
        statusLabel.setForeground(Color.BLUE);
        
        // Add copy in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create copy object
                BookCopy copy = new BookCopy();
                copy.setBookId(book.getBookId());
                copy.setStatus(BookCopy.Status.AVAILABLE);
                copy.setLocation(location);
                copy.setPurchaseDate(LocalDate.now());
                
                // Map Vietnamese condition to enum
                BookCopy.Condition copyCondition;
                switch (condition) {
                    case "Mới":
                        copyCondition = BookCopy.Condition.NEW;
                        break;
                    case "Tốt":
                        copyCondition = BookCopy.Condition.GOOD;
                        break;
                    case "Khá":
                        copyCondition = BookCopy.Condition.FAIR;
                        break;
                    case "Trung bình":
                        copyCondition = BookCopy.Condition.POOR;
                        break;
                    case "Cũ":
                        copyCondition = BookCopy.Condition.OLD;
                        break;
                    default:
                        copyCondition = BookCopy.Condition.GOOD;
                        break;
                }
                copy.setCondition(copyCondition);
                copy.setNotes(notes);
                
                boolean success = bookService.addBookCopy(book.getBookId(), copy);
                
                if (success) {
                    showStatus("Thêm bản sao thành công!", false);
                    copyAdded = true;
                    
                    // Close dialog after 1 second
                    Timer timer = new Timer(1000, e -> dispose());
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    showStatus("Thêm bản sao thất bại. Vui lòng thử lại.", true);
                }
            } catch (Exception ex) {
                LoggerUtil.error("Lỗi thêm bản sao: " + ex.getMessage());
                showStatus("Lỗi thêm bản sao: " + ex.getMessage(), true);
            } finally {
                saveButton.setEnabled(true);
                saveButton.setText("Thêm");
            }
        });
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : Color.GREEN);
    }
    
    public boolean isCopyAdded() {
        return copyAdded;
    }
}
