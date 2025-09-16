package com.library.database;

import com.library.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class UserDAO {
    private final MongoCollection<Document> collection;
    
    public UserDAO() {
        MongoDatabase database = DatabaseConnection.getDatabase();
        this.collection = database.getCollection("users");
        
        // Create default admin user if not exists
        createDefaultUser();
    }
    
    public boolean addUser(User user) {
        try {
            // Hash password
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);
            
            Document doc = userToDocument(user);
            collection.insertOne(doc);
            user.setId(doc.getObjectId("_id").toString());
            return true;
        } catch (Exception e) {
            System.err.println("Error adding user: " + e.getMessage());
            return false;
        }
    }
    
    public User authenticateUser(String email, String password) {
        try {
            Document doc = collection.find(Filters.and(
                Filters.eq("email", email),
                Filters.eq("isActive", true)
            )).first();
            
            if (doc != null) {
                String storedPassword = doc.getString("password");
                if (BCrypt.checkpw(password, storedPassword)) {
                    User user = documentToUser(doc);
                    // Update last login time
                    updateLastLogin(user.getId());
                    return user;
                }
            }
        } catch (Exception e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }
    
    public User getUserByEmail(String email) {
        try {
            Document doc = collection.find(Filters.eq("email", email)).first();
            return doc != null ? documentToUser(doc) : null;
        } catch (Exception e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            return null;
        }
    }
    
    private void updateLastLogin(String userId) {
        try {
            ObjectId objectId = new ObjectId(userId);
            collection.updateOne(
                Filters.eq("_id", objectId),
                new Document("$set", new Document("lastLoginAt", new Date()))
            );
        } catch (Exception e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }
      private void createDefaultUser() {
        try {
            // Check if default user exists
            String adminEmail = com.library.common.Configuration.getDefaultAdminEmail();
            Document existingUser = collection.find(Filters.eq("email", adminEmail)).first();
            if (existingUser == null) {
                User defaultUser = new User();
                defaultUser.setEmail(adminEmail);
                defaultUser.setPassword(com.library.common.Configuration.getDefaultAdminPassword());
                defaultUser.setFullName(com.library.common.Configuration.getDefaultAdminName());
                defaultUser.setRole(User.UserRole.ADMIN);
                
                addUser(defaultUser);
                System.out.println("Default user created successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error creating default user: " + e.getMessage());
        }
    }
    
    private Document userToDocument(User user) {
        Document doc = new Document();
        
        if (user.getId() != null) {
            doc.append("_id", new ObjectId(user.getId()));
        }
        
        doc.append("email", user.getEmail())
           .append("password", user.getPassword())
           .append("fullName", user.getFullName())
           .append("role", user.getRole().toString())
           .append("isActive", user.isActive())
           .append("createdAt", Date.from(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        
        if (user.getLastLoginAt() != null) {
            doc.append("lastLoginAt", Date.from(user.getLastLoginAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
    
    private User documentToUser(Document doc) {
        User user = new User();
        
        user.setId(doc.getObjectId("_id").toString());
        user.setEmail(doc.getString("email"));
        user.setPassword(doc.getString("password"));
        user.setFullName(doc.getString("fullName"));
        user.setRole(User.UserRole.valueOf(doc.getString("role")));
        user.setActive(doc.getBoolean("isActive", true));
        
        Date createdAt = doc.getDate("createdAt");
        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault()));
        }
        
        Date lastLoginAt = doc.getDate("lastLoginAt");
        if (lastLoginAt != null) {
            user.setLastLoginAt(LocalDateTime.ofInstant(lastLoginAt.toInstant(), ZoneId.systemDefault()));
        }
        
        return user;
    }
}
