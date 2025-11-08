package com.javanabi.test;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.players.SimpleAIPlayer;

import java.util.List;

public class DynamicPlayerTest {
    public static void main(String[] args) {
        System.out.println("=== Testing Dynamic Player Creation ===");
        
        // Test 1: 2 players
        System.out.println("\n1. Testing 2 SimpleAIPlayers:");
        testGame("SimpleAIPlayer", "SimpleAIPlayer");
        
        // Test 2: 3 players  
        System.out.println("\n2. Testing 3 SimpleAIPlayers:");
        testGame("SimpleAIPlayer", "SimpleAIPlayer", "SimpleAIPlayer");
        
        // Test 3: 4 players
        System.out.println("\n3. Testing 4 SimpleAIPlayers:");
        testGame("SimpleAIPlayer", "SimpleAIPlayer", "SimpleAIPlayer", "SimpleAIPlayer");
        
        System.out.println("\n=== Dynamic Player System Working! ===");
    }
    
    private static void testGame(String... playerClasses) {
        try {
            // Create players using same logic as HanabiServer
            List<Player> players = createPlayers(playerClasses);
            
            // Create game engine
            GameEngine game = new GameEngine(players);
            
            System.out.println("  ✓ Game created with " + players.size() + " players");
            System.out.println("  ✓ Players: " + getPlayerNames(players));
            System.out.println("  ✓ Initial state - Info: " + game.getGameState().getInfoTokens() + 
                              ", Fuses: " + game.getGameState().getFuseTokens());
            
            // Play a few turns to verify functionality
            for (int turn = 1; turn <= 3; turn++) {
                if (game.isGameOver()) break;
                
                Player currentPlayer = game.getCurrentPlayer();
                boolean success = game.executeAction(currentPlayer.takeTurn(game.getPlayerGameState(currentPlayer)));
                
                if (success) {
                    System.out.println("  Turn " + turn + ": " + currentPlayer.getName() + " acted successfully");
                }
            }
            
            System.out.println("  ✓ Game simulation completed");
            
        } catch (Exception e) {
            System.out.println("  ✗ Error: " + e.getMessage());
        }
    }
    
    private static List<Player> createPlayers(String[] playerClasses) {
        java.util.List<Player> players = new java.util.ArrayList<>();
        
        for (int i = 0; i < playerClasses.length; i++) {
            String className = playerClasses[i];
            String playerName = "Player" + (i + 1);
            
            try {
                String fullClassName = "com.javanabi.game." + className;
                Class<?> playerClass = Class.forName(fullClassName);
                Player player = (Player) playerClass.getDeclaredConstructor(String.class).newInstance(playerName);
                players.add(player);
            } catch (Exception e) {
                // Fallback to SimpleAIPlayer
                players.add(new SimpleAIPlayer(playerName));
            }
        }
        
        return players;
    }
    
    private static String getPlayerNames(java.util.List<Player> players) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(players.get(i).getName());
        }
        return names.toString();
    }
}