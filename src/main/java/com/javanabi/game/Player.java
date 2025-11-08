package com.javanabi.game;

import java.util.List;

import com.javanabi.game.action.Action;
import com.javanabi.game.state.GameState;

public interface Player {
    String getName();
    
    void initialize(GameState initialState);
    
    Action takeTurn(GameState currentState);
    
    void receiveClue(Clue clue);

    void drawCard();
    
    void notifyPlayerAction(String playerName, Action action);
    
    void notifyGameEnd(int score, boolean won);
    
    public enum ClueType {
        SUIT, RANK
    }
    
    public static final class Clue {
        private final ClueType type;
        private final Object value;
        private final List<Integer> cardIndices;
        
        public Clue(ClueType type, Object value, List<Integer> cardIndices) {
            this.type = type;
            this.value = value;
            this.cardIndices = cardIndices;
        }
        
        public ClueType getType() { return type; }
        public Object getValue() { return value; }
        public List<Integer> getCardIndices() { return cardIndices; }
    }
}