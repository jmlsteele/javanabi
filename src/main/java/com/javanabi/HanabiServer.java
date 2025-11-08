package com.javanabi;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.SimpleAIPlayer;
import com.javanabi.game.action.Action;
import com.javanabi.game.action.DiscardCardAction;
import com.javanabi.game.action.GiveInfoAction;
import com.javanabi.game.action.PlayCardAction;

import java.util.List;
import java.util.Scanner;

public class HanabiServer {
    public static void main(String[] args) {
        System.out.println("Starting Hanabi Server...");
        
        List<Player> players = List.of(
            new SimpleAIPlayer("AI_Player1"),
            new SimpleAIPlayer("AI_Player2"),
            new SimpleAIPlayer("AI_Player3")
        );
        
        GameEngine game = new GameEngine(players);
        System.out.println("Game created with " + players.size() + " AI players");
        System.out.println("Initial game state:");
        System.out.println("  Info tokens: " + game.getGameState().getInfoTokens());
        System.out.println("  Fuse tokens: " + game.getGameState().getFuseTokens());
        System.out.println("  Deck size: " + game.getGameState().getDeckSize());
        System.out.println("  Current player: " + game.getCurrentPlayer().getName());
        
        System.out.println("\nStarting AI vs AI game simulation...");
        System.out.println("Press Enter to play next turn, or 'auto' to play automatically");
        
        Scanner scanner = new Scanner(System.in);
        boolean autoPlay = false;
        
        while (!game.isGameOver()) {
            if (!autoPlay) {
                System.out.print("\n> ");
                String input = scanner.nextLine();
                if (input.trim().equalsIgnoreCase("auto")) {
                    autoPlay = true;
                }
            }
            
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
            
            if (autoPlay) {
                try {
                    Thread.sleep(1000); // Pause for readability
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        
        System.out.println("\nGame Over!");
        System.out.println("Final Score: " + game.getScore());
        System.out.println("Game Won: " + (game.getScore() == 25));
        
        scanner.close();
    }
    
    private static String getActionDescription(Action action) {
        return action.accept(new Action.ActionVisitor<String>() {
            @Override
            public String visit(GiveInfoAction giveInfoAction) {
                return "Give info about " + giveInfoAction.getClueType() + " " + giveInfoAction.getClueValue() + 
                       " to " + giveInfoAction.getTargetPlayer().getName();
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