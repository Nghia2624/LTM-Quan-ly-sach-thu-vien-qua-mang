package client.ui;

import client.NetworkService;
import models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Main GUI class for Library Management System Client
 */
public class LibraryClientGUI extends JFrame {
    private NetworkService networkService;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton connectButton, disconnectButton;
    private JLabel statusLabel;
      // Book form components
    private JTextField idField, titleField, authorField, categoryField, yearField;
    
    public LibraryClientGUI() {
        networkService = new NetworkService();
        initializeGUI();
        setupEventHandlers();
    }
    
    private void initializeGUI() {
        setTitle("Library Management System - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create menu bar
        createMenuBar();
        
        // Create main panels
        add(createConnectionPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);
        
        // Set window properties
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Book menu
        JMenu bookMenu = new JMenu("Book");
        JMenuItem addBookItem = new JMenuItem("Add Book");
        JMenuItem updateBookItem = new JMenuItem("Update Book");
        JMenuItem deleteBookItem = new JMenuItem("Delete Book");
        JMenuItem borrowBookItem = new JMenuItem("Borrow Book");
        JMenuItem returnBookItem = new JMenuItem("Return Book");
        
        addBookItem.addActionListener(e -> showAddBookDialog());
        updateBookItem.addActionListener(e -> showUpdateBookDialog());
        deleteBookItem.addActionListener(e -> deleteSelectedBook());
        borrowBookItem.addActionListener(e -> showBorrowBookDialog());
        returnBookItem.addActionListener(e -> returnSelectedBook());
        
        bookMenu.add(addBookItem);
        bookMenu.add(updateBookItem);
        bookMenu.addSeparator();
        bookMenu.add(deleteBookItem);
        bookMenu.addSeparator();
        bookMenu.add(borrowBookItem);
        bookMenu.add(returnBookItem);
        
        menuBar.add(fileMenu);
        menuBar.add(bookMenu);
        setJMenuBar(menuBar);
    }
    
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Connection"));
        
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        
        panel.add(connectButton);
        panel.add(disconnectButton);
        
        return panel;
    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh All");
        
        searchButton.addActionListener(e -> searchBooks());
        refreshButton.addActionListener(e -> loadAllBooks());
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        
        // Table panel
        createBookTable();
        JScrollPane tableScrollPane = new JScrollPane(bookTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Books"));
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private void createBookTable() {
        String[] columns = {"ID", "Title", "Author", "Category", "Year", "Status", "Borrowed By", "Borrow Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setRowHeight(25);
        
        // Set column widths
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Title
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Author
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Category
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Year
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status
        bookTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Borrowed By
        bookTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Borrow Date
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Not connected to server");
        panel.add(statusLabel);
        return panel;
    }
    
    private void setupEventHandlers() {
        // Connection buttons
        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.addActionListener(e -> disconnectFromServer());
        
        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (networkService.isConnected()) {
                    networkService.disconnect();
                }
                System.exit(0);
            }
        });
        
        // Search field enter key
        searchField.addActionListener(e -> searchBooks());
    }
    
