package com.library.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private Object data;
    private boolean success;
    private String errorMessage;
      public enum MessageType implements Serializable {
        // Authentication
        LOGIN, LOGOUT,
        
        // Book operations
        ADD_BOOK, UPDATE_BOOK, DELETE_BOOK, GET_BOOK, GET_ALL_BOOKS, SEARCH_BOOKS,
        
        // Borrow operations
        BORROW_BOOK, RETURN_BOOK, GET_BORROW_RECORDS, GET_OVERDUE_BOOKS,
        
        // Statistics
        GET_STATISTICS,
        
        // Response
        RESPONSE
    }
    
    // Constructors
    public Message() {}
    
    public Message(MessageType type, Object data) {
        this.type = type;
        this.data = data;
        this.success = true;
    }
    
    public Message(MessageType type, Object data, boolean success, String errorMessage) {
        this.type = type;
        this.data = data;
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    // Static factory methods
    public static Message success(MessageType type, Object data) {
        return new Message(type, data, true, null);
    }
    
    public static Message error(MessageType type, String errorMessage) {
        return new Message(type, null, false, errorMessage);
    }
    
    // Getters and Setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    @Override
    public String toString() {
        return String.format("Message{type=%s, success=%s, error=%s}", 
                           type, success, errorMessage);
    }
}
