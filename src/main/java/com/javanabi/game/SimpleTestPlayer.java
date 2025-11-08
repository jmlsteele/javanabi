package com.javanabi.game;

import com.javanabi.domain.Card;
import com.javanabi.game.action.Action;
import com.javanabi.game.action.PlayCardAction;
import com.javanabi.game.state.GameState;

import java.util.ArrayList;
import java.util.List;

public class SimpleTestPlayer implements Player {
    private final String name;
    private GameState currentState;
    
    public SimpleTestPlayer(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void initialize(GameState initialState) {
        this.currentState = initialState;
    }
    
    @Override
    public Action takeTurn(GameState currentState) {
        this.currentState = currentState;
        List<Card> hand = currentState.getPlayerHand(this);
        if (!hand.isEmpty()) {
            return new PlayCardAction(0);
        }
        return null;
    }
    
    @Override
    public void receiveClue(Clue clue) {
        System.out.println(name + " received clue: " + clue.getType() + " = " + clue.getValue());
    }
    
    @Override
    public void notifyGameState(GameState state) {
        this.currentState = state;
    }
    
    @Override
    public void notifyPlayerAction(String playerName, Action action) {
        System.out.println(name + " notified: " + playerName + " took action");
    }
    
    @Override
    public void notifyGameEnd(int score, boolean won) {
        System.out.println(name + " - Game ended! Score: " + score + ", Won: " + won);
    }
}