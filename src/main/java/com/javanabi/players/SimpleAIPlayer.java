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
    private Map<Player, List<CardKnowledge>> otherPlayersKnowledge;
    
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
        this.currentState = initialState;
        initializeCardKnowledge();
    }
    
    private void initializeCardKnowledge() {
        // Initialize my own knowledge
        int handSize = currentState.getPlayerHandSize(this);
        myCardKnowledge.clear();
        for (int i = 0; i < handSize; i++) {
            myCardKnowledge.add(new CardKnowledge());
        }
        
        // Initialize knowledge tracking for other players
        otherPlayersKnowledge.clear();
        for (Player player : currentState.getPlayers()) {
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
    
    private void updateOtherPlayerKnowledge(Player targetPlayer, GiveInfoAction giveInfoAction) {
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
            matchingIndices.stream().mapToInt(i -> i).toArray()
        );
        
        // Update knowledge for cards that match the clue
        for (int index : clue.getCardIndices()) {
            if (index < playerKnowledge.size()) {
                CardKnowledge updatedKnowledge = playerKnowledge.get(index).applyClue(clue);
                playerKnowledge.set(index, updatedKnowledge);
            }
        }
    }
    
    private void updateCardKnowledge() {
        // Update my own knowledge
        int handSize = currentState.getPlayerHandSize(this);
        // Ensure knowledge list matches hand size
        while (myCardKnowledge.size() < handSize) {
            myCardKnowledge.add(new CardKnowledge());
        }
        while (myCardKnowledge.size() > handSize) {
            myCardKnowledge.remove(myCardKnowledge.size() - 1);
        }
        
        // Update other players' knowledge
        for (Player player : currentState.getPlayers()) {
            if (!player.equals(this)) {
                List<CardKnowledge> playerKnowledge = otherPlayersKnowledge.get(player);
                if (playerKnowledge != null) {
                    int playerHandSize = currentState.getPlayerHandSize(player);
                    
                    // Ensure knowledge list matches hand size
                    while (playerKnowledge.size() < playerHandSize) {
                        playerKnowledge.add(new CardKnowledge());
                    }
                    while (playerKnowledge.size() > playerHandSize) {
                        playerKnowledge.remove(playerKnowledge.size() - 1);
                    }
                }
            }
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
    
    private Optional<GiveInfoAction> createHintForCard(Player targetPlayer, int cardIndex, Card card, CardKnowledge currentKnowledge) {
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
        // Track when clues are given to other players
        action.accept(new Action.ActionVisitor<Void>() {
            @Override
            public Void visit(GiveInfoAction giveInfoAction) {
                Player targetPlayer = giveInfoAction.getTargetPlayer();
                if (!targetPlayer.equals(SimpleAIPlayer.this)) {
                    // Update our tracking of this player's knowledge
                    updateOtherPlayerKnowledge(targetPlayer, giveInfoAction);
                }
                return null;
            }
            
            @Override
            public Void visit(PlayCardAction playCardAction) {
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
}