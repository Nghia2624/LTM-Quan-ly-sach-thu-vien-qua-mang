package com.dainam.library.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class đại diện cho người dùng trong hệ thống
 */
public class User {
    
    public enum Role {
        ADMIN("Quản trị viên"),
        USER("Người dùng"),
        LIBRARIAN("Thủ thư");
        
        private final String displayName;
        
        Role(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Status {
        ACTIVE("Hoạt động"),
        INACTIVE("Không hoạt động"),
        SUSPENDED("Bị khóa"),
        LOCKED("Bị khóa"),
        PENDING("Chờ duyệt");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("fullName")
    private String fullName;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("role")
    private Role role;
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("studentId")
    private String studentId;
      @JsonProperty("faculty")
    private String faculty;
      @JsonProperty("yearOfStudy")
    private String yearOfStudy;
    
    @JsonProperty("dateOfBirth")
    private LocalDate dateOfBirth;
    
    @JsonProperty("registrationDate")
    private LocalDate registrationDate;
      @JsonProperty("lastLogin")
    private LocalDate lastLogin;
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("isOnline")
    private boolean isOnline;
    
    @JsonProperty("totalBorrowed")
    private int totalBorrowed;
    
    @JsonProperty("currentBorrowed")
    private int currentBorrowed;
    
    @JsonProperty("totalFines")
    private double totalFines;
    
    @JsonProperty("notes")
    private String notes;
    
    // Constructors
    public User() {
        this.role = Role.USER;
        this.status = Status.PENDING;
        this.registrationDate = LocalDate.now();
        this.totalBorrowed = 0;
        this.currentBorrowed = 0;
        this.totalFines = 0.0;
    }
    
    public User(String userId, String email, String password, String firstName, String lastName) {
        this();
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
      public String getFaculty() {
        return faculty;
    }
    
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
      public String getYearOfStudy() {
        return yearOfStudy;
    }
    
    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
      public LocalDate getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDate lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
      public void setOnline(boolean online) {
        isOnline = online;
    }
    
    public int getTotalBorrowed() {
        return totalBorrowed;
    }
    
    public void setTotalBorrowed(int totalBorrowed) {
        this.totalBorrowed = totalBorrowed;
    }
    
    public int getCurrentBorrowed() {
        return currentBorrowed;
    }
    
    public void setCurrentBorrowed(int currentBorrowed) {
        this.currentBorrowed = currentBorrowed;
    }
    
    public double getTotalFines() {
        return totalFines;
    }
    
    public void setTotalFines(double totalFines) {
        this.totalFines = totalFines;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    
    /**
     * Kiểm tra xem người dùng có phải admin không
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    /**
     * Kiểm tra xem tài khoản có hoạt động không
     */
    public boolean isActive() {
        return status == Status.ACTIVE;
    }
    
    /**
     * Kiểm tra xem người dùng có thể mượn sách không
     */
    public boolean canBorrow() {
        return isActive() && currentBorrowed < 5; // Giới hạn 5 quyển
    }
    
    /**
     * Cập nhật thông tin đăng nhập cuối
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDate.now();
    }
    
    /**
     * Tăng số lượng sách đang mượn
     */
    public void incrementCurrentBorrowed() {
        this.currentBorrowed++;
        this.totalBorrowed++;
    }
    
    /**
     * Giảm số lượng sách đang mượn
     */
    public void decrementCurrentBorrowed() {
        if (this.currentBorrowed > 0) {
            this.currentBorrowed--;
        }
    }
    
    /**
     * Thêm phạt
     */
    public void addFine(double amount) {
        this.totalFines += amount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
