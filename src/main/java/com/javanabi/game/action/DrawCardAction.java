package com.javanabi.game.action;

import com.javanabi.domain.Card;

public final class DrawCardAction implements Action {
    private Card card;
    public DrawCardAction() {
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