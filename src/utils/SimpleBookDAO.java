package utils;

import models.Book;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe Data Access Object for managing book data without external dependencies
 * Uses simple JSON parsing without Gson to avoid dependency issues
 */
public class SimpleBookDAO {
    private static final String DATA_FILE = "books.json";
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public SimpleBookDAO() {
        // Create file if it doesn't exist
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            saveBooks(new ArrayList<>());
        }
    }
    
    /**
     * Load all books from JSON file (Thread-safe read)
     */
    public List<Book> loadBooks() {
        lock.readLock().lock();
        try {
            List<Book> books = new ArrayList<>();
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                return books;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                
                String jsonStr = json.toString().trim();
                if (!jsonStr.isEmpty()) {
                    books = parseJsonToBooks(jsonStr);
                }
            } catch (IOException e) {
                System.err.println("Error loading books: " + e.getMessage());
            }
            return books;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Save books to JSON file (Thread-safe write)
     */
    public void saveBooks(List<Book> books) {
        lock.writeLock().lock();
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
                writer.write(booksToJson(books));
            } catch (IOException e) {
                System.err.println("Error saving books: " + e.getMessage());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Add a new book (Thread-safe)
     */
    public boolean addBook(Book book) {
        lock.writeLock().lock();
        try {
            List<Book> books = loadBooksUnsafe();
            // Check if book ID already exists
            for (Book existingBook : books) {
                if (existingBook.getId().equals(book.getId())) {
                    return false; // Book ID already exists
                }
            }
            books.add(book);
            saveBooksUnsafe(books);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Update an existing book (Thread-safe)
     */
    public boolean updateBook(Book book) {
        lock.writeLock().lock();
        try {
            List<Book> books = loadBooksUnsafe();
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).getId().equals(book.getId())) {
                    books.set(i, book);
                    saveBooksUnsafe(books);
                    return true;
                }
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Delete a book by ID (Thread-safe)
     */
    public boolean deleteBook(String bookId) {
        lock.writeLock().lock();
        try {
            List<Book> books = loadBooksUnsafe();
            boolean removed = books.removeIf(book -> book.getId().equals(bookId));
            if (removed) {
                saveBooksUnsafe(books);
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Find book by ID
     */
    public Book findBookById(String bookId) {
        List<Book> books = loadBooks();
        return books.stream()
                .filter(book -> book.getId().equals(bookId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Search books by title, author, or category
     */
    public List<Book> searchBooks(String query) {
        List<Book> books = loadBooks();
        List<Book> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(lowerQuery) ||
                book.getAuthor().toLowerCase().contains(lowerQuery) ||
                book.getCategory().toLowerCase().contains(lowerQuery)) {
                results.add(book);
            }
        }
        return results;
    }
    
    // Unsafe methods for internal use (already within lock)
    private List<Book> loadBooksUnsafe() {
        List<Book> books = new ArrayList<>();
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return books;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            
            String jsonStr = json.toString().trim();
            if (!jsonStr.isEmpty()) {
                books = parseJsonToBooks(jsonStr);
            }
        } catch (IOException e) {
            System.err.println("Error loading books: " + e.getMessage());
        }
        return books;
    }
    
    private void saveBooksUnsafe(List<Book> books) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            writer.write(booksToJson(books));
        } catch (IOException e) {
            System.err.println("Error saving books: " + e.getMessage());
        }
    }
    
    // Simple JSON parsing methods
    private List<Book> parseJsonToBooks(String json) {
        List<Book> books = new ArrayList<>();
        try {
            // Remove outer brackets
            json = json.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length() - 1);
            
            if (json.trim().isEmpty()) {
                return books;
            }
            
            // Split by book objects
            List<String> bookStrings = splitJsonObjects(json);
            
            for (String bookStr : bookStrings) {
                Book book = parseJsonToBook(bookStr.trim());
                if (book != null) {
                    books.add(book);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
        return books;
    }
    
    private List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objects.add(json.substring(start, i + 1));
                    start = i + 2; // Skip comma and space
                }
            }
        }
        
        return objects;
    }
    
    private Book parseJsonToBook(String json) {
        try {
            String id = extractJsonValue(json, "id");
            String title = extractJsonValue(json, "title");
            String author = extractJsonValue(json, "author");
            String category = extractJsonValue(json, "category");
            String yearStr = extractJsonValue(json, "publishYear");
            String availableStr = extractJsonValue(json, "isAvailable");
            
            if (id == null || title == null || author == null || category == null || yearStr == null) {
                return null;
            }
            
            int year = Integer.parseInt(yearStr);
            boolean available = Boolean.parseBoolean(availableStr);
            
            Book book = new Book(id, title, author, category, year);
            book.setAvailable(available);
            
            String borrowedBy = extractJsonValue(json, "borrowedBy");
            String borrowDate = extractJsonValue(json, "borrowDate");
            
            if (borrowedBy != null && !borrowedBy.equals("null") && !borrowedBy.isEmpty()) {
                book.setBorrowedBy(borrowedBy);
            }
            if (borrowDate != null && !borrowDate.equals("null") && !borrowDate.isEmpty()) {
                book.setBorrowDate(borrowDate);
            }
            
            return book;
        } catch (Exception e) {
            System.err.println("Error parsing book: " + e.getMessage());
            return null;
        }
    }
    
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        
        if (start >= json.length()) return null;
        
        char startChar = json.charAt(start);
        if (startChar == '"') {
            start++;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && (end == start || json.charAt(end - 1) != '\\')) {
                    break;
                }
                end++;
            }
            return end < json.length() ? json.substring(start, end) : null;
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            return json.substring(start, end).trim();
        }
    }
    
    private String booksToJson(List<Book> books) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < books.size(); i++) {
            if (i > 0) json.append(",");
            json.append(bookToJson(books.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    private String bookToJson(Book book) {
        return String.format(
            "{\"id\":\"%s\",\"title\":\"%s\",\"author\":\"%s\",\"category\":\"%s\",\"publishYear\":%d,\"isAvailable\":%s,\"borrowedBy\":%s,\"borrowDate\":%s}",
            escapeJson(book.getId()),
            escapeJson(book.getTitle()),
            escapeJson(book.getAuthor()),
            escapeJson(book.getCategory()),
            book.getPublishYear(),
            book.isAvailable(),
            book.getBorrowedBy() != null ? "\"" + escapeJson(book.getBorrowedBy()) + "\"" : "null",
            book.getBorrowDate() != null ? "\"" + escapeJson(book.getBorrowDate()) + "\"" : "null"
        );
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
