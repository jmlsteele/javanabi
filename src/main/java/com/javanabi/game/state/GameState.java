package com.javanabi.game.state;

import com.javanabi.domain.Card;
import com.javanabi.game.Player;

import java.util.*;

public final class GameState {
    private final Map<Player, List<Card>> hands;
    private final Map<Card.Suit, List<Card>> playedCards;
    private final List<Card> discardPile;
    private final int infoTokens;
    private final int fuseTokens;
    private final int currentPlayerIndex;
    private final List<Player> players;
    private final int finalPlayerIndex;
    private final int deckSize;
    
    private GameState(Builder builder) {
        this.hands = Collections.unmodifiableMap(new HashMap<>(builder.hands));
        this.playedCards = Collections.unmodifiableMap(new HashMap<>(builder.playedCards));
        this.discardPile = Collections.unmodifiableList(new ArrayList<>(builder.discardPile));
        this.infoTokens = builder.infoTokens;
        this.fuseTokens = builder.fuseTokens;
        this.currentPlayerIndex = builder.currentPlayerIndex;
        this.players = Collections.unmodifiableList(new ArrayList<>(builder.players));
        this.finalPlayerIndex = builder.finalPlayerIndex;
        this.deckSize = builder.deckSize;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static GameState initialGameState(List<Player> players) {
        Builder builder = new Builder();
        builder.players = new ArrayList<>(players);
        builder.infoTokens = 8;
        builder.fuseTokens = 3;
        builder.currentPlayerIndex = 0;
        builder.finalPlayerIndex = -1;
        builder.deckSize = 50;
        
        for (Player player : players) {
            builder.hands.put(player, new ArrayList<>());
        }
        
        for (Card.Suit suit : Card.Suit.values()) {
            builder.playedCards.put(suit, new ArrayList<>());
        }
        
        return builder.build();
    }
    
    public Map<Player, List<Card>> getHands() {
        return hands;
    }
    
    public List<Card> getPlayerHand(Player player) {
        return hands.getOrDefault(player, Collections.emptyList());
    }
    
    public int getPlayerHandSize(Player player) {
        List<Card> hand = hands.get(player);
        return hand != null ? hand.size() : 0;
    }
    
    public GameState getPlayerView(Player player) {
        // Create a filtered view where the player's own hand is hidden
        Map<Player, List<Card>> filteredHands = new HashMap<>(hands);
        // Replace the player's own hand with a list of null cards (same size, but no actual cards)
        List<Card> ownHand = hands.get(player);
        if (ownHand != null) {
            filteredHands.put(player, Collections.unmodifiableList(
                Collections.nCopies(ownHand.size(), null)
            ));
        }
        
        return GameState.builder()
            .players(players)
            .currentPlayerIndex(currentPlayerIndex)
            .hands(Collections.unmodifiableMap(filteredHands))
            .playedCards(playedCards)
            .discardPile(discardPile)
            .infoTokens(infoTokens)
            .fuseTokens(fuseTokens)
            .finalPlayerIndex(finalPlayerIndex)
            .deckSize(deckSize)
            .build();
    }
    
    public Map<Card.Suit, List<Card>> getPlayedCards() {
        return playedCards;
    }
    
    public List<Card> getPlayableCards() {
        List<Card>ret = new ArrayList<Card>();
        for (Card.Suit s :Card.Suit.values()) {
            int maxPlayed = playedCards.get(s).size();
            if (maxPlayed < 5) ret.add(new Card(s,maxPlayed+1));
        }
        return ret;
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }
    
    public int getInfoTokens() {
        return infoTokens;
    }
    
    public int getFuseTokens() {
        return fuseTokens;
    }
    
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    public Player getCurrentPlayer() {
        if (players.isEmpty()) return null;
        return players.get(currentPlayerIndex);
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public int getFinalPlayerIndex() {
        return finalPlayerIndex;
    }

    public boolean isGameOver() {
        return deckSize == 0 && currentPlayerIndex == finalPlayerIndex;
    }
    
    public int getDeckSize() {
        return deckSize;
    }
    
    public int calculateScore() {
        int score = 0;
        for (List<Card> suitCards : playedCards.values()) {
            if (!suitCards.isEmpty()) {
                score += suitCards.get(suitCards.size() - 1).getRank();
            }
        }
        return score;
    }
    
    public boolean isGameWon() {
        for (List<Card> suitCards : playedCards.values()) {
            if (suitCards.size() < 5 || suitCards.get(suitCards.size() - 1).getRank() != 5) {
                return false;
            }
        }
        return true;
    }
    
    public static class Builder {
        private Map<Player, List<Card>> hands = new HashMap<>();
        private Map<Card.Suit, List<Card>> playedCards = new HashMap<>();
        private List<Card> discardPile = new ArrayList<>();
        private int infoTokens = 8;
        private int fuseTokens = 3;
        private int currentPlayerIndex = 0;
        private List<Player> players = new ArrayList<>();
        private int finalPlayerIndex = -1;
        private int deckSize = 50;
        
        public Builder hands(Map<Player, List<Card>> hands) {
            this.hands = hands;
            return this;
        }
        
        public Builder playedCards(Map<Card.Suit, List<Card>> playedCards) {
            this.playedCards = playedCards;
            return this;
        }
        
        public Builder discardPile(List<Card> discardPile) {
            this.discardPile = discardPile;
            return this;
        }
        
        public Builder infoTokens(int infoTokens) {
            this.infoTokens = infoTokens;
            return this;
        }
        
        public Builder fuseTokens(int fuseTokens) {
            this.fuseTokens = fuseTokens;
            return this;
        }
        
        public Builder currentPlayerIndex(int currentPlayerIndex) {
            this.currentPlayerIndex = currentPlayerIndex;
            return this;
        }
        
        public Builder players(List<Player> players) {
            this.players = players;
            return this;
        }
        
        public Builder finalPlayerIndex(int finalPlayerIndex) {
            this.finalPlayerIndex = finalPlayerIndex;
            return this;
        }
        
        public Builder deckSize(int deckSize) {
            this.deckSize = deckSize;
            return this;
        }
        
        public GameState build() {
            return new GameState(this);
        }
    }
}