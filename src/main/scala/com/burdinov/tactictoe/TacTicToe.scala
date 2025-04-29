package com.burdinov.tactictoe

import Player.*

enum Player:
	case X
	case O
	case Empty

extension (p: Player)
	def asString: String = p match
		case Player.X => "X"
		case Player.O => "O"
		case Player.Empty => " "

case class Board(
	cells: Vector[Board | Player],
	nextValidBoard: Option[Int] = None,  // The index of the board where next move must be played
	currentPlayer: Player = Player.X     // Track whose turn it is
):
	def isValidPosition(pos: Int): Boolean =
		pos >= 0 && pos < 9

	private def getPlayerAt(board: Board | Player): Player = board match
		case p: Player => p
		case b: Board => b.getWinner.getOrElse(Player.Empty)

	def isFull: Boolean = cells.forall {
		case b: Board => b.isFull
		case p: Player => p != Player.Empty
	}

	def getWinner: Option[Player] =
		val lines = Seq(
			// Rows
			(0, 1, 2), (3, 4, 5), (6, 7, 8),
			// Columns
			(0, 3, 6), (1, 4, 7), (2, 5, 8),
			// Diagonals
			(0, 4, 8), (2, 4, 6)
		)
		
		lines.flatMap { case (a, b, c) =>
			val players = List(a, b, c).map(i => getPlayerAt(cells(i)))
			if players.forall(_ == players.head) && players.head != Player.Empty then
				Some(players.head)
			else None
		}.headOption

	def isTargetBoardPlayable(boardIndex: Int): Boolean =
		cells(boardIndex) match
			case b: Board => !b.isFull && b.getWinner.isEmpty
			case _ => false

	def makeMove(outerPos: Int, innerPos: Int): Option[Board] =
		if !isValidPosition(outerPos) || !isValidPosition(innerPos) then 
			None
		else
			// Check if the move is valid according to meta rules
			val isValidBoard = nextValidBoard match
				case None => currentPlayer == Player.X  // First move, X can play anywhere
				case Some(validPos) => 
					// Can play in the designated board, or if that board is full/won, can play anywhere
					outerPos == validPos || !isTargetBoardPlayable(validPos)

			if !isValidBoard then
				None
			else
				cells(outerPos) match
					case innerBoard: Board =>
						if !isTargetBoardPlayable(outerPos) then
							None  // Can't play in a full or won board
						else
							innerBoard.cells(innerPos) match
								case Player.Empty =>
									val newInnerBoard = Board(innerBoard.cells.updated(innerPos, currentPlayer))
									val nextPlayer = if currentPlayer == Player.X then Player.O else Player.X
									// Set next valid board, but only if that board is playable
									val nextBoard = 
										if isTargetBoardPlayable(innerPos) then Some(innerPos)
										else None  // Next player can play anywhere if target board is full/won
									Some(Board(
										cells = cells.updated(outerPos, newInnerBoard),
										nextValidBoard = nextBoard,
										currentPlayer = nextPlayer
									))
								case _ => None
					case _ => None

object Board:
	def empty: Board = Board(Vector.fill(9)(Player.Empty))
	def emptyNested: Board = Board(Vector.fill(9)(empty))

def boardToString(board: Board, depth: Int = 0): String =
	val indent = "  " * depth
	
	// Convert each cell to a list of lines
	val cellLines = board.cells.map {
		case b: Board => 
			val winner = b.getWinner.map(_.asString).getOrElse("")
			if winner.nonEmpty then
				Vector(s" $winner ")  // Single line for a won board
			else
				// Split inner board into lines and add proper indentation
				boardToString(b, depth + 1).split("\n").toVector
		case p: Player => Vector(p.asString)
	}

	// Group cells into rows
	val rowsOfCells = cellLines.grouped(3).toVector

	// For each row, combine the lines of its cells side by side
	val formattedRows = rowsOfCells.map { row =>
		// Find the maximum height of any cell in this row
		val maxHeight = row.map(_.length).max
		// Pad each cell's lines to the maximum height
		val paddedCells = row.map { cell =>
			cell.padTo(maxHeight, " " * cell.head.length)
		}
		// Combine the lines of all cells in the row
		(0 until maxHeight).map { lineIdx =>
			indent + paddedCells.map(_(lineIdx)).mkString(" ", " | ", " ")
		}.mkString("\n")
	}

	// Create horizontal separator
	val cellWidth = formattedRows.head.takeWhile(_ != '\n').length
	val separator = indent + "-" * cellWidth

	// Add the game state information at the bottom of the main board
	val boardStr = formattedRows.mkString(s"\n$separator\n")
	if depth == 0 then
		val nextBoardInfo = board.nextValidBoard match
			case Some(pos) => 
				if board.isTargetBoardPlayable(pos) then
					s"\nNext move must be in board $pos"
				else
					"\nNext player can play in any available board (target board is full or won)"
			case None => "\nNext player can play in any available board"
		boardStr + nextBoardInfo
	else
		boardStr

@main
def main(): Unit =
	var game = Board.emptyNested
	
	println("Initial board:")
	println(boardToString(game))
	
	// Example game sequence
	val moves = List(
		(7, 0), // X plays in board 7, position 0
		(0, 4), // O plays in board 0, position 4
		(4, 4), // X plays in board 4, position 4
		(4, 0), // O plays in board 4, position 0
		(0, 0), // X plays in board 0, position 0
		(0, 8), // O plays in board 0, position 8
		(8, 8)  // X plays in board 8, position 8
	)
	
	for ((outer, inner) <- moves)
		game.makeMove(outer, inner) match
			case Some(newBoard) =>
				game = newBoard
				println(s"\nAfter ${game.currentPlayer} plays in board $outer, position $inner:")
				println(boardToString(game))
				game.getWinner.foreach(winner => println(s"Game won by $winner!"))
			case None =>
				println(s"\nInvalid move: board $outer, position $inner")