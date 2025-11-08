package com.javanabi.game.action;

public final class PlayCardAction implements Action {
    private final int handIndex;
    
    public PlayCardAction(int handIndex) {
        if (handIndex < 0) {
            throw new IllegalArgumentException("Hand index cannot be negative");
        }
        this.handIndex = handIndex;
    }
    
    public int getHandIndex() {
        return handIndex;
    }
    
    @Override
    public <T> T accept(ActionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}