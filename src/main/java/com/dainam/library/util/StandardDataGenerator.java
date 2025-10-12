package com.dainam.library.util;

import com.dainam.library.config.DatabaseConfig;
import com.dainam.library.model.Book;
import com.dainam.library.model.BookCopy;
import com.dainam.library.model.User;
import com.dainam.library.model.BorrowRecord;
import com.dainam.library.service.BookService;
import com.dainam.library.service.UserService;
import com.dainam.library.service.BorrowService;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tạo dữ liệu chuẩn cho hệ thống - chỉ có khoa, không có chuyên ngành
 */
public class StandardDataGenerator {
    
    private static BookService bookService = new BookService();
    private static UserService userService = new UserService();
    private static BorrowService borrowService = new BorrowService();
    private static Random random = new Random();      // Dữ liệu người dùng phong phú với nhiều trường hợp khác nhau
    private static final String[][] STANDARD_USERS = {
        // 1 Admin
        {"dainam@dnu.edu.vn", "Đại Nam", "Univers", "dainam", "0123456789", "Số 1 , Phú lãm , Hà Đông , Hà Nội", "ADMIN", "ACTIVE", "1671020001", "Công nghệ thông tin", "2024"},
        
        // 2 Users chính (yêu cầu cũ)
        {"nghia@dnu.edu.vn", "Nghĩa", "Đỗ Ngọc", "nghia123", "0987654321", "Tây Mỗ , Nam Từ Liêm , Hà Nội", "USER", "ACTIVE", "1671020220", "Công nghệ thông tin", "2025"},
        {"ngoc@dnu.edu.vn", "Ngọc", "Lê Thị", "ngoc123", "0912345678", "789 Đường Pasteur, Q.3, TP.HCM", "USER", "ACTIVE", "1671020221", "Kinh tế", "2024"},
        
        // 4 Users bổ sung với các trường hợp khác nhau
        // User 1: Sinh viên năm 1 - mới nhập học
        {"minh@dnu.edu.vn", "Minh", "Phạm Văn", "minh123", "0923456789", "321 Đường Điện Biên Phủ, Q.Bình Thạnh, TP.HCM", "USER", "ACTIVE", "1671020222", "Y học", "2025"},
        
        // User 2: Sinh viên năm 5 - sắp tốt nghiệp  
        {"linh@dnu.edu.vn", "Linh", "Vũ Thị", "linh123", "0934567890", "654 Đường Cộng Hòa, Q.Tân Bình, TP.HCM", "USER", "ACTIVE", "1671020223", "Kỹ thuật", "2024"},
        
        // User 3: Tài khoản bị khóa do vi phạm
        {"hoang@dnu.edu.vn", "Hoàng", "Nguyễn Văn", "hoang123", "0945678901", "987 Đường Võ Văn Tần, Q.3, TP.HCM", "USER", "LOCKED", "1671020224", "Khoa học xã hội", "2025"},
        
        // User 4: Sinh viên nghệ thuật - trường hợp đặc biệt
        {"thao@dnu.edu.vn", "Thảo", "Trần Thị", "thao123", "0956789012", "147 Đường Nam Kỳ Khởi Nghĩa, Q.1, TP.HCM", "USER", "ACTIVE", "1671020225", "Nghệ thuật", "2024"}
    };
      // Dữ liệu 25 sách phong phú với nhiều thể loại khác nhau
    private static final String[][] STANDARD_BOOKS = {
        // Công nghệ thông tin (8 quyển)
        {"Lập trình Java từ cơ bản đến nâng cao", "Nguyễn Văn Minh", "9781234567890", "2023", "Công nghệ thông tin", "Hướng dẫn lập trình Java chi tiết từ cơ bản đến nâng cao", "450", "250000", "NXB Giáo dục Việt Nam"},
        {"Cấu trúc dữ liệu và giải thuật", "Trần Thị Hương", "9781234567891", "2022", "Công nghệ thông tin", "Giáo trình cấu trúc dữ liệu và thuật toán cho sinh viên", "380", "180000", "NXB Khoa học và Kỹ thuật"},
        {"Machine Learning cơ bản", "Lê Văn Đức", "9781234567892", "2023", "Công nghệ thông tin", "Giới thiệu về machine learning và ứng dụng thực tế", "320", "220000", "NXB Công nghệ"},
        {"Lập trình Web với HTML, CSS, JavaScript", "Phạm Thị Lan", "9781234567893", "2023", "Công nghệ thông tin", "Hướng dẫn tạo website từ cơ bản đến nâng cao", "420", "200000", "NXB Công nghệ"},
        {"Cơ sở dữ liệu MySQL", "Vũ Văn Tùng", "9781234567894", "2022", "Công nghệ thông tin", "Thiết kế và quản trị cơ sở dữ liệu MySQL", "350", "190000", "NXB Khoa học"},
        {"An toàn thông tin mạng", "Nguyễn Thị Mai", "9781234567895", "2023", "Công nghệ thông tin", "Bảo mật hệ thống và mạng máy tính", "400", "240000", "NXB An ninh"},
        {"Trí tuệ nhân tạo", "Trần Văn Hoàng", "9781234567896", "2023", "Công nghệ thông tin", "Những ứng dụng AI trong cuộc sống", "380", "280000", "NXB Công nghệ"},
        {"Lập trình Python cho người mới bắt đầu", "Lê Thị Hoa", "9781234567897", "2022", "Công nghệ thông tin", "Python từ cơ bản đến ứng dụng", "300", "170000", "NXB Giáo dục"},
        
        // Kinh tế (6 quyển)
        {"Kinh tế học vi mô", "Phạm Thị Lan", "9781234567898", "2022", "Kinh tế", "Giáo trình kinh tế học vi mô cơ bản", "400", "150000", "NXB Kinh tế"},
        {"Quản trị doanh nghiệp", "Nguyễn Văn Hoàng", "9781234567899", "2022", "Kinh tế", "Giáo trình quản trị doanh nghiệp hiện đại", "350", "200000", "NXB Kinh tế"},
        {"Kế toán tài chính", "Phạm Thị Yến", "9781234567800", "2023", "Kinh tế", "Nguyên lý kế toán tài chính", "350", "170000", "NXB Kế toán"},
        {"Marketing căn bản", "Vũ Thị Linh", "9781234567801", "2023", "Kinh tế", "Chiến lược marketing hiệu quả", "320", "160000", "NXB Kinh tế"},
        {"Tài chính doanh nghiệp", "Trần Văn Minh", "9781234567802", "2022", "Kinh tế", "Quản lý tài chính trong doanh nghiệp", "380", "180000", "NXB Tài chính"},
        {"Kinh tế vĩ mô", "Nguyễn Thị Thu", "9781234567803", "2023", "Kinh tế", "Các chỉ số kinh tế vĩ mô", "420", "190000", "NXB Kinh tế"},
        
        // Y học (3 quyển)
        {"Giải phẫu học người", "Bác sĩ Nguyễn Văn An", "9781234567804", "2022", "Y học", "Giáo trình giải phẫu học cơ bản", "500", "300000", "NXB Y học"},
        {"Sinh lý học", "Bác sĩ Trần Thị Bình", "9781234567805", "2023", "Y học", "Các chức năng sinh lý của cơ thể", "450", "280000", "NXB Y học"},
        {"Dược học cơ bản", "Dược sĩ Lê Văn Cường", "9781234567806", "2022", "Y học", "Kiến thức dược học căn bản", "400", "260000", "NXB Y học"},
        
        // Kỹ thuật (4 quyển)
        {"Kỹ thuật điện tử", "Trần Văn Đức", "9781234567807", "2023", "Kỹ thuật", "Giáo trình kỹ thuật điện tử", "400", "200000", "NXB Kỹ thuật"},
        {"Cơ khí chế tạo", "Lê Văn Hoàng", "9781234567808", "2022", "Kỹ thuật", "Công nghệ chế tạo máy", "380", "180000", "NXB Cơ khí"},
        {"Xây dựng dân dụng", "Phạm Văn Nam", "9781234567809", "2023", "Kỹ thuật", "Kỹ thuật xây dựng công trình", "420", "220000", "NXB Xây dựng"},
        {"Kỹ thuật môi trường", "Vũ Thị Hương", "9781234567810", "2022", "Kỹ thuật", "Bảo vệ môi trường và xử lý ô nhiễm", "360", "190000", "NXB Môi trường"},
        
        // Khoa học tự nhiên (2 quyển)
        {"Vật lý đại cương", "Lê Thị Hoa", "9781234567811", "2023", "Khoa học tự nhiên", "Giáo trình vật lý đại cương", "420", "160000", "NXB Khoa học"},
        {"Hóa học hữu cơ", "Vũ Thị Linh", "9781234567812", "2023", "Khoa học tự nhiên", "Giáo trình hóa học hữu cơ", "360", "170000", "NXB Hóa học"},
        
        // Nghệ thuật (2 quyển)
        {"Lịch sử nghệ thuật Việt Nam", "Họa sĩ Nguyễn Văn Tài", "9781234567813", "2022", "Nghệ thuật", "Nghệ thuật truyền thống Việt Nam", "300", "150000", "NXB Nghệ thuật"},
        {"Âm nhạc dân gian", "Nhạc sĩ Trần Thị Hạnh", "9781234567814", "2023", "Nghệ thuật", "Âm nhạc truyền thống các vùng miền", "280", "120000", "NXB Âm nhạc"}
    };
    
