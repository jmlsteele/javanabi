package com.javanabi.players;

import com.javanabi.domain.Card;
import com.javanabi.game.Deck;
import com.javanabi.game.Player;
import com.javanabi.game.action.*;
import com.javanabi.game.state.GameState;
import com.javanabi.util.CardKnowledge;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleAIPlayer implements Player {
    protected final String name;
    protected GameState currentState;
    protected Map<String, List<CardKnowledge>> playerCardKnowledge;
    protected List<Card> remainingCards;
    
    public SimpleAIPlayer(String name) {
        this.name = name;
        this.playerCardKnowledge = new HashMap<>();
        this.remainingCards = new Deck().getRemainingCards();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void initialize(GameState initialState) {
        System.out.println(initialState);
        this.currentState = initialState;
        
        // Initialize knowledge tracking for all players
        playerCardKnowledge.clear();
        for (String player : currentState.getPlayers()) {
            int playerHandSize = currentState.getPlayerHandSize(player);
            List<CardKnowledge> playerKnowledge = new ArrayList<>();
            for (int i = 0; i < playerHandSize; i++) {
                playerKnowledge.add(new CardKnowledge());
            }
            playerCardKnowledge.put(player, playerKnowledge);
        }
    }
    
    @Override
    public Action takeTurn(GameState currentState) {
        this.currentState = currentState;
        System.out.println(currentState);
        
        // Priority 1: Play 100% certain card
        Optional<Integer> playableCard = findCertainPlayableCard();
        if (playableCard.isPresent()) {
            int cardIndex = playableCard.get();
            CardKnowledge playing =  playerCardKnowledge.get(this.name).get(cardIndex);
            System.out.println(playing);
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
            CardKnowledge discarding = playerCardKnowledge.get(this.name).get(cardIndex);
            System.out.println(discarding);
            return new DiscardCardAction(cardIndex);
        }
        
        // Priority 4: Discard oldest card
        CardKnowledge discarding = playerCardKnowledge.get(this.name).get(0);
        System.out.println(discarding);
        return new DiscardCardAction(0);
    }
    
    @Override
    public void receiveClue(Clue clue) {
        updateKnowledge(name, clue);
    }

    protected void updateKnowledge(String player, Clue clue) {
        for (int i=0;i<currentState.getPlayerHandSize(player);i++) {
            if (clue.getCardIndices().contains(i)) {
                playerCardKnowledge.get(player).get(i).applyClue(clue);
            } else {
                playerCardKnowledge.get(player).get(i).applyNegativeClue(clue);
            }
        }
    }


    @Override
    public void notifyPlayerAction(String playerName, Action action) {
        action.accept(new Action.ActionVisitor<Void>() {
            @Override
            public Void visit(GiveInfoAction giveInfoAction) {
                updateKnowledge(giveInfoAction.getTargetPlayer(),giveInfoAction.getClue());
                return null;
            }
            
            @Override
            public Void visit(PlayCardAction playCardAction) {
                SimpleAIPlayer.this.remainingCards.remove(playCardAction.getCard());
                playerCardKnowledge.get(playerName).remove(playCardAction.getHandIndex());
                return null;
            }
            
            @Override
            public Void visit(DiscardCardAction discardCardAction) {
                SimpleAIPlayer.this.remainingCards.remove(discardCardAction.getCard());
                playerCardKnowledge.get(playerName).remove(discardCardAction.getHandIndex());
                return null;
            }

            @Override
            public Void visit(DrawCardAction drawCardAction) {
                playerCardKnowledge.get(playerName).add(new CardKnowledge());
                return null;
            }

        });
    }
    
    @Override
    public void notifyGameEnd(int score, boolean won) {
        System.out.println(name + " - Game ended! Score: " + score + ", Won: " + won);
    }

    protected Optional<Integer> findCertainPlayableCard() {
        int handSize = currentState.getPlayerHandSize(this.name);
        
        for (int i = 0; i < handSize; i++) {
            CardKnowledge knowledge = playerCardKnowledge.get(this.name).get(i);
            if (isCardCertainPlayable(knowledge)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }
    
    protected boolean isCardCertainPlayable(CardKnowledge knowledge) {
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
    
    protected boolean isCardPlayable(Card card) {
        List<Card> playableCards = currentState.getPlayableCards();
        return playableCards.contains(card);
    }
    
    protected Optional<GiveInfoAction> findUsefulHint() {
        List<String> otherPlayers = getOtherPlayers();
        
        for (String targetPlayer : otherPlayers) {
            List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
            List<CardKnowledge> targetKnowledge = playerCardKnowledge.get(targetPlayer);
            
            if (targetKnowledge == null) continue;
            List<GiveInfoAction> hints = new ArrayList<>();

            for (int i = 0; i < targetHand.size(); i++) {
                Card card = targetHand.get(i);
                CardKnowledge knowledge = targetKnowledge.get(i);
                
                if (isCardPlayable(card)) {
                    // Check if this player doesn't already know this card well enough
                    if (!isCardCertainPlayable(knowledge)) {
                        // Check if we can make this card 100% certain with a hint
                        hints.addAll(createHintForCard(targetPlayer, i, card, knowledge));
                    }
                }
            }
            if (hints.isEmpty()) {
                System.out.println("Found no useful");
                return Optional.empty();
            }
            System.out.println(hints);
            //TODO now go through the hints and see which one would be best
            return Optional.of(hints.get(0));
        }
        
        return Optional.empty();
    }
    
    protected List<GiveInfoAction> createHintForCard(String targetPlayer, int cardIndex, Card card, CardKnowledge currentKnowledge) {
        List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
        List<Integer> matchingIndices = new ArrayList<>();
        List<GiveInfoAction> hints = new ArrayList<>();
        // Try suit hint if player doesn't already know the suit
        if (!currentKnowledge.isKnownSuit()) {
            for (int i = 0; i < targetHand.size(); i++) {
                if (targetHand.get(i).getSuit() == card.getSuit()) {
                    matchingIndices.add(i);
                }
            }
            
            if (matchingIndices.size() == 1) {
                // Single card of this suit - this would make it 100% certain for suit
                hints.add(new GiveInfoAction(targetPlayer,new Clue(ClueType.SUIT, card.getSuit(), matchingIndices)));
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
                hints.add(new GiveInfoAction(targetPlayer,new Clue(ClueType.RANK, card.getRank(), matchingIndices)));
            }
        }
        
        return hints;
    }
    
    protected Optional<Integer> findUselessCard() {
        int handSize = currentState.getPlayerHandSize(this.name);
        
        for (int i = 0; i < handSize; i++) {
            CardKnowledge knowledge = playerCardKnowledge.get(this.name).get(i);
            
            if (isCardUseless(knowledge)) {
                return Optional.of(i);
            }
        }
        
        return Optional.empty();
    }
    
    protected boolean isCardUseless(CardKnowledge knowledge) {
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
    
    protected List<String> getOtherPlayers() {
        return currentState.getPlayers().stream()
                .filter(p -> !p.equals(this.getName()))
                .collect(Collectors.toList());
    }
}