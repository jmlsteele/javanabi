package com.javanabi.game;

import com.javanabi.domain.Card;
import com.javanabi.game.action.Action;
import com.javanabi.game.action.DiscardCardAction;
import com.javanabi.game.action.GiveInfoAction;
import com.javanabi.game.action.PlayCardAction;
import com.javanabi.game.state.GameState;

import java.util.*;

public class GameEngine {
    private GameState gameState;
    private final Deck deck;
    private final List<Player> players;
    private final List<String> playerNames;
    private int currentPlayerIndex;
    
    public GameEngine(List<Player> players) {
        if (players.size() < 2 || players.size() > 5) {
            throw new IllegalArgumentException("Hanabi requires 2-5 players");
        }
        this.players = new ArrayList<>(players);
        this.playerNames = new ArrayList<>();
        for(Player p: this.players) {
            this.playerNames.add(p.getName());
        }

        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        deck.shuffle();
        gameState = GameState.initialGameState(this.playerNames);
        dealInitialHands();
    }
    
    private void dealInitialHands() {
        int handSize = players.size() >= 4 ? 4 : 5;
        
        for (Player player : players) {
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < handSize; i++) {
                Card card = deck.drawCard();
                if (card != null) {
                    hand.add(card);
                }
            }
            
            Map<String, List<Card>> updatedHands = new HashMap<>(gameState.getHands());
            updatedHands.put(player.getName(), hand);
            
            gameState = GameState.builder()
                .hands(updatedHands)
                .playedCards(gameState.getPlayedCards())
                .discardedCards(gameState.getDiscardedCards())
                .infoTokens(gameState.getInfoTokens())
                .fuseTokens(gameState.getFuseTokens())
                .currentPlayerIndex(currentPlayerIndex)
                .players(gameState.getPlayers())
                .finalPlayerIndex(gameState.getFinalPlayerIndex())
                .deckSize(deck.size())
                .build();
        }
        
