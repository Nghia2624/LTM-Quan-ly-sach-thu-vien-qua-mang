package com.dainam.library.service;

import com.dainam.library.config.DatabaseConfig;
import com.dainam.library.model.Book;
import com.dainam.library.model.BookCopy;
import com.dainam.library.model.BorrowRecord;
import com.dainam.library.model.User;
import com.dainam.library.util.LoggerUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
// import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service class cho quản lý mượn/trả sách
 */
public class BorrowService {
    
    private final MongoCollection<Document> borrowRecordsCollection;
    private final BookService bookService;
    private final UserService userService;
    
    public BorrowService() {
        this.borrowRecordsCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_BORROW_RECORDS);
        this.bookService = new BookService();
        this.userService = new UserService();
    }
    
    /**
     * Mượn sách với validation đầy đủ
     */
    public BorrowRecord borrowBook(String userId, String bookId, String copyId) {
        try {
            // Kiểm tra user
            User user = userService.getUserById(userId);
            if (user == null) {
                LoggerUtil.warn("User không tồn tại: " + userId);
                throw new RuntimeException("User không tồn tại");
            }
            
            // Kiểm tra user có hoạt động không
            if (!user.isActive()) {
                LoggerUtil.warn("User không hoạt động: " + userId);
                throw new RuntimeException("Tài khoản của bạn đã bị khóa");
            }
            
            // Kiểm tra user có sách quá hạn không
            List<BorrowRecord> overdueBooks = getOverdueBooks(userId);
            if (!overdueBooks.isEmpty()) {
                LoggerUtil.warn("User có sách quá hạn: " + userId);
                throw new RuntimeException("Bạn có sách quá hạn. Vui lòng trả sách trước khi mượn mới");
            }
            
            // Kiểm tra user có thể mượn không (tối đa 5 quyển)
            List<BorrowRecord> currentBorrows = getCurrentBorrows(userId);
            if (currentBorrows.size() >= 5) {
                LoggerUtil.warn("User đã mượn tối đa số sách: " + userId + " - Số sách hiện tại: " + currentBorrows.size());
                throw new RuntimeException("Bạn đã mượn tối đa 5 quyển sách");
            }
            
            // Kiểm tra sách
            Book book = bookService.getBookById(bookId);
            if (book == null) {
                LoggerUtil.warn("Sách không tồn tại: " + bookId);
                throw new RuntimeException("Sách không tồn tại");
            }
            
            // Kiểm tra bản sao
            BookCopy copy = bookService.getBookCopyById(copyId);
            if (copy == null) {
                LoggerUtil.warn("Bản sao không tồn tại: " + copyId);
                throw new RuntimeException("Bản sao sách không tồn tại");
            }
            
            if (!copy.isAvailable()) {
                LoggerUtil.warn("Bản sao không có sẵn: " + copyId);
                throw new RuntimeException("Bản sao này không có sẵn để mượn");
            }
            
            // Kiểm tra user đã mượn sách này chưa
            for (BorrowRecord existingRecord : currentBorrows) {
                if (bookId.equals(existingRecord.getBookId())) {
                    LoggerUtil.warn("User đã mượn sách này: " + userId + " - Book: " + bookId);
                    throw new RuntimeException("Bạn đã mượn sách '" + book.getTitle() + "' rồi. Bạn có chắc chắn muốn mượn thêm bản sao khác của sách này không?");
                }
            }
            
            // Tạo bản ghi mượn
            BorrowRecord record = new BorrowRecord();
            record.setRecordId(generateRecordId());
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setCopyId(copyId);
            record.setBorrowNotes("Mượn sách: " + book.getTitle());
            
            // Lưu bản ghi
            Document recordDoc = borrowRecordToDocument(record);
            borrowRecordsCollection.insertOne(recordDoc);
            
            // Cập nhật trạng thái bản sao
            bookService.updateBookCopyStatus(copyId, BookCopy.Status.BORROWED);
            
            // Cập nhật thông tin user
            user.incrementCurrentBorrowed();
            userService.updateUser(user);
            
            // Cập nhật số lượng sách
            bookService.updateBookCopyCount(bookId);
            
            LoggerUtil.info("Mượn sách thành công: " + record.getRecordId() + " - User: " + userId + " - Book: " + bookId);
            return record;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi mượn sách: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Trả sách
     */
    public boolean returnBook(String recordId) {
        try {
            // Lấy bản ghi mượn
            BorrowRecord record = getBorrowRecordById(recordId);
            if (record == null) {
                LoggerUtil.warn("Bản ghi mượn không tồn tại: " + recordId);
                return false;
            }
            
            // Kiểm tra có thể trả không
            if (!record.canReturn()) {
                LoggerUtil.warn("Không thể trả sách: " + recordId);
                return false;
            }
            
            // Cập nhật bản ghi
            record.markAsReturned();
            Document recordDoc = borrowRecordToDocument(record);
            Bson filter = Filters.eq("recordId", recordId);
            borrowRecordsCollection.replaceOne(filter, recordDoc);
            
            // Cập nhật trạng thái bản sao
            bookService.updateBookCopyStatus(record.getCopyId(), BookCopy.Status.AVAILABLE);
            
            // Cập nhật thông tin user
            User user = userService.getUserById(record.getUserId());
            if (user != null) {
                user.decrementCurrentBorrowed();
                userService.updateUser(user);
            }
            
            // Cập nhật số lượng sách
            bookService.updateBookCopyCount(record.getBookId());
            
            LoggerUtil.info("Trả sách thành công: " + recordId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi trả sách: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy lịch sử mượn của user
     */
    public List<BorrowRecord> getBorrowHistory(String userId) {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            
            Bson filter = Filters.eq("userId", userId);
            Bson sort = Sorts.descending("borrowDate");
            
            for (Document recordDoc : borrowRecordsCollection.find(filter).sort(sort)) {
                records.add(documentToBorrowRecord(recordDoc));
            }
            
            return records;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy lịch sử mượn: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy sách đang mượn
     */
    public List<BorrowRecord> getCurrentBorrows(String userId) {
        List<BorrowRecord> records = new ArrayList<>();
        try {
            FindIterable<Document> docs = borrowRecordsCollection.find(
                Filters.and(
                    Filters.eq("userId", userId),
                    Filters.in("status", Arrays.asList("BORROWED", "OVERDUE"))
                )
            ).sort(Sorts.descending("borrowDate"));
            
            for (Document doc : docs) {
                BorrowRecord record = documentToBorrowRecord(doc);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy sách đang mượn: " + e.getMessage());
        }
        return records;
    }
    
    /**
     * Lấy sách quá hạn
     */
    public List<BorrowRecord> getOverdueBooks(String userId) {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            
            Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("status", "OVERDUE")
            );
            
            for (Document recordDoc : borrowRecordsCollection.find(filter)) {
                records.add(documentToBorrowRecord(recordDoc));
            }
            
            return records;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy sách quá hạn: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy tất cả sách quá hạn (admin)
     */
    public List<BorrowRecord> getAllOverdueBooks() {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            
            Bson filter = Filters.eq("status", "OVERDUE");
            
            for (Document recordDoc : borrowRecordsCollection.find(filter)) {
                records.add(documentToBorrowRecord(recordDoc));
            }
            
            return records;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy tất cả sách quá hạn: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Cập nhật trạng thái quá hạn
     */
    public void updateOverdueStatus() {
        try {
            LocalDate today = LocalDate.now();
            
            Bson filter = Filters.and(
                Filters.eq("status", "BORROWED"),
                Filters.lt("expectedReturnDate", today)
            );
            
            Bson update = Updates.set("status", "OVERDUE");
            
            borrowRecordsCollection.updateMany(filter, update);
            
            LoggerUtil.info("Đã cập nhật trạng thái quá hạn");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật trạng thái quá hạn: " + e.getMessage());
        }
    }
    
    /**
     * Đánh dấu sách bị mất với tính phạt
     */
    public boolean markAsLost(String recordId, String notes) {
        try {
            BorrowRecord record = getBorrowRecordById(recordId);
            if (record == null) {
                throw new RuntimeException("Bản ghi mượn không tồn tại");
            }
            
            if (!record.canReturn()) {
                throw new RuntimeException("Không thể đánh dấu sách bị mất");
            }
            
            // Lấy thông tin sách để tính phạt
            Book book = bookService.getBookById(record.getBookId());
            if (book == null) {
                throw new RuntimeException("Không tìm thấy thông tin sách");
            }
            
            // Đánh dấu bản ghi
            record.markAsLost();
            record.setReturnNotes(notes);
            
            // Tính phạt mất sách (100% giá trị sách)
            double fineAmount = book.getPrice();
            record.setFineAmount(fineAmount);
            
            Document recordDoc = borrowRecordToDocument(record);
            Bson filter = Filters.eq("recordId", recordId);
            borrowRecordsCollection.replaceOne(filter, recordDoc);
            
            // Cập nhật trạng thái bản sao
            bookService.updateBookCopyStatus(record.getCopyId(), BookCopy.Status.LOST);
            
            // Cập nhật thông tin user
            User user = userService.getUserById(record.getUserId());
            if (user != null) {
                user.decrementCurrentBorrowed();
                user.addFine(fineAmount);
                userService.updateUser(user);
            }
            
            // Tạo phạt
            FineService fineService = new FineService();
            fineService.createLostBookFine(record.getUserId(), recordId, 
                record.getBookId(), record.getCopyId(), fineAmount);
            
            // Cập nhật số lượng sách
            bookService.updateBookCopyCount(record.getBookId());
            
            LoggerUtil.info("Đánh dấu sách bị mất thành công: " + recordId + " - Phạt: " + fineAmount + " VND");
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đánh dấu sách bị mất: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Đánh dấu sách bị hỏng với tính phạt
     */
    public boolean markAsDamaged(String recordId, String notes, double damagePercentage) {
        try {
            BorrowRecord record = getBorrowRecordById(recordId);
            if (record == null) {
                throw new RuntimeException("Bản ghi mượn không tồn tại");
            }
            
            if (!record.canReturn()) {
                throw new RuntimeException("Không thể đánh dấu sách bị hỏng");
            }
            
            // Lấy thông tin sách để tính phạt
            Book book = bookService.getBookById(record.getBookId());
            if (book == null) {
                throw new RuntimeException("Không tìm thấy thông tin sách");
            }
            
            // Đánh dấu bản ghi
            record.markAsDamaged();
            record.setReturnNotes(notes);
            
            // Tính phạt hỏng sách (theo % hư hỏng)
            double fineAmount = book.getPrice() * (damagePercentage / 100.0);
            record.setFineAmount(fineAmount);
            
            Document recordDoc = borrowRecordToDocument(record);
            Bson filter = Filters.eq("recordId", recordId);
            borrowRecordsCollection.replaceOne(filter, recordDoc);
            
            // Cập nhật trạng thái bản sao
            bookService.updateBookCopyStatus(record.getCopyId(), BookCopy.Status.DAMAGED);
            
            // Cập nhật thông tin user
            User user = userService.getUserById(record.getUserId());
            if (user != null) {
                user.decrementCurrentBorrowed();
                user.addFine(fineAmount);
                userService.updateUser(user);
            }
            
            // Tạo phạt
            FineService fineService = new FineService();
            fineService.createDamagedBookFine(record.getUserId(), recordId, 
                record.getBookId(), record.getCopyId(), fineAmount);
            
            // Cập nhật số lượng sách
            bookService.updateBookCopyCount(record.getBookId());
            
            LoggerUtil.info("Đánh dấu sách bị hỏng thành công: " + recordId + " - Phạt: " + fineAmount + " VND");
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đánh dấu sách bị hỏng: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Lấy bản ghi mượn theo ID
     */
    public BorrowRecord getBorrowRecordById(String recordId) {
        try {
            Document doc = borrowRecordsCollection.find(Filters.eq("recordId", recordId)).first();
            return doc != null ? documentToBorrowRecord(doc) : null;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy borrow record: " + e.getMessage());
            return null;
        }
    }
    
    
    /**
     * Chuyển đổi Document thành BorrowRecord
     */
    private BorrowRecord documentToBorrowRecord(Document doc) {
        BorrowRecord record = new BorrowRecord();
        record.setRecordId(doc.getString("recordId"));
        record.setUserId(doc.getString("userId"));
        record.setBookId(doc.getString("bookId"));
        record.setCopyId(doc.getString("copyId"));
        record.setStatus(BorrowRecord.Status.valueOf(doc.getString("status")));
        record.setBorrowNotes(doc.getString("borrowNotes"));
        record.setReturnNotes(doc.getString("returnNotes"));
        record.setFineAmount(doc.getDouble("fineAmount") != null ? doc.getDouble("fineAmount") : 0.0);
        record.setFinePaid(doc.getBoolean("finePaid", false));
        
        // Handle dates
        if (doc.getDate("borrowDate") != null) {
            record.setBorrowDate(doc.getDate("borrowDate").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("expectedReturnDate") != null) {
            record.setExpectedReturnDate(doc.getDate("expectedReturnDate").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("actualReturnDate") != null) {
            record.setActualReturnDate(doc.getDate("actualReturnDate").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("createdAt") != null) {
            record.setCreatedAt(doc.getDate("createdAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("updatedAt") != null) {
            record.setUpdatedAt(doc.getDate("updatedAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        
        return record;
    }
    
    /**
     * Chuyển đổi BorrowRecord thành Document
     */
    private Document borrowRecordToDocument(BorrowRecord record) {
        Document doc = new Document()
            .append("recordId", record.getRecordId())
            .append("userId", record.getUserId())
            .append("bookId", record.getBookId())
            .append("copyId", record.getCopyId())
            .append("status", record.getStatus().name())
            .append("borrowNotes", record.getBorrowNotes())
            .append("returnNotes", record.getReturnNotes())
            .append("fineAmount", record.getFineAmount())
            .append("finePaid", record.isFinePaid());
        
        // Handle dates
        if (record.getBorrowDate() != null) {
            doc.append("borrowDate", java.sql.Date.valueOf(record.getBorrowDate()));
        }
        if (record.getExpectedReturnDate() != null) {
            doc.append("expectedReturnDate", java.sql.Date.valueOf(record.getExpectedReturnDate()));
        }
        if (record.getActualReturnDate() != null) {
            doc.append("actualReturnDate", java.sql.Date.valueOf(record.getActualReturnDate()));
        }
        if (record.getCreatedAt() != null) {
            doc.append("createdAt", java.sql.Date.valueOf(record.getCreatedAt()));
        }
        if (record.getUpdatedAt() != null) {
            doc.append("updatedAt", java.sql.Date.valueOf(record.getUpdatedAt()));
        }
        
        return doc;
    }
    
    /**
     * Tạo recordId mới
     */
    private String generateRecordId() {
        return "record_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Lấy tất cả bản ghi quá hạn
     */
    public List<BorrowRecord> getOverdueRecords() {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            Bson filter = Filters.and(
                Filters.eq("status", "BORROWED"),
                Filters.lt("expectedReturnDate", today)
            );
            
            for (Document doc : borrowRecordsCollection.find(filter)) {
                records.add(documentToBorrowRecord(doc));
            }
            
            return records;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy danh sách quá hạn: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Thêm bản ghi mượn mới
     */
    public boolean addBorrowRecord(BorrowRecord record) {
        try {
            if (record.getRecordId() == null || record.getRecordId().isEmpty()) {
                record.setRecordId(generateRecordId());
            }
            
            Document doc = borrowRecordToDocument(record);
            borrowRecordsCollection.insertOne(doc);
            
            LoggerUtil.info("Thêm bản ghi mượn thành công: " + record.getRecordId());
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi thêm bản ghi mượn: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy tổng số lượt mượn
     */
    public long getTotalBorrows() {
        try {
            return borrowRecordsCollection.countDocuments();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm tổng lượt mượn: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Lấy số sách đang mượn
     */
    public long getBorrowedBooks() {
        try {
            return borrowRecordsCollection.countDocuments(
                Filters.eq("status", "BORROWED")
            );
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm sách đang mượn: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Lấy số sách quá hạn
     */
    public long getOverdueBooks() {
        try {
            return borrowRecordsCollection.countDocuments(
                Filters.eq("status", "OVERDUE")
            );
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm sách quá hạn: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Lấy tất cả bản ghi mượn với phân trang
     */
    public List<BorrowRecord> getAllBorrowRecords(int page, int size) {
        List<BorrowRecord> records = new ArrayList<>();
        try {
            int skip = (page - 1) * size;
            FindIterable<Document> docs = borrowRecordsCollection
                .find()
                .sort(Sorts.descending("borrowDate"))
                .skip(skip)
                .limit(size);
            
            for (Document doc : docs) {
                BorrowRecord record = documentToBorrowRecord(doc);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy danh sách borrow records: " + e.getMessage());
        }
        return records;
    }
    
    /**
     * Tìm kiếm bản ghi mượn
     */
    public List<BorrowRecord> searchBorrowRecords(String query, String status) {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            Bson filter = new Document();
            
            if (query != null && !query.trim().isEmpty()) {
                Bson textSearch = new Document("$text", new Document("$search", query));
                filter = new Document("$and", Arrays.asList(textSearch));
            }
            
            if (status != null && !status.equals("Tất cả trạng thái")) {
                String statusValue = getStatusValue(status);
                if (statusValue != null) {
                    filter = new Document("$and", Arrays.asList(filter, new Document("status", statusValue)));
                }
            }
            
            FindIterable<Document> documents = borrowRecordsCollection.find(filter)
                .sort(new Document("borrowDate", -1));
            
            for (Document doc : documents) {
                records.add(documentToBorrowRecord(doc));
            }
            
            return records;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tìm kiếm bản ghi mượn: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy bản ghi mượn theo ngày
     */
    public List<BorrowRecord> getBorrowRecordsByDate(LocalDate date) {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            
            Bson filter = new Document("borrowDate", 
                new Document("$gte", java.sql.Date.valueOf(date))
                    .append("$lt", java.sql.Date.valueOf(date.plusDays(1))));
            
            FindIterable<Document> documents = borrowRecordsCollection.find(filter);
            
            for (Document doc : documents) {
                records.add(documentToBorrowRecord(doc));
            }
            
            return records;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy bản ghi mượn theo ngày: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy bản ghi mượn theo user
     */
    public List<BorrowRecord> getBorrowRecordsByUser(String userId) {
        try {
            List<BorrowRecord> records = new ArrayList<>();
            
            Bson filter = new Document("userId", userId);
            
            FindIterable<Document> documents = borrowRecordsCollection.find(filter)
                .sort(new Document("borrowDate", -1));
            
            for (Document doc : documents) {
                records.add(documentToBorrowRecord(doc));
            }
            
            return records;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy bản ghi mượn theo user: " + e.getMessage());
            return new ArrayList<>();
        }    }
    
    /**
     * Bắt buộc trả sách (admin only)
     */
    public boolean forceReturn(String recordId) {
        try {
            BorrowRecord record = getBorrowRecordById(recordId);
            if (record == null) {
                return false;
            }
            
            record.markAsReturned();
            record.setReturnNotes("Bắt buộc trả bởi admin");
            
            Document recordDoc = borrowRecordToDocument(record);
            Bson filter = Filters.eq("recordId", recordId);
            borrowRecordsCollection.replaceOne(filter, recordDoc);
            
            // Cập nhật trạng thái bản sao
            bookService.updateBookCopyStatus(record.getCopyId(), BookCopy.Status.AVAILABLE);
            
            // Cập nhật thông tin user
            User user = userService.getUserById(record.getUserId());
            if (user != null) {
                user.decrementCurrentBorrowed();
                userService.updateUser(user);
            }
            
            // Cập nhật số lượng sách
            bookService.updateBookCopyCount(record.getBookId());
            
            LoggerUtil.info("Bắt buộc trả sách thành công: " + recordId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi bắt buộc trả sách: " + e.getMessage());
            return false;
        }
    }
      /**
     * Gia hạn mượn sách
     */
    public boolean extendBorrow(String recordId) {
        try {
            BorrowRecord record = getBorrowRecordById(recordId);
            if (record == null) {
                LoggerUtil.warn("Bản ghi mượn không tồn tại: " + recordId);
                return false;
            }
            
            // Kiểm tra có thể gia hạn không
            if (record.isExtended()) {
                LoggerUtil.warn("Sách đã được gia hạn: " + recordId);
                throw new RuntimeException("Sách này đã được gia hạn rồi");
            }
            
            if (record.getStatus() != BorrowRecord.Status.BORROWED && 
                record.getStatus() != BorrowRecord.Status.OVERDUE) {
                LoggerUtil.warn("Không thể gia hạn sách với trạng thái: " + record.getStatus());
                throw new RuntimeException("Không thể gia hạn sách này");
            }
            
            // Gia hạn thêm 7 ngày
            LocalDate newReturnDate = record.getExpectedReturnDate().plusDays(7);
            record.setExpectedReturnDate(newReturnDate);
            record.setExtended(true);
            record.setUpdatedAt(LocalDate.now());
            
            // Cập nhật trạng thái nếu đang quá hạn
            if (record.getStatus() == BorrowRecord.Status.OVERDUE) {
                record.setStatus(BorrowRecord.Status.BORROWED);
            }
            
            // Lưu vào database
            Document recordDoc = borrowRecordToDocument(record);
            Bson filter = Filters.eq("recordId", recordId);
            borrowRecordsCollection.replaceOne(filter, recordDoc);
            
            LoggerUtil.info("Gia hạn sách thành công: " + recordId + " - Hạn mới: " + newReturnDate);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi gia hạn sách: " + e.getMessage());
            throw new RuntimeException("Lỗi gia hạn sách: " + e.getMessage());
        }
    }
    
    /**
     * Chuyển đổi status text sang enum
     */
    private String getStatusValue(String statusText) {
        switch (statusText) {
            case "Đang mượn": return "BORROWED";
            case "Đã trả": return "RETURNED";
            case "Quá hạn": return "OVERDUE";
            case "Bị mất": return "LOST";
            case "Bị hỏng": return "DAMAGED";
            default: return null;
        }
    }
}
