package com.dainam.library.client;

import com.dainam.library.model.*;
import com.dainam.library.util.LoggerUtil;

import java.util.*;

/**
 * Service adapter để giao tiếp với server qua TCP
 * Thay thế cho việc kết nối trực tiếp đến MongoDB
 */
public class RemoteServiceAdapter {
    
    private final ServerConnection serverConnection;
    
    public RemoteServiceAdapter() {
        this.serverConnection = ServerConnection.getInstance();
    }
    
    // ==================== USER OPERATIONS ====================
    
    /**
     * Đăng nhập user
     */
    public Map<String, Object> authenticateUser(String email, String password) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        
        Map<String, Object> response = serverConnection.sendRequest("login", data);
        
        // Lưu session nếu đăng nhập thành công
        if ((Boolean) response.getOrDefault("success", false)) {
            String sessionId = (String) response.get("sessionId");
            if (sessionId != null) {
                serverConnection.setSessionId(sessionId);
            }
        }
        
        return response;
    }
    
    /**
     * Đăng xuất user
     */
    public Map<String, Object> logout() {
        Map<String, Object> response = serverConnection.sendRequest("logout", new HashMap<>());
        
        // Clear session
        serverConnection.setSessionId(null);
        
        return response;
    }
    
    /**
     * Lấy thông tin user theo ID
     */
    public User getUserById(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        
        Map<String, Object> response = serverConnection.sendRequest("getUserById", data);
        
        if ((Boolean) response.getOrDefault("success", false)) {
            Map<String, Object> userData = (Map<String, Object>) response.get("user");
            return convertMapToUser(userData);
        }
        
        return null;
    }
    
    /**
     * Lấy danh sách tất cả users (cho admin)
     */
    public List<User> getAllUsers() {
        Map<String, Object> response = serverConnection.sendRequest("getAllUsers", new HashMap<>());
        
        if ((Boolean) response.getOrDefault("success", false)) {
            List<Map<String, Object>> usersData = (List<Map<String, Object>>) response.get("users");
            List<User> users = new ArrayList<>();
            
            for (Map<String, Object> userData : usersData) {
                users.add(convertMapToUser(userData));
            }
            
            return users;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tạo user mới
     */
    public Map<String, Object> createUser(User user) {
        Map<String, Object> data = convertUserToMap(user);
        return serverConnection.sendRequest("createUser", data);
    }
    
    /**
     * Cập nhật user
     */
    public Map<String, Object> updateUser(User user) {
        Map<String, Object> data = convertUserToMap(user);
        return serverConnection.sendRequest("updateUser", data);
    }
    
    /**
     * Xóa user
     */
    public Map<String, Object> deleteUser(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        return serverConnection.sendRequest("deleteUser", data);
    }
    
    // ==================== BOOK OPERATIONS ====================
    
    /**
     * Lấy danh sách tất cả sách
     */
    public List<Book> getAllBooks() {
        Map<String, Object> response = serverConnection.sendRequest("getAllBooks", new HashMap<>());
        
        if ((Boolean) response.getOrDefault("success", false)) {
            List<Map<String, Object>> booksData = (List<Map<String, Object>>) response.get("books");
            List<Book> books = new ArrayList<>();
            
            for (Map<String, Object> bookData : booksData) {
                books.add(convertMapToBook(bookData));
            }
            
            return books;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tìm kiếm sách
     */
    public List<Book> searchBooks(String query, String category) {
        Map<String, Object> data = new HashMap<>();
        data.put("query", query);
        data.put("category", category);
        
        Map<String, Object> response = serverConnection.sendRequest("searchBooks", data);
        
        if ((Boolean) response.getOrDefault("success", false)) {
            List<Map<String, Object>> booksData = (List<Map<String, Object>>) response.get("books");
            List<Book> books = new ArrayList<>();
            
            for (Map<String, Object> bookData : booksData) {
                books.add(convertMapToBook(bookData));
            }
            
            return books;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Tạo sách mới
     */
    public Map<String, Object> createBook(Book book) {
        Map<String, Object> data = convertBookToMap(book);
        return serverConnection.sendRequest("createBook", data);
    }
    
    /**
     * Cập nhật sách
     */
    public Map<String, Object> updateBook(Book book) {
        Map<String, Object> data = convertBookToMap(book);
        return serverConnection.sendRequest("updateBook", data);
    }
    
    /**
     * Xóa sách
     */
    public Map<String, Object> deleteBook(String bookId) {
        Map<String, Object> data = new HashMap<>();
        data.put("bookId", bookId);
        return serverConnection.sendRequest("deleteBook", data);
    }
    
    // ==================== BORROW OPERATIONS ====================
    
    /**
     * Mượn sách
     */
    public Map<String, Object> borrowBook(String userId, String bookId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("bookId", bookId);
        return serverConnection.sendRequest("borrowBook", data);
    }
    
    /**
     * Trả sách
     */
    public Map<String, Object> returnBook(String borrowRecordId) {
        Map<String, Object> data = new HashMap<>();
        data.put("borrowRecordId", borrowRecordId);
        return serverConnection.sendRequest("returnBook", data);
    }
    
    /**
     * Lấy lịch sử mượn sách của user
     */
    public List<BorrowRecord> getBorrowHistory(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        
        Map<String, Object> response = serverConnection.sendRequest("getBorrowHistory", data);
        
        if ((Boolean) response.getOrDefault("success", false)) {
            List<Map<String, Object>> recordsData = (List<Map<String, Object>>) response.get("records");
            List<BorrowRecord> records = new ArrayList<>();
            
            for (Map<String, Object> recordData : recordsData) {
                records.add(convertMapToBorrowRecord(recordData));
            }
            
            return records;
        }
        
        return new ArrayList<>();
    }
    
    // ==================== CONVERSION HELPERS ====================
    
    private User convertMapToUser(Map<String, Object> data) {
        if (data == null) return null;
        
        User user = new User();
        user.setUserId((String) data.get("userId"));
        user.setEmail((String) data.get("email"));
        user.setFullName((String) data.get("fullName"));
        user.setStudentId((String) data.get("studentId"));
        user.setPhone((String) data.get("phone"));
        user.setFaculty((String) data.get("faculty"));
        
        String role = (String) data.get("role");
        if (role != null) {
            user.setRole(User.Role.valueOf(role));
        }
        
        String status = (String) data.get("status");
        if (status != null) {
            user.setStatus(User.Status.valueOf(status));
        }
        
        return user;
    }
    
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("email", user.getEmail());
        data.put("fullName", user.getFullName());
        data.put("studentId", user.getStudentId());
        data.put("phone", user.getPhone());
        data.put("faculty", user.getFaculty());
        data.put("role", user.getRole() != null ? user.getRole().name() : null);
        data.put("status", user.getStatus() != null ? user.getStatus().name() : null);
        return data;
    }
    
    private Book convertMapToBook(Map<String, Object> data) {
        if (data == null) return null;
        
        Book book = new Book();
        book.setBookId((String) data.get("bookId"));
        book.setTitle((String) data.get("title"));
        book.setAuthor((String) data.get("author"));
        book.setIsbn((String) data.get("isbn"));
        book.setCategory((String) data.get("category"));
        book.setPublisher((String) data.get("publisher"));
        book.setDescription((String) data.get("description"));
          Integer publishYear = (Integer) data.get("publishYear");
        if (publishYear != null) {
            book.setPublicationYear(publishYear);
        }
        
        Integer totalCopies = (Integer) data.get("totalCopies");
        if (totalCopies != null) {
            book.setTotalCopies(totalCopies);
        }
        
        Integer availableCopies = (Integer) data.get("availableCopies");
        if (availableCopies != null) {
            book.setAvailableCopies(availableCopies);
        }
        
        return book;
    }
    
    private Map<String, Object> convertBookToMap(Book book) {
        Map<String, Object> data = new HashMap<>();
        data.put("bookId", book.getBookId());
        data.put("title", book.getTitle());
        data.put("author", book.getAuthor());
        data.put("isbn", book.getIsbn());
        data.put("category", book.getCategory());
        data.put("publisher", book.getPublisher());
        data.put("description", book.getDescription());
        data.put("publishYear", book.getPublicationYear());
        data.put("totalCopies", book.getTotalCopies());
        data.put("availableCopies", book.getAvailableCopies());
        return data;
    }
    
    private BorrowRecord convertMapToBorrowRecord(Map<String, Object> data) {
        if (data == null) return null;
        
        BorrowRecord record = new BorrowRecord();
        record.setRecordId((String) data.get("recordId"));
        record.setUserId((String) data.get("userId"));
        record.setBookId((String) data.get("bookId"));
        record.setCopyId((String) data.get("copyId"));
        
        String status = (String) data.get("status");
        if (status != null) {
            record.setStatus(BorrowRecord.Status.valueOf(status));
        }
        
        // Note: Date conversion would need additional handling
        // record.setBorrowDate(...);
        // record.setExpectedReturnDate(...);
        // record.setActualReturnDate(...);
        
        return record;
    }
    
    /**
     * Kiểm tra trạng thái kết nối server
     */
    public boolean isConnectedToServer() {
        return serverConnection.isConnected();
    }
    
    /**
     * Kết nối lại server
     */
    public boolean reconnectToServer() {
        return serverConnection.reconnect();
    }
}
