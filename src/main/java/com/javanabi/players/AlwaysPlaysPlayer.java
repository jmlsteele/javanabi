package com.javanabi.players;

import com.javanabi.game.Player;
import com.javanabi.game.action.Action;
import com.javanabi.game.action.PlayCardAction;
import com.javanabi.game.state.GameState;

public class AlwaysPlaysPlayer implements Player {
    private final String name;
    
    public AlwaysPlaysPlayer(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void initialize(GameState initialState) {
        System.out.println(name + " initialized.");
    }

    @Override
    public Action takeTurn(GameState currentState) {
        System.out.println(name + " is playing Card 1");
        return new PlayCardAction(0);
    }
    
    @Override
    public void receiveClue(Clue clue) {
        System.out.println(name + " received clue: " + clue.getType() + " = " + clue.getValue());
    }
    
    @Override
    public void notifyPlayerAction(String playerName, Action action) {
        System.out.println(name + " notified: " + playerName + " took action");
    }
    
    @Override
    public void notifyGameEnd(int score, boolean won) {
        System.out.println(name + " - Game ended! Score: " + score + ", Won: " + won);
    }

    @Override
    public void drawCard() {
        System.out.println(name + " - drew card!");
        
    }

}