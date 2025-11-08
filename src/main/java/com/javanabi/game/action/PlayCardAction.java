package com.javanabi.game.action;

import com.javanabi.domain.Card;

public final class PlayCardAction implements Action {
    private final int handIndex;
    private Card card;
    
    public PlayCardAction(int handIndex) {
        if (handIndex < 0) {
            throw new IllegalArgumentException("Hand index cannot be negative");
        }
        this.handIndex = handIndex;
    }
    
    public int getHandIndex() {
        return handIndex;
    }
    
    public void setCard(Card card) {
        this.card=card;
    }

    public Card getCard() {
        return this.card;
    }

    @Override
    public <T> T accept(ActionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}