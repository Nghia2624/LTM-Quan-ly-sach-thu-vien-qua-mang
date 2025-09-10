package client;

import models.*;
import java.io.*;
import java.net.*;

/**
 * Network service class to communicate with server
 */
public class NetworkService {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isConnected;
    
    public NetworkService() {
        this.isConnected = false;
    }
    
    /**
     * Connect to the server
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            isConnected = false;
            return false;
        }
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            isConnected = false;
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server");
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Send request to server and get response
     */
    public Response sendRequest(Request request) {
        if (!isConnected) {
            return new Response(Response.Status.ERROR, "Not connected to server");
        }
        
        try {
            outputStream.writeObject(request);
            outputStream.flush();
            return (Response) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error sending request: " + e.getMessage());
            return new Response(Response.Status.ERROR, "Communication error: " + e.getMessage());
        }
    }
    
    /**
     * Check if connected to server
     */
    public boolean isConnected() {
        return isConnected && socket != null && !socket.isClosed();
    }
}
