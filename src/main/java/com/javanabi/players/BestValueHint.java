package com.javanabi.players;

import com.javanabi.domain.Card;
import com.javanabi.domain.Card.Suit;
import com.javanabi.game.Deck;
import com.javanabi.game.action.*;
import com.javanabi.util.CardKnowledge;

import java.util.*;

public class BestValueHint extends SimpleAIPlayer {
    
    private final double weight_matched;
    private final double weight_complete;
    private final double weight_playable;
    private final double weight_discardable;
    private final double weight_final_card;
    

    public BestValueHint(String name) {
        super(name);
        weight_matched = getWeightFromEnv("WEIGHT_MATCHED", 1.0);
        weight_complete = getWeightFromEnv("WEIGHT_COMPLETE", 1.0);
        weight_playable = getWeightFromEnv("WEIGHT_PLAYABLE", 1.0);
        weight_discardable = getWeightFromEnv("WEIGHT_DISCARDABLE", 1.0);
        weight_final_card = getWeightFromEnv("WEIGHT_FINAL_CARD", 1.0);
    }

    private double getWeightFromEnv(String key, double defaultValue) {
        String value = System.getenv(key);
        return value != null ? Double.parseDouble(value) : defaultValue;
    }    

    @Override
    protected Optional<GiveInfoAction> findUsefulHint() {
        List<String> otherPlayers = getOtherPlayers();
        double maxValue=0;
        Clue bestClue = null;
        String bestCluePlayer = null;

        //iterate over the players
        for (String targetPlayer : otherPlayers) {
            //iterate over every hint we can give
            Map<Clue,Double> clueValues = new HashMap<Clue,Double>();
            for (Suit s : Suit.values()) {
                Clue clue = new Clue(ClueType.SUIT, s, null);
                clueValues.put(clue,this.determineClueValue(clue, targetPlayer));
            }

            for(int rank=1;rank<=5;rank++) {
                Clue clue = new Clue(ClueType.RANK, rank, null);
                clueValues.put(clue,this.determineClueValue(clue, targetPlayer));
            }

            for( Map.Entry<Clue,Double> kv: clueValues.entrySet()) {
                if (kv.getValue() > maxValue) {
                    
                    maxValue = kv.getValue();
                    bestClue=kv.getKey();
                    bestCluePlayer=targetPlayer;
                    //System.out.println("Possible best clue (" + maxValue + ") to " + targetPlayer + " " + bestClue);
                }
            }
        }
        if (maxValue > 0) {
            return Optional.of(new GiveInfoAction(bestCluePlayer, bestClue));
        }
        //give the most valuable hint
        return Optional.empty();
    }

    private double determineClueValue(Clue clue, String targetPlayer) {
        List<Card> targetHand = currentState.getPlayerHand(targetPlayer);
        List<CardKnowledge> targetKnowledge = playerCardKnowledge.get(targetPlayer);
        double value = 0;
        for (int i=0;i<targetHand.size();i++) {
            Card c = targetHand.get(i);
            if (
                    (clue.getType() == ClueType.RANK && c.getRank() == (int)clue.getValue())
                ||  (clue.getType() == ClueType.SUIT && c.getSuit() == (Suit)clue.getValue())
            ) {
                //this clue matches this card
                value += weight_matched;

                //will this give them complete info about this card?
                if (
                        (clue.getType() == ClueType.RANK && targetKnowledge.get(i).isKnownSuit()) 
                    ||  (clue.getType() == ClueType.SUIT && targetKnowledge.get(i).isKnownRank())
                ) {
                    value += weight_complete;
                }

                //is this card playable?
                if (this.currentState.getPlayableCards().contains(c)) {
                    value += weight_playable;
                }
                //is this card discardable because it's been played
                if (this.currentState.getPlayedCards().get(c.getSuit()).contains(c)) {
                    value += weight_discardable;
                } else {
                    //if the card has not been played...

                    //is this the last of this card (includes 5s because they are all the last of this card)
                    int discardedCount=0;
                    for (Card dc : this.currentState.getDiscardedCards().get(c.getSuit())) {
                        if (dc == c) discardedCount++;
                    }
                    if (discardedCount == Deck.RANK_COUNTS[c.getRank()-1]-1) {
                        value += weight_final_card;
                    }
                }
                //is this card discardable because it can never be played? (e.g 5 if both 4s are discarded)
                //TODO

                //do they already know this info? then there is no value here
                if (targetKnowledge.get(i).isKnownRank()) value = 0;
            }
        }
        return value;
    }
}