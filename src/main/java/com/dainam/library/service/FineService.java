package com.dainam.library.service;

import com.dainam.library.config.DatabaseConfig;
import com.dainam.library.model.Fine;
import com.dainam.library.util.LoggerUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
// import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class cho quản lý phạt
 */
public class FineService {
    
    private final MongoCollection<Document> finesCollection;
    
    public FineService() {
        this.finesCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_FINES);
    }
    
    /**
     * Tạo phạt quá hạn
     */
    public Fine createOverdueFine(String userId, String recordId, String bookId, String copyId, double amount) {
        try {
            Fine fine = new Fine();
            fine.setFineId(generateFineId());
            fine.setUserId(userId);
            fine.setRecordId(recordId);
            fine.setBookId(bookId);
            fine.setCopyId(copyId);
            fine.setType(Fine.Type.OVERDUE);
            fine.setAmount(amount);
            fine.setDescription("Phạt quá hạn trả sách");
            
            Document fineDoc = fineToDocument(fine);
            finesCollection.insertOne(fineDoc);
            
            LoggerUtil.info("Tạo phạt quá hạn thành công: " + fine.getFineId());
            return fine;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo phạt quá hạn: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo phạt mất sách
     */
    public Fine createLostBookFine(String userId, String recordId, String bookId, String copyId, double bookPrice) {
        try {
            Fine fine = new Fine();
            fine.setFineId(generateFineId());
            fine.setUserId(userId);
            fine.setRecordId(recordId);
            fine.setBookId(bookId);
            fine.setCopyId(copyId);
            fine.setType(Fine.Type.LOST);
            fine.setAmount(bookPrice);
            fine.setDescription("Phạt mất sách");
            
            Document fineDoc = fineToDocument(fine);
            finesCollection.insertOne(fineDoc);
            
            LoggerUtil.info("Tạo phạt mất sách thành công: " + fine.getFineId());
            return fine;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo phạt mất sách: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tạo phạt hỏng sách
     */
    public Fine createDamagedBookFine(String userId, String recordId, String bookId, String copyId, double damageAmount) {
        try {
            Fine fine = new Fine();
            fine.setFineId(generateFineId());
            fine.setUserId(userId);
            fine.setRecordId(recordId);
            fine.setBookId(bookId);
            fine.setCopyId(copyId);
            fine.setType(Fine.Type.DAMAGED);
            fine.setAmount(damageAmount);
            fine.setDescription("Phạt hỏng sách");
            
            Document fineDoc = fineToDocument(fine);
            finesCollection.insertOne(fineDoc);
            
            LoggerUtil.info("Tạo phạt hỏng sách thành công: " + fine.getFineId());
            return fine;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tạo phạt hỏng sách: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lấy phạt theo ID
     */
    public Fine getFineById(String fineId) {
        try {
            Bson filter = Filters.eq("fineId", fineId);
            Document fineDoc = finesCollection.find(filter).first();
            
            return fineDoc != null ? documentToFine(fineDoc) : null;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy phạt by id: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Thêm phạt mới
     */
    public boolean addFine(Fine fine) {
        try {
            if (fine.getFineId() == null || fine.getFineId().isEmpty()) {
                fine.setFineId("fine_" + UUID.randomUUID().toString().substring(0, 8));
            }
            
            Document doc = fineToDocument(fine);
            finesCollection.insertOne(doc);
            
            LoggerUtil.info("Thêm phạt thành công: " + fine.getFineId());
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi thêm phạt: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Lấy tổng số phạt
     */
    public double getTotalFines() {
        try {
            double total = 0.0;
            for (Document doc : finesCollection.find()) {
                total += doc.getDouble("amount") != null ? doc.getDouble("amount") : 0.0;
            }
            return total;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tính tổng phạt: " + e.getMessage());
            return 0.0;
        }
    }
    
    
    /**
     * Lấy phạt của user
     */
    public List<Fine> getFinesByUser(String userId) {
        try {
            List<Fine> fines = new ArrayList<>();
            
            Bson filter = Filters.eq("userId", userId);
            Bson sort = Sorts.descending("createdAt");
            
            for (Document fineDoc : finesCollection.find(filter).sort(sort)) {
                fines.add(documentToFine(fineDoc));
            }
            
            return fines;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy phạt của user: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy phạt chưa thanh toán của user
     */
    public List<Fine> getUnpaidFinesByUser(String userId) {
        try {
            List<Fine> fines = new ArrayList<>();
            
            Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("status", "PENDING")
            );
            
            for (Document fineDoc : finesCollection.find(filter)) {
                fines.add(documentToFine(fineDoc));
            }
            
            return fines;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy phạt chưa thanh toán: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy tất cả phạt (admin)
     */
    public List<Fine> getAllFines() {
        try {
            List<Fine> fines = new ArrayList<>();
            
            Bson sort = Sorts.descending("createdAt");
            
            for (Document fineDoc : finesCollection.find().sort(sort)) {
                fines.add(documentToFine(fineDoc));
            }
            
            return fines;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy tất cả phạt: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy phạt theo trạng thái
     */
    public List<Fine> getFinesByStatus(Fine.Status status) {
        try {
            List<Fine> fines = new ArrayList<>();
            
            Bson filter = Filters.eq("status", status.name());
            Bson sort = Sorts.descending("createdAt");
            
            for (Document fineDoc : finesCollection.find(filter).sort(sort)) {
                fines.add(documentToFine(fineDoc));
            }
            
            return fines;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy phạt theo trạng thái: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Thanh toán phạt
     */
    public boolean payFine(String fineId, String paymentMethod, String paymentReference) {
        try {
            Fine fine = getFineById(fineId);
            if (fine == null) {
                LoggerUtil.warn("Phạt không tồn tại: " + fineId);
                return false;
            }
            
            if (fine.isPaid()) {
                LoggerUtil.warn("Phạt đã được thanh toán: " + fineId);
                return false;
            }
            
            fine.markAsPaid(paymentMethod, paymentReference);
            
            Document fineDoc = fineToDocument(fine);
            Bson filter = Filters.eq("fineId", fineId);
            finesCollection.replaceOne(filter, fineDoc);
            
            LoggerUtil.info("Thanh toán phạt thành công: " + fineId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi thanh toán phạt: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Miễn phạt
     */
    public boolean waiveFine(String fineId, String waivedBy, String waivedReason) {
        try {
            Fine fine = getFineById(fineId);
            if (fine == null) {
                LoggerUtil.warn("Phạt không tồn tại: " + fineId);
                return false;
            }
            
            if (fine.isPaid()) {
                LoggerUtil.warn("Phạt đã được thanh toán: " + fineId);
                return false;
            }
            
            fine.waive(waivedBy, waivedReason);
            
            Document fineDoc = fineToDocument(fine);
            Bson filter = Filters.eq("fineId", fineId);
            finesCollection.replaceOne(filter, fineDoc);
            
            LoggerUtil.info("Miễn phạt thành công: " + fineId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi miễn phạt: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Hủy phạt
     */
    public boolean cancelFine(String fineId) {
        try {
            Fine fine = getFineById(fineId);
            if (fine == null) {
                LoggerUtil.warn("Phạt không tồn tại: " + fineId);
                return false;
            }
            
            if (fine.isPaid()) {
                LoggerUtil.warn("Phạt đã được thanh toán: " + fineId);
                return false;
            }
            
            fine.cancel();
            
            Document fineDoc = fineToDocument(fine);
            Bson filter = Filters.eq("fineId", fineId);
            finesCollection.replaceOne(filter, fineDoc);
            
            LoggerUtil.info("Hủy phạt thành công: " + fineId);
            return true;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi hủy phạt: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Tính tổng phạt của user
     */
    public double getTotalFinesByUser(String userId) {
        try {
            List<Fine> unpaidFines = getUnpaidFinesByUser(userId);
            return unpaidFines.stream().mapToDouble(Fine::getAmount).sum();
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tính tổng phạt: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Tính tổng doanh thu từ phạt
     */
    public double getTotalRevenue() {
        try {
            List<Fine> paidFines = getFinesByStatus(Fine.Status.PAID);
            return paidFines.stream().mapToDouble(Fine::getAmount).sum();
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi tính tổng doanh thu: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Lấy phạt quá hạn thanh toán
     */
    public List<Fine> getOverdueFines() {
        try {
            List<Fine> fines = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            Bson filter = Filters.and(
                Filters.eq("status", "PENDING"),
                Filters.lt("dueDate", today)
            );
            
            for (Document fineDoc : finesCollection.find(filter)) {
                fines.add(documentToFine(fineDoc));
            }
            
            return fines;
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi lấy phạt quá hạn: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Chuyển đổi Document thành Fine
     */
    private Fine documentToFine(Document doc) {
        Fine fine = new Fine();
        fine.setFineId(doc.getString("fineId"));
        fine.setUserId(doc.getString("userId"));
        fine.setRecordId(doc.getString("recordId"));
        fine.setBookId(doc.getString("bookId"));
        fine.setCopyId(doc.getString("copyId"));
        fine.setType(Fine.Type.valueOf(doc.getString("type")));
        fine.setStatus(Fine.Status.valueOf(doc.getString("status")));
        fine.setAmount(doc.getDouble("amount") != null ? doc.getDouble("amount") : 0.0);
        fine.setDescription(doc.getString("description"));
        fine.setPaymentMethod(doc.getString("paymentMethod"));
        fine.setPaymentReference(doc.getString("paymentReference"));
        fine.setWaivedBy(doc.getString("waivedBy"));
        fine.setWaivedReason(doc.getString("waivedReason"));
        
        // Handle dates
        if (doc.getDate("dueDate") != null) {
            fine.setDueDate(doc.getDate("dueDate").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("paidDate") != null) {
            fine.setPaidDate(doc.getDate("paidDate").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        if (doc.getDate("createdAt") != null) {
            fine.setCreatedAt(doc.getDate("createdAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        if (doc.getDate("updatedAt") != null) {
            fine.setUpdatedAt(doc.getDate("updatedAt").toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return fine;
    }
    
    /**
     * Chuyển đổi Fine thành Document
     */
    private Document fineToDocument(Fine fine) {
        Document doc = new Document()
            .append("fineId", fine.getFineId())
            .append("userId", fine.getUserId())
            .append("recordId", fine.getRecordId())
            .append("bookId", fine.getBookId())
            .append("copyId", fine.getCopyId())
            .append("type", fine.getType().name())
            .append("status", fine.getStatus().name())
            .append("amount", fine.getAmount())
            .append("description", fine.getDescription())
            .append("paymentMethod", fine.getPaymentMethod())
            .append("paymentReference", fine.getPaymentReference())
            .append("waivedBy", fine.getWaivedBy())
            .append("waivedReason", fine.getWaivedReason());
        
        // Handle dates
        if (fine.getDueDate() != null) {
            doc.append("dueDate", java.sql.Date.valueOf(fine.getDueDate()));
        }
        if (fine.getPaidDate() != null) {
            doc.append("paidDate", java.sql.Date.valueOf(fine.getPaidDate()));
        }
        if (fine.getCreatedAt() != null) {
            doc.append("createdAt", java.sql.Timestamp.valueOf(fine.getCreatedAt()));
        }
        if (fine.getUpdatedAt() != null) {
            doc.append("updatedAt", java.sql.Timestamp.valueOf(fine.getUpdatedAt()));
        }
        
        return doc;
    }
    
    /**
     * Tạo fineId mới
     */
    private String generateFineId() {
        return "fine_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
