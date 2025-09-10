package server;

import models.*;
import utils.SimpleBookDAO;
import utils.SampleDataGenerator;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Server class to handle client requests
 */
public class LibraryServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private SimpleBookDAO bookDAO;
    private boolean isRunning;
      public LibraryServer() {
        this.bookDAO = new SimpleBookDAO();
        this.isRunning = false;
        // Initialize sample data if database is empty
        SampleDataGenerator.initializeSampleData();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("Library Server started on port " + PORT);
            System.out.println("Waiting for clients...");
            
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Handle each client in a separate thread
                new Thread(new ClientHandler(clientSocket, bookDAO)).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }
    
    public void stop() {
        try {
            isRunning = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("Server stopped.");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        LibraryServer server = new LibraryServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        server.start();
    }
}

/**
 * Handler for individual client connections
 */
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private SimpleBookDAO bookDAO;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    public ClientHandler(Socket clientSocket, SimpleBookDAO bookDAO) {
        this.clientSocket = clientSocket;
        this.bookDAO = bookDAO;
    }
    
    @Override
    public void run() {
        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            
            Request request;
            while ((request = (Request) inputStream.readObject()) != null) {
                Response response = handleRequest(request);
                outputStream.writeObject(response);
                outputStream.flush();
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    private Response handleRequest(Request request) {
        try {
            switch (request.getType()) {
                case ADD_BOOK:
                    return handleAddBook((Book) request.getData());
                    
                case UPDATE_BOOK:
                    return handleUpdateBook((Book) request.getData());
                    
                case DELETE_BOOK:
                    return handleDeleteBook((String) request.getData());
                    
                case GET_ALL_BOOKS:
                    return handleGetAllBooks();
                    
                case SEARCH_BOOK:
                    return handleSearchBook(request.getSearchQuery());
                    
                case BORROW_BOOK:
                    return handleBorrowBook((String[]) request.getData());
                    
                case RETURN_BOOK:
                    return handleReturnBook((String) request.getData());
                    
                default:
                    return new Response(Response.Status.ERROR, "Unknown request type");
            }
        } catch (Exception e) {
            return new Response(Response.Status.ERROR, "Server error: " + e.getMessage());
        }
    }
    
    private Response handleAddBook(Book book) {
        if (bookDAO.addBook(book)) {
            return new Response(Response.Status.SUCCESS, "Book added successfully");
        } else {
            return new Response(Response.Status.ERROR, "Book with ID " + book.getId() + " already exists");
        }
    }
    
    private Response handleUpdateBook(Book book) {
        if (bookDAO.updateBook(book)) {
            return new Response(Response.Status.SUCCESS, "Book updated successfully");
        } else {
            return new Response(Response.Status.NOT_FOUND, "Book not found");
        }
    }
    
    private Response handleDeleteBook(String bookId) {
        if (bookDAO.deleteBook(bookId)) {
            return new Response(Response.Status.SUCCESS, "Book deleted successfully");
        } else {
            return new Response(Response.Status.NOT_FOUND, "Book not found");
        }
    }
    
    private Response handleGetAllBooks() {
        List<Book> books = bookDAO.loadBooks();
        return new Response(Response.Status.SUCCESS, "Books retrieved successfully", books);
    }
    
    private Response handleSearchBook(String query) {
        List<Book> books = bookDAO.searchBooks(query);
        return new Response(Response.Status.SUCCESS, "Search completed", books);
    }
    
    private Response handleBorrowBook(String[] data) {
        String bookId = data[0];
        String borrowerName = data[1];
        
        Book book = bookDAO.findBookById(bookId);
        if (book == null) {
            return new Response(Response.Status.NOT_FOUND, "Book not found");
        }
        
        if (!book.isAvailable()) {
            return new Response(Response.Status.ERROR, "Book is already borrowed");
        }
        
        book.setAvailable(false);
        book.setBorrowedBy(borrowerName);
        book.setBorrowDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        
        if (bookDAO.updateBook(book)) {
            return new Response(Response.Status.SUCCESS, "Book borrowed successfully");
        } else {
            return new Response(Response.Status.ERROR, "Failed to update book status");
        }
    }
    
    private Response handleReturnBook(String bookId) {
        Book book = bookDAO.findBookById(bookId);
        if (book == null) {
            return new Response(Response.Status.NOT_FOUND, "Book not found");
        }
        
        if (book.isAvailable()) {
            return new Response(Response.Status.ERROR, "Book is not currently borrowed");
        }
        
        book.setAvailable(true);
        book.setBorrowedBy(null);
        book.setBorrowDate(null);
        
        if (bookDAO.updateBook(book)) {
            return new Response(Response.Status.SUCCESS, "Book returned successfully");
        } else {
            return new Response(Response.Status.ERROR, "Failed to update book status");
        }
    }
}
