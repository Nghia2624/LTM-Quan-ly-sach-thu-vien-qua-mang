package com.library.database;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SampleDataGenerator {
    private final BookDAO bookDAO;
    private final UserDAO userDAO;
    private final BorrowRecordDAO borrowRecordDAO;
    private final Random random = new Random();
    
    public SampleDataGenerator() {
        this.bookDAO = new BookDAO();
        this.userDAO = new UserDAO();
        this.borrowRecordDAO = new BorrowRecordDAO();
    }
    
    public void generateSampleData() {
        System.out.println("=== Generating Enhanced Sample Data ===");
        
        // Check existing data to avoid duplicates
        checkExistingData();
        
        // Generate comprehensive sample books
        generateRichSampleBooks();
        
        // Generate diverse sample borrow records
        generateRichSampleBorrowRecords();
        
        // Generate additional users if needed
        generateSampleUsers();
        
        System.out.println("=== Enhanced Sample Data Generation Complete ===");
    }
    
    private void checkExistingData() {
        List<Book> existingBooks = bookDAO.getAllBooks();
        List<BorrowRecord> existingRecords = borrowRecordDAO.getAllBorrowRecords();
        
        System.out.println("Existing data check:");
        System.out.println("- Books in database: " + existingBooks.size());
        System.out.println("- Borrow records in database: " + existingRecords.size());
        
        if (existingBooks.size() > 20) {
            System.out.println("Database already contains sufficient sample data. Skipping book generation.");
            return;
        }
    }
    
    /**
     * Check if sufficient data already exists to avoid regeneration
     */
    public boolean hasEnoughSampleData() {
        try {
            List<Book> books = bookDAO.getAllBooks();
            List<BorrowRecord> records = borrowRecordDAO.getAllBorrowRecords();
            
            // Consider "enough" if we have at least 20 books and 10 records
            return books.size() >= 20 && records.size() >= 10;
        } catch (Exception e) {
            System.err.println("Error checking existing data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate sample data only if needed
     */
    public void generateSampleDataIfNeeded() {
        System.out.println("=== Smart Sample Data Generation ===");
        
        if (hasEnoughSampleData()) {
            System.out.println("✅ Sufficient sample data already exists. Skipping generation.");
            System.out.println("💡 Use 'force' parameter to regenerate anyway.");
            return;
        }
        
        System.out.println("📊 Insufficient sample data detected. Generating...");
        generateSampleData();
    }
    
    /**
     * Force regeneration of all sample data (for testing)
     */
    public void regenerateAllSampleData() {
        System.out.println("=== Forced Sample Data Regeneration ===");
        System.out.println("⚠️  This will add new data even if duplicates exist");
        generateSampleData();
    }
    
    /**
     * Clear all sample data (dangerous - for development only)
     */
    public void clearAllData() {
        System.out.println("⚠️  WARNING: This will clear ALL data from the database!");
        System.out.println("This operation is irreversible!");
        
        // In a real scenario, you might want to add confirmation
        // For now, we'll just print a warning
        System.out.println("❌ Clear operation not implemented for safety reasons");
        System.out.println("💡 Manually clear collections in MongoDB if needed");
    }
    
    private void generateRichSampleBooks() {
        List<Book> existingBooks = bookDAO.getAllBooks();
        if (existingBooks.size() > 20) {
            System.out.println("Sufficient books already exist. Skipping book generation.");
            return;
        }
        
        List<Book> sampleBooks = new ArrayList<>();
        
        // Văn học Việt Nam cổ điển
        sampleBooks.addAll(Arrays.asList(
            new Book("Truyện Kiều", "Nguyễn Du", "978-604-2-12345-1", "Văn học cổ điển", 
                    "NXB Văn học", 1820, 8, "Tác phẩm văn học kinh điển của Việt Nam, kể về số phận của Thúy Kiều"),
            new Book("Chinh phụ ngâm", "Đặng Trần Côn", "978-604-2-12345-4", "Văn học cổ điển", 
                    "NXB Văn học", 1740, 3, "Bài ca về nỗi niềm người phụ nữ có chồng đi chinh chiến"),
            new Book("Cung oán ngâm khúc", "Nguyễn Gia Thiều", "978-604-2-12345-5", "Văn học cổ điển", 
                    "NXB Văn học", 1790, 2, "Tác phẩm thể hiện nỗi khổ tâm của người cung nữ")
        ));
        
        // Văn học Việt Nam hiện đại
        sampleBooks.addAll(Arrays.asList(
            new Book("Số đỏ", "Vũ Trọng Phụng", "978-604-2-12345-2", "Văn học hiện đại", 
                    "NXB Hội Nhà văn", 1936, 5, "Tiểu thuyết châm biếm xã hội Việt Nam thập niên 1930"),
            new Book("Tắt đèn", "Ngô Tất Tố", "978-604-2-12345-3", "Văn học hiện đại", 
                    "NXB Văn học", 1939, 6, "Tác phẩm viết về cuộc sống nông thôn Việt Nam đầu thế kỷ 20"),
            new Book("Vang bóng một thời", "Nguyễn Tuân", "978-604-2-12345-6", "Văn học hiện đại", 
                    "NXB Hội Nhà văn", 1956, 4, "Tập truyện ngắn về cuộc sống thời kỳ kháng chiến"),
            new Book("Chí Phèo", "Nam Cao", "978-604-2-12345-7", "Văn học hiện đại", 
                    "NXB Văn học", 1941, 3, "Tác phẩm kinh điển về số phận người nông dân nghèo")
        ));
        
        // Văn học thế giới
        sampleBooks.addAll(Arrays.asList(
            new Book("Chiến tranh và Hòa bình", "Leo Tolstoy", "978-020-1-11111-1", "Văn học thế giới", 
                    "NXB Văn học", 1869, 4, "Tiểu thuyết vĩ đại về nước Nga thời Napoleon"),
            new Book("Tội ác và Hình phạt", "Fyodor Dostoevsky", "978-020-1-11111-2", "Văn học thế giới", 
                    "NXB Văn học", 1866, 3, "Tác phẩm tâm lý học sâu sắc về tội lỗi và sự cứu rỗi"),
            new Book("Bố già", "Mario Puzo", "978-020-1-11111-3", "Văn học thế giới", 
                    "NXB Hội Nhà văn", 1969, 5, "Tiểu thuyết về gia đình mafia ở Mỹ"),
            new Book("Đại Gatsby", "F. Scott Fitzgerald", "978-020-1-11111-4", "Văn học thế giới", 
                    "NXB Văn học", 1925, 4, "Tác phẩm về giấc mơ Mỹ và sự thất vọng")
        ));
          // Công nghệ thông tin
        sampleBooks.addAll(Arrays.asList(
            new Book("Lập trình Java từ cơ bản đến nâng cao", "Hoàng Văn Minh", "978-604-2-54321-1", "Công nghệ thông tin", 
                    "NXB Thống kê", 2023, 12, "Hướng dẫn toàn diện về lập trình Java, từ cú pháp cơ bản đến Spring Framework"),
            new Book("Cơ sở dữ liệu MongoDB và NoSQL", "Trần Thị Lan", "978-604-2-54321-2", "Công nghệ thông tin", 
                    "NXB Khoa học Kỹ thuật", 2024, 10, "Tài liệu chuyên sâu về MongoDB, thiết kế schema và tối ưu hiệu suất"),
            new Book("Máy học với Python", "Phạm Thị Mai", "978-604-2-54321-5", "Công nghệ thông tin", 
                    "NXB Khoa học Tự nhiên", 2024, 6, "Hướng dẫn thực hành Machine Learning với thư viện Scikit-learn")
        ));
        
        // Tâm lý học - Triết học
        sampleBooks.addAll(Arrays.asList(
            new Book("Tâm lý học đại cương", "David G. Myers", "978-604-2-55555-1", "Tâm lý học", 
                    "NXB Khoa học Xã hội", 2022, 3, "Giáo trình tâm lý học toàn diện từ cơ bản đến nâng cao"),
            new Book("Nghệ thuật tư duy", "Edward de Bono", "978-604-2-55555-2", "Tâm lý học", 
                    "NXB Trẻ", 2021, 4, "Phương pháp phát triển tư duy sáng tạo và giải quyết vấn đề"),
            new Book("Triết học phương Đông", "Nguyễn Hùng Hậu", "978-604-2-55555-3", "Triết học", 
                    "NXB Chính trị Quốc gia", 2020, 2, "Tư tưởng triết học Đông phương qua các thời kỳ"),
            new Book("Đạo đức học", "Aristotle", "978-604-2-55555-4", "Triết học", 
                    "NXB Văn học", 2019, 3, "Tác phẩm kinh điển về đạo đức học phương Tây")        ));
        
        // Ngoại ngữ
        sampleBooks.addAll(Arrays.asList(
            new Book("English Grammar in Use", "Raymond Murphy", "978-052-1-12345-1", "Ngoại ngữ", 
                    "Cambridge University Press", 2019, 10, "Sách học ngữ pháp tiếng Anh thực hành tốt nhất thế giới"),
            new Book("TOEIC Complete Guide", "Nguyễn Văn Đạt", "978-604-2-67890-1", "Ngoại ngữ", 
                    "NXB Đại học Quốc gia", 2023, 7, "Hướng dẫn toàn diện cho kỳ thi TOEIC với 1000+ câu hỏi thực hành")
        ));
        
        // Kinh tế - Quản trị
        sampleBooks.addAll(Arrays.asList(
            new Book("Kinh tế học vi mô", "Paul Samuelson", "978-604-2-11111-1", "Kinh tế", 
                    "NXB Kinh tế", 2020, 6, "Giáo trình kinh tế học vi mô cơ bản với nhiều ví dụ thực tế"),
            new Book("Quản trị doanh nghiệp hiện đại", "Peter Drucker", "978-604-2-22222-1", "Kinh tế", 
                    "NXB Lao động", 2021, 8, "Những nguyên lý quản trị doanh nghiệp trong thời đại số")
        ));
        
        // Khoa学 tự nhiên
        sampleBooks.addAll(Arrays.asList(
            new Book("Vật lý đại cương", "Halliday & Resnick", "978-604-2-33333-1", "Khoa học tự nhiên", 
                    "NXB Giáo dục", 2022, 5, "Giáo trình vật lý đại cương với phương pháp giải bài tập chi tiết"),
            new Book("Hóa học hữu cơ", "John McMurry", "978-604-2-44444-1", "Khoa học tự nhiên", 
                    "NXB Khoa học Tự nhiên", 2021, 6, "Sách về hóa học hữu cơ với cơ chế phản ứng chi tiết")
        ));
        
        // Add books to database with duplicate check
        int addedCount = 0;
        for (Book book : sampleBooks) {
            try {
                // Check if book already exists by ISBN
                List<Book> existingByIsbn = bookDAO.getAllBooks();
                boolean exists = existingByIsbn.stream()
                    .anyMatch(existing -> existing.getIsbn().equals(book.getIsbn()));
                
                if (!exists) {
                    if (bookDAO.addBook(book)) {
                        System.out.println("✓ Added book: " + book.getTitle() + " by " + book.getAuthor());
                        addedCount++;
                    } else {
                        System.out.println("✗ Failed to add book: " + book.getTitle());
                    }
                } else {
                    System.out.println("⚠ Book already exists: " + book.getTitle());
                }
            } catch (Exception e) {
                System.err.println("Error adding book " + book.getTitle() + ": " + e.getMessage());
            }
        }
        System.out.println("Added " + addedCount + " new books to the database.");
    }
      private void generateRichSampleBorrowRecords() {
        List<BorrowRecord> existingRecords = borrowRecordDAO.getAllBorrowRecords();
        if (existingRecords.size() > 15) {
            System.out.println("Sufficient borrow records already exist. Skipping generation.");
            return;
        }
        
        List<Book> allBooks = bookDAO.getAllBooks();
        if (allBooks.isEmpty()) {
            System.out.println("No books available to create borrow records");
            return;
        }
        
        // Sample borrower data
        String[][] borrowers = {
            {"Nguyễn Văn An", "nguyenvanan@email.com", "0901234567"},
            {"Trần Thị Bình", "tranthibinh@email.com", "0912345678"},
            {"Lê Văn Cường", "levancuong@email.com", "0923456789"},
            {"Phạm Thị Dung", "phamthidung@email.com", "0934567890"},
            {"Hoàng Minh Tuấn", "hoangminhtuan@email.com", "0945678901"},
            {"Vũ Thị Hạnh", "vuthihanh@email.com", "0956789012"},
            {"Đặng Văn Hải", "dangvanhai@email.com", "0967890123"},
            {"Bùi Thị Lan", "buithilan@email.com", "0978901234"},
            {"Phan Văn Nam", "phanvannam@email.com", "0989012345"},
            {"Lý Thị Oanh", "lythioanh@email.com", "0990123456"},
            {"Trịnh Văn Phong", "trinhvanphong@email.com", "0901112233"},
            {"Ngô Thị Quỳnh", "ngothiquynh@email.com", "0912223344"},
            {"Cao Văn Sơn", "caovanson@email.com", "0923334455"},
            {"Đỗ Thị Tâm", "dothitam@email.com", "0934445566"},
            {"Lưu Văn Uy", "luuvanuy@email.com", "0945556677"}
        };
        
        String[] notes = {
            "Mượn để nghiên cứu luận văn",
            "Mượn cho đồ án tốt nghiệp",
            "Sách tham khảo cho môn học",
            "Mượn để học thêm",
            "Nghiên cứu cá nhân",
            "Tham khảo cho công việc",
            "Đọc giải trí cuối tuần",
            "Chuẩn bị cho kỳ thi",
            "Tài liệu dạy học",
            "Nghiên cứu khoa học",
            "Mượn cho con học",
            "Đọc để nâng cao kiến thức",
            "Tham khảo cho dự án",
            "Học tập nhóm",
            "Chuẩn bị báo cáo"
        };
        
        List<BorrowRecord> sampleRecords = new ArrayList<>();
        int recordCount = Math.min(allBooks.size(), 20); // Create up to 20 records
        
        for (int i = 0; i < recordCount; i++) {
            Book book = allBooks.get(i % allBooks.size());
            String[] borrower = borrowers[i % borrowers.length];
            String note = notes[i % notes.length];
            
            // Vary the borrow scenarios
            BorrowRecord record;
            int scenario = i % 4;
            
            switch (scenario) {
                case 0: // Currently borrowed - due soon
                    record = new BorrowRecord(
                        book.getId(), book.getTitle(),
                        borrower[0], borrower[1], borrower[2],
                        LocalDateTime.now().plusDays(random.nextInt(14) + 1)
                    );
                    record.setNotes(note);
                    break;
                    
                case 1: // Currently borrowed - due later
                    record = new BorrowRecord(
                        book.getId(), book.getTitle(),
                        borrower[0], borrower[1], borrower[2],
                        LocalDateTime.now().plusDays(random.nextInt(14) + 15)
                    );
                    record.setNotes(note);
                    break;
                    
                case 2: // Overdue
                    record = new BorrowRecord(
                        book.getId(), book.getTitle(),
                        borrower[0], borrower[1], borrower[2],
                        LocalDateTime.now().minusDays(random.nextInt(10) + 1)
                    );
                    record.setNotes("Đã quá hạn - " + note);
                    break;
                    
                case 3: // Returned
                    int daysBorrowed = random.nextInt(30) + 1;
                    record = new BorrowRecord(
                        book.getId(), book.getTitle(),
                        borrower[0], borrower[1], borrower[2],
                        LocalDateTime.now().minusDays(daysBorrowed)
                    );
                    record.setActualReturnDate(LocalDateTime.now().minusDays(random.nextInt(daysBorrowed)));
                    record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
                    record.setNotes("Đã trả - " + note);
                    break;
                    
                default:
                    continue;
            }
            
            sampleRecords.add(record);
        }
        
        // Add borrow records and update book availability
        int addedCount = 0;
        for (BorrowRecord record : sampleRecords) {
            try {
                // Check for duplicate records
                boolean exists = existingRecords.stream().anyMatch(existing -> 
                    existing.getBookId().equals(record.getBookId()) &&
                    existing.getBorrowerEmail().equals(record.getBorrowerEmail()) &&
                    existing.getBorrowDate().equals(record.getBorrowDate())
                );
                
                if (!exists) {
                    if (borrowRecordDAO.addBorrowRecord(record)) {
                        System.out.println("✓ Added borrow record: " + record.getBookTitle() + " - " + record.getBorrowerName());
                        addedCount++;
                        
                        // Update book availability if currently borrowed
                        if (record.getStatus() == BorrowRecord.BorrowStatus.BORROWED) {
                            Book book = bookDAO.getBook(record.getBookId());
                            if (book != null && book.getAvailableCopies() > 0) {
                                bookDAO.updateAvailableCopies(record.getBookId(), book.getAvailableCopies() - 1);
                            }
                        }
                    } else {
                        System.out.println("✗ Failed to add borrow record for: " + record.getBookTitle());
                    }
                } else {
                    System.out.println("⚠ Borrow record already exists: " + record.getBookTitle() + " - " + record.getBorrowerName());
                }
            } catch (Exception e) {
                System.err.println("Error adding borrow record for " + record.getBookTitle() + ": " + e.getMessage());
            }
        }
        System.out.println("Added " + addedCount + " new borrow records to the database.");
    }
    
    private void generateSampleUsers() {
        // Additional users beyond the default admin
        List<User> sampleUsers = Arrays.asList(
            new User("librarian", "librarian@library.com", "librarian123", User.UserRole.LIBRARIAN),
            new User("staff1", "staff1@library.com", "staff123", User.UserRole.LIBRARIAN),
            new User("staff2", "staff2@library.com", "staff123", User.UserRole.LIBRARIAN)
        );
        
        int addedCount = 0;
        for (User user : sampleUsers) {
            try {
                if (userDAO.getUserByEmail(user.getEmail()) == null) {
                    if (userDAO.addUser(user)) {
                        System.out.println("✓ Added user: " + user.getFullName() + " (" + user.getRole() + ")");
                        addedCount++;
                    } else {
                        System.out.println("✗ Failed to add user: " + user.getFullName());
                    }
                } else {
                    System.out.println("⚠ User already exists: " + user.getEmail());
                }
            } catch (Exception e) {
                System.err.println("Error adding user " + user.getEmail() + ": " + e.getMessage());
            }
        }        System.out.println("Added " + addedCount + " new users to the database.");
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Enhanced Sample Data Generator ===");
            
            // Test database connection
            if (!DatabaseConnection.testConnection()) {
                System.err.println("Failed to connect to database. Please ensure MongoDB is running.");
                return;
            }
            
            SampleDataGenerator generator = new SampleDataGenerator();
            
            // Check command line arguments
            String action = args.length > 0 ? args[0].toLowerCase() : "smart";
            
            switch (action) {
                case "force":
                    System.out.println("🔄 Force mode: Generating data regardless of existing content");
                    generator.generateSampleData();
                    break;
                case "smart":
                default:
                    generator.generateSampleDataIfNeeded();
                    break;
            }
            
            System.out.println("=== Generation Complete ===");
            
            // Run consistency check
            System.out.println();
            System.out.println("🔍 Running data consistency check...");
            DatabaseInitializer.checkDataConsistency();
            
        } catch (Exception e) {
            System.err.println("Error generating sample data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}
