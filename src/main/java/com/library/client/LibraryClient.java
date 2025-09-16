package com.library.client;

import com.library.common.Message;

import java.io.*;
import java.net.Socket;

public class LibraryClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            // Important: Create ObjectOutputStream first, then ObjectInputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // Flush the header
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            System.out.println("Connected to server successfully!");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            connected = false;
            return false;
        }
    }
    
    public void disconnect() {
        try {
            connected = false;
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public synchronized Message sendRequest(Message request) {
        if (!connected || socket == null || socket.isClosed()) {
            return Message.error(Message.MessageType.RESPONSE, "Not connected to server");
        }
        
        try {
            // Reset the ObjectOutputStream to clear any cached references
            out.reset();
            out.writeObject(request);
            out.flush();
            
            Object response = in.readObject();
            if (response instanceof Message) {
                return (Message) response;
            } else {
                return Message.error(Message.MessageType.RESPONSE, "Invalid response type: " + response.getClass());
            }
        } catch (IOException e) {
            System.err.println("IO Error sending request: " + e.getMessage());
            e.printStackTrace();
            // Try to reconnect
            disconnect();
            return Message.error(Message.MessageType.RESPONSE, "Communication error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found error: " + e.getMessage());
            e.printStackTrace();
            return Message.error(Message.MessageType.RESPONSE, "Serialization error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return Message.error(Message.MessageType.RESPONSE, "Unexpected error: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
