# Hanabi Game Server

A Java implementation of the Hanabi cooperative card game server.

## Overview

Hanabi is a cooperative card game where players work together to create a fireworks display by playing cards in the correct order. The twist is that players can see everyone's cards except their own, and must give clues to help each other play the right cards.

## Features

- Complete implementation of Hanabi game rules
- Player interface for both human and AI players
- Network server for multiplayer games
- Card knowledge tracking system
- Action validation and game state management
- Support for 2-5 players

## Architecture

The project follows a clean architecture with clear separation of concerns:

- **Domain**: Core game entities (Card, Suit, Rank)
- **Game**: Game logic, player interface, actions, and state management
- **Server**: Network layer for multiplayer functionality
- **Util**: Helper classes and configuration

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build
```bash
mvn compile
```

### Run Tests
```bash
mvn test
```

### Run Server
```bash
mvn exec:java -Dexec.mainClass="com.javanabi.HanabiServer"
```

### Package
```bash
mvn package
```

## Game Rules

- **Cards**: 5 suits (white, yellow, green, blue, red) with ranks 1-5
- **Distribution**: 3x1, 2x2, 2x3, 2x4, 1x5 per suit (50 cards total)
- **Setup**: 8 info tokens, 3 fuse tokens
- **Hand size**: 5 cards (4 cards for 4-5 players)
- **Actions**: Give info, play card, discard card
- **Win condition**: Play all 5s successfully (25 points)
- **Lose condition**: Run out of fuse tokens

## Player Interface

The `Player` interface defines the contract for all players (human or AI):

```java
public interface Player {
    String getName();
    void initialize(GameState initialState);
    Action takeTurn(GameState currentState);
    void receiveClue(Clue clue);
    void notifyGameState(GameState state);
    void notifyPlayerAction(String playerName, Action action);
    void notifyGameEnd(int score, boolean won);
}
```

## Network Protocol

The server uses a simple text-based protocol:

- `LIST_CLIENTS` - Get list of available clients
- `CREATE_GAME player1,player2,...` - Create a new game
- `START_GAME [gameId]` - Start a game
- `GAME_ACTION actionData` - Submit a game action
- `GET_GAME_STATE` - Get current game state

## Contributing

Follow the coding guidelines in AGENTS.md when contributing to this project.

## License

This project is open source. See LICENSE file for details.