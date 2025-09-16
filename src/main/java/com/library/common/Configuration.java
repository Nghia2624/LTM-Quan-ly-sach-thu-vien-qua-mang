package com.library.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties = new Properties();
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            System.out.println("Configuration loaded from " + CONFIG_FILE);
        } catch (IOException e) {
            System.out.println("Using default configuration");
            setDefaultProperties();
        }
    }
    
    private static void setDefaultProperties() {
        properties.setProperty("database.host", "localhost");
        properties.setProperty("database.port", "27017");
        properties.setProperty("database.name", "library_management");
        properties.setProperty("server.port", "8888");
        properties.setProperty("server.max_clients", "100");
        properties.setProperty("default.admin.email", "dainam@gmail.com");
        properties.setProperty("default.admin.password", "dainam");
        properties.setProperty("default.admin.name", "Dai Nam");
        properties.setProperty("borrow.default_days", "14");
        properties.setProperty("borrow.max_days", "365");
        properties.setProperty("borrow.warning_days", "3");
    }
    
    public static String get(String key) {
        return properties.getProperty(key);
    }
    
    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
    
    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static String getDatabaseHost() {
        return get("database.host", "localhost");
    }
    
    public static int getDatabasePort() {
        return getInt("database.port", 27017);
    }
    
    public static String getDatabaseName() {
        return get("database.name", "library_management");
    }
    
    public static String getDatabaseConnectionString() {
        return "mongodb://" + getDatabaseHost() + ":" + getDatabasePort();
    }
    
    public static int getServerPort() {
        return getInt("server.port", 8888);
    }
    
    public static int getMaxClients() {
        return getInt("server.max_clients", 100);
    }
    
    public static String getDefaultAdminEmail() {
        return get("default.admin.email", "dainam@gmail.com");
    }
    
    public static String getDefaultAdminPassword() {
        return get("default.admin.password", "dainam");
    }
    
    public static String getDefaultAdminName() {
        return get("default.admin.name", "Dai Nam");
    }
    
    public static int getDefaultBorrowDays() {
        return getInt("borrow.default_days", 14);
    }
    
    public static int getMaxBorrowDays() {
        return getInt("borrow.max_days", 365);
    }
    
    public static int getBorrowWarningDays() {
        return getInt("borrow.warning_days", 3);
    }
}
