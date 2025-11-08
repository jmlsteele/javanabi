package com.javanabi.game.state;

import com.javanabi.domain.Card;

import java.util.*;

public final class GameState {
    private final Map<String, List<Card>> hands;
    private final Map<Card.Suit, List<Card>> playedCards;
    private final Map<Card.Suit, List<Card>> discardedCards;
    private final int infoTokens;
    private final int fuseTokens;
    private final int currentPlayerIndex;
    private final List<String> players;
    private final int finalPlayerIndex;
    private final int deckSize;
    
    private GameState(Builder builder) {
        this.hands = Collections.unmodifiableMap(new HashMap<>(builder.hands));
        this.playedCards = Collections.unmodifiableMap(new HashMap<>(builder.playedCards));
        this.discardedCards = Collections.unmodifiableMap(new HashMap<>(builder.discardedCards));
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
    
    public static GameState initialGameState(List<String> players) {
        Builder builder = new Builder();
        builder.players = players;
        builder.infoTokens = 8;
        builder.fuseTokens = 3;
        builder.currentPlayerIndex = 0;
        builder.finalPlayerIndex = -1;
        builder.deckSize = 50;
        
        for (String player : players) {
            builder.hands.put(player, new ArrayList<>());
        }
        
        for (Card.Suit suit : Card.Suit.values()) {
            builder.playedCards.put(suit, new ArrayList<>());
            builder.discardedCards.put(suit, new ArrayList<>());
        }
        
        return builder.build();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cards in Deck: " + deckSize + "\n");
        sb.append("Cards Played:\n");
        for (Card.Suit s:Card.Suit.values()) {
            int size = playedCards.get(s).size();
            if (size > 0) {
                sb.append("\t" + s);
                for (int i=0;i<size;i++) {
                    sb.append(" " + (i+1));
                }
                sb.append("\n");
            }
        }
        sb.append("Cards Discarded:\n");
        //separate into suits
        for (Card.Suit s:Card.Suit.values()) {
            sb.append("\t" + s + ":");
            Collections.sort(discardedCards.get(s), new Comparator<Card>() {
                @Override
                public int compare(Card arg0, Card arg1) {
                    return arg0.getRank() - arg1.getRank();
                }

            });
            for (Card c: discardedCards.get(s)) {
                sb.append(" " + c.getRank());
            }
            sb.append("\n");
        }
        sb.append("Hands:\n");
        for (String p: players) {
            sb.append("\t" + p + ": ");
            sb.append(hands.get(p));
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public Map<String, List<Card>> getHands() {
        return hands;
    }
    
    public List<Card> getPlayerHand(String player) {
        return hands.getOrDefault(player, Collections.emptyList());
    }
    
    public int getPlayerHandSize(String player) {
        List<Card> hand = hands.get(player);
        return hand != null ? hand.size() : 0;
    }
    
    public GameState getPlayerView(String player) {
        // Create a filtered view where the player's own hand is hidden
        Map<String, List<Card>> filteredHands = new HashMap<>(hands);
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
            .discardedCards(discardedCards)
            .infoTokens(infoTokens)
            .fuseTokens(fuseTokens)
            .finalPlayerIndex(finalPlayerIndex)
            .deckSize(deckSize)
            .build();
    }
    
    public Map<Card.Suit, List<Card>> getPlayedCards() {
        return playedCards;
    }

    public Map<Card.Suit, List<Card>> getDiscardedCards() {
        return discardedCards;
    }

    public List<Card> getPlayableCards() {
        List<Card>ret = new ArrayList<Card>();
        for (Card.Suit s :Card.Suit.values()) {
            int maxPlayed = playedCards.get(s).size();
            if (maxPlayed < 5) ret.add(new Card(s,maxPlayed+1));
        }
        return ret;
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
    
    public String getCurrentPlayer() {
        if (players.isEmpty()) return null;
        return players.get(currentPlayerIndex);
    }
    
    public List<String> getPlayers() {
        return players;
    }
    
    public int getFinalPlayerIndex() {
        return finalPlayerIndex;
    }

    public boolean isGameOver() {
        return fuseTokens == 0 || (deckSize == 0 && currentPlayerIndex == finalPlayerIndex);
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
        private Map<String, List<Card>> hands = new HashMap<>();
        private Map<Card.Suit, List<Card>> playedCards = new HashMap<>();
        private Map<Card.Suit, List<Card>> discardedCards = new HashMap<>();
        private int infoTokens = 8;
        private int fuseTokens = 3;
        private int currentPlayerIndex = 0;
        private List<String> players = new ArrayList<>();
        private int finalPlayerIndex = -1;
        private int deckSize = 50;
        
        public Builder hands(Map<String, List<Card>> hands) {
            this.hands = hands;
            return this;
        }
        
        public Builder playedCards(Map<Card.Suit, List<Card>> playedCards) {
            this.playedCards = playedCards;
            return this;
        }

        public Builder discardedCards(Map<Card.Suit, List<Card>> discardedCards) {
            this.discardedCards = discardedCards;
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
        
        public Builder players(List<String> players) {
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