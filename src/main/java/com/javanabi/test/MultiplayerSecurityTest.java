package com.javanabi.test;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.state.GameState;
import com.javanabi.players.SimpleAIPlayer;

import java.util.List;

public class MultiplayerSecurityTest {
    public static void main(String[] args) {
        System.out.println("Testing multiplayer hand access security...");
        
        List<Player> players = List.of(
            new SimpleAIPlayer("Player1"),
            new SimpleAIPlayer("Player2"),
            new SimpleAIPlayer("Player3")
        );
        
        GameEngine game = new GameEngine(players);
        
        // Test each player's view
        for (Player testPlayer : players) {
            System.out.println("\nTesting " + testPlayer.getName() + "'s view:");
            GameState playerView = game.getPlayerGameState(testPlayer);
            
            // Check own hand (should be hidden)
            List<?> ownHand = playerView.getPlayerHand(testPlayer.getName());
            boolean ownHandHidden = ownHand.stream().allMatch(card -> card == null);
            System.out.println("  Own hand hidden: " + (ownHandHidden ? "✅" : "❌"));
            
            // Check other players' hands (should be visible)
            boolean otherHandsVisible = true;
            for (Player otherPlayer : players) {
                if (!otherPlayer.equals(testPlayer)) {
                    List<?> otherHand = playerView.getPlayerHand(otherPlayer.getName());
                    if (otherHand.stream().anyMatch(card -> card == null)) {
                        otherHandsVisible = false;
                        break;
                    }
                }
            }
            System.out.println("  Other hands visible: " + (otherHandsVisible ? "✅" : "❌"));
        }
        
        System.out.println("\nMultiplayer security test completed.");
    }
}