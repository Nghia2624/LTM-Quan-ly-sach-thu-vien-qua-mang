package com.dainam.library.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class đại diện cho phạt trong hệ thống
 */
public class Fine {
    
    public enum Type {
        OVERDUE("Quá hạn"),
        LOST("Mất sách"),
        DAMAGED("Hỏng sách"),
        OTHER("Khác");
        
        private final String displayName;
        
        Type(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Status {
        PENDING("Chờ thanh toán"),
        PAID("Đã thanh toán"),
        UNPAID("Chưa thanh toán"),
        WAIVED("Được miễn"),
        CANCELLED("Đã hủy");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @JsonProperty("fineId")
    private String fineId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("recordId")
    private String recordId;
    
    @JsonProperty("bookId")
    private String bookId;
    
    @JsonProperty("copyId")
    private String copyId;
    
    @JsonProperty("type")
    private Type type;
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("amount")
    private double amount;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("dueDate")
    private LocalDate dueDate;
    
    @JsonProperty("fineDate")
    private LocalDate fineDate;
    
    @JsonProperty("paidDate")
    private LocalDate paidDate;
    
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    @JsonProperty("paymentReference")
    private String paymentReference;
    
    @JsonProperty("waivedBy")
    private String waivedBy;
    
    @JsonProperty("waivedReason")
    private String waivedReason;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Fine() {
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Fine(String fineId, String userId, String recordId, Type type, double amount) {
        this();
        this.fineId = fineId;
        this.userId = userId;
        this.recordId = recordId;
        this.type = type;
        this.amount = amount;
        this.dueDate = LocalDate.now().plusDays(30); // Hạn thanh toán 30 ngày
    }
    
    // Getters and Setters
    public String getFineId() {
        return fineId;
    }
    
    public void setFineId(String fineId) {
        this.fineId = fineId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getRecordId() {
        return recordId;
    }
    
    public void setRecordId(String recordId) {
        this.recordId = recordId;
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
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDate getFineDate() {
        return fineDate;
    }
    
    public void setFineDate(LocalDate fineDate) {
        this.fineDate = fineDate;
    }
    
    public LocalDate getPaidDate() {
        return paidDate;
    }
    
    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentReference() {
        return paymentReference;
    }
    
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    
    public String getWaivedBy() {
        return waivedBy;
    }
    
    public void setWaivedBy(String waivedBy) {
        this.waivedBy = waivedBy;
    }
    
    public String getWaivedReason() {
        return waivedReason;
    }
    
    public void setWaivedReason(String waivedReason) {
        this.waivedReason = waivedReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Cập nhật thời gian sửa đổi
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Kiểm tra xem phạt có quá hạn thanh toán không
     */
    public boolean isOverdue() {
        return status == Status.PENDING && LocalDate.now().isAfter(dueDate);
    }
    
    /**
     * Kiểm tra xem phạt đã được thanh toán chưa
     */
    public boolean isPaid() {
        return status == Status.PAID;
    }
    
    /**
     * Đánh dấu phạt là đã thanh toán
     */
    public void markAsPaid(String paymentMethod, String paymentReference) {
        this.status = Status.PAID;
        this.paidDate = LocalDate.now();
        this.paymentMethod = paymentMethod;
        this.paymentReference = paymentReference;
        updateTimestamp();
    }
    
    /**
     * Miễn phạt
     */
    public void waive(String waivedBy, String waivedReason) {
        this.status = Status.WAIVED;
        this.waivedBy = waivedBy;
        this.waivedReason = waivedReason;
        updateTimestamp();
    }
    
    /**
     * Hủy phạt
     */
    public void cancel() {
        this.status = Status.CANCELLED;
        updateTimestamp();
    }
    
    /**
     * Lấy số ngày còn lại trước khi hết hạn thanh toán
     */
    public long getDaysUntilDue() {
        if (status != Status.PENDING) {
            return 0;
        }
        return dueDate.toEpochDay() - LocalDate.now().toEpochDay();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fine fine = (Fine) o;
        return Objects.equals(fineId, fine.fineId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fineId);
    }
    
    @Override
    public String toString() {
        return "Fine{" +
                "fineId='" + fineId + '\'' +
                ", userId='" + userId + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", amount=" + amount +
                '}';
    }
}
