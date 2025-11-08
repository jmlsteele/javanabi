package com.javanabi.game.action;

import com.javanabi.game.Player;

public interface Action {
    <T> T accept(ActionVisitor<T> visitor);
    
    interface ActionVisitor<T> {
        T visit(GiveInfoAction giveInfoAction);
        T visit(PlayCardAction playCardAction);
        T visit(DiscardCardAction discardCardAction);
    }
}