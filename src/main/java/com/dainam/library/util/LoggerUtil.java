package com.dainam.library.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class cho logging
 */
public class LoggerUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Khởi tạo logger
     */
    public static void initialize() {
        // Logger đã được khởi tạo tự động bởi SLF4J
        info("Logger đã được khởi tạo");
    }
    
    /**
     * Log thông tin
     */
    public static void info(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] INFO: " + message);
        logger.info(message);
    }
    
    /**
     * Log cảnh báo
     */
    public static void warn(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] WARN: " + message);
        logger.warn(message);
    }
    
    /**
     * Log lỗi
     */
    public static void error(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.err.println("[" + timestamp + "] ERROR: " + message);
        logger.error(message);
    }
    
    /**
     * Log debug
     */
    public static void debug(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] DEBUG: " + message);
        logger.debug(message);
    }
    
    /**
     * Log với exception
     */
    public static void error(String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.err.println("[" + timestamp + "] ERROR: " + message);
        throwable.printStackTrace();
        logger.error(message, throwable);
    }
}
