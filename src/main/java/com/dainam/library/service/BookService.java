package com.dainam.library.service;

import com.dainam.library.config.DatabaseConfig;
import com.dainam.library.model.Book;
import com.dainam.library.model.BookCopy;
import com.dainam.library.util.LoggerUtil;
import com.dainam.library.util.ValidationUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

// import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class cho quản lý sách
 */
public class BookService {
    
    private final MongoCollection<Document> booksCollection;
    private final MongoCollection<Document> bookCopiesCollection;
    
    public BookService() {
        this.booksCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_BOOKS);
        this.bookCopiesCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_BOOK_COPIES);
    }
    
    /**
     * Thêm sách mới
     */
    public boolean addBook(Book book) {
        try {
            // Validate input
            String validationError = ValidationUtil.validateBook(
                book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPublisher(), book.getPublicationYear(), book.getPrice()
            );
            
            if (validationError != null) {
                LoggerUtil.warn("Validation error: " + validationError);
                return false;
            }
            
            // Kiểm tra ISBN đã tồn tại
            if (getBookByISBN(book.getIsbn()) != null) {
                LoggerUtil.warn("ISBN đã tồn tại: " + book.getIsbn());
                return false;
            }
            
            // Tạo bookId nếu chưa có
            if (book.getBookId() == null) {
                book.setBookId(generateBookId());
            }
            
            // Chuyển đổi sang Document và lưu
            Document bookDoc = bookToDocument(book);
            booksCollection.insertOne(bookDoc);
            
            LoggerUtil.info("Thêm sách thành công: " + book.getTitle());
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi thêm sách: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cập nhật thông tin sách
     */
    public boolean updateBook(Book book) {
        try {
            Bson filter = Filters.eq("bookId", book.getBookId());
            book.updateTimestamp();
            
            Document bookDoc = bookToDocument(book);
            booksCollection.replaceOne(filter, bookDoc);
            
            LoggerUtil.info("Cập nhật sách thành công: " + book.getBookId());
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật sách: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa sách
     */
    public boolean deleteBook(String bookId) {
        try {
            // Xóa tất cả bản sao trước
            Bson copyFilter = Filters.eq("bookId", bookId);
            bookCopiesCollection.deleteMany(copyFilter);
            
            // Xóa sách
            Bson bookFilter = Filters.eq("bookId", bookId);
            booksCollection.deleteOne(bookFilter);
            
            LoggerUtil.info("Xóa sách thành công: " + bookId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi xóa sách: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy sách theo ID
     */
    public Book getBookById(String bookId) {
        try {
            Bson filter = Filters.eq("bookId", bookId);
            Document bookDoc = booksCollection.find(filter).first();
            
            return bookDoc != null ? documentToBook(bookDoc) : null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy sách by id: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy sách theo ISBN
     */
    public Book getBookByISBN(String isbn) {
        try {
            Bson filter = Filters.eq("isbn", isbn);
            Document bookDoc = booksCollection.find(filter).first();
            
            return bookDoc != null ? documentToBook(bookDoc) : null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy sách by ISBN: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy danh sách sách với phân trang
     */
    public List<Book> getBooks(int page, int size, String category) {
        try {
            List<Book> books = new ArrayList<>();
            Bson filter = category != null ? Filters.eq("category", category) : new Document();
            
            int skip = (page - 1) * size;
            
            for (Document bookDoc : booksCollection.find(filter).skip(skip).limit(size)) {
                books.add(documentToBook(bookDoc));
            }
            
            return books;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy danh sách sách: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Tìm kiếm sách
     */
    public List<Book> searchBooks(String query) {
        try {
            List<Book> books = new ArrayList<>();
            
            // Text search
            Bson textFilter = Filters.text(query, new TextSearchOptions().caseSensitive(false));
            
            for (Document bookDoc : booksCollection.find(textFilter)) {
                books.add(documentToBook(bookDoc));
            }
            
            return books;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tìm kiếm sách: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy tất cả categories
     */
    public List<String> getCategories() {
        try {
            List<String> categories = new ArrayList<>();
            
            for (String category : booksCollection.distinct("category", String.class)) {
                if (category != null) {
                    categories.add(category);
                }
            }
            
            return categories;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy categories: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Thêm bản sao sách
     */
    public boolean addBookCopy(String bookId, BookCopy copy) {
        try {
            // Kiểm tra sách tồn tại
            Book book = getBookById(bookId);
            if (book == null) {
                LoggerUtil.warn("Sách không tồn tại: " + bookId);
                return false;
            }
            
            // Tạo copyId nếu chưa có
            if (copy.getCopyId() == null) {
                copy.setCopyId(generateCopyId());
            }
            
            copy.setBookId(bookId);
            
            // Lưu bản sao
            Document copyDoc = bookCopyToDocument(copy);
            bookCopiesCollection.insertOne(copyDoc);
            
            // Cập nhật số lượng sách
            updateBookCopyCount(bookId);
            
            LoggerUtil.info("Thêm bản sao thành công: " + copy.getCopyId());
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi thêm bản sao: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Xóa bản sao sách
     */
    public boolean deleteBookCopy(String copyId) {
        try {
            // Lấy thông tin bản sao
            BookCopy copy = getBookCopyById(copyId);
            if (copy == null) {
                LoggerUtil.warn("Bản sao không tồn tại: " + copyId);
                return false;
            }
            
            // Xóa bản sao
            Bson filter = Filters.eq("copyId", copyId);
            bookCopiesCollection.deleteOne(filter);
            
            // Cập nhật số lượng sách
            updateBookCopyCount(copy.getBookId());
            
            LoggerUtil.info("Xóa bản sao thành công: " + copyId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi xóa bản sao: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy bản sao theo ID
     */
    public BookCopy getBookCopyById(String copyId) {
        try {
            Bson filter = Filters.eq("copyId", copyId);
            Document copyDoc = bookCopiesCollection.find(filter).first();
            
            return copyDoc != null ? documentToBookCopy(copyDoc) : null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy bản sao by id: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy bản sao có sẵn của sách
     */
    public List<BookCopy> getAvailableCopies(String bookId) {
        try {
            List<BookCopy> copies = new ArrayList<>();
            
            Bson filter = Filters.and(
                Filters.eq("bookId", bookId),
                Filters.eq("status", "AVAILABLE")
            );
            
            for (Document copyDoc : bookCopiesCollection.find(filter)) {
                copies.add(documentToBookCopy(copyDoc));
            }
            
            return copies;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy bản sao có sẵn: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Cập nhật trạng thái bản sao
     */
    public boolean updateBookCopyStatus(String copyId, BookCopy.Status status) {
        try {
            Bson filter = Filters.eq("copyId", copyId);
            Bson update = Updates.set("status", status.name());
            
            bookCopiesCollection.updateOne(filter, update);
            
            LoggerUtil.info("Cập nhật trạng thái bản sao thành công: " + copyId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật trạng thái bản sao: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy danh sách bản sao của một sách
     */
    public List<BookCopy> getBookCopies(String bookId) {
        try {
            List<BookCopy> copies = new ArrayList<>();
            
            Bson filter = Filters.eq("bookId", bookId);
            
            for (Document copyDoc : bookCopiesCollection.find(filter)) {
                copies.add(documentToBookCopy(copyDoc));
            }
            
            return copies;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy danh sách bản sao: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Cập nhật số lượng bản sao của sách
     */
    public void updateBookCopyCount(String bookId) {
        try {
            // Đếm tổng số bản sao
            long totalCopies = bookCopiesCollection.countDocuments(Filters.eq("bookId", bookId));
            
            // Đếm số bản sao có sẵn
            long availableCopies = bookCopiesCollection.countDocuments(
                Filters.and(
                    Filters.eq("bookId", bookId),
                    Filters.eq("status", "AVAILABLE")
                )
            );
            
            // Cập nhật sách
            Bson filter = Filters.eq("bookId", bookId);
            Bson update = Updates.combine(
                Updates.set("totalCopies", (int) totalCopies),
                Updates.set("availableCopies", (int) availableCopies)
            );
            
            booksCollection.updateOne(filter, update);
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật số lượng bản sao: " + e.getMessage());
        }
    }
    
    
    /**
     * Chuyển đổi Document thành Book
     */
    private Book documentToBook(Document doc) {
        Book book = new Book();
        book.setBookId(doc.getString("bookId"));
        book.setTitle(doc.getString("title"));
        book.setAuthor(doc.getString("author"));
        book.setIsbn(doc.getString("isbn"));
        book.setPublisher(doc.getString("publisher"));
        book.setPublicationYear(doc.getInteger("publicationYear", 0));
        book.setCategory(doc.getString("category"));
        book.setDescription(doc.getString("description"));
        book.setLanguage(doc.getString("language"));
        book.setPageCount(doc.getInteger("pageCount", 0));
        book.setPrice(doc.getDouble("price") != null ? doc.getDouble("price") : 0.0);
        book.setCoverImage(doc.getString("coverImage"));
        book.setTotalCopies(doc.getInteger("totalCopies", 0));
        book.setAvailableCopies(doc.getInteger("availableCopies", 0));
        
        // Handle dates
        if (doc.getDate("createdAt") != null) {
            book.setCreatedAt(doc.getDate("createdAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("updatedAt") != null) {
            book.setUpdatedAt(doc.getDate("updatedAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        
        return book;
    }
    
    /**
     * Chuyển đổi Book thành Document
     */    private Document bookToDocument(Book book) {
        Document doc = new Document()
            .append("bookId", book.getBookId())
            .append("title", book.getTitle())
            .append("author", book.getAuthor())
            .append("isbn", book.getIsbn())
            .append("publisher", book.getPublisher())
            .append("publicationYear", book.getPublicationYear())
            .append("category", book.getCategory())
            .append("description", book.getDescription())
            .append("pageCount", book.getPageCount())
            .append("price", book.getPrice())
            .append("coverImage", book.getCoverImage())
            .append("totalCopies", book.getTotalCopies())
            .append("availableCopies", book.getAvailableCopies());
        
        // Only include language if it's not null and not empty
        if (book.getLanguage() != null && !book.getLanguage().trim().isEmpty()) {
            doc.append("language", book.getLanguage());
        }
        
        // Handle dates
        if (book.getCreatedAt() != null) {
            doc.append("createdAt", java.sql.Date.valueOf(book.getCreatedAt()));
        }
        if (book.getUpdatedAt() != null) {
            doc.append("updatedAt", java.sql.Date.valueOf(book.getUpdatedAt()));
        }
        
        return doc;
    }
    
    /**
     * Chuyển đổi Document thành BookCopy
     */
    private BookCopy documentToBookCopy(Document doc) {
        BookCopy copy = new BookCopy();
        copy.setCopyId(doc.getString("copyId"));
        copy.setBookId(doc.getString("bookId"));
        
        // Safe status conversion
        String statusStr = doc.getString("status");
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                copy.setStatus(BookCopy.Status.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                copy.setStatus(BookCopy.Status.AVAILABLE); // Default
            }
        } else {
            copy.setStatus(BookCopy.Status.AVAILABLE); // Default
        }
        
        copy.setLocation(doc.getString("location"));
        copy.setShelf(doc.getString("shelf"));
        copy.setPurchasePrice(doc.getDouble("purchasePrice") != null ? doc.getDouble("purchasePrice") : 0.0);
        
        // Safe condition conversion
        String conditionStr = doc.getString("condition");
        if (conditionStr != null && !conditionStr.isEmpty()) {
            try {
                copy.setCondition(BookCopy.Condition.valueOf(conditionStr));
            } catch (IllegalArgumentException e) {
                copy.setCondition(BookCopy.Condition.GOOD); // Default
            }
        } else {
            copy.setCondition(BookCopy.Condition.GOOD); // Default
        }
        
        copy.setNotes(doc.getString("notes"));
        
        // Handle dates
        if (doc.getDate("purchaseDate") != null) {
            copy.setPurchaseDate(doc.getDate("purchaseDate").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("createdAt") != null) {
            copy.setCreatedAt(doc.getDate("createdAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("updatedAt") != null) {
            copy.setUpdatedAt(doc.getDate("updatedAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        
        return copy;
    }
    
    /**
     * Chuyển đổi BookCopy thành Document
     */    private Document bookCopyToDocument(BookCopy copy) {
        Document doc = new Document()
            .append("copyId", copy.getCopyId())
            .append("bookId", copy.getBookId())
            .append("status", copy.getStatus().name())
            .append("location", copy.getLocation())
            .append("condition", copy.getCondition().name())
            .append("notes", copy.getNotes());
        
        // Handle dates
        if (copy.getPurchaseDate() != null) {
            doc.append("purchaseDate", java.sql.Date.valueOf(copy.getPurchaseDate()));
        }
        if (copy.getCreatedAt() != null) {
            doc.append("createdAt", java.sql.Date.valueOf(copy.getCreatedAt()));
        }
        if (copy.getUpdatedAt() != null) {
            doc.append("updatedAt", java.sql.Date.valueOf(copy.getUpdatedAt()));
        }
        
        return doc;
    }
    
    /**
     * Tạo bookId mới
     */
    private String generateBookId() {
        return "book_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Tạo copyId mới
     */
    private String generateCopyId() {
        return "copy_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Lấy tất cả sách với phân trang
     */
    public List<Book> getAllBooks(int page, int size) {
        try {
            List<Book> books = new ArrayList<>();
            int skip = page * size;
            
            for (Document doc : booksCollection.find().skip(skip).limit(size)) {
                books.add(documentToBook(doc));
            }
            
            return books;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy danh sách sách: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Cập nhật bản sao sách
     */
    public boolean updateBookCopy(BookCopy copy) {
        try {
            Bson filter = Filters.eq("copyId", copy.getCopyId());
            Document doc = bookCopyToDocument(copy);
            
            bookCopiesCollection.replaceOne(filter, doc);
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi cập nhật bản sao sách: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy tổng số sách
     */
    public int getTotalBooks() {
        try {
            return (int) booksCollection.countDocuments();
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm tổng số sách: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Lấy số sách có sẵn
     */
    public int getAvailableBooks() {
        try {
            return (int) bookCopiesCollection.countDocuments(Filters.eq("status", "AVAILABLE"));
        } catch (Exception e) {
            LoggerUtil.error("Lỗi đếm sách có sẵn: " + e.getMessage());
            return 0;
        }
    }
}
