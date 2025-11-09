package com.javanabi.game.action;

import com.javanabi.game.Player.Clue;

public final class GiveInfoAction implements Action {
    private final String targetPlayer;
    private final Clue clue;
    
    public GiveInfoAction(String targetPlayer, Clue clue) {
        this.targetPlayer = targetPlayer;
        this.clue = clue;
    }
    
    public String getTargetPlayer() {
        return targetPlayer;
    }
    
    public Clue getClue() {
        return clue;
    }
    
    @Override
    public <T> T accept(ActionVisitor<T> visitor) {
        return visitor.visit(this);
    }
    public String toString() {
        return this.targetPlayer + ": " + this.clue;
    }
}