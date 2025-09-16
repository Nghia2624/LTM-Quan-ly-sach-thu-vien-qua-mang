package com.library.server;

import com.library.common.Message;
import com.library.common.LoginRequest;
import com.library.common.SearchCriteria;
import com.library.database.BookDAO;
import com.library.database.BorrowRecordDAO;
import com.library.database.UserDAO;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final BookDAO bookDAO;
    private final BorrowRecordDAO borrowRecordDAO;
    private final UserDAO userDAO;
    private User currentUser;
      public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        // Server creates ObjectInputStream first, then ObjectOutputStream (opposite of client)
        this.in = new ObjectInputStream(clientSocket.getInputStream());
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.out.flush(); // Flush the header
        this.bookDAO = new BookDAO();
        this.borrowRecordDAO = new BorrowRecordDAO();
        this.userDAO = new UserDAO();
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            
            while (!clientSocket.isClosed()) {                try {
                    Message request = (Message) in.readObject();
                    System.out.println("Received request: " + (request != null ? request.getType() : "null"));
                    Message response = handleRequest(request);
                    out.reset(); // Clear cached references
                    out.writeObject(response);
                    out.flush();
                } catch (Exception e) {
                    System.err.println("Error handling client request: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }
      private Message handleRequest(Message request) {
        if (request == null) {
            return Message.error(Message.MessageType.RESPONSE, "Request is null");
        }
        
        try {
            switch (request.getType()) {
                case LOGIN:
                    return handleLogin(request);
                case LOGOUT:
                    return handleLogout();
                case ADD_BOOK:
                    return handleAddBook(request);
                case UPDATE_BOOK:
                    return handleUpdateBook(request);
                case DELETE_BOOK:
                    return handleDeleteBook(request);
                case GET_BOOK:
                    return handleGetBook(request);
                case GET_ALL_BOOKS:
                    return handleGetAllBooks();
                case SEARCH_BOOKS:
                    return handleSearchBooks(request);
                case BORROW_BOOK:
                    return handleBorrowBook(request);
                case RETURN_BOOK:
                    return handleReturnBook(request);
                case GET_BORROW_RECORDS:
                    return handleGetBorrowRecords();
                case GET_OVERDUE_BOOKS:
                    return handleGetOverdueBooks();
                case GET_STATISTICS:
                    return handleGetStatistics();
                default:
                    return Message.error(Message.MessageType.RESPONSE, "Unknown request type");
            }
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Server error: " + e.getMessage());
        }
    }
    
    private Message handleLogin(Message request) {
        try {
            LoginRequest loginRequest = (LoginRequest) request.getData();
            User user = userDAO.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            
            if (user != null) {
                this.currentUser = user;
                return Message.success(Message.MessageType.RESPONSE, user);
            } else {
                return Message.error(Message.MessageType.RESPONSE, "Invalid email or password");
            }
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Login error: " + e.getMessage());
        }
    }
    
    private Message handleLogout() {
        this.currentUser = null;
        return Message.success(Message.MessageType.RESPONSE, "Logged out successfully");
    }
    
    private Message handleAddBook(Message request) {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            Book book = (Book) request.getData();
            boolean success = bookDAO.addBook(book);
            
            if (success) {
                return Message.success(Message.MessageType.RESPONSE, book);
            } else {
                return Message.error(Message.MessageType.RESPONSE, "Failed to add book");
            }
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error adding book: " + e.getMessage());
        }
    }
    
    private Message handleUpdateBook(Message request) {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            Book book = (Book) request.getData();
            boolean success = bookDAO.updateBook(book);
            
            if (success) {
                return Message.success(Message.MessageType.RESPONSE, book);
            } else {
                return Message.error(Message.MessageType.RESPONSE, "Failed to update book");
            }
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error updating book: " + e.getMessage());
        }
    }
    
    private Message handleDeleteBook(Message request) {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            String bookId = (String) request.getData();
            
            // Check if book is currently borrowed
            BorrowRecord borrowRecord = borrowRecordDAO.getBorrowRecordByBookId(bookId);
            if (borrowRecord != null) {
                return Message.error(Message.MessageType.RESPONSE, 
                    "Cannot delete book. It is currently borrowed.");
            }
            
            boolean success = bookDAO.deleteBook(bookId);
            
            if (success) {
                return Message.success(Message.MessageType.RESPONSE, "Book deleted successfully");
            } else {
                return Message.error(Message.MessageType.RESPONSE, "Failed to delete book");
            }
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error deleting book: " + e.getMessage());
        }
    }
    
    private Message handleGetBook(Message request) {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            String bookId = (String) request.getData();
            Book book = bookDAO.getBook(bookId);
            
            if (book != null) {
                return Message.success(Message.MessageType.RESPONSE, book);
            } else {
                return Message.error(Message.MessageType.RESPONSE, "Book not found");
            }
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error getting book: " + e.getMessage());
        }
    }
    
    private Message handleGetAllBooks() {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            List<Book> books = bookDAO.getAllBooks();
            return Message.success(Message.MessageType.RESPONSE, books);
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error getting books: " + e.getMessage());
        }
    }
    
    private Message handleSearchBooks(Message request) {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            SearchCriteria criteria = (SearchCriteria) request.getData();
            List<Book> books = bookDAO.searchBooks(criteria);
            return Message.success(Message.MessageType.RESPONSE, books);
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error searching books: " + e.getMessage());
        }
    }
    
    private Message handleBorrowBook(Message request) {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            BorrowRecord borrowRecord = (BorrowRecord) request.getData();
            
            // Check if book exists and is available
            Book book = bookDAO.getBook(borrowRecord.getBookId());
            if (book == null) {
                return Message.error(Message.MessageType.RESPONSE, "Book not found");
            }
            
            if (book.getAvailableCopies() <= 0) {
                return Message.error(Message.MessageType.RESPONSE, "No available copies");
            }
            
            // Add borrow record
            boolean borrowSuccess = borrowRecordDAO.addBorrowRecord(borrowRecord);
            if (!borrowSuccess) {
                return Message.error(Message.MessageType.RESPONSE, "Failed to create borrow record");
            }
            
            // Update available copies
            boolean updateSuccess = bookDAO.updateAvailableCopies(
                borrowRecord.getBookId(), 
                book.getAvailableCopies() - 1
            );
            
            if (updateSuccess) {
                return Message.success(Message.MessageType.RESPONSE, borrowRecord);
            } else {
                return Message.error(Message.MessageType.RESPONSE, "Failed to update book availability");
            }
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error borrowing book: " + e.getMessage());
        }
    }
    
    private Message handleReturnBook(Message request) {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            String borrowRecordId = (String) request.getData();
            
            // Get borrow record
            BorrowRecord borrowRecord = borrowRecordDAO.getAllBorrowRecords().stream()
                .filter(r -> r.getId().equals(borrowRecordId))
                .findFirst()
                .orElse(null);
            
            if (borrowRecord == null) {
                return Message.error(Message.MessageType.RESPONSE, "Borrow record not found");
            }
            
            // Update borrow record
            borrowRecord.setActualReturnDate(LocalDateTime.now());
            borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);
            
            boolean updateRecordSuccess = borrowRecordDAO.updateBorrowRecord(borrowRecord);
            if (!updateRecordSuccess) {
                return Message.error(Message.MessageType.RESPONSE, "Failed to update borrow record");
            }
            
            // Update available copies
            Book book = bookDAO.getBook(borrowRecord.getBookId());
            if (book != null) {
                bookDAO.updateAvailableCopies(
                    borrowRecord.getBookId(), 
                    book.getAvailableCopies() + 1
                );
            }
            
            return Message.success(Message.MessageType.RESPONSE, borrowRecord);
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error returning book: " + e.getMessage());
        }
    }
    
    private Message handleGetBorrowRecords() {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            List<BorrowRecord> records = borrowRecordDAO.getAllBorrowRecords();
            return Message.success(Message.MessageType.RESPONSE, records);
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error getting borrow records: " + e.getMessage());
        }
    }
    
    private Message handleGetOverdueBooks() {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            List<BorrowRecord> overdueBooks = borrowRecordDAO.getOverdueBooks();
            return Message.success(Message.MessageType.RESPONSE, overdueBooks);
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error getting overdue books: " + e.getMessage());
        }
    }
    
    private Message handleGetStatistics() {
        if (!isAuthenticated()) {
            return Message.error(Message.MessageType.RESPONSE, "Authentication required");
        }
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            List<Book> allBooks = bookDAO.getAllBooks();
            statistics.put("totalBooks", allBooks.size());
            statistics.put("totalCopies", allBooks.stream().mapToInt(Book::getTotalCopies).sum());
            statistics.put("availableCopies", allBooks.stream().mapToInt(Book::getAvailableCopies).sum());
            statistics.put("borrowedBooks", borrowRecordDAO.getTotalBorrowedBooks());
            statistics.put("returnedBooks", borrowRecordDAO.getTotalReturnedBooks());
            statistics.put("overdueBooks", borrowRecordDAO.getTotalOverdueBooks());
            
            return Message.success(Message.MessageType.RESPONSE, statistics);
        } catch (Exception e) {
            return Message.error(Message.MessageType.RESPONSE, "Error getting statistics: " + e.getMessage());
        }
    }
    
    private boolean isAuthenticated() {
        return currentUser != null;
    }
    
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
