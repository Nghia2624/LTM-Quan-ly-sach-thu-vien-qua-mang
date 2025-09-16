package com.library.database;

import com.library.model.Book;
import com.library.common.SearchCriteria;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class BookDAO {
    private final MongoCollection<Document> collection;
    
    public BookDAO() {
        MongoDatabase database = DatabaseConnection.getDatabase();
        this.collection = database.getCollection("books");
    }
    
    public boolean addBook(Book book) {
        try {
            Document doc = bookToDocument(book);
            collection.insertOne(doc);
            book.setId(doc.getObjectId("_id").toString());
            return true;
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateBook(Book book) {
        try {
            ObjectId objectId = new ObjectId(book.getId());
            Document doc = bookToDocument(book);
            doc.remove("_id"); // Don't update the ID
            
            collection.replaceOne(Filters.eq("_id", objectId), doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteBook(String bookId) {
        try {
            ObjectId objectId = new ObjectId(bookId);
            collection.deleteOne(Filters.eq("_id", objectId));
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }
    
    public Book getBook(String bookId) {
        try {
            ObjectId objectId = new ObjectId(bookId);
            Document doc = collection.find(Filters.eq("_id", objectId)).first();
            return doc != null ? documentToBook(doc) : null;
        } catch (Exception e) {
            System.err.println("Error getting book: " + e.getMessage());
            return null;
        }
    }
    
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                books.add(documentToBook(doc));
            }
        } catch (Exception e) {
            System.err.println("Error getting all books: " + e.getMessage());
        }
        return books;
    }
    
    public List<Book> searchBooks(SearchCriteria criteria) {
        List<Book> books = new ArrayList<>();
        try {
            List<Bson> filters = new ArrayList<>();
            
            if (criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
                Pattern pattern = Pattern.compile(criteria.getTitle(), Pattern.CASE_INSENSITIVE);
                filters.add(Filters.regex("title", pattern));
            }
            
            if (criteria.getAuthor() != null && !criteria.getAuthor().trim().isEmpty()) {
                Pattern pattern = Pattern.compile(criteria.getAuthor(), Pattern.CASE_INSENSITIVE);
                filters.add(Filters.regex("author", pattern));
            }
            
            if (criteria.getIsbn() != null && !criteria.getIsbn().trim().isEmpty()) {
                filters.add(Filters.eq("isbn", criteria.getIsbn()));
            }
            
            if (criteria.getCategory() != null && !criteria.getCategory().trim().isEmpty()) {
                Pattern pattern = Pattern.compile(criteria.getCategory(), Pattern.CASE_INSENSITIVE);
                filters.add(Filters.regex("category", pattern));
            }
            
            if (criteria.getPublisher() != null && !criteria.getPublisher().trim().isEmpty()) {
                Pattern pattern = Pattern.compile(criteria.getPublisher(), Pattern.CASE_INSENSITIVE);
                filters.add(Filters.regex("publisher", pattern));
            }
            
            if (criteria.getPublishYear() != null) {
                filters.add(Filters.eq("publishYear", criteria.getPublishYear()));
            }
            
            Bson combinedFilter = filters.isEmpty() ? new Document() : Filters.and(filters);
            
            for (Document doc : collection.find(combinedFilter)) {
                books.add(documentToBook(doc));
            }
        } catch (Exception e) {
            System.err.println("Error searching books: " + e.getMessage());
        }
        return books;
    }
    
    public boolean updateAvailableCopies(String bookId, int newAvailableCopies) {
        try {
            ObjectId objectId = new ObjectId(bookId);
            collection.updateOne(
                Filters.eq("_id", objectId),
                Updates.combine(
                    Updates.set("availableCopies", newAvailableCopies),
                    Updates.set("updatedAt", new Date())
                )
            );
            return true;
        } catch (Exception e) {
            System.err.println("Error updating available copies: " + e.getMessage());
            return false;
        }
    }
    
    private Document bookToDocument(Book book) {
        Document doc = new Document();
        
        if (book.getId() != null) {
            doc.append("_id", new ObjectId(book.getId()));
        }
        
        doc.append("title", book.getTitle())
           .append("author", book.getAuthor())
           .append("isbn", book.getIsbn())
           .append("category", book.getCategory())
           .append("publisher", book.getPublisher())
           .append("publishYear", book.getPublishYear())
           .append("totalCopies", book.getTotalCopies())
           .append("availableCopies", book.getAvailableCopies())
           .append("description", book.getDescription())
           .append("createdAt", Date.from(book.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()))
           .append("updatedAt", Date.from(book.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        
        return doc;
    }
    
    private Book documentToBook(Document doc) {
        Book book = new Book();
        
        book.setId(doc.getObjectId("_id").toString());
        book.setTitle(doc.getString("title"));
        book.setAuthor(doc.getString("author"));
        book.setIsbn(doc.getString("isbn"));
        book.setCategory(doc.getString("category"));
        book.setPublisher(doc.getString("publisher"));
        book.setPublishYear(doc.getInteger("publishYear", 0));
        book.setTotalCopies(doc.getInteger("totalCopies", 0));
        book.setAvailableCopies(doc.getInteger("availableCopies", 0));
        book.setDescription(doc.getString("description"));
        
        Date createdAt = doc.getDate("createdAt");
        if (createdAt != null) {
            book.setCreatedAt(LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault()));
        }
        
        Date updatedAt = doc.getDate("updatedAt");
        if (updatedAt != null) {
            book.setUpdatedAt(LocalDateTime.ofInstant(updatedAt.toInstant(), ZoneId.systemDefault()));
        }
        
        return book;
    }
}
