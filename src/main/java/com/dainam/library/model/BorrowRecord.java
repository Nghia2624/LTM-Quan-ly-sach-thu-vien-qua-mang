package com.dainam.library.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class đại diện cho bản ghi mượn/trả sách
 */
public class BorrowRecord {
    
    public enum Status {
        BORROWED("Đang mượn"),
        RETURNED("Đã trả"),
        OVERDUE("Quá hạn"),
        LOST("Bị mất"),
        DAMAGED("Bị hỏng");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @JsonProperty("recordId")
    private String recordId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("bookId")
    private String bookId;
    
    @JsonProperty("copyId")
    private String copyId;
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("borrowDate")
    private LocalDate borrowDate;
    
    @JsonProperty("expectedReturnDate")
    private LocalDate expectedReturnDate;
    
    @JsonProperty("actualReturnDate")
    private LocalDate actualReturnDate;
    
    @JsonProperty("borrowNotes")
    private String borrowNotes;
    
    @JsonProperty("returnNotes")
    private String returnNotes;
    
    @JsonProperty("fineAmount")
    private double fineAmount;
    
    @JsonProperty("finePaid")
    private boolean finePaid;
    
    @JsonProperty("extended")
    private boolean extended;
    
    @JsonProperty("createdAt")
    private LocalDate createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDate updatedAt;
    
    // Constructors
    public BorrowRecord() {
        this.status = Status.BORROWED;
        this.borrowDate = LocalDate.now();
        this.expectedReturnDate = LocalDate.now().plusDays(14); // Mượn 14 ngày
        this.fineAmount = 0.0;
        this.finePaid = false;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }
    
    public BorrowRecord(String recordId, String userId, String bookId, String copyId) {
        this();
        this.recordId = recordId;
        this.userId = userId;
        this.bookId = bookId;
        this.copyId = copyId;
    }
    
    // Getters and Setters
    public String getRecordId() {
        return recordId;
    }
    
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getCopyId() {
        return copyId;
    }
    
    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDate.now();
    }
    
    public LocalDate getBorrowDate() {
        return borrowDate;
    }
    
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }
    
    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }
    
    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }
    
    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }
    
    public String getBorrowNotes() {
        return borrowNotes;
    }
    
    public void setBorrowNotes(String borrowNotes) {
        this.borrowNotes = borrowNotes;
    }
    
    public String getReturnNotes() {
        return returnNotes;
    }
    
    public void setReturnNotes(String returnNotes) {
        this.returnNotes = returnNotes;
    }
    
    public double getFineAmount() {
        return fineAmount;
    }
    
    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }
    
    public boolean isFinePaid() {
        return finePaid;
    }
    
    public void setFinePaid(boolean finePaid) {
        this.finePaid = finePaid;
    }
    
    public boolean isExtended() {
        return extended;
    }
    
    public void setExtended(boolean extended) {
        this.extended = extended;
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
     * Kiểm tra xem có quá hạn không
     */
    public boolean isOverdue() {
        return status == Status.BORROWED && LocalDate.now().isAfter(expectedReturnDate);
    }
    
    /**
     * Tính số ngày quá hạn
     */
    public long getOverdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        return LocalDate.now().toEpochDay() - expectedReturnDate.toEpochDay();
    }
    
    /**
     * Tính phạt quá hạn (1000 VND/ngày)
     */
    public double calculateOverdueFine() {
        long overdueDays = getOverdueDays();
        return overdueDays * 1000.0;
    }
    
    /**
     * Đánh dấu là đã trả
     */
    public void markAsReturned() {
        this.status = Status.RETURNED;
        this.actualReturnDate = LocalDate.now();
        updateTimestamp();
    }
    
    /**
     * Đánh dấu là quá hạn
     */
    public void markAsOverdue() {
        this.status = Status.OVERDUE;
        this.fineAmount = calculateOverdueFine();
        updateTimestamp();
    }
    
    /**
     * Đánh dấu là bị mất
     */
    public void markAsLost() {
        this.status = Status.LOST;
        this.actualReturnDate = LocalDate.now();
        updateTimestamp();
    }
    
    /**
     * Đánh dấu là bị hỏng
     */
    public void markAsDamaged() {
        this.status = Status.DAMAGED;
        this.actualReturnDate = LocalDate.now();
        updateTimestamp();
    }
    
    /**
     * Kiểm tra xem có thể trả sách không
     */
    public boolean canReturn() {
        return status == Status.BORROWED || status == Status.OVERDUE;
    }
    
    /**
     * Lấy số ngày còn lại trước khi hết hạn
     */
    public long getDaysUntilDue() {
        if (status != Status.BORROWED) {
            return 0;
        }
        return expectedReturnDate.toEpochDay() - LocalDate.now().toEpochDay();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowRecord that = (BorrowRecord) o;
        return Objects.equals(recordId, that.recordId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }
    
    @Override
    public String toString() {
        return "BorrowRecord{" +
                "recordId='" + recordId + '\'' +
                ", userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", copyId='" + copyId + '\'' +
                ", status=" + status +
                ", borrowDate=" + borrowDate +
                ", expectedReturnDate=" + expectedReturnDate +
                '}';
    }
}
