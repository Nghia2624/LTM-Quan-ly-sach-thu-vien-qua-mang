package com.dainam.library.service;

import com.dainam.library.config.DatabaseConfig;
import com.dainam.library.model.User;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.PasswordUtil;
import com.dainam.library.util.ValidationUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service class cho quản lý người dùng
 */
public class UserService {
    
    private final MongoCollection<Document> usersCollection;
      // Lưu trữ session của user đang online (userId -> sessionId)
    private static final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    
    // Lưu trữ thời gian login (userId -> loginTime)
    private static final Map<String, LocalDateTime> loginTimes = new ConcurrentHashMap<>();
    
    // Flag để chỉ reset sessions một lần khi ứng dụng khởi động
    private static boolean sessionsInitialized = false;    public UserService() {
        this.usersCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_USERS);
        
        // Chỉ khởi tạo sessions một lần khi ứng dụng khởi động
        initializeSessionsOnce();
    }
      /**
     * Khởi tạo sessions chỉ một lần khi ứng dụng khởi động
     */
    private static synchronized void initializeSessionsOnce() {
        LoggerUtil.info("initializeSessionsOnce called - sessionsInitialized: " + sessionsInitialized);
        if (!sessionsInitialized) {
            LoggerUtil.info("Initializing sessions for the first time");
            loadActiveSessionsFromDatabase();
            sessionsInitialized = true;
        } else {
            LoggerUtil.info("Sessions already initialized, skipping");
        }
    }/**
     * Load active sessions từ database khi khởi động
     * Nhưng reset tất cả để tránh session zombie
     */
    private static void loadActiveSessionsFromDatabase() {
        try {
            MongoCollection<Document> usersCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_USERS);
            
            // Reset tất cả online status khi khởi động ứng dụng mới
            // Vì không có cách nào biết các session cũ còn active hay không
            Bson filter = Filters.eq("isOnline", true);
            Bson update = Updates.combine(
                Updates.unset("sessionId"),
                Updates.set("isOnline", false)
            );
            usersCollection.updateMany(filter, update);
            
            LoggerUtil.info("Reset all online status on application startup");
        } catch (Exception e) {
            LoggerUtil.error("Lỗi reset online status: " + e.getMessage());
        }
    }/**
     * Xác thực người dùng với single-session check
     */
    public User authenticate(String email, String password) {
        try {
            // Debug current state
            debugUserState(email);
            
            Bson filter = Filters.eq("email", email);
            Document userDoc = usersCollection.find(filter).first();
            
            if (userDoc != null) {
                String hashedPassword = userDoc.getString("password");
                if (PasswordUtil.checkPassword(password, hashedPassword)) {
                    User user = documentToUser(userDoc);
                      // Kiểm tra single session - kiểm tra cả memory và database
                    boolean inMemory = isUserLoggedIn(user.getUserId());
                    boolean inDatabase = isUserOnlineInDatabase(user.getUserId());
                    
                    LoggerUtil.info("Session check for " + email + " - Memory: " + inMemory + ", Database: " + inDatabase);
                    
                    // Nếu có session cũ, cleanup trước khi tạo session mới
                    if (inMemory || inDatabase) {
                        LoggerUtil.info("Found existing session for " + email + ", cleaning up old session...");
                        
                        // Cleanup session cũ
                        logout(user.getUserId());
                        
                        LoggerUtil.info("Old session cleaned up, proceeding with new login");
                    }
                    
                    // Tạo session mới
                    String sessionId = generateSessionId();
                    activeSessions.put(user.getUserId(), sessionId);
                    loginTimes.put(user.getUserId(), LocalDateTime.now());
                    
                    user.updateLastLogin();
                    user.setSessionId(sessionId);
                    user.setOnline(true);
                    
                    // Cập nhật lastLogin và sessionId
                    Bson update = Updates.combine(
                        Updates.set("lastLogin", LocalDate.now()),
                        Updates.set("sessionId", sessionId),
                        Updates.set("isOnline", true)
                    );
                    usersCollection.updateOne(filter, update);
                    
                    LoggerUtil.info("User đăng nhập thành công: " + email + " với session: " + sessionId);
                    return user;
                }
            }
            
            LoggerUtil.warn("Đăng nhập thất bại cho email: " + email);
            return null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi xác thực user: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Đăng xuất người dùng
     */
    public boolean logout(String userId) {
        try {
            activeSessions.remove(userId);
            loginTimes.remove(userId);
            
            // Cập nhật trạng thái offline trong database
            Bson filter = Filters.eq("userId", userId);
            Bson update = Updates.combine(
                Updates.unset("sessionId"),
                Updates.set("isOnline", false)
            );
            usersCollection.updateOne(filter, update);
            
            LoggerUtil.info("User đăng xuất: " + userId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đăng xuất: " + e.getMessage());
            return false;
        }
    }
      /**
     * Kiểm tra user có đang đăng nhập không
     */
    public boolean isUserLoggedIn(String userId) {
        return activeSessions.containsKey(userId);
    }
      /**
     * Kiểm tra user có online trong database không
     */
    public boolean isUserOnlineInDatabase(String userId) {
        try {
            Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("isOnline", true)
            );
            Document userDoc = usersCollection.find(filter).first();
            boolean isOnline = userDoc != null;
            
            if (isOnline) {
                String sessionId = userDoc.getString("sessionId");
                LoggerUtil.info("User " + userId + " found online in database with session: " + sessionId);
            }
            
            return isOnline;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi kiểm tra online status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Tạo session ID
     */
    private String generateSessionId() {
        return "SESSION_" + System.currentTimeMillis() + "_" + Math.random();
    }
      /**
     * Force logout user (admin only)
     */
    public boolean forceLogout(String userId) {
        return logout(userId);
    }
    
    /**
     * Cleanup tất cả sessions khi ứng dụng thoát
     */
    public static void cleanupAllSessions() {
        try {
            // Clear memory sessions
            activeSessions.clear();
            loginTimes.clear();
            
            // Reset all online status in database
            MongoCollection<Document> usersCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_USERS);
            Bson filter = Filters.eq("isOnline", true);
            Bson update = Updates.combine(
                Updates.unset("sessionId"),
                Updates.set("isOnline", false)
            );
            usersCollection.updateMany(filter, update);
            
            LoggerUtil.info("Cleaned up all user sessions");
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cleanup sessions: " + e.getMessage());
        }
    }
      /**
     * Lấy danh sách users online
     */
    public static Map<String, String> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }
    
    /**
     * Debug method để kiểm tra trạng thái user
     */
    public void debugUserState(String email) {
        try {
            Bson filter = Filters.eq("email", email);
            Document userDoc = usersCollection.find(filter).first();
            
            if (userDoc != null) {
                String userId = userDoc.getString("userId");
                boolean isOnlineInDB = userDoc.getBoolean("isOnline", false);
                String sessionId = userDoc.getString("sessionId");
                boolean isInMemory = activeSessions.containsKey(userId);
                
                LoggerUtil.info("=== DEBUG USER STATE ===");
                LoggerUtil.info("Email: " + email);
                LoggerUtil.info("UserID: " + userId);
                LoggerUtil.info("Online in DB: " + isOnlineInDB);
                LoggerUtil.info("Session ID in DB: " + sessionId);
                LoggerUtil.info("In memory sessions: " + isInMemory);
                LoggerUtil.info("Memory session ID: " + activeSessions.get(userId));
                LoggerUtil.info("Total memory sessions: " + activeSessions.size());
                LoggerUtil.info("========================");
            } else {
                LoggerUtil.info("User not found: " + email);
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi debug user state: " + e.getMessage());
        }
    }
    
    /**
     * Đăng ký người dùng mới
     */
    public boolean register(User user) {
        try {
            // Validate input
            String validationError = ValidationUtil.validateUserRegistration(
                user.getEmail(), user.getPassword(), user.getFirstName(),
                user.getLastName(), user.getPhone(), user.getStudentId()
            );
            
            if (validationError != null) {
                LoggerUtil.warn("Validation error: " + validationError);
                return false;
            }
            
            // Kiểm tra email đã tồn tại
            if (getUserByEmail(user.getEmail()) != null) {
                LoggerUtil.warn("Email đã tồn tại: " + user.getEmail());
                return false;
            }
            
            // Kiểm tra studentId đã tồn tại
            if (getUserByStudentId(user.getStudentId()) != null) {
                LoggerUtil.warn("Mã sinh viên đã tồn tại: " + user.getStudentId());
                return false;
            }
            
            // Tạo userId nếu chưa có
            if (user.getUserId() == null) {
                user.setUserId(generateUserId());
            }
            
            // Mã hóa mật khẩu
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            
            // Chuyển đổi sang Document và lưu
            Document userDoc = userToDocument(user);
            usersCollection.insertOne(userDoc);
            
            LoggerUtil.info("User đăng ký thành công: " + user.getEmail());
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đăng ký user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Thêm user mới (admin only)
     */
    public boolean addUser(User user) {
        try {
            // Kiểm tra email đã tồn tại
            if (getUserByEmail(user.getEmail()) != null) {
                LoggerUtil.warn("Email đã tồn tại: " + user.getEmail());
                return false;
            }
            
            // Tạo userId nếu chưa có
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                user.setUserId(generateUserId());
            }
            
            // Mã hóa mật khẩu
            if (user.getPassword() != null) {
                user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            }
            
            // Chuyển đổi và lưu
            Document userDoc = userToDocument(user);
            usersCollection.insertOne(userDoc);
            
            LoggerUtil.info("Thêm user thành công: " + user.getEmail());
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi thêm user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cập nhật thông tin người dùng
     */
    public boolean updateUser(User user) {
        try {
            Bson filter = Filters.eq("userId", user.getUserId());
            Document userDoc = userToDocument(user);
            
            usersCollection.replaceOne(filter, userDoc);
            
            LoggerUtil.info("Cập nhật user thành công: " + user.getUserId());
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy user theo email
     */
    public User getUserByEmail(String email) {
        try {
            Bson filter = Filters.eq("email", email);
            Document userDoc = usersCollection.find(filter).first();
            
            return userDoc != null ? documentToUser(userDoc) : null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy user by email: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy user theo studentId
     */
    public User getUserByStudentId(String studentId) {
        try {
            Bson filter = Filters.eq("studentId", studentId);
            Document userDoc = usersCollection.find(filter).first();
            
            return userDoc != null ? documentToUser(userDoc) : null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy user by studentId: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy user theo userId
     */
    public User getUserById(String userId) {
        try {
            Bson filter = Filters.eq("userId", userId);
            Document userDoc = usersCollection.find(filter).first();
            
            return userDoc != null ? documentToUser(userDoc) : null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy user by id: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy tất cả users (admin only)
     */
    public List<User> getAllUsers() {
        try {
            List<User> users = new ArrayList<>();
            
            for (Document userDoc : usersCollection.find()) {
                users.add(documentToUser(userDoc));
            }
            
            return users;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy tất cả users: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy users với pagination (admin only)
     */
    public List<User> getUsers(int page, int limit) {
        try {
            List<User> users = new ArrayList<>();
            int skip = (page - 1) * limit;
            
            FindIterable<Document> documents = usersCollection.find()
                .skip(skip)
                .limit(limit)
                .sort(Sorts.descending("registrationDate"));
            
            for (Document userDoc : documents) {
                users.add(documentToUser(userDoc));
            }
            
            return users;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy users với pagination: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Đếm tổng số users
     */
    public long getUserCount() {
        try {
            return usersCollection.countDocuments();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm users: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Tìm kiếm users theo từ khóa
     */
    public List<User> searchUsers(String keyword, int page, int limit) {
        try {
            List<User> users = new ArrayList<>();
            int skip = (page - 1) * limit;
            
            // Tạo filter tìm kiếm
            Bson filter = Filters.or(
                Filters.regex("fullName", keyword, "i"),
                Filters.regex("email", keyword, "i"),
                Filters.regex("studentId", keyword, "i"),
                Filters.regex("faculty", keyword, "i")
            );
            
            FindIterable<Document> documents = usersCollection.find(filter)
                .skip(skip)
                .limit(limit)
                .sort(Sorts.descending("registrationDate"));
            
            for (Document userDoc : documents) {
                users.add(documentToUser(userDoc));
            }
            
            return users;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tìm kiếm users: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy users theo khoa
     */
    public List<User> getUsersByFaculty(String faculty) {
        try {
            List<User> users = new ArrayList<>();
            
            Bson filter = Filters.eq("faculty", faculty);
            
            for (Document userDoc : usersCollection.find(filter)) {
                users.add(documentToUser(userDoc));
            }
            
            return users;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy users theo khoa: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy thống kê users theo khoa
     */
    public List<FacultyStatistics> getFacultyStatistics() {
        try {
            List<FacultyStatistics> statistics = new ArrayList<>();
            List<User> allUsers = getAllUsers();
            
            // Group by faculty
            java.util.Map<String, FacultyStatistics> facultyMap = new java.util.HashMap<>();
            
            for (User user : allUsers) {
                String faculty = user.getFaculty() != null && !user.getFaculty().trim().isEmpty() 
                    ? user.getFaculty() : "Chưa phân khoa";
                
                FacultyStatistics stat = facultyMap.computeIfAbsent(faculty, k -> new FacultyStatistics(k));
                
                stat.totalUsers++;
                if (user.getStatus() == User.Status.ACTIVE) {
                    stat.activeUsers++;
                } else {
                    stat.inactiveUsers++;
                }
                stat.totalCurrentBorrowed += user.getCurrentBorrowed();
                stat.totalFines += user.getTotalFines();
            }
            
            statistics.addAll(facultyMap.values());
            return statistics;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy thống kê khoa: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Duyệt user (admin only)
     */
    public boolean approveUser(String userId) {
        try {
            Bson filter = Filters.eq("userId", userId);
            Bson update = Updates.set("status", "ACTIVE");
            
            usersCollection.updateOne(filter, update);
            
            LoggerUtil.info("Duyệt user thành công: " + userId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi duyệt user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Khóa user (admin only)
     */
    public boolean suspendUser(String userId) {
        try {
            Bson filter = Filters.eq("userId", userId);
            Bson update = Updates.set("status", "SUSPENDED");
            
            usersCollection.updateOne(filter, update);
            
            LoggerUtil.info("Khóa user thành công: " + userId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi khóa user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mở khóa user (admin only)
     */
    public boolean unsuspendUser(String userId) {
        try {
            Bson filter = Filters.eq("userId", userId);
            Bson update = Updates.set("status", "ACTIVE");
            
            usersCollection.updateOne(filter, update);
            
            LoggerUtil.info("Mở khóa user thành công: " + userId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi mở khóa user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa user (admin only)
     */
    public boolean deleteUser(String userId) {
        try {
            // Không cho phép xóa admin chính
            User user = getUserById(userId);
            if (user != null && "dainam@dnu.edu.vn".equals(user.getEmail())) {
                LoggerUtil.warn("Không thể xóa admin chính");
                return false;
            }
            
            long deletedCount = usersCollection.deleteOne(Filters.eq("userId", userId)).getDeletedCount();
            
            if (deletedCount > 0) {
                LoggerUtil.info("Xóa user thành công: " + userId);
                return true;
            } else {
                LoggerUtil.warn("User không tồn tại: " + userId);
                return false;
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi xóa user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset mật khẩu
     */
    public boolean resetPassword(String userId, String newPassword) {
        try {
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            
            Bson filter = Filters.eq("userId", userId);
            Bson update = Updates.set("password", hashedPassword);
            
            usersCollection.updateOne(filter, update);
            
            LoggerUtil.info("Reset mật khẩu thành công: " + userId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi reset mật khẩu: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Tạo userId mới
     */
    private String generateUserId() {
        // Tìm số thứ tự lớn nhất hiện có
        int maxUserNumber = 0;
        
        try {
            // Tìm tất cả user có userId theo định dạng user_xxx
            FindIterable<Document> users = usersCollection.find(Filters.regex("userId", "^user_\\d{3}$"));
            
            for (Document doc : users) {
                String userId = doc.getString("userId");
                if (userId != null && userId.startsWith("user_")) {
                    try {
                        String numberPart = userId.substring(5); // Bỏ "user_"
                        int number = Integer.parseInt(numberPart);
                        maxUserNumber = Math.max(maxUserNumber, number);
                    } catch (NumberFormatException e) {
                        // Bỏ qua các userId không đúng định dạng
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Lỗi khi tìm số thứ tự user: " + e.getMessage());
        }
        
        // Tạo userId mới với số thứ tự tiếp theo
        return "user_" + String.format("%03d", maxUserNumber + 1);
    }
    
    /**
     * Chuyển đổi Document thành User
     */
    private User documentToUser(Document doc) {
        User user = new User();
        user.setUserId(doc.getString("userId"));
        user.setEmail(doc.getString("email"));
        user.setPassword(doc.getString("password"));
        user.setFirstName(doc.getString("firstName"));
        user.setLastName(doc.getString("lastName"));
        
        // Set fullName từ database hoặc tạo từ firstName + lastName
        String fullName = doc.getString("fullName");
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        } else {
            String firstName = doc.getString("firstName");
            String lastName = doc.getString("lastName");
            if (firstName != null && lastName != null) {
                user.setFullName(firstName + " " + lastName);
            } else if (firstName != null) {
                user.setFullName(firstName);
            } else if (lastName != null) {
                user.setFullName(lastName);
            } else {
                user.setFullName("Không xác định");
            }
        }
        
        user.setPhone(doc.getString("phone"));
        user.setAddress(doc.getString("address"));
          // Handle role and status safely
        String roleStr = doc.getString("role");
        if (roleStr != null) {
            try {
                user.setRole(User.Role.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                user.setRole(User.Role.USER); // Default role
            }
        } else {
            user.setRole(User.Role.USER);
        }
        
        String statusStr = doc.getString("status");
        if (statusStr != null) {
            try {
                user.setStatus(User.Status.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                user.setStatus(User.Status.ACTIVE); // Default status
            }
        } else {
            user.setStatus(User.Status.ACTIVE);
        }
        
        user.setStudentId(doc.getString("studentId"));
        user.setFaculty(doc.getString("faculty"));
        user.setYearOfStudy(doc.getString("yearOfStudy"));
        user.setTotalBorrowed(doc.getInteger("totalBorrowed", 0));
        user.setCurrentBorrowed(doc.getInteger("currentBorrowed", 0));
        user.setTotalFines(doc.getDouble("totalFines") != null ? doc.getDouble("totalFines") : 0.0);
        user.setNotes(doc.getString("notes"));
        
        // Handle dates
        if (doc.getDate("dateOfBirth") != null) {
            user.setDateOfBirth(doc.getDate("dateOfBirth").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("registrationDate") != null) {
            user.setRegistrationDate(doc.getDate("registrationDate").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }        if (doc.getDate("lastLogin") != null) {
            user.setLastLogin(doc.getDate("lastLogin").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        
        // Handle session fields
        user.setSessionId(doc.getString("sessionId"));
        user.setOnline(doc.getBoolean("isOnline", false));
        
        return user;
    }
    
    /**
     * Chuyển đổi User thành Document
     */
    private Document userToDocument(User user) {
        Document doc = new Document()
            .append("userId", user.getUserId())
            .append("email", user.getEmail())
            .append("password", user.getPassword())
            .append("firstName", user.getFirstName())
            .append("lastName", user.getLastName())
            .append("fullName", user.getFullName())
            .append("phone", user.getPhone())
            .append("address", user.getAddress())
            .append("role", user.getRole().name())
            .append("status", user.getStatus().name())
            .append("studentId", user.getStudentId())
            .append("faculty", user.getFaculty())
            .append("yearOfStudy", user.getYearOfStudy())
            .append("totalBorrowed", user.getTotalBorrowed())
            .append("currentBorrowed", user.getCurrentBorrowed())
            .append("totalFines", user.getTotalFines())
            .append("notes", user.getNotes());
        
        // Handle dates
        if (user.getDateOfBirth() != null) {
            doc.append("dateOfBirth", java.sql.Date.valueOf(user.getDateOfBirth()));
        }
        if (user.getRegistrationDate() != null) {
            doc.append("registrationDate", java.sql.Date.valueOf(user.getRegistrationDate()));
        }
        if (user.getLastLogin() != null) {
            doc.append("lastLogin", java.sql.Date.valueOf(user.getLastLogin()));
        }
        
        return doc;
    }
    
    /**
     * Lấy tổng số người dùng
     */
    public int getTotalUsers() {
        try {
            return (int) usersCollection.countDocuments();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm tổng số users: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Lấy số người dùng chờ duyệt
     */
    public int getPendingUsers() {
        try {
            return (int) usersCollection.countDocuments(Filters.eq("status", "PENDING"));
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm users chờ duyệt: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Class thống kê theo khoa
     */
    public static class FacultyStatistics {
        public String facultyName;
        public int totalUsers = 0;
        public int activeUsers = 0;
        public int inactiveUsers = 0;
        public int totalCurrentBorrowed = 0;
        public double totalFines = 0.0;
        
        public FacultyStatistics(String facultyName) {
            this.facultyName = facultyName;
        }
    }
}
