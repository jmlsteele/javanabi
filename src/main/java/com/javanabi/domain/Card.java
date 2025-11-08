package com.javanabi.domain;

import java.util.Objects;

public final class Card {
    private final Suit suit;
    private final int rank;

    public Card(Suit suit, int rank) {
        if (rank < 1 || rank > 5) {
            throw new IllegalArgumentException("Rank must be between 1 and 5");
        }
        this.suit = Objects.requireNonNull(suit);
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    @Override
    public String toString() {
        return suit + " " + rank;
    }

    public enum Suit {
        WHITE, YELLOW, GREEN, BLUE, RED
    }
}