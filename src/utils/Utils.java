package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for common operations
 */
public class Utils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get current date as formatted string
     */
    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }
    
    /**
     * Format date for display
     */
    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }
    
    /**
     * Check if string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Generate unique ID based on current timestamp
     */
    public static String generateId() {
        return "BOOK_" + System.currentTimeMillis();
    }
    
    /**
     * Validate book ID format
     */
    public static boolean isValidBookId(String id) {
        return !isNullOrEmpty(id) && id.matches("^[A-Z0-9_]+$");
    }
    
    /**
     * Validate year range
     */
    public static boolean isValidYear(int year) {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        return year > 0 && year <= currentYear + 5; // Allow future publications
    }
}
