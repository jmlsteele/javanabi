package com.javanabi.game.action;

import com.javanabi.game.Player;

public final class GiveInfoAction implements Action {
    private final String targetPlayer;
    private final Player.ClueType clueType;
    private final Object clueValue;
    
    public GiveInfoAction(String targetPlayer, Player.ClueType clueType, Object clueValue) {
        this.targetPlayer = targetPlayer;
        this.clueType = clueType;
        this.clueValue = clueValue;
    }
    
    public String getTargetPlayer() {
        return targetPlayer;
    }
    
    public Player.ClueType getClueType() {
        return clueType;
    }
    
    public Object getClueValue() {
        return clueValue;
    }
    
    @Override
    public <T> T accept(ActionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}