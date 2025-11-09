package com.javanabi.test;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.state.GameState;
import com.javanabi.players.SimpleAIPlayer;

import java.util.List;

public class SecurityTest {
    public static void main(String[] args) {
        System.out.println("Testing player hand access security...");
        
        List<Player> players = List.of(
            new SimpleAIPlayer("TestPlayer"),
            new SimpleAIPlayer("OtherPlayer")
        );
        
        GameEngine game = new GameEngine(players);
        
        // Get the player's view of the game state
        Player testPlayer = players.get(0);
        GameState playerView = game.getPlayerGameState(testPlayer);
        
        // Try to access the player's own hand
        List<?> ownHand = playerView.getPlayerHand(testPlayer.getName());
        
        System.out.println("Player hand size: " + playerView.getPlayerHandSize(testPlayer.getName()));
        System.out.println("Hand contents: " + ownHand);
        
        // Verify that all cards are null (hidden)
        boolean allNull = true;
        for (Object card : ownHand) {
            if (card != null) {
                allNull = false;
                break;
            }
        }
        
        if (allNull) {
            System.out.println("✅ SECURITY TEST PASSED: Player cannot see their own cards");
        } else {
            System.out.println("❌ SECURITY TEST FAILED: Player can see their own cards!");
        }
        
        // Verify that other players' hands are still visible (in multiplayer games)
        System.out.println("Security test completed.");
    }
}