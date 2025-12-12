package com.javanabi.game;

import com.javanabi.domain.Card;

import java.util.*;

public class Deck {
    private final Queue<Card> cards;
    public static final int[] RANK_COUNTS = {3, 2, 2, 2, 1};
    
    public Deck() {
        this.cards = new ArrayDeque<>();
        initializeDeck();
    }
    
    private void initializeDeck() {
        cards.clear();
        
        for (Card.Suit suit : Card.Suit.values()) {
            for (int rank = 1; rank <= 5; rank++) {
                int count = RANK_COUNTS[rank - 1];
                for (int i = 0; i < count; i++) {
                    cards.offer(new Card(suit, rank));
                }
            }
        }
    }
    
    public void shuffle() {
        List<Card> cardList = new ArrayList<>(cards);
        Collections.shuffle(cardList);
        cards.clear();
        cards.addAll(cardList);
    }
    
    public Card drawCard() {
        return cards.poll();
    }
    
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    public int size() {
        return cards.size();
    }
    
    public List<Card> getRemainingCards() {
        return new ArrayList<>(cards);
    }
}