    public static void generateStandardData() {
        try {
            LoggerUtil.info("Bắt đầu tạo dữ liệu chuẩn...");
            
            // Xóa dữ liệu cũ
            clearAllData();
            
            // Tạo users chuẩn
            createStandardUsers();
            
            // Tạo sách chuẩn
            createStandardBooks();
              // Tạo bản sao sách
            createBookCopies();
            
            // Tạo một số giao dịch mượn/trả để có dữ liệu thực tế
            createSampleBorrowRecords();
            
            LoggerUtil.info("Hoàn thành tạo dữ liệu chuẩn!");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo dữ liệu chuẩn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void clearAllData() {
        try {
            MongoCollection<Document> usersCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_USERS);
            MongoCollection<Document> booksCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_BOOKS);
            MongoCollection<Document> bookCopiesCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_BOOK_COPIES);
            MongoCollection<Document> borrowRecordsCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_BORROW_RECORDS);
            MongoCollection<Document> finesCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_FINES);
            
            usersCollection.deleteMany(new Document());
            booksCollection.deleteMany(new Document());
            bookCopiesCollection.deleteMany(new Document());
            borrowRecordsCollection.deleteMany(new Document());
            finesCollection.deleteMany(new Document());
            
            LoggerUtil.info("Đã xóa tất cả dữ liệu cũ");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi xóa dữ liệu cũ: " + e.getMessage());
        }
    }
      private static void createStandardUsers() {
        try {
            int adminCount = 1;
            int userCount = 1;
            
            for (String[] userData : STANDARD_USERS) {
                User user = new User();
                
                // Tạo userId tự động
                String userId;
                if (userData[6].equals("ADMIN")) {
                    userId = "ADMIN" + String.format("%03d", adminCount++);
                } else {
                    userId = "USER" + String.format("%03d", userCount++);
                }
                user.setUserId(userId);
                
                user.setEmail(userData[0]);
                user.setFullName(userData[1] + " " + userData[2]);
                user.setFirstName(userData[1]);
                user.setLastName(userData[2]);
                user.setPassword(PasswordUtil.hashPassword(userData[3]));
                user.setPhone(userData[4]);
                user.setAddress(userData[5]);
                user.setRole(User.Role.valueOf(userData[6]));
                user.setStatus(User.Status.valueOf(userData[7]));
                user.setStudentId(userData[8]);
                user.setFaculty(userData[9]); // Chỉ có khoa, không có chuyên ngành
                user.setYearOfStudy(userData[10]);
                user.setDateOfBirth(LocalDate.of(2000 + random.nextInt(5), random.nextInt(12) + 1, random.nextInt(28) + 1));
                user.setRegistrationDate(LocalDate.now().minusDays(random.nextInt(365)));
                user.setLastLogin(LocalDate.now().minusDays(random.nextInt(30)));
                user.setTotalBorrowed(0);
                user.setCurrentBorrowed(0);
                user.setTotalFines(0.0);
                
                boolean success = userService.addUser(user);
                if (success) {
                    LoggerUtil.info("Đã tạo user: " + user.getEmail() + " với ID: " + userId);
                } else {
                    LoggerUtil.error("Lỗi tạo user: " + user.getEmail());
                }
            }
            
            LoggerUtil.info("Đã tạo " + STANDARD_USERS.length + " users chuẩn");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo users chuẩn: " + e.getMessage());
        }
    }
      private static void createStandardBooks() {
        try {
            for (String[] bookData : STANDARD_BOOKS) {
                Book book = new Book();
                book.setTitle(bookData[0]);
                book.setAuthor(bookData[1]);
                book.setIsbn(bookData[2]);
                book.setPublicationYear(Integer.parseInt(bookData[3]));
                book.setCategory(bookData[4]);
                book.setDescription(bookData[5]);
                book.setPageCount(Integer.parseInt(bookData[6]));
                book.setPrice(Double.parseDouble(bookData[7]));
                book.setPublisher(bookData[8]);
                // book.setLanguage("vi"); // Remove language field to avoid MongoDB text index issues
                book.setAvailableCopies(random.nextInt(6) + 10); // 10-15 bản sao
                book.setTotalCopies(book.getAvailableCopies());
                
                boolean success = bookService.addBook(book);
                if (success) {
                    LoggerUtil.info("Đã tạo sách: " + book.getTitle());
                } else {
                    LoggerUtil.error("Lỗi tạo sách: " + book.getTitle());
                }
            }
            
            LoggerUtil.info("Đã tạo " + STANDARD_BOOKS.length + " sách chuẩn");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo sách chuẩn: " + e.getMessage());
        }
    }    private static void createBookCopies() {
        try {
            List<Book> books = bookService.getAllBooks(0, 100); // Start from page 0
            LoggerUtil.info("Tìm thấy " + books.size() + " sách để tạo bản sao");
            
            if (books.isEmpty()) {
                LoggerUtil.warn("Không tìm thấy sách nào để tạo bản sao");
                return;
            }
            
            for (Book book : books) {
                int totalCopies = book.getTotalCopies(); // Sử dụng số bản sao đã được set (10-15)
                LoggerUtil.info("Tạo " + totalCopies + " bản sao cho sách: " + book.getTitle());
                
                // Tạo bản sao theo số lượng đã định
                for (int i = 1; i <= totalCopies; i++) {
                    BookCopy copy = new BookCopy();
                    copy.setBookId(book.getBookId());
                    copy.setCopyId(book.getBookId() + "_" + String.format("%03d", i));
                    
                    // Tạo điều kiện ngẫu nhiên cho bản sao
                    BookCopy.Condition[] conditions = BookCopy.Condition.values();
                    copy.setCondition(conditions[random.nextInt(conditions.length)]);
                    copy.setStatus(BookCopy.Status.AVAILABLE);
                    
                    // Vị trí ngẫu nhiên
                    copy.setLocation("Tầng " + (random.nextInt(5) + 1) + " - Kệ " + (random.nextInt(30) + 1) + " - Ngăn " + (random.nextInt(10) + 1));
                    copy.setNotes("Bản sao " + i + " của " + book.getTitle());
                    
                    boolean success = bookService.addBookCopy(book.getBookId(), copy);
                    if (success) {
                        LoggerUtil.info("Đã tạo bản sao " + i + "/" + totalCopies + " cho sách: " + book.getTitle());
                    } else {
                        LoggerUtil.error("Lỗi tạo bản sao " + i + " cho sách: " + book.getTitle());
                    }
                }
                
                LoggerUtil.info("Hoàn thành tạo " + totalCopies + " bản sao cho sách: " + book.getTitle());
            }
            
            LoggerUtil.info("Đã tạo bản sao cho tất cả " + books.size() + " sách");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo bản sao sách: " + e.getMessage());
            e.printStackTrace();
        }
    }
      private static void createSampleBorrowRecords() {
        try {
            // Lấy danh sách users và books
            List<User> users = userService.getAllUsers();
            List<Book> books = bookService.getAllBooks(0, 25);
            
            LoggerUtil.info("Tìm thấy " + users.size() + " users và " + books.size() + " sách để tạo borrow records");
            
            // Loại bỏ admin khỏi danh sách
            users = users.stream()
                    .filter(user -> user.getRole() == User.Role.USER)
                    .collect(java.util.stream.Collectors.toList());
            
            if (users.isEmpty() || books.isEmpty()) {
                LoggerUtil.warn("Không có user hoặc sách để tạo borrow records. Users: " + users.size() + ", Books: " + books.size());
                return;
            }
            
            LoggerUtil.info("Có " + users.size() + " users và " + books.size() + " sách để tạo borrow records");
            
            // Tạo một số giao dịch mượn sách
            int borrowCount = 0;
            
            // User nghia mượn 2 sách (đang mượn)
            User nghia = users.stream().filter(u -> u.getEmail().equals("nghia@dnu.edu.vn")).findFirst().orElse(null);
            if (nghia != null && books.size() >= 2) {
                createBorrowRecord(nghia, books.get(0), false); // Chưa trả
                createBorrowRecord(nghia, books.get(1), false); // Chưa trả
                borrowCount += 2;
            }
            
            // User ngoc mượn 1 sách và đã trả
            User ngoc = users.stream().filter(u -> u.getEmail().equals("ngoc@dnu.edu.vn")).findFirst().orElse(null);
            if (ngoc != null && books.size() >= 3) {
                createBorrowRecord(ngoc, books.get(2), true); // Đã trả
                borrowCount += 1;
            }
            
            // User minh (năm 1) mượn 1 sách đầu tiên
            User minh = users.stream().filter(u -> u.getEmail().equals("minh@dnu.edu.vn")).findFirst().orElse(null);
            if (minh != null && books.size() >= 4) {
                createBorrowRecord(minh, books.get(3), false); // Chưa trả
                borrowCount += 1;
            }
            
            // User linh (năm 5) mượn nhiều sách (đã trả hết)
            User linh = users.stream().filter(u -> u.getEmail().equals("linh@dnu.edu.vn")).findFirst().orElse(null);
            if (linh != null && books.size() >= 7) {
                createBorrowRecord(linh, books.get(4), true); // Đã trả
                createBorrowRecord(linh, books.get(5), true); // Đã trả  
                createBorrowRecord(linh, books.get(6), true); // Đã trả
                borrowCount += 3;
            }
            
            LoggerUtil.info("Đã tạo " + borrowCount + " borrow records mẫu");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo borrow records: " + e.getMessage());
        }
    }
      private static void createBorrowRecord(User user, Book book, boolean isReturned) {
        try {
            // Lấy bản sao đầu tiên có sẵn của sách
            List<BookCopy> copies = bookService.getBookCopies(book.getBookId());
            if (copies.isEmpty()) {
                LoggerUtil.error("Không có bản sao cho sách: " + book.getTitle());
                return;
            }
            
            BookCopy copy = copies.get(0); // Lấy bản sao đầu tiên
            
            BorrowRecord record = new BorrowRecord();
            record.setUserId(user.getUserId());
            record.setBookId(book.getBookId());
            record.setCopyId(copy.getCopyId());
            record.setBorrowDate(LocalDate.now().minusDays(random.nextInt(30) + 1)); // 1-30 ngày trước
            
            if (isReturned) {
                record.setActualReturnDate(record.getBorrowDate().plusDays(random.nextInt(14) + 1)); // Trả sau 1-14 ngày
                record.setStatus(BorrowRecord.Status.RETURNED);
                copy.setStatus(BookCopy.Status.AVAILABLE);
            } else {
                record.setExpectedReturnDate(record.getBorrowDate().plusDays(14)); // Hạn trả 14 ngày
                record.setStatus(BorrowRecord.Status.BORROWED);
                copy.setStatus(BookCopy.Status.BORROWED);
                
                // Cập nhật user stats
                user.setCurrentBorrowed(user.getCurrentBorrowed() + 1);
            }
            
            // Cập nhật user stats
            user.setTotalBorrowed(user.getTotalBorrowed() + 1);
            
            // Lưu borrow record
            borrowService.addBorrowRecord(record);
            
            // Cập nhật book copy status
            bookService.updateBookCopy(copy);
            
            // Cập nhật user
            userService.updateUser(user);
            
            LoggerUtil.info("Tạo borrow record: " + user.getEmail() + " mượn " + book.getTitle() + 
                           (isReturned ? " (đã trả)" : " (đang mượn)"));
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo borrow record: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        try {
            DatabaseConfig.initializeWithoutSampleData();
            generateStandardData();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi khởi tạo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
