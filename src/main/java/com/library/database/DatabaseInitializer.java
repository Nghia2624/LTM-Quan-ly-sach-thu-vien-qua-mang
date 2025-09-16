package com.library.database;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

import java.util.List;
import java.util.ArrayList;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        System.out.println("🔧 Initializing Library Management Database...");
        
        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            
            // Initialize collections with proper indexes
            initializeBooksCollection(database);
            initializeUsersCollection(database);
            initializeBorrowRecordsCollection(database);
            
            System.out.println("✅ Database initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private static void initializeBooksCollection(MongoDatabase database) {
        System.out.println("📚 Initializing books collection...");
        
        MongoCollection<Document> books = database.getCollection("books");
        
        // Create indexes for better performance
        try {
            // Unique index on ISBN
            books.createIndex(Indexes.ascending("isbn"), 
                new IndexOptions().unique(true));
            
            // Text index for search functionality
            books.createIndex(Indexes.compoundIndex(
                Indexes.text("title"),
                Indexes.text("author"),
                Indexes.text("category"),
                Indexes.text("publisher"),
                Indexes.text("description")
            ));
            
            // Index on category and publisher for filtering
            books.createIndex(Indexes.ascending("category"));
            books.createIndex(Indexes.ascending("publisher"));
            books.createIndex(Indexes.ascending("author"));
            
            System.out.println("  ✓ Books collection indexes created");
            
        } catch (Exception e) {
            System.out.println("  ⚠ Books indexes already exist or creation failed: " + e.getMessage());
        }
    }
    
    private static void initializeUsersCollection(MongoDatabase database) {
        System.out.println("👥 Initializing users collection...");
        
        MongoCollection<Document> users = database.getCollection("users");
        
        try {
            // Unique index on email
            users.createIndex(Indexes.ascending("email"), 
                new IndexOptions().unique(true));
            
            System.out.println("  ✓ Users collection indexes created");
            
        } catch (Exception e) {
            System.out.println("  ⚠ Users indexes already exist or creation failed: " + e.getMessage());
        }
    }
    
    private static void initializeBorrowRecordsCollection(MongoDatabase database) {
        System.out.println("📖 Initializing borrow records collection...");
        
        MongoCollection<Document> borrowRecords = database.getCollection("borrowRecords");
        
        try {
            // Compound indexes for efficient queries
            borrowRecords.createIndex(Indexes.ascending("bookId"));
            borrowRecords.createIndex(Indexes.ascending("borrowerEmail"));
            borrowRecords.createIndex(Indexes.ascending("status"));
            borrowRecords.createIndex(Indexes.ascending("expectedReturnDate"));
            
            // Compound index for overdue books query
            borrowRecords.createIndex(Indexes.compoundIndex(
                Indexes.ascending("status"),
                Indexes.ascending("expectedReturnDate")
            ));
            
            System.out.println("  ✓ Borrow records collection indexes created");
            
        } catch (Exception e) {
            System.out.println("  ⚠ Borrow records indexes already exist or creation failed: " + e.getMessage());
        }
    }
    
    public static void checkDataConsistency() {
        System.out.println("🔍 Checking data consistency...");
        
        try {
            BookDAO bookDAO = new BookDAO();
            BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
            
            List<Book> books = bookDAO.getAllBooks();
            List<BorrowRecord> borrowRecords = borrowRecordDAO.getAllBorrowRecords();
            
            List<String> issues = new ArrayList<>();
            
            // Check for books with negative available copies
            for (Book book : books) {
                if (book.getAvailableCopies() < 0) {
                    issues.add("Book '" + book.getTitle() + "' has negative available copies: " + book.getAvailableCopies());
                }
                if (book.getAvailableCopies() > book.getTotalCopies()) {
                    issues.add("Book '" + book.getTitle() + "' has more available copies than total copies");
                }
            }
            
            // Check for borrow records with invalid book references
            for (BorrowRecord record : borrowRecords) {
                boolean bookExists = books.stream().anyMatch(book -> book.getId().equals(record.getBookId()));
                if (!bookExists) {
                    issues.add("Borrow record references non-existent book ID: " + record.getBookId());
                }
            }
            
            // Check for consistency between borrowed books and available copies
            for (Book book : books) {
                long activeBorrowCount = borrowRecords.stream()
                    .filter(record -> record.getBookId().equals(book.getId()))
                    .filter(record -> record.getStatus() == BorrowRecord.BorrowStatus.BORROWED)
                    .count();
                
                int expectedAvailable = book.getTotalCopies() - (int)activeBorrowCount;
                if (book.getAvailableCopies() != expectedAvailable) {
                    issues.add("Book '" + book.getTitle() + "' availability mismatch. " +
                        "Available: " + book.getAvailableCopies() + ", Expected: " + expectedAvailable);
                }
            }
            
            if (issues.isEmpty()) {
                System.out.println("✅ Data consistency check passed - no issues found");
            } else {
                System.out.println("⚠️  Data consistency issues found:");
                for (String issue : issues) {
                    System.out.println("   • " + issue);
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Data consistency check failed: " + e.getMessage());
        }
    }
    
    public static void fixDataConsistency() {
        System.out.println("🔧 Attempting to fix data consistency issues...");
        
        try {
            BookDAO bookDAO = new BookDAO();
            BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
            
            List<Book> books = bookDAO.getAllBooks();
            List<BorrowRecord> borrowRecords = borrowRecordDAO.getAllBorrowRecords();
            
            int fixedBooks = 0;
            
            // Fix book availability based on actual borrow records
            for (Book book : books) {
                long activeBorrowCount = borrowRecords.stream()
                    .filter(record -> record.getBookId().equals(book.getId()))
                    .filter(record -> record.getStatus() == BorrowRecord.BorrowStatus.BORROWED)
                    .count();
                
                int correctAvailable = book.getTotalCopies() - (int)activeBorrowCount;
                correctAvailable = Math.max(0, correctAvailable); // Ensure non-negative
                
                if (book.getAvailableCopies() != correctAvailable) {
                    System.out.println("  🔧 Fixing book '" + book.getTitle() + "': " + 
                        book.getAvailableCopies() + " → " + correctAvailable);
                    
                    if (bookDAO.updateAvailableCopies(book.getId(), correctAvailable)) {
                        fixedBooks++;
                    }
                }
            }
            
            System.out.println("✅ Data consistency fix completed. Fixed " + fixedBooks + " books.");
            
        } catch (Exception e) {
            System.err.println("❌ Data consistency fix failed: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Library Database Initializer ===");
            
            if (!DatabaseConnection.testConnection()) {
                System.err.println("❌ Cannot connect to MongoDB. Please ensure MongoDB is running.");
                return;
            }
            
            String action = args.length > 0 ? args[0] : "init";
            
            switch (action.toLowerCase()) {
                case "init":
                    initializeDatabase();
                    break;
                case "check":
                    checkDataConsistency();
                    break;
                case "fix":
                    checkDataConsistency();
                    fixDataConsistency();
                    checkDataConsistency();
                    break;
                case "all":
                    initializeDatabase();
                    checkDataConsistency();
                    break;
                default:
                    System.out.println("Usage: DatabaseInitializer [init|check|fix|all]");
                    System.out.println("  init - Initialize database with indexes");
                    System.out.println("  check - Check data consistency");  
                    System.out.println("  fix - Check and fix data consistency");
                    System.out.println("  all - Initialize and check consistency");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Database initializer failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}