        for (Player player : players) {
            player.initialize(gameState.getPlayerView(player.getName()));
        }
    }
    
    public boolean executeAction(Action action) {
        if (gameState.isGameOver()) {
            return false;
        }
        
        Player currentPlayer = players.get(currentPlayerIndex);
        
        if (!validateAction(action, currentPlayer)) {
            return false;
        }
        
        Action.ActionVisitor<GameState> visitor = new Action.ActionVisitor<GameState>() {
            @Override
            public GameState visit(GiveInfoAction giveInfoAction) {
                return handleGiveInfoAction(giveInfoAction);
            }
            
            @Override
            public GameState visit(PlayCardAction playCardAction) {
                return handlePlayCardAction(playCardAction);
            }
            
            @Override
            public GameState visit(DiscardCardAction discardCardAction) {
                return handleDiscardCardAction(discardCardAction);
            }
        };
        
        gameState = action.accept(visitor);
        
        // Notify all players about the action that was taken
        notifyPlayerAction(currentPlayer, action);
        nextTurn();
        return true;
    }
    
    private boolean validateAction(Action action, Player currentPlayer) {
        return action.accept(new Action.ActionVisitor<Boolean>() {
            @Override
            public Boolean visit(GiveInfoAction giveInfoAction) {
                return gameState.getInfoTokens() > 0 && 
                       !giveInfoAction.getTargetPlayer().equals(currentPlayer);
            }
            
            @Override
            public Boolean visit(PlayCardAction playCardAction) {
                List<Card> hand = gameState.getPlayerHand(currentPlayer.getName());
                return playCardAction.getHandIndex() < hand.size();
            }
            
            @Override
            public Boolean visit(DiscardCardAction discardCardAction) {
                List<Card> hand = gameState.getPlayerHand(currentPlayer.getName());
                return discardCardAction.getHandIndex() < hand.size();
            }
        });
    }
    
    private GameState handleGiveInfoAction(GiveInfoAction action) {
        String targetPlayerName = action.getTargetPlayer();
        Player targetPlayer = this.players.get(this.playerNames.indexOf(targetPlayerName));
        List<Card> targetHand = gameState.getPlayerHand(targetPlayerName);
        List<Integer> matchingIndices = new ArrayList<>();
        
        for (int i = 0; i < targetHand.size(); i++) {
            Card card = targetHand.get(i);
            boolean matches = false;
            
            if (action.getClueType() == Player.ClueType.SUIT) {
                matches = card.getSuit().equals(action.getClueValue());
            } else if (action.getClueType() == Player.ClueType.RANK) {
                matches = card.getRank() == (Integer) action.getClueValue();
            }
            
            if (matches) {
                matchingIndices.add(i);
            }
        }
        
        Player.Clue clue = new Player.Clue(
            action.getClueType(),
            action.getClueValue(),
            matchingIndices
        );
        
        targetPlayer.receiveClue(clue);
        
        return GameState.builder()
            .hands(gameState.getHands())
            .playedCards(gameState.getPlayedCards())
            .discardedCards(gameState.getDiscardedCards())
            .infoTokens(gameState.getInfoTokens() - 1)
            .fuseTokens(gameState.getFuseTokens())
            .currentPlayerIndex(currentPlayerIndex)
            .players(gameState.getPlayers())
            .finalPlayerIndex(gameState.getFinalPlayerIndex())
            .deckSize(deck.size())
            .build();
    }
    
    private GameState handlePlayCardAction(PlayCardAction action) {
        Player currentPlayer = players.get(currentPlayerIndex);
        List<Card> hand = new ArrayList<>(gameState.getPlayerHand(currentPlayer.getName()));
        Card playedCard = hand.remove(action.getHandIndex());
        Map<Card.Suit, List<Card>> playedCards = new HashMap<>(gameState.getPlayedCards());
        Map<Card.Suit, List<Card>> discardedCards = new HashMap<>(gameState.getDiscardedCards());
        List<Card> suitCards = new ArrayList<>(playedCards.get(playedCard.getSuit()));
        
        int infoTokens = gameState.getInfoTokens();
        int fuseTokens = gameState.getFuseTokens();

        if (playedCard.getRank() == suitCards.size() + 1) {
            suitCards.add(playedCard);
            if (playedCard.getRank() == 5) {
                infoTokens = Math.min(infoTokens + 1, 8);
            }            
        } else {
            discardedCards.get(playedCard.getSuit()).add(playedCard);
            fuseTokens--;
        }

        playedCards.put(playedCard.getSuit(), suitCards);
        
        
        int finalPlayerIndex = gameState.getFinalPlayerIndex();
        
        Card drawnCard = deck.drawCard();
        if (drawnCard == null) {
            if (finalPlayerIndex == -1) finalPlayerIndex = currentPlayerIndex;
        } else {
            hand.add(drawnCard);
            currentPlayer.drawCard();
        }
        Map<String, List<Card>> updatedHands = new HashMap<>(gameState.getHands());
        updatedHands.put(currentPlayer.getName(), hand);
        
        return GameState.builder()
            .hands(updatedHands)
            .playedCards(playedCards)
            .discardedCards(discardedCards)
            .infoTokens(infoTokens)
            .fuseTokens(fuseTokens)
            .currentPlayerIndex(currentPlayerIndex)
            .players(gameState.getPlayers())
            .finalPlayerIndex(finalPlayerIndex)
            .deckSize(deck.size())
            .build();
    }
    
    private GameState handleDiscardCardAction(DiscardCardAction action) {
        Player currentPlayer = players.get(currentPlayerIndex);
        List<Card> hand = new ArrayList<>(gameState.getPlayerHand(currentPlayer.getName()));
        Map<Card.Suit, List<Card>> discardedCards = new HashMap<>(gameState.getDiscardedCards());
        Card discardedCard = hand.remove(action.getHandIndex());
        
        Map<String, List<Card>> updatedHands = new HashMap<>(gameState.getHands());
        updatedHands.put(currentPlayer.getName(), hand);
        
        discardedCards.get(discardedCard.getSuit()).add(discardedCard);
        
        int finalPlayerIndex = gameState.getFinalPlayerIndex();
        
        Card drawnCard = deck.drawCard();
        if (drawnCard == null) {
            if (finalPlayerIndex == -1) finalPlayerIndex = currentPlayerIndex;
        } else {
            hand.add(drawnCard);
            updatedHands.put(currentPlayer.getName(), hand);
            currentPlayer.drawCard();
        }
        int infoTokens = Math.min(gameState.getInfoTokens() + 1, 8);

        return GameState.builder()
            .hands(updatedHands)
            .playedCards(gameState.getPlayedCards())
            .discardedCards(discardedCards)
            .infoTokens(infoTokens)
            .fuseTokens(gameState.getFuseTokens())
            .currentPlayerIndex(currentPlayerIndex)
            .players(gameState.getPlayers())
            .finalPlayerIndex(finalPlayerIndex)
            .deckSize(deck.size())
            .build();
    }
    
    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
    
    private void notifyPlayerAction(Player currentPlayer, Action action) {
        for (Player player : players) {
            player.notifyPlayerAction(currentPlayer.getName(), action);
        }
    }
    
    public GameState getGameState() {
        return gameState; // Full game state for server/admin use
    }
    
    public GameState getPlayerGameState(Player player) {
        return gameState.getPlayerView(player.getName()); // Filtered view for players
    }
    
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    public boolean isGameOver() {
        return gameState.isGameOver();
    }
    
    public int getScore() {
        return gameState.calculateScore();
    }
}