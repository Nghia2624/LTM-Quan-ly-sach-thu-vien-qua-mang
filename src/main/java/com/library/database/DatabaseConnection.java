package com.library.database;

import com.library.common.Configuration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConnection {
    private static final String CONNECTION_STRING = Configuration.getDatabaseConnectionString();
    private static final String DATABASE_NAME = Configuration.getDatabaseName();
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    // Singleton pattern
    private DatabaseConnection() {}
    
    public static synchronized MongoDatabase getDatabase() {
        if (database == null) {
            try {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DATABASE_NAME);
                System.out.println("Connected to MongoDB successfully!");
            } catch (Exception e) {
                System.err.println("Failed to connect to MongoDB: " + e.getMessage());
                throw new RuntimeException("Database connection failed", e);
            }
        }
        return database;
    }
    
    public static void closeConnection() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                System.out.println("MongoDB connection closed.");
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
    
    public static boolean testConnection() {
        try {
            MongoDatabase db = getDatabase();
            // Ping the database
            db.runCommand(new org.bson.Document("ping", 1));
            return true;
        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Test MongoDB Connection ===");
        
        try {
            System.out.println("Testing MongoDB connection...");
            
            if (testConnection()) {
                System.out.println("✓ MongoDB connection successful!");
                
                // Display database info
                MongoDatabase db = getDatabase();
                System.out.println("Database name: " + db.getName());
                System.out.println("Collections:");
                
                for (String collectionName : db.listCollectionNames()) {
                    System.out.println("  - " + collectionName);
                }
                
            } else {
                System.out.println("✗ MongoDB connection failed!");
            }
            
        } catch (Exception e) {
            System.err.println("Error testing connection: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }
}
