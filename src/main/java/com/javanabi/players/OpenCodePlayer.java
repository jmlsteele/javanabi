package com.javanabi.players;

import com.javanabi.game.Player;
import com.javanabi.game.action.Action;
import com.javanabi.game.state.GameState;

public class OpenCodePlayer implements Player {
    private String name;

    public OpenCodePlayer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void initialize(GameState initialState) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
    }

    @Override
    public Action takeTurn(GameState currentState) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'takeTurn'");
    }

    @Override
    public void receiveClue(Clue clue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveClue'");
    }

    @Override
    public void notifyPlayerAction(String playerName, Action action) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyPlayerAction'");
    }

    @Override
    public void notifyGameEnd(int score, boolean won) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyGameEnd'");
    }
}
