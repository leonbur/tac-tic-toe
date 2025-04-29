package com.burdinov.tactictoe

import munit.FunSuite
import Player.*

class TacTicToeTest extends FunSuite {
  
  test("Player.asString should return correct string representation") {
    assertEquals(X.asString, "X")
    assertEquals(O.asString, "O")
    assertEquals(Empty.asString, " ")
  }

  test("Game.emptyNested should create an empty game with X as first player") {
    val game = Game.emptyNested
    assertEquals(game.currentPlayer, X)
    game.cells.foreach {
      case v: Vector[Player] => 
        v.foreach(cell => assertEquals(cell, Empty))
      case p: Player => 
        fail("New game should not have any won boards")
    }
    assertEquals(game.nextValidBoard, None)
  }

  test("Game.isValidPosition should correctly validate board positions") {
    val game = Game.emptyNested
    assert(game.isValidPosition(0))
    assert(game.isValidPosition(8))
    assert(!game.isValidPosition(-1))
    assert(!game.isValidPosition(9))
  }

  test("makeMove should correctly update the board for valid moves") {
    val game = Game.emptyNested
    val move = game.makeMove(4, 4) // Center of center board
    
    assert(move.isDefined)
    move.get.cells(4) match {
      case v: Vector[Player] => 
        assertEquals(v(4), X)
      case _ => 
        fail("Center board should still be a vector")
    }
    assertEquals(move.get.currentPlayer, O)
    assertEquals(move.get.nextValidBoard, Some(4))
  }

  test("makeMove should reject invalid moves") {
    val game = Game.emptyNested
    assertEquals(game.makeMove(-1, 0), None) // Invalid outer position
    assertEquals(game.makeMove(9, 0), None)  // Invalid outer position
    assertEquals(game.makeMove(0, -1), None) // Invalid inner position
    assertEquals(game.makeMove(0, 9), None)  // Invalid inner position
  }

  test("makeMove should reject moves on occupied spaces") {
    val game = Game.emptyNested
    val afterFirstMove = game.makeMove(0, 0).get
    assertEquals(afterFirstMove.makeMove(0, 0), None) // Same position
  }

  test("getBoardWinner should identify horizontal wins") {
    val horizontalWinBoard = Vector(
      X, X, X,
      O, O, Empty,
      Empty, Empty, Empty
    )
    assertEquals(getBoardWinner(horizontalWinBoard), Some(X))
  }

  test("getBoardWinner should identify vertical wins") {
    val verticalWinBoard = Vector(
      X, O, Empty,
      X, O, Empty,
      X, Empty, Empty
    )
    assertEquals(getBoardWinner(verticalWinBoard), Some(X))
  }

  test("getBoardWinner should identify diagonal wins") {
    val diagonalWinBoard = Vector(
      O, Empty, X,
      Empty, O, X,
      X, Empty, O
    )
    assertEquals(getBoardWinner(diagonalWinBoard), Some(O))
  }

  test("getBoardWinner should return None when there's no winner") {
    val noWinnerBoard = Vector(
      X, O, X,
      O, X, O,
      O, X, Empty
    )
    assertEquals(getBoardWinner(noWinnerBoard), None)
  }

  test("getWinner should identify wins in the meta board") {
    val winningGame = Game(Vector(
      X, X, X, // Top row won by X
      Vector.fill(9)(Empty),
      Vector.fill(9)(Empty),
      Vector.fill(9)(Empty),
      Vector.fill(9)(Empty),
      Vector.fill(9)(Empty),
      Vector.fill(9)(Empty),
      Vector.fill(9)(Empty),
      Vector.fill(9)(Empty)
    ), None, O)

    assertEquals(winningGame.getWinner, Some(X))
  }

  test("isTargetBoardPlayable should correctly identify playable boards") {
    val game = Game.emptyNested
    assert(game.isTargetBoardPlayable(0))
    
    // Test full board
    val fullBoard = Vector.fill(9)(X)
    val gameWithFullBoard = Game(game.cells.updated(0, fullBoard), None, O)
    assert(!gameWithFullBoard.isTargetBoardPlayable(0))
    
    // Test won board
    val wonBoard = X // Board converted to winner
    val gameWithWonBoard = Game(game.cells.updated(0, wonBoard), None, O)
    assert(!gameWithWonBoard.isTargetBoardPlayable(0))
  }

  test("Game should correctly identify tie states") {
    // Create a game where all boards are full but no winner
    val tieBoards = Vector.tabulate(9) { i =>
      Vector(
        X, O, X,
        X, X, O,
        O, X, O
      )
    }
    val tieGame = Game(tieBoards, None, X)
    assert(tieGame.isTie)
    assert(!tieGame.hasAvailableMoves)
  }

  test("Game should correctly identify when moves are still available") {
    val game = Game.emptyNested
    assert(game.hasAvailableMoves)
    assert(!game.isTie)
  }

  test("Game should enforce next valid board rules") {
    val game = Game.emptyNested
    val firstMove = game.makeMove(0, 1).get // Should force next move in board 1
    
    assertEquals(firstMove.nextValidBoard, Some(1))
    assertEquals(firstMove.makeMove(0, 0), None) // Invalid - wrong board
    assert(firstMove.makeMove(1, 0).isDefined)  // Valid - correct board
  }

  test("Game should allow play anywhere when target board is won") {
    val wonBoard = X // Represents a won board
    val game = Game(Game.emptyNested.cells.updated(4, wonBoard), Some(4), O)
    
    // Should be able to play in any other board
    assert(game.makeMove(0, 0).isDefined)
    assert(game.makeMove(8, 8).isDefined)
  }
} 