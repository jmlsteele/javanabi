package com.javanabi.server;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.state.GameState;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HanabiServer {
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final Map<String, ClientConnection> clients;
    private final Map<String, GameSession> gameSessions;
    private final AtomicInteger gameIdCounter;
    private volatile boolean running;
    
    public HanabiServer(int port) {
        this.port = port;
        this.executorService = Executors.newCachedThreadPool();
        this.clients = new ConcurrentHashMap<>();
        this.gameSessions = new ConcurrentHashMap<>();
        this.gameIdCounter = new AtomicInteger(1);
        this.running = false;
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("Hanabi server started on port " + port);
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClientConnection(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        
        for (ClientConnection client : clients.values()) {
            client.close();
        }
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
    
    private void handleClientConnection(Socket clientSocket) {
        try {
            ClientConnection client = new ClientConnection(clientSocket, this);
            String clientId = client.getClientId();
            clients.put(clientId, client);
            
            System.out.println("Client connected: " + clientId);
            
            client.run();
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        }
    }
    
    public void createGame(String hostClientId, List<String> playerIds) {
        String gameId = "game_" + gameIdCounter.getAndIncrement();
        GameSession session = new GameSession(gameId, hostClientId, playerIds, this);
        gameSessions.put(gameId, session);
        
        for (String playerId : playerIds) {
            ClientConnection client = clients.get(playerId);
            if (client != null) {
                client.assignToGame(gameId);
            }
        }
        
        System.out.println("Created game " + gameId + " with players: " + playerIds);
    }
    
    public void startGame(String gameId) {
        GameSession session = gameSessions.get(gameId);
        if (session != null) {
            session.startGame();
        }
    }
    
    public void removeClient(String clientId) {
        clients.remove(clientId);
        System.out.println("Client disconnected: " + clientId);
    }
    
    public List<String> getAvailableClients() {
        return new ArrayList<>(clients.keySet());
    }
    
    public Map<String, GameSession> getGameSessions() {
        return new HashMap<>(gameSessions);
    }
    
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        
        HanabiServer server = new HanabiServer(port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (IOException e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
}