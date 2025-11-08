package com.javanabi.util;

public final class GameConfig {
    private final int playerCount;
    private final int initialInfoTokens;
    private final int initialFuseTokens;
    private final int maxInfoTokens;
    private final boolean enableRainbowSuit;
    private final boolean enableSixthSuit;
    
    private GameConfig(Builder builder) {
        this.playerCount = builder.playerCount;
        this.initialInfoTokens = builder.initialInfoTokens;
        this.initialFuseTokens = builder.initialFuseTokens;
        this.maxInfoTokens = builder.maxInfoTokens;
        this.enableRainbowSuit = builder.enableRainbowSuit;
        this.enableSixthSuit = builder.enableSixthSuit;
    }
    
    public static GameConfig standard() {
        return new Builder().build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public int getPlayerCount() {
        return playerCount;
    }
    
    public int getInitialInfoTokens() {
        return initialInfoTokens;
    }
    
    public int getInitialFuseTokens() {
        return initialFuseTokens;
    }
    
    public int getMaxInfoTokens() {
        return maxInfoTokens;
    }
    
    public boolean isRainbowSuitEnabled() {
        return enableRainbowSuit;
    }
    
    public boolean isSixthSuitEnabled() {
        return enableSixthSuit;
    }
    
    public int getInitialHandSize() {
        return playerCount >= 4 ? 4 : 5;
    }
    
    public static class Builder {
        private int playerCount = 2;
        private int initialInfoTokens = 8;
        private int initialFuseTokens = 3;
        private int maxInfoTokens = 8;
        private boolean enableRainbowSuit = false;
        private boolean enableSixthSuit = false;
        
        public Builder playerCount(int playerCount) {
            if (playerCount < 2 || playerCount > 5) {
                throw new IllegalArgumentException("Player count must be between 2 and 5");
            }
            this.playerCount = playerCount;
            return this;
        }
        
        public Builder initialInfoTokens(int initialInfoTokens) {
            if (initialInfoTokens < 0) {
                throw new IllegalArgumentException("Initial info tokens cannot be negative");
            }
            this.initialInfoTokens = initialInfoTokens;
            return this;
        }
        
        public Builder initialFuseTokens(int initialFuseTokens) {
            if (initialFuseTokens < 0) {
                throw new IllegalArgumentException("Initial fuse tokens cannot be negative");
            }
            this.initialFuseTokens = initialFuseTokens;
            return this;
        }
        
        public Builder maxInfoTokens(int maxInfoTokens) {
            if (maxInfoTokens < 0) {
                throw new IllegalArgumentException("Max info tokens cannot be negative");
            }
            this.maxInfoTokens = maxInfoTokens;
            return this;
        }
        
        public Builder enableRainbowSuit(boolean enableRainbowSuit) {
            this.enableRainbowSuit = enableRainbowSuit;
            return this;
        }
        
        public Builder enableSixthSuit(boolean enableSixthSuit) {
            this.enableSixthSuit = enableSixthSuit;
            return this;
        }
        
        public GameConfig build() {
            return new GameConfig(this);
        }
    }
}