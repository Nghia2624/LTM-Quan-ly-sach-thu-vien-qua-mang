package com.dainam.library.util;

import com.dainam.library.config.DatabaseConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Utility để reset trạng thái online của users
 */
public class SessionResetUtil {
    
    public static void resetAllOnlineStatus() {
        try {
            MongoCollection<Document> usersCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_USERS);
            
            // Reset tất cả online status
            Bson filter = Filters.eq("isOnline", true);
            Bson update = Updates.combine(
                Updates.unset("sessionId"),
                Updates.set("isOnline", false)
            );
            
            long updatedCount = usersCollection.updateMany(filter, update).getModifiedCount();
            
            LoggerUtil.info("Reset online status cho " + updatedCount + " users");
            System.out.println("Reset online status cho " + updatedCount + " users");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi reset online status: " + e.getMessage());
            System.err.println("Lỗi reset online status: " + e.getMessage());
        }
    }
    
    public static void resetUserOnlineStatus(String email) {
        try {
            MongoCollection<Document> usersCollection = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_USERS);
            
            // Reset online status cho user cụ thể
            Bson filter = Filters.eq("email", email);
            Bson update = Updates.combine(
                Updates.unset("sessionId"),
                Updates.set("isOnline", false)
            );
            
            long updatedCount = usersCollection.updateOne(filter, update).getModifiedCount();
            
            if (updatedCount > 0) {
                LoggerUtil.info("Reset online status cho user: " + email);
                System.out.println("Reset online status cho user: " + email);
            } else {
                LoggerUtil.info("Không tìm thấy user hoặc user đã offline: " + email);
                System.out.println("Không tìm thấy user hoặc user đã offline: " + email);
            }
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi reset online status cho user " + email + ": " + e.getMessage());
            System.err.println("Lỗi reset online status cho user " + email + ": " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        if (args.length > 0) {
            // Reset cho user cụ thể
            resetUserOnlineStatus(args[0]);
        } else {
            // Reset tất cả
            resetAllOnlineStatus();
        }
    }
}
