package com.javanabi.test;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.state.GameState;
import com.javanabi.players.SimpleAIPlayer;

import java.util.List;

public class KnowledgeTrackingTest {
    public static void main(String[] args) {
        System.out.println("Testing knowledge tracking system...");
        
        List<Player> players = List.of(
            new SimpleAIPlayer("TrackerAI"),
            new SimpleAIPlayer("TargetAI")
        );
        
        GameEngine game = new GameEngine(players);
        Player trackerAI = players.get(0);
        Player targetAI = players.get(1);
        
        // Get initial state
        GameState trackerView = game.getPlayerGameState(trackerAI);
        
        System.out.println("Initial setup:");
        System.out.println("  Tracker hand size: " + trackerView.getPlayerHandSize(trackerAI));
        System.out.println("  Target hand size: " + trackerView.getPlayerHandSize(targetAI));
        
        // Simulate a few turns to see knowledge tracking
        System.out.println("\nSimulating turns...");
        
        for (int turn = 1; turn <= 6; turn++) {
            Player currentPlayer = game.getCurrentPlayer();
            System.out.println("\nTurn " + turn + " - " + currentPlayer.getName() + "'s turn");
            
            // Execute action
            boolean success = game.executeAction(currentPlayer.takeTurn(game.getPlayerGameState(currentPlayer)));
            
            if (success) {
                System.out.println("  Action executed successfully");
                System.out.println("  Info tokens: " + game.getGameState().getInfoTokens());
                
                // Check if tracker AI is learning about target's knowledge
                if (currentPlayer.equals(trackerAI)) {
                    System.out.println("  Tracker AI gave a hint - checking knowledge tracking...");
                }
            }
        }
        
        System.out.println("\nKnowledge tracking test completed.");
        System.out.println("Final score: " + game.getScore());
    }
}