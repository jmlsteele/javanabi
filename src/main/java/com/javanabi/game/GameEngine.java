package com.javanabi.game;

import com.javanabi.domain.Card;
import com.javanabi.game.action.Action;
import com.javanabi.game.action.DiscardCardAction;
import com.javanabi.game.action.DrawCardAction;
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
        //if this is a GiveInfoAction we need to fix the indicies in the clue
        if (action instanceof GiveInfoAction) {
            GiveInfoAction gia = (GiveInfoAction) action;
            String targetPlayerName = gia.getTargetPlayer();
            List<Card> targetHand = gameState.getPlayerHand(targetPlayerName);
            List<Integer> matchingIndices = new ArrayList<>();

            Player.Clue actionClue = gia.getClue();
            for (int i = 0; i < targetHand.size(); i++) {
                Card card = targetHand.get(i);
                
                if (actionClue.getType() == Player.ClueType.SUIT && card.getSuit().equals(actionClue.getValue())) {
                    matchingIndices.add(i);
                } else if (actionClue.getType() == Player.ClueType.RANK && card.getRank() == (Integer) actionClue.getValue()){
                    matchingIndices.add(i);
                }
            }
            
            Player.Clue clue = new Player.Clue(
                gia.getClue().getType(),
                gia.getClue().getValue(),
                matchingIndices
            );
            action = new GiveInfoAction(targetPlayerName, clue);
        }
        
        Action.ActionVisitor<GameState> visitor = new Action.ActionVisitor<GameState>() {
            @Override
            public GameState visit(GiveInfoAction giveInfoAction) {
                return handleGiveInfoAction(giveInfoAction);
            }
            
            @Override
            public GameState visit(PlayCardAction playCardAction) {
                playCardAction.setCard(GameEngine.this.gameState.getPlayerHand(currentPlayer.getName()).get(playCardAction.getHandIndex()));
                return handlePlayCardAction(playCardAction);
            }
            
            @Override
            public GameState visit(DiscardCardAction discardCardAction) {
                discardCardAction.setCard(GameEngine.this.gameState.getPlayerHand(currentPlayer.getName()).get(discardCardAction.getHandIndex()));
                return handleDiscardCardAction(discardCardAction);
            }

            @Override
            public GameState visit(DrawCardAction giveInfoAction) {
                //this doesn't get used here
                throw new UnsupportedOperationException("Unimplemented method 'visit'");
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
                       !giveInfoAction.getTargetPlayer().equals(currentPlayer.getName());
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

            @Override
            public Boolean visit(DrawCardAction giveInfoAction) {
                //this doesn't get used here
                throw new UnsupportedOperationException("Unimplemented method 'visit'");
            }
        });
    }
    
    private GameState handleGiveInfoAction(GiveInfoAction action) {
        String targetPlayerName = action.getTargetPlayer();
        Player targetPlayer = this.players.get(this.playerNames.indexOf(targetPlayerName));
        targetPlayer.receiveClue(action.getClue());
        
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
            notifyPlayerAction(currentPlayer, new DrawCardAction());
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
            notifyPlayerAction(currentPlayer, new DrawCardAction());
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