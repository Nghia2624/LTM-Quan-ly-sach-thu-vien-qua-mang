package com.library.common;

import java.io.Serializable;

public class SearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String publisher;
    private Integer publishYear;
    
    public SearchCriteria() {}
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }
    
    public boolean isEmpty() {
        return (title == null || title.trim().isEmpty()) &&
               (author == null || author.trim().isEmpty()) &&
               (isbn == null || isbn.trim().isEmpty()) &&
               (category == null || category.trim().isEmpty()) &&
               (publisher == null || publisher.trim().isEmpty()) &&
               publishYear == null;
    }
}