    private void connectToServer() {
        if (networkService.connect()) {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            statusLabel.setText("Connected to server");
            statusLabel.setForeground(Color.GREEN);
            loadAllBooks();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to connect to server!", "Connection Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Connection failed");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void disconnectFromServer() {
        networkService.disconnect();
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        statusLabel.setText("Disconnected from server");
        statusLabel.setForeground(Color.BLACK);
        tableModel.setRowCount(0); // Clear table
    }
    
    private void loadAllBooks() {
        if (!networkService.isConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect to server first!", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Request request = new Request(Request.RequestType.GET_ALL_BOOKS);
        Response response = networkService.sendRequest(request);
        
        if (response.getStatus() == Response.Status.SUCCESS) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            updateBookTable(books);
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchBooks() {
        if (!networkService.isConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect to server first!", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadAllBooks();
            return;
        }
        
        Request request = new Request(Request.RequestType.SEARCH_BOOK, query);
        Response response = networkService.sendRequest(request);
        
        if (response.getStatus() == Response.Status.SUCCESS) {
            @SuppressWarnings("unchecked")
            List<Book> books = (List<Book>) response.getData();
            updateBookTable(books);
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateBookTable(List<Book> books) {
        tableModel.setRowCount(0); // Clear existing data
        
        for (Book book : books) {
            Object[] rowData = {
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getPublishYear(),
                book.isAvailable() ? "Available" : "Borrowed",
                book.getBorrowedBy() != null ? book.getBorrowedBy() : "",
                book.getBorrowDate() != null ? book.getBorrowDate() : ""
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void showAddBookDialog() {
        if (!networkService.isConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect to server first!", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Create form fields
        idField = new JTextField(15);
        titleField = new JTextField(15);
        authorField = new JTextField(15);
        categoryField = new JTextField(15);
        yearField = new JTextField(15);
        
        // Add form components
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(authorField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(categoryField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Publish Year:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(yearField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");
        
        addButton.addActionListener(e -> {
            if (addBook()) {
                dialog.dispose();
                loadAllBooks(); // Refresh table
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private boolean addBook() {
        try {
            String id = idField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String yearText = yearField.getText().trim();
            
            if (id.isEmpty() || title.isEmpty() || author.isEmpty() || category.isEmpty() || yearText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            int year = Integer.parseInt(yearText);
            Book book = new Book(id, title, author, category, year);
            
            Request request = new Request(Request.RequestType.ADD_BOOK, book);
            Response response = networkService.sendRequest(request);
            
            if (response.getStatus() == Response.Status.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    
    private void showUpdateBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to update!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!networkService.isConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect to server first!", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected book data
        String bookId = (String) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        String author = (String) tableModel.getValueAt(selectedRow, 2);
        String category = (String) tableModel.getValueAt(selectedRow, 3);
        int year = (Integer) tableModel.getValueAt(selectedRow, 4);
        
        JDialog dialog = new JDialog(this, "Update Book", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Create form fields with existing data
        idField = new JTextField(bookId, 15);
        idField.setEditable(false); // ID shouldn't be editable
        titleField = new JTextField(title, 15);
        authorField = new JTextField(author, 15);
        categoryField = new JTextField(category, 15);
        yearField = new JTextField(String.valueOf(year), 15);
        
        // Add form components (similar to add dialog)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(idField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(authorField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(categoryField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel("Publish Year:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        dialog.add(yearField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");
        
        updateButton.addActionListener(e -> {
            if (updateBook()) {
                dialog.dispose();
                loadAllBooks(); // Refresh table
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private boolean updateBook() {
        try {
            String id = idField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String yearText = yearField.getText().trim();
            
            if (title.isEmpty() || author.isEmpty() || category.isEmpty() || yearText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            int year = Integer.parseInt(yearText);
            Book book = new Book(id, title, author, category, year);
            
            Request request = new Request(Request.RequestType.UPDATE_BOOK, book);
            Response response = networkService.sendRequest(request);
            
            if (response.getStatus() == Response.Status.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Book updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year format!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    
    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!networkService.isConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect to server first!", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookId = (String) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete the book:\n" + title + " (ID: " + bookId + ")?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Request request = new Request(Request.RequestType.DELETE_BOOK, bookId);
            Response response = networkService.sendRequest(request);
            
            if (response.getStatus() == Response.Status.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAllBooks(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showBorrowBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!networkService.isConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect to server first!", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookId = (String) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if ("Borrowed".equals(status)) {
            JOptionPane.showMessageDialog(this, "This book is already borrowed!", "Book Not Available", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String borrowerName = JOptionPane.showInputDialog(this, 
            "Enter borrower name for book:\n" + title + " (ID: " + bookId + ")", 
            "Borrow Book", JOptionPane.QUESTION_MESSAGE);
        
        if (borrowerName != null && !borrowerName.trim().isEmpty()) {
            String[] data = {bookId, borrowerName.trim()};
            Request request = new Request(Request.RequestType.BORROW_BOOK, data);
            Response response = networkService.sendRequest(request);
            
            if (response.getStatus() == Response.Status.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Book borrowed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAllBooks(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void returnSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to return!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!networkService.isConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect to server first!", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String bookId = (String) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        String status = (String) tableModel.getValueAt(selectedRow, 5);
        
        if ("Available".equals(status)) {
            JOptionPane.showMessageDialog(this, "This book is not currently borrowed!", "Book Already Available", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to return the book:\n" + title + " (ID: " + bookId + ")?", 
            "Confirm Return", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Request request = new Request(Request.RequestType.RETURN_BOOK, bookId);
            Response response = networkService.sendRequest(request);
            
            if (response.getStatus() == Response.Status.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAllBooks(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }    public static void main(String[] args) {        // Set system look and feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> new LibraryClientGUI());
    }
}
