package com.javanabi;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.SimpleTestPlayer;

import java.util.List;

public class HanabiServer {
    public static void main(String[] args) {
        System.out.println("Starting Hanabi Server...");
        
        List<Player> players = List.of(
            new SimpleTestPlayer("Player1"),
            new SimpleTestPlayer("Player2"),
            new SimpleTestPlayer("Player3")
        );
        
        GameEngine game = new GameEngine(players);
        System.out.println("Game created with " + players.size() + " players");
        System.out.println("Initial game state:");
        System.out.println("  Info tokens: " + game.getGameState().getInfoTokens());
        System.out.println("  Fuse tokens: " + game.getGameState().getFuseTokens());
        System.out.println("  Deck size: " + game.getGameState().getDeckSize());
        System.out.println("  Current player: " + game.getCurrentPlayer().getName());
        
        System.out.println("\nGame ready! Players can connect to start playing.");
    }
}