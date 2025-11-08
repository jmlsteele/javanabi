package com.javanabi.test;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.action.Action;
import com.javanabi.players.SimpleAIPlayer;

import java.util.List;

public class QuickAITest {
    public static void main(String[] args) {
        System.out.println("Testing Hanabi AI players...");
        
        List<Player> players = List.of(
            new SimpleAIPlayer("AI_1"),
            new SimpleAIPlayer("AI_2")
        );
        
        GameEngine game = new GameEngine(players);
        
        // Initialize all players
        for (Player player : players) {
            player.initialize(game.getPlayerGameState(player));
        }
        
        System.out.println("Game started with " + players.size() + " AI players");
        System.out.println("Initial state - Info: " + game.getGameState().getInfoTokens() + 
                          ", Fuses: " + game.getGameState().getFuseTokens() + 
                          ", Deck: " + game.getGameState().getDeckSize());
        
        int turnCount = 0;
        while (!game.isGameOver() && turnCount < 20) { // Limit to 20 turns for quick test
            Player currentPlayer = game.getCurrentPlayer();
            System.out.println("\nTurn " + (turnCount + 1) + " - " + currentPlayer.getName() + "'s turn");
            
            Action action = currentPlayer.takeTurn(game.getPlayerGameState(currentPlayer));
            boolean success = game.executeAction(action);
            
            System.out.println("Action: " + action.getClass().getSimpleName() + " - " + 
                              (success ? "Success" : "Failed"));
            System.out.println("Score: " + game.getGameState().calculateScore() + 
                              ", Info: " + game.getGameState().getInfoTokens() + 
                              ", Fuses: " + game.getGameState().getFuseTokens());
            
            turnCount++;
        }
        
        System.out.println("\nTest completed!");
        System.out.println("Final Score: " + game.getScore());
        System.out.println("Game Over: " + game.isGameOver());
    }
}