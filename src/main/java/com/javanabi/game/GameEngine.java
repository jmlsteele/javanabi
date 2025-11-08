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
    private int currentPlayerIndex;
    
    public GameEngine(List<Player> players) {
        if (players.size() < 2 || players.size() > 5) {
            throw new IllegalArgumentException("Hanabi requires 2-5 players");
        }
        this.players = new ArrayList<>(players);
        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        initializeGame();
    }
    
    private void initializeGame() {
        deck.shuffle();
        gameState = GameState.initialGameState(players);
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
            
            Map<Player, List<Card>> updatedHands = new HashMap<>(gameState.getHands());
            updatedHands.put(player, hand);
            
            gameState = GameState.builder()
                .hands(updatedHands)
                .playedCards(gameState.getPlayedCards())
                .discardPile(gameState.getDiscardPile())
                .infoTokens(gameState.getInfoTokens())
                .fuseTokens(gameState.getFuseTokens())
                .currentPlayerIndex(currentPlayerIndex)
                .players(gameState.getPlayers())
                .gameOver(gameState.isGameOver())
                .deckSize(deck.size())
                .build();
        }
        
        for (Player player : players) {
            player.initialize(gameState.getPlayerView(player));
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
        
        if (!gameState.isGameOver()) {
            nextTurn();
        }
        
        notifyPlayers();
        
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
                List<Card> hand = gameState.getPlayerHand(currentPlayer);
                return playCardAction.getHandIndex() < hand.size();
            }
            
            @Override
            public Boolean visit(DiscardCardAction discardCardAction) {
                List<Card> hand = gameState.getPlayerHand(currentPlayer);
                return discardCardAction.getHandIndex() < hand.size();
            }
        });
    }
    
    private GameState handleGiveInfoAction(GiveInfoAction action) {
        Player targetPlayer = action.getTargetPlayer();
        List<Card> targetHand = gameState.getPlayerHand(targetPlayer);
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
            matchingIndices.stream().mapToInt(i -> i).toArray()
        );
        
        targetPlayer.receiveClue(clue);
        
        return GameState.builder()
            .hands(gameState.getHands())
            .playedCards(gameState.getPlayedCards())
            .discardPile(gameState.getDiscardPile())
            .infoTokens(gameState.getInfoTokens() - 1)
            .fuseTokens(gameState.getFuseTokens())
            .currentPlayerIndex(currentPlayerIndex)
            .players(gameState.getPlayers())
            .gameOver(gameState.isGameOver())
            .deckSize(deck.size())
            .build();
    }
    
    private GameState handlePlayCardAction(PlayCardAction action) {
        Player currentPlayer = players.get(currentPlayerIndex);
        List<Card> hand = new ArrayList<>(gameState.getPlayerHand(currentPlayer));
        Card playedCard = hand.remove(action.getHandIndex());
        
        Map<Player, List<Card>> updatedHands = new HashMap<>(gameState.getHands());
        updatedHands.put(currentPlayer, hand);
        
        Map<Card.Suit, List<Card>> playedCards = new HashMap<>(gameState.getPlayedCards());
        List<Card> suitCards = new ArrayList<>(playedCards.get(playedCard.getSuit()));
        
        boolean playSuccessful = false;
        if (suitCards.isEmpty() && playedCard.getRank() == 1) {
            suitCards.add(playedCard);
            playSuccessful = true;
        } else if (!suitCards.isEmpty()) {
            Card lastPlayed = suitCards.get(suitCards.size() - 1);
            if (lastPlayed.getRank() == playedCard.getRank() - 1) {
                suitCards.add(playedCard);
                playSuccessful = true;
            }
        }
        
        playedCards.put(playedCard.getSuit(), suitCards);
        
        List<Card> discardPile = new ArrayList<>(gameState.getDiscardPile());
        int infoTokens = gameState.getInfoTokens();
        int fuseTokens = gameState.getFuseTokens();
        
        if (!playSuccessful) {
            discardPile.add(playedCard);
            fuseTokens--;
        } else if (playedCard.getRank() == 5) {
            infoTokens = Math.min(infoTokens + 1, 8);
        }
        
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            hand.add(drawnCard);
            updatedHands.put(currentPlayer, hand);
        }
        
        boolean gameOver = fuseTokens == 0 || isGameWon(playedCards) || (deck.isEmpty() && isFinalRound());
        
        return GameState.builder()
            .hands(updatedHands)
            .playedCards(playedCards)
            .discardPile(discardPile)
            .infoTokens(infoTokens)
            .fuseTokens(fuseTokens)
            .currentPlayerIndex(currentPlayerIndex)
            .players(gameState.getPlayers())
            .gameOver(gameOver)
            .deckSize(deck.size())
            .build();
    }
    
    private GameState handleDiscardCardAction(DiscardCardAction action) {
        Player currentPlayer = players.get(currentPlayerIndex);
        List<Card> hand = new ArrayList<>(gameState.getPlayerHand(currentPlayer));
        Card discardedCard = hand.remove(action.getHandIndex());
        
        Map<Player, List<Card>> updatedHands = new HashMap<>(gameState.getHands());
        updatedHands.put(currentPlayer, hand);
        
        List<Card> discardPile = new ArrayList<>(gameState.getDiscardPile());
        discardPile.add(discardedCard);
        
        Card drawnCard = deck.drawCard();
        if (drawnCard != null) {
            hand.add(drawnCard);
            updatedHands.put(currentPlayer, hand);
        }
        
        int infoTokens = Math.min(gameState.getInfoTokens() + 1, 8);
        boolean gameOver = deck.isEmpty() && isFinalRound();
        
        return GameState.builder()
            .hands(updatedHands)
            .playedCards(gameState.getPlayedCards())
            .discardPile(discardPile)
            .infoTokens(infoTokens)
            .fuseTokens(gameState.getFuseTokens())
            .currentPlayerIndex(currentPlayerIndex)
            .players(gameState.getPlayers())
            .gameOver(gameOver)
            .deckSize(deck.size())
            .build();
    }
    
    private boolean isGameWon(Map<Card.Suit, List<Card>> playedCards) {
        for (List<Card> suitCards : playedCards.values()) {
            if (suitCards.size() < 5 || suitCards.get(suitCards.size() - 1).getRank() != 5) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isFinalRound() {
        for (List<Card> hand : gameState.getHands().values()) {
            if (!hand.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
    
    private void notifyPlayerAction(Player currentPlayer, Action action) {
        for (Player player : players) {
            player.notifyPlayerAction(currentPlayer.getName(), action);
        }
    }
    
    private void notifyPlayers() {
        for (Player player : players) {
            player.notifyGameState(gameState.getPlayerView(player));
        }
    }
    
    public GameState getGameState() {
        return gameState; // Full game state for server/admin use
    }
    
    public GameState getPlayerGameState(Player player) {
        return gameState.getPlayerView(player); // Filtered view for players
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