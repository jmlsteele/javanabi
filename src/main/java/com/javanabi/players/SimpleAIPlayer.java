package com.javanabi.players;

import com.javanabi.domain.Card;
import com.javanabi.game.Player;
import com.javanabi.game.action.*;
import com.javanabi.game.state.GameState;
import com.javanabi.util.CardKnowledge;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleAIPlayer implements Player {
    private final String name;
    private GameState currentState;
    private List<CardKnowledge> myCardKnowledge;
    private Map<String, List<CardKnowledge>> otherPlayersKnowledge;
    
    public SimpleAIPlayer(String name) {
        this.name = name;
        this.myCardKnowledge = new ArrayList<>();
        this.otherPlayersKnowledge = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void initialize(GameState initialState) {
        System.out.println(initialState);
        this.currentState = initialState;
        // Initialize my own knowledge
        int handSize = currentState.getPlayerHandSize(this.name);
        myCardKnowledge.clear();
        for (int i = 0; i < handSize; i++) {
            myCardKnowledge.add(new CardKnowledge());
        }
        
        // Initialize knowledge tracking for other players
        otherPlayersKnowledge.clear();
        for (String player : currentState.getPlayers()) {
            if (!player.equals(this)) {
                int playerHandSize = currentState.getPlayerHandSize(player);
                List<CardKnowledge> playerKnowledge = new ArrayList<>();
                for (int i = 0; i < playerHandSize; i++) {
                    playerKnowledge.add(new CardKnowledge());
                }
                otherPlayersKnowledge.put(player, playerKnowledge);
            }
        }
    }
    
    @Override
    public Action takeTurn(GameState currentState) {
        this.currentState = currentState;
        
        // Priority 1: Play 100% certain card
        Optional<Integer> playableCard = findCertainPlayableCard();
        if (playableCard.isPresent()) {
            int cardIndex = playableCard.get();
            myCardKnowledge.remove(cardIndex);
            return new PlayCardAction(cardIndex);
        }
        
        // Priority 2: Give useful hint
        if (currentState.getInfoTokens() > 0) {
            Optional<GiveInfoAction> usefulHint = findUsefulHint();
            if (usefulHint.isPresent()) {
                return usefulHint.get();
            }
        }
        
        // Priority 3: Discard known useless card
        Optional<Integer> uselessCard = findUselessCard();
        if (uselessCard.isPresent()) {
            int cardIndex = uselessCard.get();
            myCardKnowledge.remove(cardIndex);
            return new DiscardCardAction(cardIndex);
        }
        
        // Priority 4: Discard oldest card
        System.out.println(myCardKnowledge.size());
        myCardKnowledge.remove(0);
        return new DiscardCardAction(0);
    }
    
    @Override
    public void receiveClue(Clue clue) {
        for (int i=0;i<currentState.getPlayerHandSize(this.name);i++) {
            if (clue.getCardIndices().contains(i)) {
                myCardKnowledge.get(i).applyClue(clue);
            }else {
                myCardKnowledge.get(i).applyNegativeClue(clue);
            }
        }
    }

    @Override
    public void drawCard() {
        myCardKnowledge.add(new CardKnowledge());
    }

    @Override
    public void notifyPlayerAction(String playerName, Action action) {
        // Track when clues are given to other players
        action.accept(new Action.ActionVisitor<Void>() {
            @Override
            public Void visit(GiveInfoAction giveInfoAction) {
                String targetPlayer = giveInfoAction.getTargetPlayer();
                if (!targetPlayer.equals(SimpleAIPlayer.this.name)) {
                    // Update our tracking of this player's knowledge
                    updateOtherPlayerKnowledge(targetPlayer, giveInfoAction);
                }
                return null;
            }
            
            @Override
            public Void visit(PlayCardAction playCardAction) {
                playCardAction.getHandIndex();
                return null;
            }
            
            @Override
            public Void visit(DiscardCardAction discardCardAction) {
                return null;
            }
        });
    }
    
    @Override
    public void notifyGameEnd(int score, boolean won) {
        System.out.println(name + " - Game ended! Score: " + score + ", Won: " + won);
    }

    private void updateOtherPlayerKnowledge(String targetPlayer, GiveInfoAction giveInfoAction) {
        List<CardKnowledge> playerKnowledge = otherPlayersKnowledge.get(targetPlayer);
        if (playerKnowledge == null) {
            // Initialize knowledge for this player if not already tracked
            playerKnowledge = new ArrayList<>();
            otherPlayersKnowledge.put(targetPlayer, playerKnowledge);
        }
        
        // Calculate which cards match the clue (same logic as GameEngine)
        List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
        List<Integer> matchingIndices = new ArrayList<>();
        
        for (int i = 0; i < targetHand.size(); i++) {
            Card card = targetHand.get(i);
            boolean matches = false;
            
            if (giveInfoAction.getClueType() == Player.ClueType.SUIT) {
                matches = card.getSuit().equals(giveInfoAction.getClueValue());
            } else if (giveInfoAction.getClueType() == Player.ClueType.RANK) {
                matches = card.getRank() == (Integer) giveInfoAction.getClueValue();
            }
            
            if (matches) {
                matchingIndices.add(i);
            }
        }
        
        // Create clue object
        Player.Clue clue = new Player.Clue(
            giveInfoAction.getClueType(),
            giveInfoAction.getClueValue(),
            matchingIndices
        );
        
        // Update knowledge for cards that match the clue
        for (int index : clue.getCardIndices()) {
            if (index < playerKnowledge.size()) {
                playerKnowledge.get(index).applyClue(clue);
            }
        }
    }
    
    private Optional<Integer> findCertainPlayableCard() {
        int handSize = currentState.getPlayerHandSize(this.name);
        
        for (int i = 0; i < handSize; i++) {
            CardKnowledge knowledge = myCardKnowledge.get(i);
            
            if (isCardCertainPlayable(knowledge)) {
                return Optional.of(i);
            }
        }
        
        return Optional.empty();
    }
    
    private boolean isCardCertainPlayable(CardKnowledge knowledge) {
        //if we know exactly what card it is, we know if it's playable or not
        if (knowledge.isKnownSuit() && knowledge.isKnownRank()) {
            return isCardPlayable(new Card(knowledge.getKnownSuit(), knowledge.getKnownRank()));
        }
        
        //if we know the rank, and all cards of that rank are playable then the card is playable
        if (knowledge.isKnownRank()) {
            boolean playable = true;
            for (Card.Suit s : Card.Suit.values()) {
                if (!isCardPlayable(new Card(s,knowledge.getKnownRank()))) {playable=false; break;}
            }
            return playable;
        }
        return false;
    }
    
    private boolean isCardPlayable(Card card) {
        List<Card> playableCards = currentState.getPlayableCards();
        return playableCards.contains(card);
    }
    
    private Optional<GiveInfoAction> findUsefulHint() {
        List<String> otherPlayers = getOtherPlayers();
        
        for (String targetPlayer : otherPlayers) {
            List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
            List<CardKnowledge> targetKnowledge = otherPlayersKnowledge.get(targetPlayer);
            
            if (targetKnowledge == null) continue;
            
            for (int i = 0; i < targetHand.size(); i++) {
                Card card = targetHand.get(i);
                CardKnowledge knowledge = targetKnowledge.get(i);
                
                if (isCardPlayable(card)) {
                    // Check if this player doesn't already know this card well enough
                    if (!isCardCertainPlayable(knowledge)) {
                        // Check if we can make this card 100% certain with a hint
                        Optional<GiveInfoAction> hint = createHintForCard(targetPlayer, i, card, knowledge);
                        if (hint.isPresent()) {
                            return hint;
                        }
                    }
                }
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<GiveInfoAction> createHintForCard(String targetPlayer, int cardIndex, Card card, CardKnowledge currentKnowledge) {
        List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
        List<Integer> matchingIndices = new ArrayList<>();
        
        // Try suit hint if player doesn't already know the suit
        if (!currentKnowledge.isKnownSuit()) {
            for (int i = 0; i < targetHand.size(); i++) {
                if (targetHand.get(i).getSuit() == card.getSuit()) {
                    matchingIndices.add(i);
                }
            }
            
            if (matchingIndices.size() == 1) {
                // Single card of this suit - this would make it 100% certain for suit
                return Optional.of(new GiveInfoAction(targetPlayer, ClueType.SUIT, card.getSuit()));
            }
        }
        
        // Try rank hint if player doesn't already know the rank
        if (!currentKnowledge.isKnownRank()) {
            matchingIndices.clear();
            for (int i = 0; i < targetHand.size(); i++) {
                if (targetHand.get(i).getRank() == card.getRank()) {
                    matchingIndices.add(i);
                }
            }
            
            if (matchingIndices.size() == 1) {
                // Single card of this rank - this would make it 100% certain for rank
                return Optional.of(new GiveInfoAction(targetPlayer, ClueType.RANK, card.getRank()));
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<Integer> findUselessCard() {
        int handSize = currentState.getPlayerHandSize(this.name);
        
        for (int i = 0; i < handSize; i++) {
            CardKnowledge knowledge = myCardKnowledge.get(i);
            
            if (isCardUseless(knowledge)) {
                return Optional.of(i);
            }
        }
        
        return Optional.empty();
    }
    
    private boolean isCardUseless(CardKnowledge knowledge) {
        if (!knowledge.isKnownSuit() || !knowledge.isKnownRank()) {
            return false;
        }
        
        Card.Suit suit = knowledge.getKnownSuit();
        int rank = knowledge.getKnownRank();
        
        // Check if card rank is lower than highest played card of same suit
        Map<Card.Suit, List<Card>> playedCards = currentState.getPlayedCards();
        List<Card> suitCards = playedCards.get(suit);
        
        if (!suitCards.isEmpty()) {
            int highestPlayed = suitCards.get(suitCards.size() - 1).getRank();
            if (rank <= highestPlayed) {
                return true;
            }
        }
        
        return false;
    }
    
    private List<String> getOtherPlayers() {
        return currentState.getPlayers().stream()
                .filter(p -> !p.equals(this))
                .collect(Collectors.toList());
    }
}