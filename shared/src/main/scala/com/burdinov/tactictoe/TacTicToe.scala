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

case class Game(
	cells: Vector[Vector[Player] | Player],
	nextValidBoard: Option[Int] = None,  // The index of the board where next move must be played
	currentPlayer: Player = Player.X     // Track whose turn it is
):
	def isValidPosition(pos: Int): Boolean =
		pos >= 0 && pos < 9

	private def getPlayerAt(cell: Vector[Player] | Player): Player = cell match
		case p: Player => p
		case v: Vector[Player] => getBoardWinner(v).getOrElse(Player.Empty)

	def isFull: Boolean = cells.forall {
		case v: Vector[Player] => v.forall(_ != Player.Empty)
		case p: Player => p != Player.Empty
	}

	def hasAvailableMoves: Boolean = cells.zipWithIndex.exists {
		case (v: Vector[Player], idx) => isTargetBoardPlayable(idx)
		case _ => false
	}

	def isTie: Boolean = !hasAvailableMoves && getWinner.isEmpty

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
			case v: Vector[Player] => 
				// A board is playable if it's not full and not won
				!v.forall(_ != Player.Empty) && getBoardWinner(v).isEmpty
			case p: Player => 
				// If it's already a player (X or O), the board is won and not playable
				false

	def makeMove(outerPos: Int, innerPos: Int): Option[Game] =
		if !isValidPosition(outerPos) || !isValidPosition(innerPos) then 
			None
		else
			// Check if the move is valid according to meta rules
			val isValidBoard = nextValidBoard match
				case None => true
					// currentPlayer == Player.X  // First move, X can play anywhere
				case Some(validPos) => 
					// Can play in the designated board, or if that board is full/won, can play anywhere
					outerPos == validPos || !isTargetBoardPlayable(validPos)

			if !isValidBoard then
				None
			else
				cells(outerPos) match
					case innerBoard: Vector[Player] =>
						if !isTargetBoardPlayable(outerPos) then
							None  // Can't play in a full or won board
						else
							innerBoard(innerPos) match
								case Player.Empty =>
									val newInnerBoard = innerBoard.updated(innerPos, currentPlayer)
									val nextPlayer = if currentPlayer == Player.X then Player.O else Player.X
									// Check if this move won the inner board
									val updatedInnerBoard: Vector[Player] | Player = getBoardWinner(newInnerBoard) match
										case Some(winner) => winner  // Convert to winner if board is won
										case None => newInnerBoard   // Keep as board if not won
									// Set next valid board, but only if that board is playable
									val nextBoard = 
										if isTargetBoardPlayable(innerPos) then Some(innerPos)
										else None  // Next player can play anywhere if target board is full/won
									Some(Game(
										cells = cells.updated(outerPos, updatedInnerBoard),
										nextValidBoard = nextBoard,
										currentPlayer = nextPlayer
									))
								case _ => None
					case _ => None

object Game:
	def empty: Game = Game(Vector.fill(9)(Player.Empty))
	def emptyNested: Game = Game(Vector.fill(9)(Vector.fill(9)(Player.Empty)))

private def getBoardWinner(cells: Vector[Player]): Option[Player] =
	val lines = Seq(
		// Rows
		(0, 1, 2), (3, 4, 5), (6, 7, 8),
		// Columns
		(0, 3, 6), (1, 4, 7), (2, 5, 8),
		// Diagonals
		(0, 4, 8), (2, 4, 6)
	)
	
	lines.flatMap { case (a, b, c) =>
		val players = List(a, b, c).map(i => cells(i))
		if players.forall(_ == players.head) && players.head != Player.Empty then
			Some(players.head)
		else None
	}.headOption 