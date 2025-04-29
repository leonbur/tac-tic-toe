package com.burdinov.tactictoe

//> using platform scala-js
//> using scala 3.3.1
//> using dep "org.scala-js::scalajs-dom::2.8.0"
//> using dep "org.scala-js::scalajs-library::1.14.0"
//> using jsDom


import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import Player.*

@JSExportTopLevel("TacTicToeUI")
object TacTicToeUI:
  def setupUI(): Unit =
    val container = document.getElementById("game")
    val gameTitle = document.createElement("h1")
    gameTitle.textContent = "Nested Tic-Tac-Toe"
    container.appendChild(gameTitle)

    var game = Board.emptyNested
    renderGame(game, container)

  def renderGame(board: Board, container: dom.Element): Unit =
    // Clear previous content
    while container.childNodes.length > 1 do // Keep the title
      container.removeChild(container.lastChild)

    val gameBoard = document.createElement("div")
    gameBoard.className = "game-board"
    
    // Create the outer board
    val outerBoard = document.createElement("div")
    outerBoard.className = "outer-board"
    
    // Render each cell of the outer board
    for (outerIdx, outerCell) <- board.cells.zipWithIndex do
      val outerCellDiv = document.createElement("div")
      outerCellDiv.className = "outer-cell"
      
      outerCell match
        case innerBoard: Board =>
          // Create inner board
          val innerBoardDiv = document.createElement("div")
          innerBoardDiv.className = "inner-board"
          
          // Check if this inner board is won
          innerBoard.getWinner match
            case Some(winner) =>
              innerBoardDiv.className = s"inner-board winner-${winner.asString.toLowerCase}"
              innerBoardDiv.textContent = winner.asString
            case None =>
              // Render inner board cells
              for (innerCell, innerIdx) <- innerBoard.cells.zipWithIndex do
                val cellButton = document.createElement("button")
                cellButton.className = "cell"
                innerCell match
                  case Player.X => 
                    cellButton.textContent = "X"
                    cellButton.className += " x"
                  case Player.O => 
                    cellButton.textContent = "O"
                    cellButton.className += " o"
                  case Player.Empty => 
                    cellButton.textContent = ""
                    // Only enable click if this is a valid move
                    board.nextValidBoard match
                      case Some(validPos)  =>
                    //   if validPos == outerIdx =>
                        cellButton.onclick = _ => makeMove(board, outerIdx, innerIdx, container)
                      case None => // First move or free choice
                        cellButton.onclick = _ => makeMove(board, outerIdx, innerIdx, container)
                      case _ => cellButton.disabled = true
                
                innerBoardDiv.appendChild(cellButton)
          
          outerCellDiv.appendChild(innerBoardDiv)
          
        case player: Player =>
          outerCellDiv.textContent = player.asString
          outerCellDiv.className += s" winner-${player.asString.toLowerCase}"
      
      outerBoard.appendChild(outerCellDiv)
    
    gameBoard.appendChild(outerBoard)
    
    // Add game status
    val status = document.createElement("div")
    status.className = "status"
    board.getWinner match
      case Some(winner) => 
        status.textContent = s"Game Over! ${winner.asString} wins!"
      case None =>
        val nextMoveInfo = board.nextValidBoard match
          case Some(pos) if board.isTargetBoardPlayable(pos) =>
            s"${board.currentPlayer.asString}'s turn - Must play in board $pos"
          case Some(_) =>
            s"${board.currentPlayer.asString}'s turn - Can play in any available board"
          case None =>
            s"${board.currentPlayer.asString}'s turn - Can play in any board"
        status.textContent = nextMoveInfo
    
    gameBoard.appendChild(status)
    container.appendChild(gameBoard)

  private def makeMove(board: Board, outerPos: Int, innerPos: Int, container: dom.Element): Unit =
    board.makeMove(outerPos, innerPos) match
      case Some(newBoard) => renderGame(newBoard, container)
      case None => () // Invalid move

  @JSExportTopLevel("startGame")
  def startGame(): Unit = 
    document.addEventListener("DOMContentLoaded", _ => setupUI()) 