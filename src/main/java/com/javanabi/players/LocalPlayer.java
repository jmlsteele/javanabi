package com.javanabi.players;

import com.javanabi.domain.Card;
import com.javanabi.game.Player;
import com.javanabi.game.action.*;
import com.javanabi.game.state.GameState;

import java.util.List;
import java.util.Scanner;

public class LocalPlayer implements Player {
    private final String name;
    private final Scanner scanner;
    private GameState currentState;
    
    public LocalPlayer(String name) {
        this.name = name;
        this.scanner = new Scanner(System.in);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void initialize(GameState initialState) {
        this.currentState = initialState;
        System.out.println("\n=== " + name + " initialized ===");
        displayGameState();
    }
    
    @Override
    public Action takeTurn(GameState currentState) {
        this.currentState = currentState;
        System.out.println("\n=== " + name + "'s Turn ===");
        displayGameState();
        
        while (true) {
            System.out.println("\nChoose your action:");
            System.out.println("1. Play card");
            System.out.println("2. Discard card");
            if (currentState.getInfoTokens() > 0) {
                System.out.println("3. Give hint");
            }
            System.out.print("Enter choice (1-" + (currentState.getInfoTokens() > 0 ? "3" : "2") + "): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        return handlePlayCard();
                    case 2:
                        return handleDiscardCard();
                    case 3:
                        if (currentState.getInfoTokens() > 0) {
                            return handleGiveHint();
                        } else {
                            System.out.println("No info tokens available!");
                        }
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void receiveClue(Clue clue) {
        System.out.println("\n" + name + " received clue: " + clue);
    }
    
    @Override
    public void notifyPlayerAction(String playerName, Action action) {
        System.out.println("\n" + playerName + " performed: " + action);
    }
    
    @Override
    public void notifyGameEnd(int score, boolean won) {
        System.out.println("\n=== Game Over ===");
        System.out.println("Final Score: " + score);
        System.out.println("Result: " + (won ? "WON!" : "LOST"));
    }
    
    private void displayGameState() {
        System.out.println("\n--- Game State ---");
        System.out.println("Info Tokens: " + currentState.getInfoTokens());
        System.out.println("Fuse Tokens: " + currentState.getFuseTokens());
        System.out.println("Played Cards: " + currentState.getPlayedCards());
        System.out.println("Discarded Cards: " + currentState.getDiscardedCards());
        
        System.out.println("\n--- Your Hand ---");
        List<Card> hand = currentState.getPlayerHand(name);
        for (int i = 0; i < hand.size(); i++) {
            System.out.println(i + ": " + hand.get(i));
        }
        
        System.out.println("\n--- Other Players ---");
        for (String player : currentState.getPlayers()) {
            if (!player.equals(name)) {
                System.out.println(player + "'s hand: " + currentState.getPlayerHand(player));
            }
        }
    }
    
    private Action handlePlayCard() {
        List<Card> hand = currentState.getPlayerHand(name);
        System.out.print("Enter card index to play (0-" + (hand.size() - 1) + "): ");
        
        int index = Integer.parseInt(scanner.nextLine().trim());
        if (index < 0 || index >= hand.size()) {
            throw new IllegalArgumentException("Invalid card index");
        }
        
        return new PlayCardAction(index);
    }
    
    private Action handleDiscardCard() {
        List<Card> hand = currentState.getPlayerHand(name);
        System.out.print("Enter card index to discard (0-" + (hand.size() - 1) + "): ");
        
        int index = Integer.parseInt(scanner.nextLine().trim());
        if (index < 0 || index >= hand.size()) {
            throw new IllegalArgumentException("Invalid card index");
        }
        
        return new DiscardCardAction(index);
    }
    
    private Action handleGiveHint() {
        System.out.println("\n--- Give Hint ---");
        
        // Select target player
        List<String> otherPlayers = currentState.getPlayers().stream()
                .filter(p -> !p.equals(name))
                .toList();
        
        System.out.println("Select target player:");
        for (int i = 0; i < otherPlayers.size(); i++) {
            System.out.println(i + ": " + otherPlayers.get(i));
        }
        System.out.print("Enter player index (0-" + (otherPlayers.size() - 1) + "): ");
        
        int playerIndex = Integer.parseInt(scanner.nextLine().trim());
        if (playerIndex < 0 || playerIndex >= otherPlayers.size()) {
            throw new IllegalArgumentException("Invalid player index");
        }
        
        String targetPlayer = otherPlayers.get(playerIndex);
        List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
        
        // Select hint type
        System.out.println("\nSelect hint type:");
        System.out.println("1. Color (Suit)");
        System.out.println("2. Number (Rank)");
        System.out.print("Enter choice (1-2): ");
        
        int hintType = Integer.parseInt(scanner.nextLine().trim());
        
        if (hintType == 1) {
            // Color hint
            System.out.println("\nAvailable colors:");
            Card.Suit[] suits = Card.Suit.values();
            for (int i = 0; i < suits.length; i++) {
                System.out.println(i + ": " + suits[i]);
            }
            System.out.print("Enter color index (0-" + (suits.length - 1) + "): ");
            
            int colorIndex = Integer.parseInt(scanner.nextLine().trim());
            if (colorIndex < 0 || colorIndex >= suits.length) {
                throw new IllegalArgumentException("Invalid color index");
            }
            
            Card.Suit selectedSuit = suits[colorIndex];
            
            // Find matching cards
            java.util.ArrayList<Integer> matchingIndices = new java.util.ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                if (targetHand.get(i).getSuit() == selectedSuit) {
                    matchingIndices.add(i);
                }
            }
            
            if (matchingIndices.isEmpty()) {
                throw new IllegalArgumentException("No cards match that color");
            }
            
            Clue clue = new Clue(Player.ClueType.SUIT, selectedSuit, matchingIndices);
            return new GiveInfoAction(targetPlayer, clue);
            
        } else if (hintType == 2) {
            // Number hint
            System.out.println("\nAvailable numbers:");
            for (int i = 1; i <= 5; i++) {
                System.out.println(i + ": " + i);
            }
            System.out.print("Enter number (1-5): ");
            
            int rank = Integer.parseInt(scanner.nextLine().trim());
            if (rank < 1 || rank > 5) {
                throw new IllegalArgumentException("Invalid rank");
            }
            
            // Find matching cards
            java.util.ArrayList<Integer> matchingIndices = new java.util.ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                if (targetHand.get(i).getRank() == rank) {
                    matchingIndices.add(i);
                }
            }
            
            if (matchingIndices.isEmpty()) {
                throw new IllegalArgumentException("No cards match that number");
            }
            
            Clue clue = new Clue(Player.ClueType.RANK, rank, matchingIndices);
            return new GiveInfoAction(targetPlayer, clue);
            
        } else {
            throw new IllegalArgumentException("Invalid hint type");
        }
    }
}