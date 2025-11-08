package com.javanabi.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.UUID;

public class ClientConnection {
    private final Socket socket;
    private final HanabiServer server;
    private final String clientId;
    private PrintWriter out;
    private BufferedReader in;
    private String currentGameId;
    
    public ClientConnection(Socket socket, HanabiServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.clientId = UUID.randomUUID().toString();
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.currentGameId = null;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public String getCurrentGameId() {
        return currentGameId;
    }
    
    public void assignToGame(String gameId) {
        this.currentGameId = gameId;
    }
    
    public void run() {
        try {
            sendMessage("CONNECTED", clientId);
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                handleMessage(inputLine);
            }
        } catch (IOException e) {
            System.err.println("Error in client connection " + clientId + ": " + e.getMessage());
        } finally {
            close();
        }
    }
    
    private void handleMessage(String message) {
        String[] parts = message.split(" ", 3);
        String command = parts[0];
        
        try {
            switch (command) {
                case "LIST_CLIENTS":
                    handleListClients();
                    break;
                case "CREATE_GAME":
                    handleCreateGame(parts.length > 1 ? parts[1] : "");
                    break;
                case "START_GAME":
                    handleStartGame(parts.length > 1 ? parts[1] : "");
                    break;
                case "GAME_ACTION":
                    handleGameAction(parts.length > 1 ? parts[1] : "");
                    break;
                case "GET_GAME_STATE":
                    handleGetGameState();
                    break;
                default:
                    sendMessage("ERROR", "Unknown command: " + command);
            }
        } catch (Exception e) {
            sendMessage("ERROR", e.getMessage());
        }
    }
    
    private void handleListClients() {
        StringBuilder response = new StringBuilder();
        for (String clientId : server.getAvailableClients()) {
            if (response.length() > 0) response.append(",");
            response.append(clientId);
        }
        sendMessage("CLIENTS_LIST", response.toString());
    }
    
    private void handleCreateGame(String playerListStr) {
        String[] playerIds = playerListStr.split(",");
        if (playerIds.length < 2 || playerIds.length > 5) {
            sendMessage("ERROR", "Game requires 2-5 players");
            return;
        }
        
        server.createGame(clientId, List.of(playerIds));
        sendMessage("GAME_CREATED", "Waiting for game to start");
    }
    
    private void handleStartGame(String gameId) {
        if (gameId.isEmpty() && currentGameId != null) {
            gameId = currentGameId;
        }
        
        server.startGame(gameId);
        sendMessage("GAME_STARTED", "Game has begun");
    }
    
    private void handleGameAction(String actionData) {
        if (currentGameId == null) {
            sendMessage("ERROR", "Not in a game");
            return;
        }
        
        // This would be implemented to handle game actions
        sendMessage("ACTION_PROCESSED", "Action received");
    }
    
    private void handleGetGameState() {
        if (currentGameId == null) {
            sendMessage("ERROR", "Not in a game");
            return;
        }
        
        // This would be implemented to send current game state
        sendMessage("GAME_STATE", "State data here");
    }
    
    public void sendMessage(String type, String data) {
        out.println(type + " " + data);
    }
    
    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            server.removeClient(clientId);
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}