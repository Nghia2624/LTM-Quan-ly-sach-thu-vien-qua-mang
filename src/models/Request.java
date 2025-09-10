package models;

import java.io.Serializable;

/**
 * Request class for client-server communication
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum RequestType {
        ADD_BOOK, UPDATE_BOOK, DELETE_BOOK, 
        GET_ALL_BOOKS, SEARCH_BOOK, 
        BORROW_BOOK, RETURN_BOOK
    }
    
    private RequestType type;
    private Object data;
    private String searchQuery;
    
    public Request() {}
    
    public Request(RequestType type) {
        this.type = type;
    }
    
    public Request(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    public Request(RequestType type, String searchQuery) {
        this.type = type;
        this.searchQuery = searchQuery;
    }
    
    // Getters and Setters
    public RequestType getType() { return type; }
    public void setType(RequestType type) { this.type = type; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
}
