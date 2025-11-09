package com.javanabi.game;

import com.javanabi.domain.Card;
import com.javanabi.game.action.*;
import com.javanabi.game.state.GameState;
import com.javanabi.players.AlwaysPlaysPlayer;
import com.javanabi.players.SimpleAIPlayer;

import java.util.*;

public class SimpleAITest {
    public static void main(String[] args) {
        System.out.println("Testing SimpleAIPlayer logic...");
        
        // Test 1: AI should play 100% certain card
        testCertainCardPlay();
        
        // Test 2: AI should give useful hint
        testUsefulHint();
        
        // Test 3: AI should discard useless card
        testUselessCardDiscard();
        
        System.out.println("All tests completed!");
    }
    
    private static void testCertainCardPlay() {
        System.out.println("\n=== Test 1: Certain Card Play ===");
        
        SimpleAIPlayer ai = new SimpleAIPlayer("TestAI");
        
        // Create a mock game state where AI has a known playable card
        GameState.Builder stateBuilder = GameState.builder();
        
        // Set up played cards (nothing played yet, so 1s are playable)
        Map<Card.Suit, List<Card>> playedCards = new HashMap<>();
        for (Card.Suit suit : Card.Suit.values()) {
            playedCards.put(suit, new ArrayList<>());
        }
        stateBuilder.playedCards(playedCards);
        
        // Set up AI hand with a known 1
        List<Card> aiHand = Arrays.asList(new Card(Card.Suit.BLUE, 1));
        Map<String, List<Card>> hands = new HashMap<>();
        hands.put(ai.getName(), aiHand);
        stateBuilder.hands(hands);
        
        // Set up other players
        List<String> players = Arrays.asList(ai.getName(), new AlwaysPlaysPlayer("Other").getName());
        stateBuilder.players(players);
        stateBuilder.currentPlayerIndex(0);
        stateBuilder.infoTokens(8);
        stateBuilder.fuseTokens(3);
        
        GameState testState = stateBuilder.build();
        // Manually set card knowledge to make the card 100% certain
        ai.receiveClue(new Player.Clue(Player.ClueType.SUIT, Card.Suit.BLUE, Arrays.asList(0)));
        ai.receiveClue(new Player.Clue(Player.ClueType.RANK, 1, Arrays.asList(0)));
        
        Action action = ai.takeTurn(testState);
        
        if (action instanceof PlayCardAction) {
            System.out.println("✓ AI correctly chose to play certain card");
        } else {
            System.out.println("✗ AI should have played certain card, but chose: " + action.getClass().getSimpleName());
        }
    }
    
    private static void testUsefulHint() {
        System.out.println("\n=== Test 2: Useful Hint ===");
        
        SimpleAIPlayer ai = new SimpleAIPlayer("TestAI");
        AlwaysPlaysPlayer other = new AlwaysPlaysPlayer("Other");
        
        // Create game state where other player has a playable card
        GameState.Builder stateBuilder = GameState.builder();
        
        Map<Card.Suit, List<Card>> playedCards = new HashMap<>();
        for (Card.Suit suit : Card.Suit.values()) {
            playedCards.put(suit, new ArrayList<>());
        }
        stateBuilder.playedCards(playedCards);
        
        // AI has no certain playable cards
        List<Card> aiHand = Arrays.asList(new Card(Card.Suit.RED, 2));
        // Other player has a 1 that could be played
        List<Card> otherHand = Arrays.asList(new Card(Card.Suit.BLUE, 1));
        
        Map<String, List<Card>> hands = new HashMap<>();
        hands.put(ai.getName(), aiHand);
        hands.put(other.getName(), otherHand);
        stateBuilder.hands(hands);
        
        List<String> players = Arrays.asList(ai.getName(), other.getName());
        stateBuilder.players(players);
        stateBuilder.currentPlayerIndex(0);
        stateBuilder.infoTokens(8);
        stateBuilder.fuseTokens(3);
        
        GameState testState = stateBuilder.build();
        ai.initialize(testState);
        
        Action action = ai.takeTurn(testState);
        
        if (action instanceof GiveInfoAction) {
            System.out.println("✓ AI correctly chose to give hint");
        } else {
            System.out.println("✗ AI should have given hint, but chose: " + action.getClass().getSimpleName());
        }
    }
    
    private static void testUselessCardDiscard() {
        System.out.println("\n=== Test 3: Useless Card Discard ===");
        
        SimpleAIPlayer ai = new SimpleAIPlayer("TestAI");
        AlwaysPlaysPlayer other = new AlwaysPlaysPlayer("Other");
        
        // Create game state where AI has a useless card
        GameState.Builder stateBuilder = GameState.builder();
        
        Map<Card.Suit, List<Card>> playedCards = new HashMap<>();
        for (Card.Suit suit : Card.Suit.values()) {
            playedCards.put(suit, new ArrayList<>());
        }
        // Blue 2 is already played, so blue 1 is useless
        playedCards.get(Card.Suit.BLUE).add(new Card(Card.Suit.BLUE, 2));
        stateBuilder.playedCards(playedCards);
        
        // AI has a known useless card (blue 1 when blue 2 is played)
        List<Card> aiHand = Arrays.asList(new Card(Card.Suit.BLUE, 1));
        List<Card> otherHand = Arrays.asList(new Card(Card.Suit.RED, 1));
        
        Map<String, List<Card>> hands = new HashMap<>();
        hands.put(ai.getName(), aiHand);
        hands.put(other.getName(), otherHand);
        stateBuilder.hands(hands);
        
        List<String> players = Arrays.asList(ai.getName(), other.getName());
        stateBuilder.players(players);
        stateBuilder.currentPlayerIndex(0);
        stateBuilder.infoTokens(0); // No hints available
        stateBuilder.fuseTokens(3);
        
        GameState testState = stateBuilder.build();
        ai.initialize(testState);
        
        // Make AI aware of the useless card
        ai.receiveClue(new Player.Clue(Player.ClueType.SUIT, Card.Suit.BLUE, Arrays.asList(0)));
        ai.receiveClue(new Player.Clue(Player.ClueType.RANK, 1, Arrays.asList(0)));
        
        Action action = ai.takeTurn(testState);
        
        if (action instanceof DiscardCardAction) {
            System.out.println("✓ AI correctly chose to discard useless card");
        } else {
            System.out.println("✗ AI should have discarded useless card, but chose: " + action.getClass().getSimpleName());
        }
    }
}