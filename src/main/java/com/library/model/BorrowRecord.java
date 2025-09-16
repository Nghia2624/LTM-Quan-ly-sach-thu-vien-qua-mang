package com.library.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class BorrowRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String bookId;
    private String bookTitle;
    private String borrowerName;
    private String borrowerEmail;
    private String borrowerPhone;
    private LocalDateTime borrowDate;
    private LocalDateTime expectedReturnDate;
    private LocalDateTime actualReturnDate;
    private BorrowStatus status;
    private String notes;
      public enum BorrowStatus implements Serializable {
        BORROWED, RETURNED, OVERDUE
    }
    
    // Constructors
    public BorrowRecord() {
        this.borrowDate = LocalDateTime.now();
        this.status = BorrowStatus.BORROWED;
    }
    
    public BorrowRecord(String bookId, String bookTitle, String borrowerName, 
                       String borrowerEmail, String borrowerPhone, 
                       LocalDateTime expectedReturnDate) {
        this();
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowerName = borrowerName;
        this.borrowerEmail = borrowerEmail;
        this.borrowerPhone = borrowerPhone;
        this.expectedReturnDate = expectedReturnDate;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    
    public String getBorrowerEmail() { return borrowerEmail; }
    public void setBorrowerEmail(String borrowerEmail) { this.borrowerEmail = borrowerEmail; }
    
    public String getBorrowerPhone() { return borrowerPhone; }
    public void setBorrowerPhone(String borrowerPhone) { this.borrowerPhone = borrowerPhone; }
    
    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }
    
    public LocalDateTime getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    
    public LocalDateTime getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDateTime actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    
    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public boolean isOverdue() {
        return status == BorrowStatus.BORROWED && 
               expectedReturnDate.isBefore(LocalDateTime.now());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowRecord that = (BorrowRecord) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (%s)", bookTitle, borrowerName, status);
    }
}
