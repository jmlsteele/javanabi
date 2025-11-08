package com.javanabi.util;

import com.javanabi.domain.Card;
import com.javanabi.game.Player;

import java.util.*;

public final class PlayerHand {
    private final List<Card> cards;
    private final List<CardKnowledge> knowledge;
    
    public PlayerHand() {
        this.cards = new ArrayList<>();
        this.knowledge = new ArrayList<>();
    }
    
    public PlayerHand(List<Card> initialCards) {
        this.cards = new ArrayList<>(initialCards);
        this.knowledge = new ArrayList<>();
        for (int i = 0; i < initialCards.size(); i++) {
            knowledge.add(new CardKnowledge());
        }
    }
    
    public void addCard(Card card) {
        cards.add(card);
        knowledge.add(new CardKnowledge());
    }
    
    public Card removeCard(int index) {
        if (index < 0 || index >= cards.size()) {
            throw new IndexOutOfBoundsException("Invalid card index: " + index);
        }
        Card removedCard = cards.remove(index);
        knowledge.remove(index);
        return removedCard;
    }
    
    public Card getCard(int index) {
        if (index < 0 || index >= cards.size()) {
            throw new IndexOutOfBoundsException("Invalid card index: " + index);
        }
        return cards.get(index);
    }
    
    public CardKnowledge getKnowledge(int index) {
        if (index < 0 || index >= knowledge.size()) {
            throw new IndexOutOfBoundsException("Invalid knowledge index: " + index);
        }
        return knowledge.get(index);
    }
    
    public void applyClue(Player.Clue clue) {
        for (int index : clue.getCardIndices()) {
            if (index >= 0 && index < knowledge.size()) {
                CardKnowledge currentKnowledge = knowledge.get(index);
                CardKnowledge updatedKnowledge = currentKnowledge.applyClue(clue);
                knowledge.set(index, updatedKnowledge);
            }
        }
    }
    
    public int size() {
        return cards.size();
    }
    
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }
    
    public List<CardKnowledge> getAllKnowledge() {
        return Collections.unmodifiableList(knowledge);
    }
    
    public boolean isEmpty() {
        return cards.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PlayerHand{cards=[");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(i).append(":").append(cards.get(i));
        }
        sb.append("]}");
        return sb.toString();
    }
}