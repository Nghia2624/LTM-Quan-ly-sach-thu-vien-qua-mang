package com.library.database;

import com.library.model.BorrowRecord;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BorrowRecordDAO {
    private final MongoCollection<Document> collection;
    
    public BorrowRecordDAO() {
        MongoDatabase database = DatabaseConnection.getDatabase();
        this.collection = database.getCollection("borrow_records");
    }
    
    public boolean addBorrowRecord(BorrowRecord record) {
        try {
            Document doc = recordToDocument(record);
            collection.insertOne(doc);
            record.setId(doc.getObjectId("_id").toString());
            return true;
        } catch (Exception e) {
            System.err.println("Error adding borrow record: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateBorrowRecord(BorrowRecord record) {
        try {
            ObjectId objectId = new ObjectId(record.getId());
            Document doc = recordToDocument(record);
            doc.remove("_id");
            
            collection.replaceOne(Filters.eq("_id", objectId), doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating borrow record: " + e.getMessage());
            return false;
        }
    }
    
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        try {
            for (Document doc : collection.find().sort(Sorts.descending("borrowDate"))) {
                records.add(documentToRecord(doc));
            }
        } catch (Exception e) {
            System.err.println("Error getting all borrow records: " + e.getMessage());
        }
        return records;
    }
    
    public List<BorrowRecord> getBorrowedBooks() {
        List<BorrowRecord> records = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.eq("status", "BORROWED"))
                    .sort(Sorts.descending("borrowDate"))) {
                records.add(documentToRecord(doc));
            }
        } catch (Exception e) {
            System.err.println("Error getting borrowed books: " + e.getMessage());
        }
        return records;
    }
    
    public List<BorrowRecord> getOverdueBooks() {
        List<BorrowRecord> records = new ArrayList<>();
        try {
            Date now = new Date();
            for (Document doc : collection.find(Filters.and(
                    Filters.eq("status", "BORROWED"),
                    Filters.lt("expectedReturnDate", now)
            )).sort(Sorts.ascending("expectedReturnDate"))) {
                BorrowRecord record = documentToRecord(doc);
                record.setStatus(BorrowRecord.BorrowStatus.OVERDUE);
                records.add(record);
            }
        } catch (Exception e) {
            System.err.println("Error getting overdue books: " + e.getMessage());
        }
        return records;
    }
    
    public BorrowRecord getBorrowRecordByBookId(String bookId) {
        try {
            Document doc = collection.find(Filters.and(
                Filters.eq("bookId", bookId),
                Filters.eq("status", "BORROWED")
            )).first();
            return doc != null ? documentToRecord(doc) : null;
        } catch (Exception e) {
            System.err.println("Error getting borrow record by book ID: " + e.getMessage());
            return null;
        }
    }
    
    public long getTotalBorrowedBooks() {
        try {
            return collection.countDocuments(Filters.eq("status", "BORROWED"));
        } catch (Exception e) {
            System.err.println("Error getting total borrowed books: " + e.getMessage());
            return 0;
        }
    }
    
    public long getTotalReturnedBooks() {
        try {
            return collection.countDocuments(Filters.eq("status", "RETURNED"));
        } catch (Exception e) {
            System.err.println("Error getting total returned books: " + e.getMessage());
            return 0;
        }
    }
    
    public long getTotalOverdueBooks() {
        try {
            Date now = new Date();
            return collection.countDocuments(Filters.and(
                Filters.eq("status", "BORROWED"),
                Filters.lt("expectedReturnDate", now)
            ));
        } catch (Exception e) {
            System.err.println("Error getting total overdue books: " + e.getMessage());
            return 0;
        }
    }
    
    private Document recordToDocument(BorrowRecord record) {
        Document doc = new Document();
        
        if (record.getId() != null) {
            doc.append("_id", new ObjectId(record.getId()));
        }
        
        doc.append("bookId", record.getBookId())
           .append("bookTitle", record.getBookTitle())
           .append("borrowerName", record.getBorrowerName())
           .append("borrowerEmail", record.getBorrowerEmail())
           .append("borrowerPhone", record.getBorrowerPhone())
           .append("borrowDate", Date.from(record.getBorrowDate().atZone(ZoneId.systemDefault()).toInstant()))
           .append("expectedReturnDate", Date.from(record.getExpectedReturnDate().atZone(ZoneId.systemDefault()).toInstant()))
           .append("status", record.getStatus().toString())
           .append("notes", record.getNotes());
        
        if (record.getActualReturnDate() != null) {
            doc.append("actualReturnDate", Date.from(record.getActualReturnDate().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
    
    private BorrowRecord documentToRecord(Document doc) {
        BorrowRecord record = new BorrowRecord();
        
        record.setId(doc.getObjectId("_id").toString());
        record.setBookId(doc.getString("bookId"));
        record.setBookTitle(doc.getString("bookTitle"));
        record.setBorrowerName(doc.getString("borrowerName"));
        record.setBorrowerEmail(doc.getString("borrowerEmail"));
        record.setBorrowerPhone(doc.getString("borrowerPhone"));
        record.setStatus(BorrowRecord.BorrowStatus.valueOf(doc.getString("status")));
        record.setNotes(doc.getString("notes"));
        
        Date borrowDate = doc.getDate("borrowDate");
        if (borrowDate != null) {
            record.setBorrowDate(LocalDateTime.ofInstant(borrowDate.toInstant(), ZoneId.systemDefault()));
        }
        
        Date expectedReturnDate = doc.getDate("expectedReturnDate");
        if (expectedReturnDate != null) {
            record.setExpectedReturnDate(LocalDateTime.ofInstant(expectedReturnDate.toInstant(), ZoneId.systemDefault()));
        }
        
        Date actualReturnDate = doc.getDate("actualReturnDate");
        if (actualReturnDate != null) {
            record.setActualReturnDate(LocalDateTime.ofInstant(actualReturnDate.toInstant(), ZoneId.systemDefault()));
        }
        
        return record;
    }
}
