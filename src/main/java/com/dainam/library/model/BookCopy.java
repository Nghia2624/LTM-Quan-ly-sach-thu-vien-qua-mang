package com.dainam.library.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class đại diện cho một bản sao cụ thể của sách
 */
public class BookCopy {
    
    public enum Status {
        AVAILABLE("Có sẵn"),
        BORROWED("Đang mượn"),
        RESERVED("Đã đặt trước"),
        LOST("Bị mất"),
        DAMAGED("Bị hỏng"),
        MAINTENANCE("Bảo trì");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Condition {
        NEW("Mới"),
        GOOD("Tốt"),
        FAIR("Khá"),
        POOR("Trung bình"),
        OLD("Cũ");
        
        private final String displayName;
        
        Condition(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @JsonProperty("copyId")
    private String copyId;
    
    @JsonProperty("bookId")
    private String bookId;
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("shelf")
    private String shelf;
    
    @JsonProperty("purchaseDate")
    private LocalDate purchaseDate;
    
    @JsonProperty("purchasePrice")
    private double purchasePrice;
    
    @JsonProperty("condition")
    private Condition condition;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("createdAt")
    private LocalDate createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDate updatedAt;
    
    // Constructors
    public BookCopy() {
        this.status = Status.AVAILABLE;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }
    
    public BookCopy(String copyId, String bookId) {
        this();
        this.copyId = copyId;
        this.bookId = bookId;
    }
    
    // Getters and Setters
    public String getCopyId() {
        return copyId;
    }
    
    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        updateTimestamp();
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getShelf() {
        return shelf;
    }
    
    public void setShelf(String shelf) {
        this.shelf = shelf;
    }
    
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public Condition getCondition() {
        return condition;
    }
    
    public void setCondition(Condition condition) {
        this.condition = condition;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
     * Kiểm tra xem bản sao có sẵn để mượn không
     */
    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }
    
    /**
     * Đánh dấu bản sao là đang mượn
     */
    public void markAsBorrowed() {
        this.status = Status.BORROWED;
        updateTimestamp();
    }
    
    /**
     * Đánh dấu bản sao là có sẵn
     */
    public void markAsAvailable() {
        this.status = Status.AVAILABLE;
        updateTimestamp();
    }
    
    /**
     * Đánh dấu bản sao là bị mất
     */
    public void markAsLost() {
        this.status = Status.LOST;
        updateTimestamp();
    }
    
    /**
     * Đánh dấu bản sao là bị hỏng
     */
    public void markAsDamaged() {
        this.status = Status.DAMAGED;
        updateTimestamp();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCopy bookCopy = (BookCopy) o;
        return Objects.equals(copyId, bookCopy.copyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(copyId);
    }
    
    @Override
    public String toString() {
        return "BookCopy{" +
                "copyId='" + copyId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", status=" + status +
                ", location='" + location + '\'' +
                '}';
    }
}
