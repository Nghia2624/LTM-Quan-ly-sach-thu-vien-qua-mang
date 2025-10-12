package com.dainam.library.util;

import java.util.regex.Pattern;

/**
 * Utility class cho validation
 */
public class ValidationUtil {
    
    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+84|0)[0-9]{9,10}$"
    );    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile(
        "^167102[0-9]{4}$"  // Format: 1671020000-1671029999
    );
    
    private static final Pattern ISBN_PATTERN = Pattern.compile(
        "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$"
    );
    
    /**
     * Kiểm tra email hợp lệ
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Kiểm tra số điện thoại hợp lệ
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Kiểm tra mã sinh viên hợp lệ
     */
    public static boolean isValidStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return false;
        }
        return STUDENT_ID_PATTERN.matcher(studentId.trim()).matches();
    }
    
    /**
     * Kiểm tra ISBN hợp lệ
     */
    public static boolean isValidISBN(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        return ISBN_PATTERN.matcher(isbn.trim()).matches();
    }
    
    /**
     * Kiểm tra chuỗi không rỗng
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Kiểm tra chuỗi có độ dài hợp lệ
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * Kiểm tra số dương
     */
    public static boolean isPositiveNumber(double number) {
        return number > 0;
    }
    
    /**
     * Kiểm tra số nguyên dương
     */
    public static boolean isPositiveInteger(int number) {
        return number > 0;
    }
    
    /**
     * Kiểm tra năm hợp lệ
     */
    public static boolean isValidYear(int year) {
        int currentYear = java.time.LocalDate.now().getYear();
        return year >= 1900 && year <= currentYear + 1;
    }
    
    /**
     * Sanitize chuỗi input
     */
    public static String sanitizeString(String str) {
        if (str == null) {
            return null;
        }
        return str.trim().replaceAll("[<>\"'&]", "");
    }
      /**
     * Kiểm tra tên hợp lệ (chỉ chữ cái và khoảng trắng)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Regex hỗ trợ đầy đủ tiếng Việt có dấu và không dấu
        String vietnamesePattern = "^[a-zA-ZàáảãạâầấẩẫậăằắẳẵặèéẻẽẹêềếểễệđìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵÀÁẢÃẠÂẦẤẨẪẬĂẰẮẲẴẶÈÉẺẼẸÊỀẾỂỄỆĐÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴ\\s]+$";
        return name.trim().matches(vietnamesePattern);
    }
    
    /**
     * Validate thông tin đăng ký user
     */
    public static String validateUserRegistration(String email, String password, String firstName, 
                                                String lastName, String phone, String studentId) {
        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        
        if (!PasswordUtil.isStrongPassword(password)) {
            return "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số";
        }
        
        if (!isValidName(firstName)) {
            return "Tên không hợp lệ";
        }
        
        if (!isValidName(lastName)) {
            return "Họ không hợp lệ";
        }
        
        if (!isValidPhone(phone)) {
            return "Số điện thoại không hợp lệ";
        }
          if (!isValidStudentId(studentId)) {
            return "Mã sinh viên không hợp lệ (phải từ 1671020000 đến 1671029999)";
        }
        
        return null; // Hợp lệ
    }
    
    /**
     * Validate thông tin sách
     */
    public static String validateBook(String title, String author, String isbn, 
                                    String publisher, int publicationYear, double price) {
        if (!isNotEmpty(title) || !isValidLength(title, 1, 200)) {
            return "Tiêu đề sách không hợp lệ";
        }
        
        if (!isNotEmpty(author) || !isValidLength(author, 1, 100)) {
            return "Tác giả không hợp lệ";
        }
        
        if (!isValidISBN(isbn)) {
            return "ISBN không hợp lệ";
        }
        
        if (!isNotEmpty(publisher) || !isValidLength(publisher, 1, 100)) {
            return "Nhà xuất bản không hợp lệ";
        }
        
        if (!isValidYear(publicationYear)) {
            return "Năm xuất bản không hợp lệ";
        }
        
        if (!isPositiveNumber(price)) {
            return "Giá sách phải là số dương";
        }
        
        return null; // Hợp lệ
    }
}
