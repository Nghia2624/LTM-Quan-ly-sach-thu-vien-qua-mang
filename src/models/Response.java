package models;

import java.io.Serializable;

/**
 * Response class for server-client communication
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Status {
        SUCCESS, ERROR, NOT_FOUND
    }
    
    private Status status;
    private String message;
    private Object data;
    
    public Response() {}
    
    public Response(Status status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public Response(Status status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    // Getters and Setters
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
