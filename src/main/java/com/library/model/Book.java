package com.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String publisher;
    private int publishYear;
    private int totalCopies;
    private int availableCopies;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Book() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Book(String title, String author, String isbn, String category, 
                String publisher, int publishYear, int totalCopies, String description) {
        this();
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { 
        this.author = author;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { 
        this.isbn = isbn;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { 
        this.publisher = publisher;
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getPublishYear() { return publishYear; }
    public void setPublishYear(int publishYear) { 
        this.publishYear = publishYear;
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { 
        this.totalCopies = totalCopies;
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { 
        this.availableCopies = availableCopies;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id) || Objects.equals(isbn, book.isbn);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, isbn);
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (%s)", title, author, isbn);
    }
}
