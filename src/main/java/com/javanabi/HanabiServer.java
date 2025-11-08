package com.javanabi;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.action.Action;
import com.javanabi.game.action.DiscardCardAction;
import com.javanabi.game.action.GiveInfoAction;
import com.javanabi.game.action.PlayCardAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HanabiServer {
    public static void main(String[] args) {
        // Validate player count
        if (args.length < 2 || args.length > 5) {
            System.err.println("Usage: java HanabiServer <PlayerClass1> <PlayerClass2> [PlayerClass3] [PlayerClass4] [PlayerClass5]");
            System.err.println("Valid player count: 2-5");
            System.err.println("Example: java HanabiServer SimpleAIPlayer SimpleAIPlayer AdvancedAIPlayer");
            System.err.println("Available player classes: SimpleAIPlayer");
            System.exit(1);
        }
        
        System.out.println("Starting Hanabi Server...");
        
        // Create players dynamically based on arguments
        List<Player> players = new ArrayList<>();
        
        for (int i = 0; i < args.length; i++) {
            String className = args[i];
            String playerName = "Player " + (i + 1);
            
            try {
                // Try to load the class from com.javanabi.game package
                String fullClassName = "com.javanabi.players." + className;
                Class<?> playerClass = Class.forName(fullClassName);
                
                // Create instance using constructor that takes String parameter (player name)
                Player player =  (Player) playerClass.getDeclaredConstructor(String.class).newInstance(playerName);
                players.add(player);
                System.out.println("Created " + playerName + " as " + className);
            } catch (Exception e) {
                System.err.println("Error creating " + playerName + " as " + className + ": " + e.getMessage());
                System.exit(1);
            }
        }
        
        GameEngine game = new GameEngine(players);
        System.out.println("Game created with " + players.size() + " players");
        System.out.println("Players: " + getPlayerNames(players));
        System.out.println("Initial game state:");
        System.out.println("  Info tokens: " + game.getGameState().getInfoTokens());
        System.out.println("  Fuse tokens: " + game.getGameState().getFuseTokens());
        System.out.println("  Deck size: " + game.getGameState().getDeckSize());
        System.out.println("  Current player: " + game.getCurrentPlayer().getName());
        
        System.out.println("\nStarting game simulation...");
        
        while (!game.isGameOver()) {
            Player currentPlayer = game.getCurrentPlayer();
            System.out.println("\n" + currentPlayer.getName() + "'s turn:");
            
            Action action = currentPlayer.takeTurn(game.getPlayerGameState(currentPlayer));
            boolean success = game.executeAction(action);
            
            if (success) {
                System.out.println("Action executed: " + getActionDescription(action));
                System.out.println("  Info tokens: " + game.getGameState().getInfoTokens());
                System.out.println("  Fuse tokens: " + game.getGameState().getFuseTokens());
                System.out.println("  Deck size: " + game.getGameState().getDeckSize());
                System.out.println("  Score: " + game.getGameState().calculateScore());
            } else {
                System.out.println("Invalid action!");
            }
        }
        
        System.out.println("\nGame Over!");
        System.out.println("Final Score: " + game.getScore());
        System.out.println("Game Won: " + (game.getScore() == 25));
    }
    
    private static String getPlayerNames(List<Player> players) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(players.get(i).getName());
        }
        return names.toString();
    }
    
    private static String getActionDescription(Action action) {
        return action.accept(new Action.ActionVisitor<String>() {
            @Override
            public String visit(GiveInfoAction giveInfoAction) {
                return "Give info about " + giveInfoAction.getClueType() + " " + giveInfoAction.getClueValue() + 
                       " to " + giveInfoAction.getTargetPlayer();
            }
            
            @Override
            public String visit(PlayCardAction playCardAction) {
                return "Play card at position " + (playCardAction.getHandIndex() + 1);
            }
            
            @Override
            public String visit(DiscardCardAction discardCardAction) {
                return "Discard card at position " + (discardCardAction.getHandIndex() + 1);
            }
        });
    }
}