package com.dainam.library.server;

import com.dainam.library.model.*;
import com.dainam.library.service.*;
import com.dainam.library.util.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Xử lý kết nối từ client với hỗ trợ multi-user
 */
public class ClientHandler implements Runnable {
    
    private final Socket clientSocket;
    private final ObjectMapper objectMapper;
    private final Map<String, User> activeSessions;
    private final SessionManager sessionManager;
    private PrintWriter writer;
    private String currentSessionId;
    
    // Services
    private final UserService userService;
    private final BookService bookService;
    private final BorrowService borrowService;
      public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.objectMapper = new ObjectMapper();
        // Configure ObjectMapper to handle Java 8 time types
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionManager = SessionManager.getInstance();
        
        // Initialize services
        this.userService = new UserService();
        this.bookService = new BookService();
        this.borrowService = new BorrowService();
        
        // Register with session manager
        sessionManager.registerClient(this);
    }
    
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
            
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                try {
                    // Parse request
                    Request request = objectMapper.readValue(inputLine, Request.class);
                    LoggerUtil.debug("Nhận request: " + request.getAction());
                    
                    // Update session activity if exists
                    if (currentSessionId != null) {
                        SessionManager.ClientSession session = sessionManager.getSession(currentSessionId);
                        if (session != null) {
                            session.updateActivity();
                        }
                    }
                    
                    // Process request
                    Response response = processRequest(request);
                    
                    // Send response
                    String responseJson = objectMapper.writeValueAsString(response);
                    writer.println(responseJson);
                    
                } catch (Exception e) {
                    LoggerUtil.error("Lỗi xử lý request: " + e.getMessage());
                    Response errorResponse = new Response(false, "Lỗi xử lý request: " + e.getMessage(), null);
                    sendMessage(objectMapper.writeValueAsString(errorResponse));
                }
            }
            
        } catch (IOException e) {
            LoggerUtil.error("Lỗi kết nối client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
      /**
     * Xử lý request từ client
     */
    private Response processRequest(Request request) {
        String action = request.getAction();
        Map<String, Object> data = request.getData();
        String sessionId = request.getSessionId();
        
        // Add sessionId to data for handler methods to use
        if (data == null) {
            data = new HashMap<>();
        }
        data.put("sessionId", sessionId);
          try {
            switch (action) {
                case "login":
                    return handleLogin(data);
                case "logout":
                    return handleLogout(data);
                case "register":
                    return handleRegister(data);
                case "ping":
                    return handlePing(data);
                    
                // User management
                case "getAllUsers":
                    return handleGetAllUsers(data);
                case "getUsers":
                    return handleGetUsers(data);
                case "getUserById":
                    return handleGetUserById(data);
                case "createUser":
                    return handleCreateUser(data);
                case "addUser":
                    return handleAddUser(data);
                case "updateUser":
                    return handleUpdateUser(data);
                case "deleteUser":
                    return handleDeleteUser(data);
                    
                // Book management
                case "getAllBooks":
                    return handleGetAllBooks(data);
                case "getBooks":
                    return handleGetBooks(data);
                case "getBookById":
                    return handleGetBookById(data);
                case "searchBooks":
                    return handleSearchBooks(data);
                case "createBook":
                    return handleCreateBook(data);
                case "addBook":
                    return handleAddBook(data);
                case "updateBook":
                    return handleUpdateBook(data);
                case "deleteBook":
                    return handleDeleteBook(data);
                    
                // Borrow management
                case "borrowBook":
                    return handleBorrowBook(data);
                case "returnBook":
                    return handleReturnBook(data);
                case "extendBorrow":
                    return handleExtendBorrow(data);
                case "getBorrowHistory":
                    return handleGetBorrowHistory(data);
                case "getCurrentBorrows":
                    return handleGetCurrentBorrows(data);
                case "getBorrowRecords":
                    return handleGetBorrowRecords(data);
                    
                // Statistics
                case "getStats":
                    return handleGetStats(data);
                default:
                    return new Response(false, "Action không được hỗ trợ: " + action, null);
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi xử lý action " + action + ": " + e.getMessage());
            return new Response(false, "Lỗi xử lý: " + e.getMessage(), null);
        }
    }
    
    // Authentication handlers
    private Response handleLogin(Map<String, Object> data) {
        try {
            String email = (String) data.get("email");
            String password = (String) data.get("password");
            
            User user = userService.authenticate(email, password);
            if (user != null) {
                // Tạo session
                String sessionId = generateSessionId();
                activeSessions.put(sessionId, user);
                
                // Register with session manager
                sessionManager.createSession(user.getUserId(), sessionId, this);
                currentSessionId = sessionId;
                
                Map<String, Object> responseData = Map.of(
                    "sessionId", sessionId,
                    "user", user,
                    "connectedUsers", sessionManager.getActiveUserCount()
                );
                
                LoggerUtil.info("User logged in: " + user.getEmail() + " (Session: " + sessionId + ")");
                return new Response(true, "Đăng nhập thành công", responseData);
            } else {
                return new Response(false, "Email hoặc mật khẩu không đúng", null);
            }
        } catch (Exception e) {
            return new Response(false, "Lỗi đăng nhập: " + e.getMessage(), null);
        }
    }
    
    private Response handleLogout(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        if (sessionId != null) {
            activeSessions.remove(sessionId);
            sessionManager.removeSession(sessionId);
            if (sessionId.equals(currentSessionId)) {
                currentSessionId = null;
            }
        }
        return new Response(true, "Đăng xuất thành công", null);
    }
    
    private Response handleRegister(Map<String, Object> data) {
        try {
            User user = objectMapper.convertValue(data.get("user"), User.class);
            boolean success = userService.register(user);
            if (success) {
                return new Response(true, "Đăng ký thành công", null);
            } else {
                return new Response(false, "Email đã tồn tại", null);
            }
        } catch (Exception e) {
            return new Response(false, "Lỗi đăng ký: " + e.getMessage(), null);
        }
    }
    
    // Book handlers
    private Response handleGetBooks(Map<String, Object> data) {
        try {
            int page = (Integer) data.getOrDefault("page", 1);
            int size = (Integer) data.getOrDefault("size", 10);
            List<Book> books = bookService.getAllBooks(page, size);
            return new Response(true, "Lấy danh sách sách thành công", books);
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy danh sách sách: " + e.getMessage(), null);
        }
    }
    
    private Response handleSearchBooks(Map<String, Object> data) {
        try {
            String query = (String) data.get("query");
            List<Book> books = bookService.searchBooks(query);
            return new Response(true, "Tìm kiếm thành công", books);
        } catch (Exception e) {
            return new Response(false, "Lỗi tìm kiếm: " + e.getMessage(), null);
        }
    }
    
    private Response handleGetBookById(Map<String, Object> data) {
        try {
            String bookId = (String) data.get("bookId");
            Book book = bookService.getBookById(bookId);
            return new Response(true, "Lấy thông tin sách thành công", book);
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy thông tin sách: " + e.getMessage(), null);
        }
    }
    
    // Admin-only book operations
    private Response handleAddBook(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            Book book = objectMapper.convertValue(data.get("book"), Book.class);
            boolean success = bookService.addBook(book);
            return new Response(success, success ? "Thêm sách thành công" : "Lỗi thêm sách", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi thêm sách: " + e.getMessage(), null);
        }
    }
    
    private Response handleUpdateBook(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            Book book = objectMapper.convertValue(data.get("book"), Book.class);
            boolean success = bookService.updateBook(book);
            return new Response(success, success ? "Cập nhật sách thành công" : "Lỗi cập nhật sách", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi cập nhật sách: " + e.getMessage(), null);
        }
    }
    
    private Response handleDeleteBook(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            String bookId = (String) data.get("bookId");
            boolean success = bookService.deleteBook(bookId);
            return new Response(success, success ? "Xóa sách thành công" : "Lỗi xóa sách", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi xóa sách: " + e.getMessage(), null);
        }
    }
    
    // Borrow handlers
    private Response handleBorrowBook(Map<String, Object> data) {
        try {
            User user = getCurrentUser(data);
            if (user == null) {
                return new Response(false, "Vui lòng đăng nhập", null);
            }
            
            String bookId = (String) data.get("bookId");
            String copyId = (String) data.get("copyId");
            
            BorrowRecord record = borrowService.borrowBook(user.getUserId(), bookId, copyId);
            return new Response(true, "Mượn sách thành công", record);
        } catch (Exception e) {
            return new Response(false, "Lỗi mượn sách: " + e.getMessage(), null);
        }
    }
    
    private Response handleReturnBook(Map<String, Object> data) {
        try {
            User user = getCurrentUser(data);
            if (user == null) {
                return new Response(false, "Vui lòng đăng nhập", null);
            }
            
            String recordId = (String) data.get("recordId");
            boolean success = borrowService.returnBook(recordId);
            return new Response(success, success ? "Trả sách thành công" : "Lỗi trả sách", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi trả sách: " + e.getMessage(), null);
        }
    }
    
    private Response handleExtendBorrow(Map<String, Object> data) {
        try {
            User user = getCurrentUser(data);
            if (user == null) {
                return new Response(false, "Vui lòng đăng nhập", null);
            }
            
            String recordId = (String) data.get("recordId");
            boolean success = borrowService.extendBorrow(recordId);
            return new Response(success, success ? "Gia hạn thành công" : "Lỗi gia hạn", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi gia hạn: " + e.getMessage(), null);
        }
    }
    
    private Response handleGetBorrowHistory(Map<String, Object> data) {
        try {
            User user = getCurrentUser(data);
            if (user == null) {
                return new Response(false, "Vui lòng đăng nhập", null);
            }
            
            List<BorrowRecord> history = borrowService.getBorrowHistory(user.getUserId());
            return new Response(true, "Lấy lịch sử mượn thành công", history);
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy lịch sử: " + e.getMessage(), null);
        }
    }
    
    private Response handleGetCurrentBorrows(Map<String, Object> data) {
        try {
            User user = getCurrentUser(data);
            if (user == null) {
                return new Response(false, "Vui lòng đăng nhập", null);
            }
            
            List<BorrowRecord> currentBorrows = borrowService.getCurrentBorrows(user.getUserId());
            return new Response(true, "Lấy sách đang mượn thành công", currentBorrows);        } catch (Exception e) {
            return new Response(false, "Lỗi lấy sách đang mượn: " + e.getMessage(), null);
        }
    }
    
    // User management (Admin only)
    private Response handleGetUsers(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            List<User> users = userService.getAllUsers();
            return new Response(true, "Lấy danh sách user thành công", users);
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy danh sách user: " + e.getMessage(), null);
        }
    }
    
    private Response handleAddUser(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            User user = objectMapper.convertValue(data.get("user"), User.class);
            boolean success = userService.addUser(user);
            return new Response(success, success ? "Thêm user thành công" : "Lỗi thêm user", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi thêm user: " + e.getMessage(), null);
        }
    }
    
    private Response handleUpdateUser(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            User user = objectMapper.convertValue(data.get("user"), User.class);
            boolean success = userService.updateUser(user);
            return new Response(success, success ? "Cập nhật user thành công" : "Lỗi cập nhật user", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi cập nhật user: " + e.getMessage(), null);
        }
    }
    
    private Response handleDeleteUser(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            String userId = (String) data.get("userId");
            boolean success = userService.deleteUser(userId);
            return new Response(success, success ? "Xóa user thành công" : "Lỗi xóa user", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi xóa user: " + e.getMessage(), null);
        }
    }
    
    private Response handleGetBorrowRecords(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            int page = (Integer) data.getOrDefault("page", 1);
            int size = (Integer) data.getOrDefault("size", 10);
            List<BorrowRecord> records = borrowService.getAllBorrowRecords(page, size);
            return new Response(true, "Lấy danh sách mượn/trả thành công", records);
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy danh sách mượn/trả: " + e.getMessage(), null);
        }
    }
    
    private Response handleGetStats(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            Map<String, Object> stats = Map.of(
                "totalUsers", userService.getTotalUsers(),
                "totalBooks", bookService.getTotalBooks(),
                "borrowedBooks", borrowService.getBorrowedBooks(),
                "overdueBooks", borrowService.getOverdueBooks(),
                "totalBorrows", borrowService.getTotalBorrows(),
                "connectedUsers", sessionManager.getActiveUserCount()
            );
            
            return new Response(true, "Lấy thống kê thành công", stats);
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy thống kê: " + e.getMessage(), null);
        }
    }
    
    // Ping handler for connection testing
    private Response handlePing(Map<String, Object> data) {
        return new Response(true, "Server is alive", Map.of("timestamp", System.currentTimeMillis()));
    }
    
    // Additional User handlers for TCP support
    private Response handleGetAllUsers(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            List<User> users = userService.getAllUsers();
            return new Response(true, "Lấy danh sách người dùng thành công", Map.of("users", users));
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy danh sách người dùng: " + e.getMessage(), null);
        }
    }
    
    private Response handleGetUserById(Map<String, Object> data) {
        try {
            String userId = (String) data.get("userId");
            User user = userService.getUserById(userId);
            return new Response(true, "Lấy thông tin người dùng thành công", Map.of("user", user));
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy thông tin người dùng: " + e.getMessage(), null);
        }
    }
      private Response handleCreateUser(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) data.get("user");
            User user = convertMapToUser(userData);
            boolean success = userService.register(user);
            return new Response(success, success ? "Tạo người dùng thành công" : "Email đã tồn tại", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi tạo người dùng: " + e.getMessage(), null);
        }
    }
      // Additional Book handlers
    private Response handleGetAllBooks(Map<String, Object> data) {
        try {
            List<Book> books = bookService.getAllBooks(1, 1000); // Get first 1000 books
            return new Response(true, "Lấy danh sách sách thành công", Map.of("books", books));
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy danh sách sách: " + e.getMessage(), null);
        }
    }
      private Response handleCreateBook(Map<String, Object> data) {
        if (!isAdmin(data)) {
            return new Response(false, "Không có quyền thực hiện", null);
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> bookData = (Map<String, Object>) data.get("book");
            Book book = convertMapToBook(bookData);
            boolean success = bookService.addBook(book);
            return new Response(success, success ? "Tạo sách thành công" : "Lỗi tạo sách", null);
        } catch (Exception e) {
            return new Response(false, "Lỗi tạo sách: " + e.getMessage(), null);
        }
    }    // Helper method to convert Map to User
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
      // Helper method to convert Map to Book
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
    
    /**
     * Tạo session ID ngẫu nhiên
     */
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Gửi message tới client
     */
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
      /**
     * Cleanup khi client disconnect
     */
    private void cleanup() {
        try {
            // Logout user if currently logged in
            if (currentSessionId != null) {
                User currentUser = activeSessions.get(currentSessionId);
                if (currentUser != null) {
                    userService.logout(currentUser.getUserId());
                    LoggerUtil.info("Auto logout user on disconnect: " + currentUser.getEmail());
                }
            }
            
            // Remove from session manager
            sessionManager.unregisterClient(this);
            
            // Remove current session
            if (currentSessionId != null) {
                sessionManager.removeSession(currentSessionId);
                activeSessions.remove(currentSessionId);
            }
            
            // Close socket
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            
            // Close writer
            if (writer != null) {
                writer.close();
            }
            
            LoggerUtil.info("Client cleanup completed");
            
        } catch (IOException e) {
            LoggerUtil.error("Lỗi cleanup client: " + e.getMessage());
        }
    }
    
    // Helper methods for authorization
    private boolean isAdmin(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        if (sessionId == null) return false;
        
        User user = activeSessions.get(sessionId);
        return user != null && user.getRole() == User.Role.ADMIN;
    }
    
    private User getCurrentUser(Map<String, Object> data) {
        String sessionId = (String) data.get("sessionId");
        if (sessionId == null) return null;
        
        return activeSessions.get(sessionId);    }
    
    // Request and Response classes
    
    public static class Request {
        private String action;
        private Map<String, Object> data;
        private String sessionId;
        
        public Request() {}
        
        public Request(String action, Map<String, Object> data) {
            this.action = action;
            this.data = data;
        }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
    
    public static class Response {
        private boolean success;
        private String message;
        private Object data;
        
        public Response() {}
        
        public Response(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
