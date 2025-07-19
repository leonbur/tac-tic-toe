# <img src="js/assets/favicon.svg" width="30"/> Tac-Tic-Toe (tactical tic-tac-toe) 

A nested Tic-Tac-Toe game implemented in Scala and Scala.js. The game features a 3x3 grid where each cell contains another 3x3 grid, creating a meta-game of Tic-Tac-Toe where your move determines which grid your opponent must play in next.

## Project Structure

This is a Scala.js cross-project with:
- Shared logic in `shared/`
- JVM-specific code in `jvm/`
- JavaScript frontend in `js/`

## Building and Running

### Prerequisites

- JDK 11 or later
- SBT 1.5.0 or later

### Running the JVM Console Version (this is mainly for testing out stuff)

```bash
sbt tacTicToeJVM/run
```

### Building and Running the JS Version (the pretty version)

```bash
# Build the JavaScript version
sbt tacTicToeJS/fastLinkJS

# The compiled JavaScript will be in:
# js/target/scala-3.X/tac-tic-toe-fastopt/

# Start the development server with Vite
cd js
npm run dev

# For production builds:
cd js
sbt tacTicToeJS/fullLinkJS
npm run build
```

Then visit http://localhost:3000 in your browser.

## Game Rules

1. The game consists of 9 small Tic-Tac-Toe boards arranged in a 3x3 grid
2. The first player can place their mark anywhere on any of the small boards
3. The position of the last mark determines which board the next player must play in
4. If a player is sent to a board that is already full or won, they can choose any open board
5. Win three small boards in a row to win the game

## Development

### Compiling

```bash
# Compile all projects
sbt compile

# Compile just the JS project
sbt tacTicToeJS/compile

# Compile just the JVM project
sbt tacTicToeJVM/compile
```

### Testing

```bash
# Run all tests
sbt test
``` 