package models;

import java.io.Serializable;

/**
 * Model class representing a Book in the library system
 */
public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String author;
    private String category;
    private int publishYear;
    private boolean isAvailable;
    private String borrowedBy;
    private String borrowDate;
    
    public Book() {}
    
    public Book(String id, String title, String author, String category, int publishYear) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publishYear = publishYear;
        this.isAvailable = true;
        this.borrowedBy = null;
        this.borrowDate = null;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public int getPublishYear() { return publishYear; }
    public void setPublishYear(int publishYear) { this.publishYear = publishYear; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    public String getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy; }
    
    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    
    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", publishYear=" + publishYear +
                ", isAvailable=" + isAvailable +
                ", borrowedBy='" + borrowedBy + '\'' +
                ", borrowDate='" + borrowDate + '\'' +
                '}';
    }
}
