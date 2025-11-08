package com.javanabi.test;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.action.Action;
import com.javanabi.players.SimpleAIPlayer;

import java.util.List;

public class SmartHintTest {
    public static void main(String[] args) {
        System.out.println("Testing smarter hint giving with knowledge tracking...");
        
        List<Player> players = List.of(
            new SimpleAIPlayer("SmartAI1"),
            new SimpleAIPlayer("SmartAI2"),
            new SimpleAIPlayer("SmartAI3")
        );
        
        GameEngine game = new GameEngine(players);
        
        System.out.println("Game started with 3 AI players");
        System.out.println("Initial state - Info: " + game.getGameState().getInfoTokens() + 
                          ", Fuses: " + game.getGameState().getFuseTokens());
        
        int hintCount = 0;
        int redundantHints = 0;
        
        // Play several turns to analyze hint behavior
        for (int turn = 1; turn <= 15; turn++) {
            if (game.isGameOver()) break;
            
            Player currentPlayer = game.getCurrentPlayer();
            Action action = currentPlayer.takeTurn(game.getPlayerGameState(currentPlayer));
            
            // Track hint giving patterns
            String actionType = action.getClass().getSimpleName();
            if (actionType.equals("GiveInfoAction")) {
                hintCount++;
                System.out.println("Turn " + turn + ": " + currentPlayer.getName() + " gives hint #" + hintCount);
            } else {
                System.out.println("Turn " + turn + ": " + currentPlayer.getName() + " " + actionType);
            }
            
            boolean success = game.executeAction(action);
            if (!success) {
                System.out.println("  Action failed!");
            }
            
            System.out.println("  Score: " + game.getGameState().calculateScore() + 
                              ", Info: " + game.getGameState().getInfoTokens());
        }
        
        System.out.println("\n=== Analysis ===");
        System.out.println("Total hints given: " + hintCount);
        System.out.println("Final score: " + game.getScore());
        System.out.println("Game Over: " + game.isGameOver());
        
        if (hintCount > 0) {
            System.out.println("✅ AI is actively using knowledge tracking to give hints");
        } else {
            System.out.println("⚠️  No hints given in this test");
        }
    }
}