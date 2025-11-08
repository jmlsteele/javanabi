package com.javanabi.game;

import com.javanabi.domain.Card;
import com.javanabi.game.action.*;
import com.javanabi.game.state.GameState;
import com.javanabi.util.CardKnowledge;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleAIPlayer implements Player {
    private final String name;
    private GameState currentState;
    private List<CardKnowledge> myCardKnowledge;
    
    public SimpleAIPlayer(String name) {
        this.name = name;
        this.myCardKnowledge = new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void initialize(GameState initialState) {
        this.currentState = initialState;
        initializeCardKnowledge();
    }
    
    private void initializeCardKnowledge() {
        int handSize = currentState.getPlayerHandSize(this);
        myCardKnowledge.clear();
        for (int i = 0; i < handSize; i++) {
            myCardKnowledge.add(new CardKnowledge());
        }
    }
    
    @Override
    public Action takeTurn(GameState currentState) {
        this.currentState = currentState;
        updateCardKnowledge();
        
        // Priority 1: Play 100% certain card
        Optional<Integer> playableCard = findCertainPlayableCard();
        if (playableCard.isPresent()) {
            return new PlayCardAction(playableCard.get());
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
            return new DiscardCardAction(uselessCard.get());
        }
        
        // Priority 4: Discard oldest card
        return new DiscardCardAction(0);
    }
    
    private void updateCardKnowledge() {
        int handSize = currentState.getPlayerHandSize(this);
        // Ensure knowledge list matches hand size
        while (myCardKnowledge.size() < handSize) {
            myCardKnowledge.add(new CardKnowledge());
        }
        while (myCardKnowledge.size() > handSize) {
            myCardKnowledge.remove(myCardKnowledge.size() - 1);
        }
    }
    
    private Optional<Integer> findCertainPlayableCard() {
        int handSize = currentState.getPlayerHandSize(this);
        
        for (int i = 0; i < handSize; i++) {
            CardKnowledge knowledge = myCardKnowledge.get(i);
            
            if (isCardCertainPlayable(knowledge)) {
                return Optional.of(i);
            }
        }
        
        return Optional.empty();
    }
    
    private boolean isCardCertainPlayable(CardKnowledge knowledge) {
        if (!knowledge.isKnownSuit() || !knowledge.isKnownRank()) {
            return false;
        }
        
        // Check if this card would be playable based on the known suit and rank
        return isCardPlayable(knowledge.getKnownSuit(), knowledge.getKnownRank());
    }
    
    private boolean isCardPlayable(Card card) {
        Map<Card.Suit, List<Card>> playedCards = currentState.getPlayedCards();
        List<Card> suitCards = playedCards.get(card.getSuit());
        
        if (suitCards.isEmpty()) {
            return card.getRank() == 1;
        } else {
            Card highestPlayed = suitCards.get(suitCards.size() - 1);
            return card.getRank() == highestPlayed.getRank() + 1;
        }
    }
    
    private boolean isCardPlayable(Card.Suit suit, int rank) {
        Map<Card.Suit, List<Card>> playedCards = currentState.getPlayedCards();
        List<Card> suitCards = playedCards.get(suit);
        
        if (suitCards.isEmpty()) {
            return rank == 1;
        } else {
            Card highestPlayed = suitCards.get(suitCards.size() - 1);
            return rank == highestPlayed.getRank() + 1;
        }
    }
    
    private Optional<GiveInfoAction> findUsefulHint() {
        List<Player> otherPlayers = getOtherPlayers();
        
        for (Player targetPlayer : otherPlayers) {
            List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
            
            for (int i = 0; i < targetHand.size(); i++) {
                Card card = targetHand.get(i);
                
                if (isCardPlayable(card)) {
                    // Check if we can make this card 100% certain with a hint
                    Optional<GiveInfoAction> hint = createHintForCard(targetPlayer, i, card);
                    if (hint.isPresent()) {
                        return hint;
                    }
                }
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<GiveInfoAction> createHintForCard(Player targetPlayer, int cardIndex, Card card) {
        List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
        List<Integer> matchingIndices = new ArrayList<>();
        
        // Try suit hint
        for (int i = 0; i < targetHand.size(); i++) {
            if (targetHand.get(i).getSuit() == card.getSuit()) {
                matchingIndices.add(i);
            }
        }
        
        if (matchingIndices.size() == 1) {
            // Single card of this suit - this would make it 100% certain
            return Optional.of(new GiveInfoAction(targetPlayer, ClueType.SUIT, card.getSuit()));
        }
        
        // Try rank hint
        matchingIndices.clear();
        for (int i = 0; i < targetHand.size(); i++) {
            if (targetHand.get(i).getRank() == card.getRank()) {
                matchingIndices.add(i);
            }
        }
        
        if (matchingIndices.size() == 1) {
            // Single card of this rank - this would make it 100% certain
            return Optional.of(new GiveInfoAction(targetPlayer, ClueType.RANK, card.getRank()));
        }
        
        return Optional.empty();
    }
    
    private Optional<Integer> findUselessCard() {
        int handSize = currentState.getPlayerHandSize(this);
        
        for (int i = 0; i < handSize; i++) {
            CardKnowledge knowledge = myCardKnowledge.get(i);
            
            if (isCardUseless(knowledge)) {
                return Optional.of(i);
            }
        }
        
        return Optional.empty();
    }
    
    private boolean isCardUseless(Card card, CardKnowledge knowledge) {
        if (!knowledge.isKnownSuit() || !knowledge.isKnownRank()) {
            return false;
        }
        
        // Check if card rank is lower than highest played card of same suit
        Map<Card.Suit, List<Card>> playedCards = currentState.getPlayedCards();
        List<Card> suitCards = playedCards.get(card.getSuit());
        
        if (!suitCards.isEmpty()) {
            int highestPlayed = suitCards.get(suitCards.size() - 1).getRank();
            if (card.getRank() <= highestPlayed) {
                return true;
            }
        }
        
        // Check if all cards of this rank are already played/discarded
        if (areAllCardsOfRankPlayed(card.getRank())) {
            return true;
        }
        
        return false;
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
        
        // Check if all cards of this rank are already played/discarded
        if (areAllCardsOfRankPlayed(rank)) {
            return true;
        }
        
        return false;
    }
    
    private boolean areAllCardsOfRankPlayed(int rank) {
        Map<Card.Suit, List<Card>> playedCards = currentState.getPlayedCards();
        List<Card> discardPile = currentState.getDiscardPile();
        
        int cardsNeeded = getCardsNeededForRank(rank);
        int cardsFound = 0;
        
        // Count played cards of this rank
        for (List<Card> suitCards : playedCards.values()) {
            for (Card playedCard : suitCards) {
                if (playedCard.getRank() == rank) {
                    cardsFound++;
                }
            }
        }
        
        // Count discarded cards of this rank
        for (Card discardedCard : discardPile) {
            if (discardedCard.getRank() == rank) {
                cardsFound++;
            }
        }
        
        return cardsFound >= cardsNeeded;
    }
    
    private int getCardsNeededForRank(int rank) {
        return switch (rank) {
            case 1 -> 3;
            case 2, 3, 4 -> 2;
            case 5 -> 1;
            default -> 0;
        };
    }
    
    private List<Player> getOtherPlayers() {
        return currentState.getPlayers().stream()
                .filter(p -> !p.equals(this))
                .collect(Collectors.toList());
    }
    
    @Override
    public void receiveClue(Clue clue) {
        // Update knowledge for cards that match the clue
        for (int index : clue.getCardIndices()) {
            if (index < myCardKnowledge.size()) {
                CardKnowledge updatedKnowledge = myCardKnowledge.get(index).applyClue(clue);
                myCardKnowledge.set(index, updatedKnowledge);
            }
        }
    }
    
    @Override
    public void notifyGameState(GameState state) {
        this.currentState = state;
        updateCardKnowledge();
    }
    
    @Override
    public void notifyPlayerAction(String playerName, Action action) {
        // Could track actions for better strategy, but keeping it simple for now
    }
    
    @Override
    public void notifyGameEnd(int score, boolean won) {
        System.out.println(name + " - Game ended! Score: " + score + ", Won: " + won);
    }
}