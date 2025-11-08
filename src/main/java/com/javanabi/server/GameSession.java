package com.javanabi.server;

import com.javanabi.game.GameEngine;
import com.javanabi.game.Player;
import com.javanabi.game.action.Action;
import com.javanabi.game.state.GameState;

import java.util.*;

public class GameSession {
    private final String gameId;
    private final String hostClientId;
    private final List<String> playerIds;
    private final HanabiServer server;
    private GameEngine gameEngine;
    private boolean gameStarted;
    
    public GameSession(String gameId, String hostClientId, List<String> playerIds, HanabiServer server) {
        this.gameId = gameId;
        this.hostClientId = hostClientId;
        this.playerIds = new ArrayList<>(playerIds);
        this.server = server;
        this.gameStarted = false;
    }
    
    public void startGame() {
        if (gameStarted) {
            return;
        }
        
        List<Player> players = new ArrayList<>();
        for (String playerId : playerIds) {
            players.add(new NetworkPlayer(playerId, server));
        }
        
        this.gameEngine = new GameEngine(players);
        this.gameStarted = true;
        
        notifyAllPlayers("GAME_STARTED", gameId);
        broadcastGameState();
    }
    
    public boolean processAction(String playerId, Action action) {
        if (!gameStarted || gameEngine.isGameOver()) {
            return false;
        }
        
        Player currentPlayer = gameEngine.getCurrentPlayer();
        if (!currentPlayer.getName().equals(playerId)) {
            return false;
        }
        
        boolean success = gameEngine.executeAction(action);
        if (success) {
            broadcastGameState();
            
            if (gameEngine.isGameOver()) {
                int score = gameEngine.getScore();
                boolean won = score == 25;
                notifyAllPlayers("GAME_OVER", score + " " + won);
            }
        }
        
        return success;
    }
    
    public GameState getGameState() {
        return gameEngine != null ? gameEngine.getGameState() : null;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public String getHostClientId() {
        return hostClientId;
    }
    
    public List<String> getPlayerIds() {
        return new ArrayList<>(playerIds);
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    private void broadcastGameState() {
        if (gameEngine == null) return;
        
        GameState state = gameEngine.getGameState();
        String stateData = serializeGameState(state);
        notifyAllPlayers("GAME_STATE_UPDATE", stateData);
    }
    
    private String serializeGameState(GameState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("info_tokens:").append(state.getInfoTokens());
        sb.append(",fuse_tokens:").append(state.getFuseTokens());
        sb.append(",deck_size:").append(state.getDeckSize());
        sb.append(",current_player:").append(state.getCurrentPlayerIndex());
        sb.append(",score:").append(state.calculateScore());
        sb.append(",game_over:").append(state.isGameOver());
        
        return sb.toString();
    }
    
    private void notifyAllPlayers(String messageType, String data) {
        for (String playerId : playerIds) {
            // This would need to be implemented to send messages to specific clients
            // server.getClient(playerId).sendMessage(messageType, data);
        }
    }
    
    private static class NetworkPlayer implements Player {
        private final String playerId;
        private final HanabiServer server;
        
        public NetworkPlayer(String playerId, HanabiServer server) {
            this.playerId = playerId;
            this.server = server;
        }
        
        @Override
        public String getName() {
            return playerId;
        }
        
        @Override
        public void initialize(GameState initialState) {
            // Send initial game state to client
        }
        
        @Override
        public Action takeTurn(GameState currentState) {
            // This would be implemented to receive action from client
            return null;
        }
        
        @Override
        public void receiveClue(Clue clue) {
            // Send clue information to client
        }
        
        @Override
        public void notifyGameState(GameState state) {
            // Send game state update to client
        }
        
        @Override
        public void notifyPlayerAction(String playerName, Action action) {
            // Send action notification to client
        }
        
        @Override
        public void notifyGameEnd(int score, boolean won) {
            // Send game end notification to client
        }
    }
}