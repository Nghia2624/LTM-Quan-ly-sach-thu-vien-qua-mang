package com.dainam.library.util;

import com.dainam.library.config.DatabaseConfig;

/**
 * Utility class để khởi tạo dữ liệu cho hệ thống
 */
public class DataInitializer {
    
    /**
     * Khởi tạo dữ liệu cho hệ thống
     */
    public static void initialize() {
        try {
            LoggerUtil.info("Bắt đầu khởi tạo dữ liệu hệ thống...");
            
            // Khởi tạo database connection (không tạo sample data)
            DatabaseConfig.initializeWithoutSampleData();
            
            // Kiểm tra xem có cần tạo dữ liệu mẫu không
            if (shouldCreateSampleData()) {
                LoggerUtil.info("Đang tạo dữ liệu chuẩn...");
                StandardDataGenerator.generateStandardData();
                LoggerUtil.info("Dữ liệu chuẩn đã được tạo thành công");
            } else {
                LoggerUtil.info("Dữ liệu đã tồn tại, bỏ qua việc tạo mẫu");
            }
            
            LoggerUtil.info("Hoàn thành khởi tạo dữ liệu hệ thống!");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi khởi tạo dữ liệu: " + e.getMessage());
            throw new RuntimeException("Không thể khởi tạo dữ liệu hệ thống", e);
        }
    }/**
     * Kiểm tra xem có nên tạo dữ liệu mẫu không
     */
    private static boolean shouldCreateSampleData() {
        try {
            // Kiểm tra xem đã có user nào chưa
            long userCount = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_USERS).countDocuments();
            long bookCount = DatabaseConfig.getCollection(DatabaseConfig.COLLECTION_BOOKS).countDocuments();
            
            // Nếu không có user nào hoặc không có sách, thì tạo sample data
            LoggerUtil.info("User count: " + userCount + ", Book count: " + bookCount);
            boolean shouldCreate = userCount == 0 && bookCount == 0;
            LoggerUtil.info("Should create sample data: " + shouldCreate);            return shouldCreate;
        } catch (Exception e) {
            LoggerUtil.error("Lỗi kiểm tra dữ liệu: " + e.getMessage());
            return false;
        }
    }
      /**
     * Reset toàn bộ dữ liệu hệ thống
     */
    public static void reset() {
        try {
            LoggerUtil.info("Bắt đầu reset dữ liệu hệ thống...");
            
            // Tạo lại dữ liệu chuẩn
            StandardDataGenerator.generateStandardData();
            LoggerUtil.info("Reset dữ liệu đã được thực hiện");
            
            LoggerUtil.info("Hoàn thành reset dữ liệu hệ thống!");
            
        } catch (Exception e) {
            LoggerUtil.error("Lỗi reset dữ liệu: " + e.getMessage());
            throw new RuntimeException("Không thể reset dữ liệu hệ thống", e);
        }
    }
}
