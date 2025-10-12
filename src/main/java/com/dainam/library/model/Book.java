package com.dainam.library.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class đại diện cho một cuốn sách trong hệ thống
 */
public class Book {
    
    @JsonProperty("bookId")
    private String bookId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("author")
    private String author;
    
    @JsonProperty("isbn")
    private String isbn;
    
    @JsonProperty("publisher")
    private String publisher;
    
    @JsonProperty("publicationYear")
    private int publicationYear;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("pageCount")
    private int pageCount;
    
    @JsonProperty("price")
    private double price;
    
    @JsonProperty("coverImage")
    private String coverImage;
    
    @JsonProperty("totalCopies")
    private int totalCopies;
    
    @JsonProperty("availableCopies")
    private int availableCopies;
    
    @JsonProperty("createdAt")
    private LocalDate createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDate updatedAt;
    
    // Constructors
    public Book() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }
    
    public Book(String bookId, String title, String author, String isbn) {
        this();
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }
    
    // Getters and Setters
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public int getPublicationYear() {
        return publicationYear;
    }
    
    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public int getPageCount() {
        return pageCount;
    }
    
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public int getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public int getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
    
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDate getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Cập nhật thời gian sửa đổi
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDate.now();
    }
    
    /**
     * Kiểm tra xem sách có sẵn để mượn không
     */
    public boolean isAvailable() {
        return availableCopies > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "bookId='" + bookId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", availableCopies=" + availableCopies +
                '}';
    }
}
