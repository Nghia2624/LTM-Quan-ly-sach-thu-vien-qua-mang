package com.dainam.library.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.dainam.library.util.LoggerUtil;
import org.bson.Document;

/**
 * Cấu hình kết nối MongoDB
 */
public class DatabaseConfig {
    
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "library_management";
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    // Collection names
    public static final String COLLECTION_BOOKS = "books";
    public static final String COLLECTION_BOOK_COPIES = "book_copies";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_BORROW_RECORDS = "borrow_records";
    public static final String COLLECTION_FINES = "fines";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_SYSTEM_CONFIG = "system_config";
    
    /**
     * Khởi tạo kết nối database
     */
    public static void initialize() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            
            // Tạo indexes
            createIndexes();
            
            // Khởi tạo dữ liệu mẫu nếu cần
            initializeSampleData();
            
            LoggerUtil.info("Kết nối MongoDB thành công!");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi kết nối MongoDB: " + e.getMessage());
            throw new RuntimeException("Không thể kết nối đến MongoDB", e);
        }
    }
    
    /**
     * Khởi tạo kết nối database mà không tạo sample data
     */
    public static void initializeWithoutSampleData() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            
            // Tạo indexes
            createIndexes();
            
            LoggerUtil.info("Kết nối MongoDB thành công!");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi kết nối MongoDB: " + e.getMessage());
            throw new RuntimeException("Không thể kết nối đến MongoDB", e);
        }
    }
    
    /**
     * Tạo các indexes cần thiết
     */
    private static void createIndexes() {
        try {
            // Books indexes
            MongoCollection<Document> booksCollection = database.getCollection(COLLECTION_BOOKS);
            booksCollection.createIndex(new Document("bookId", 1));
            booksCollection.createIndex(new Document("title", "text").append("author", "text"));
            booksCollection.createIndex(new Document("isbn", 1));
            booksCollection.createIndex(new Document("category", 1));
            
            // Users indexes
            MongoCollection<Document> usersCollection = database.getCollection(COLLECTION_USERS);
            usersCollection.createIndex(new Document("userId", 1));
            usersCollection.createIndex(new Document("email", 1));
            usersCollection.createIndex(new Document("studentId", 1));
            
            // Borrow records indexes
            MongoCollection<Document> borrowRecordsCollection = database.getCollection(COLLECTION_BORROW_RECORDS);
            borrowRecordsCollection.createIndex(new Document("userId", 1));
            borrowRecordsCollection.createIndex(new Document("bookId", 1));
            borrowRecordsCollection.createIndex(new Document("status", 1));
            borrowRecordsCollection.createIndex(new Document("expectedReturnDate", 1));
            
            // Book copies indexes
            MongoCollection<Document> bookCopiesCollection = database.getCollection(COLLECTION_BOOK_COPIES);
            bookCopiesCollection.createIndex(new Document("copyId", 1));
            bookCopiesCollection.createIndex(new Document("bookId", 1));
            bookCopiesCollection.createIndex(new Document("status", 1));
            
            // Fines indexes
            MongoCollection<Document> finesCollection = database.getCollection(COLLECTION_FINES);
            finesCollection.createIndex(new Document("userId", 1));
            finesCollection.createIndex(new Document("status", 1));
            finesCollection.createIndex(new Document("dueDate", 1));
            
            LoggerUtil.info("Đã tạo các indexes thành công!");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo indexes: " + e.getMessage());
        }
    }
      /**
     * Khởi tạo dữ liệu mẫu
     */
    private static void initializeSampleData() {
        try {
            LoggerUtil.info("Đang khởi tạo dữ liệu mẫu cơ bản...");
            
            // Chỉ tạo categories, không tạo admin (để tránh duplicate)
            createSampleCategories();
            
            LoggerUtil.info("Đã khởi tạo dữ liệu mẫu cơ bản thành công!");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi khởi tạo dữ liệu mẫu: " + e.getMessage());
        }
    }
      /**
     * Tạo tài khoản admin mặc định
     */
    private static void createDefaultAdmin() {
        try {
            MongoCollection<Document> usersCollection = database.getCollection(COLLECTION_USERS);
            
            // Kiểm tra xem admin đã tồn tại chưa
            Document existingAdmin = usersCollection.find(
                new Document("email", "dainam@dnu.edu.vn")
            ).first();
            
            if (existingAdmin != null) {
                LoggerUtil.info("Tài khoản admin đã tồn tại, bỏ qua việc tạo mới");
                return;
            }
            
            Document admin = new Document()
                    .append("userId", "admin001")
                    .append("email", "dainam@dnu.edu.vn")
                    .append("password", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi") // "dainam"
                    .append("firstName", "Đại")
                    .append("lastName", "Nam")
                    .append("fullName", "Đại Nam")
                    .append("role", "ADMIN")
                    .append("status", "ACTIVE")
                    .append("registrationDate", java.time.LocalDate.now())
                    .append("phone", "0987654321")
                    .append("address", "Hà Nội");
            
            usersCollection.insertOne(admin);
            LoggerUtil.info("Đã tạo tài khoản admin mặc định");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo admin mặc định: " + e.getMessage());
        }
    }
      /**
     * Tạo categories mẫu
     */
    private static void createSampleCategories() {
        try {
            MongoCollection<Document> categoriesCollection = database.getCollection(COLLECTION_CATEGORIES);
            
            // Kiểm tra xem đã có categories chưa
            if (categoriesCollection.countDocuments() > 0) {
                LoggerUtil.info("Categories đã tồn tại, bỏ qua việc tạo mới");
                return;
            }
            
            String[] categories = {
                "Khoa học máy tính", "Toán học", "Vật lý", "Hóa học", "Sinh học",
                "Văn học", "Lịch sử", "Địa lý", "Kinh tế", "Luật",
                "Y học", "Kỹ thuật", "Ngoại ngữ", "Tâm lý học", "Triết học"
            };
            
            for (String category : categories) {
                Document categoryDoc = new Document()
                        .append("name", category)
                        .append("description", "Thể loại " + category)
                        .append("createdAt", java.time.LocalDate.now());
                
                categoriesCollection.insertOne(categoryDoc);
            }
            
            LoggerUtil.info("Đã tạo " + categories.length + " categories mẫu");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo categories mẫu: " + e.getMessage());
        }
    }
    
    /**
     * Lấy database instance
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            initialize();
        }
        return database;
    }
    
    /**
     * Lấy collection theo tên
     */
    public static MongoCollection<Document> getCollection(String collectionName) {
        return getDatabase().getCollection(collectionName);
    }
    
    /**
     * Đóng kết nối
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            LoggerUtil.info("Đã đóng kết nối MongoDB");
        }
    }
}
