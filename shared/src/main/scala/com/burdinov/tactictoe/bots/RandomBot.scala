package com.burdinov.tactictoe.bots

import com.burdinov.tactictoe._
import scala.util.Random

object RandomBot {
  /**
   * Returns (outerBoardIdx, innerCellIdx) for a random legal move, or None if no moves available.
   */
  def randomMove(game: Game): Option[(Int, Int)] = {
    val validBoards: Seq[Int] = game.nextValidBoard match {
      case Some(idx) if game.isTargetBoardPlayable(idx) => Seq(idx)
      case _ => game.cells.zipWithIndex.collect {
        case (v: Vector[Player], idx) if game.isTargetBoardPlayable(idx) => idx
      }
    }
    val possibleMoves = for {
      outerIdx <- validBoards
      innerBoard = game.cells(outerIdx).asInstanceOf[Vector[Player]]
      innerIdx <- innerBoard.indices if innerBoard(innerIdx) == Player.Empty
    } yield (outerIdx, innerIdx)
    if possibleMoves.nonEmpty then Some(Random.shuffle(possibleMoves).head)
    else None
  }
} 