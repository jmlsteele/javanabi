package com.javanabi.util;

import com.javanabi.domain.Card;
import com.javanabi.game.Player;

import java.util.*;

public final class CardKnowledge {
    private Set<Card.Suit> possibleSuits;
    private Set<Integer> possibleRanks;
    
    public CardKnowledge() {
        this.possibleSuits = EnumSet.allOf(Card.Suit.class);
        this.possibleRanks = new HashSet<>(Set.of(1, 2, 3, 4, 5));
    }
    
    public void applyClue(Player.Clue clue) {
        if (clue.getType() == Player.ClueType.SUIT) {
            Card.Suit clueSuit = (Card.Suit) clue.getValue();
            this.possibleSuits.clear();
            this.possibleSuits.add(clueSuit);
            
        } else if (clue.getType() == Player.ClueType.RANK) {
            int clueRank = (Integer) clue.getValue();
            this.possibleRanks.clear();
            this.possibleRanks.add(clueRank);
        }
    }
    
    public void applyNegativeClue(Player.Clue clue) {
        if (clue.getType() == Player.ClueType.SUIT) {
            Card.Suit clueSuit = (Card.Suit) clue.getValue();
            this.possibleSuits.remove(clueSuit);
            
        } else if (clue.getType() == Player.ClueType.RANK) {
            int clueRank = (Integer) clue.getValue();
            this.possibleRanks.remove(clueRank);
        }
    }


    public Set<Card.Suit> getPossibleSuits() {
        return Collections.unmodifiableSet(possibleSuits);
    }
    
    public Set<Integer> getPossibleRanks() {
        return Collections.unmodifiableSet(possibleRanks);
    }
    
    public boolean isKnownSuit() {
        return this.possibleSuits.size() == 1;
    }
    
    public boolean isKnownRank() {
        return this.possibleRanks.size() == 1;
    }
    
    public Card.Suit getKnownSuit() {
        if (!isKnownSuit()) {
            throw new IllegalStateException("Suit is not known");
        }
        return possibleSuits.iterator().next();
    }
    
    public int getKnownRank() {
        if (!isKnownRank()) {
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
                           possibleSuits, possibleRanks, isKnownSuit(), isKnownRank());
    }
}