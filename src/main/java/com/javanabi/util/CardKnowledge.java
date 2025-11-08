package com.javanabi.util;

import com.javanabi.domain.Card;
import com.javanabi.game.Player;

import java.util.*;

public final class CardKnowledge {
    private final Set<Card.Suit> possibleSuits;
    private final Set<Integer> possibleRanks;
    private final boolean isKnownSuit;
    private final boolean isKnownRank;
    
    public CardKnowledge() {
        this.possibleSuits = EnumSet.allOf(Card.Suit.class);
        this.possibleRanks = new HashSet<>(Set.of(1, 2, 3, 4, 5));
        this.isKnownSuit = false;
        this.isKnownRank = false;
    }
    
    private CardKnowledge(Set<Card.Suit> possibleSuits, Set<Integer> possibleRanks, 
                         boolean isKnownSuit, boolean isKnownRank) {
        this.possibleSuits = EnumSet.copyOf(possibleSuits);
        this.possibleRanks = new HashSet<>(possibleRanks);
        this.isKnownSuit = isKnownSuit;
        this.isKnownRank = isKnownRank;
    }
    
    public CardKnowledge applyClue(Player.Clue clue) {
        Set<Card.Suit> newPossibleSuits = EnumSet.copyOf(possibleSuits);
        Set<Integer> newPossibleRanks = new HashSet<>(possibleRanks);
        boolean newIsKnownSuit = isKnownSuit;
        boolean newIsKnownRank = isKnownRank;
        
        if (clue.getType() == Player.ClueType.SUIT) {
            Card.Suit clueSuit = (Card.Suit) clue.getValue();
            boolean[] matches = new boolean[5];
            for (int index : clue.getCardIndices()) {
                matches[index] = true;
            }
            
            if (matches[0]) {
                newPossibleSuits = EnumSet.of(clueSuit);
                newIsKnownSuit = true;
            } else {
                newPossibleSuits.remove(clueSuit);
                if (newPossibleSuits.size() == 1) {
                    newIsKnownSuit = true;
                }
            }
        } else if (clue.getType() == Player.ClueType.RANK) {
            int clueRank = (Integer) clue.getValue();
            boolean[] matches = new boolean[5];
            for (int index : clue.getCardIndices()) {
                matches[index] = true;
            }
            
            if (matches[0]) {
                newPossibleRanks = Set.of(clueRank);
                newIsKnownRank = true;
            } else {
                newPossibleRanks.remove(clueRank);
                if (newPossibleRanks.size() == 1) {
                    newIsKnownRank = true;
                }
            }
        }
        
        return new CardKnowledge(newPossibleSuits, newPossibleRanks, newIsKnownSuit, newIsKnownRank);
    }
    
    public Set<Card.Suit> getPossibleSuits() {
        return Collections.unmodifiableSet(possibleSuits);
    }
    
    public Set<Integer> getPossibleRanks() {
        return Collections.unmodifiableSet(possibleRanks);
    }
    
    public boolean isKnownSuit() {
        return isKnownSuit;
    }
    
    public boolean isKnownRank() {
        return isKnownRank;
    }
    
    public Card.Suit getKnownSuit() {
        if (!isKnownSuit) {
            throw new IllegalStateException("Suit is not known");
        }
        return possibleSuits.iterator().next();
    }
    
    public int getKnownRank() {
        if (!isKnownRank) {
            throw new IllegalStateException("Rank is not known");
        }
        return possibleRanks.iterator().next();
    }
    
    public boolean isCardPossible(Card card) {
        return possibleSuits.contains(card.getSuit()) && possibleRanks.contains(card.getRank());
    }
    
    public int getPossibilityCount() {
        return possibleSuits.size() * possibleRanks.size();
    }
    
    @Override
    public String toString() {
        return String.format("CardKnowledge{suits=%s, ranks=%s, knownSuit=%s, knownRank=%s}", 
                           possibleSuits, possibleRanks, isKnownSuit, isKnownRank);
    }
}