package com.dainam.library.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class cho xử lý mật khẩu
 */
public class PasswordUtil {
    
    private static final int ROUNDS = 10;
    
    /**
     * Mã hóa mật khẩu
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(ROUNDS));
    }
    
    /**
     * Kiểm tra mật khẩu
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            LoggerUtil.error("Lỗi kiểm tra mật khẩu: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra độ mạnh mật khẩu
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpperCase && hasLowerCase && hasDigit;
    }
    
    /**
     * Tạo mật khẩu ngẫu nhiên
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
}
