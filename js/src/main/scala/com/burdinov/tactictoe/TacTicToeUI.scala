package com.burdinov.tactictoe

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.html
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.Random
import Player.*
import Game.*

@JSExportTopLevel("TacTicToeUI")
object TacTicToeUI:
  def setupUI(): Unit =
    val menu = document.getElementById("menu").asInstanceOf[dom.html.Element]
    val app = document.getElementById("app").asInstanceOf[dom.html.Element]
    menu.innerHTML = """
      <div class='menu'>
        <img src='favicon.svg' alt='Tac-Tic-Toe Logo' class='logo'/>
        <h2>Welcome to Tac-Tic-Toe!</h2>
        <button id='play-human'>Play vs Human</button>
        <button id='play-bot'>Play vs Bot</button>
        <div class='instructions' style='max-width: 400px; margin-top: 2rem;'>
          <h3>How to Play</h3>
          <p>1. Win small tic-tac-toe boards to claim that space in the big board.</p>
          <p>2. Your move determines which small board your opponent must play in next.</p>
          <p>3. If sent to a completed board, your opponent can play in any available board.</p>
          <p>4. Win three small boards in a row to win the game!</p>
        </div>
      </div>
    """
    app.asInstanceOf[dom.html.Element].style.display = "none"
    document.getElementById("play-human").addEventListener("click", _ => {
      menu.asInstanceOf[dom.html.Element].style.display = "none"
      app.asInstanceOf[dom.html.Element].style.display = "block"
      startGame(humanVsBot = false)
    })
    document.getElementById("play-bot").addEventListener("click", _ => {
      menu.asInstanceOf[dom.html.Element].style.display = "none"
      app.asInstanceOf[dom.html.Element].style.display = "block"
      startGame(humanVsBot = true)
    })

  def startGame(humanVsBot: Boolean): Unit =
    var game = Game.emptyNested
    var humanPlayer: Player = X
    var botPlayer: Player = O
    var botActive = false
    if humanVsBot then
      if Random.nextBoolean() then
        humanPlayer = X; botPlayer = O
      else
        humanPlayer = O; botPlayer = X
      botActive = (game.currentPlayer == botPlayer)
      dom.window.setTimeout(() => {
        renderGameWithBot(game, humanPlayer, botPlayer, botActive)
      }, 0)
    else
      renderGame(game, document.getElementById("app"))

  def renderGameWithBot(game: Game, humanPlayer: Player, botPlayer: Player, botActive: Boolean): Unit =
    val app = document.getElementById("app")
    renderGame(game, app, Some((humanPlayer, botPlayer, botActive)))
    if !game.getWinner.isDefined && !game.isTie && botActive then
      // Bot's turn
      dom.window.setTimeout(() => {
        val move = getRandomBotMove(game)
        move.foreach { case (outer, inner) =>
          game.makeMove(outer, inner) match
            case Some(newGame) =>
              renderGameWithBot(newGame, humanPlayer, botPlayer, !botActive)
            case None =>
              renderGameWithBot(game, humanPlayer, botPlayer, botActive) // Should not happen
        }
      }, 600)

  def getRandomBotMove(game: Game): Option[(Int, Int)] =
    val validBoards: Seq[Int] = game.nextValidBoard match
      case Some(idx) if game.isTargetBoardPlayable(idx) => Seq(idx)
      case _ => game.cells.zipWithIndex.collect {
        case (v: Vector[Player], idx) if game.isTargetBoardPlayable(idx) => idx
      }
    val possibleMoves = for {
      outerIdx <- validBoards
      innerBoard = game.cells(outerIdx).asInstanceOf[Vector[Player]]
      innerIdx <- innerBoard.indices if innerBoard(innerIdx) == Player.Empty
    } yield (outerIdx, innerIdx)
    if possibleMoves.nonEmpty then Some(Random.shuffle(possibleMoves).head)
    else None

  def renderGame(game: Game, container: dom.Element, botInfo: Option[(Player, Player, Boolean)] = None): Unit =
    // Clear previous content
    while container.childNodes.length > 0 do container.removeChild(container.lastChild)

    val gameBoard = document.createElement("div").asInstanceOf[dom.html.Div]
    gameBoard.className = "game-board"
    
    // Create the outer board
    val outerBoard = document.createElement("div").asInstanceOf[dom.html.Div]
    outerBoard.className = "outer-board"
    
    // Determine which board is valid for the next move
    val nextValidBoardIndex = game.nextValidBoard
    
    // Render each cell of the outer board
    for (outerCell, outerIdx) <- game.cells.zipWithIndex do
      val outerCellDiv = document.createElement("div").asInstanceOf[dom.html.Div]
      outerCellDiv.className = "outer-cell"
      
      outerCell match
        case innerBoard: Vector[Player] =>
          // Create inner board container with border
          val innerBoardContainer = document.createElement("div").asInstanceOf[dom.html.Div]
          innerBoardContainer.className = "inner-board-container"
          
          // Create inner board
          val innerBoardDiv = document.createElement("div").asInstanceOf[dom.html.Div]
          innerBoardDiv.className = "inner-board"
          
          // Check if this inner board is won
          getBoardWinner(innerBoard) match
            case Some(winner) =>
              innerBoardDiv.className = s"inner-board winner-${winner.asString.toLowerCase}"
              val winnerMarker = document.createElement("div").asInstanceOf[dom.html.Div]
              winnerMarker.className = "winner-marker"
              winnerMarker.textContent = winner.asString
              innerBoardDiv.appendChild(winnerMarker)
            case None =>
              if innerBoard.forall(_ != Player.Empty) then
                innerBoardContainer.className += " full"
              for (innerCell, innerIdx) <- innerBoard.zipWithIndex do
                val cellButton = document.createElement("button").asInstanceOf[dom.html.Button]
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
                    val gameIsOver = game.getWinner.isDefined
                    val isMoveAllowed = !gameIsOver && (nextValidBoardIndex match
                      case Some(posIndex) if game.isTargetBoardPlayable(posIndex) => 
                        posIndex == outerIdx
                      case Some(_) => true
                      case None => true
                    )
                    val isBotTurn = botInfo.exists { case (human, bot, botActive) => botActive && game.currentPlayer == bot }
                    if isMoveAllowed && (!botInfo.isDefined || !isBotTurn) then
                      cellButton.onclick = _ =>
                        val newGame = game.makeMove(outerIdx, innerIdx)
                        newGame match
                          case Some(g) =>
                            botInfo match
                              case Some((human, bot, _)) =>
                                renderGameWithBot(g, human, bot, botActive = true)
                              case None =>
                                renderGame(g, container)
                          case None => ()
                    else
                      cellButton.disabled = true
                innerBoardDiv.appendChild(cellButton)
          innerBoardContainer.appendChild(innerBoardDiv)
          outerCellDiv.appendChild(innerBoardContainer)
        case player: Player =>
          outerCellDiv.textContent = player.asString
          outerCellDiv.className += s" inner-board-container winner-${player.asString.toLowerCase}"
      outerBoard.appendChild(outerCellDiv)
    gameBoard.appendChild(outerBoard)
    // Add game status
    val status = document.createElement("div").asInstanceOf[dom.html.Div]
    status.className = "status"
    if game.getWinner.isDefined then
      status.className += " game-over"
      status.textContent = s"Game Over! ${game.getWinner.get.asString} wins!"
    else if game.isTie then
      status.className += " game-over tie"
      status.textContent = "Game Over! It's a tie!"
    else
      val nextMoveInfo = game.nextValidBoard match
        case Some(pos) if game.isTargetBoardPlayable(pos) =>
          val location = pos match
            case 0 => "Top Left"
            case 1 => "Top Center" 
            case 2 => "Top Right"
            case 3 => "Middle Left"
            case 4 => "Middle Center"
            case 5 => "Middle Right" 
            case 6 => "Bottom Left"
            case 7 => "Bottom Center"
            case 8 => "Bottom Right"
            case _ => pos.toString
          s"${game.currentPlayer.asString}'s turn - Must play in the $location board"
        case Some(_) =>
          s"${game.currentPlayer.asString}'s turn - Can play in any available board"
        case None =>
          s"${game.currentPlayer.asString}'s turn - Can play in any board"
      status.textContent = nextMoveInfo
    gameBoard.appendChild(status)
    // Add game instructions
    val instructions = document.createElement("div").asInstanceOf[dom.html.Div]
    instructions.className = "instructions"
    instructions.innerHTML = """
      <h3>How to Play</h3>
      <p>1. Win small tic-tac-toe boards to claim that space in the big board.</p>
      <p>2. Your move determines which small board your opponent must play in next.</p>
      <p>3. If sent to a completed board, your opponent can play in any available board.</p>
      <p>4. Win three small boards in a row to win the game!</p>
    """
    gameBoard.appendChild(instructions)
    container.appendChild(gameBoard)

  @JSExportTopLevel("startGame")
  def startGame(): Unit =
    document.addEventListener("DOMContentLoaded", _ => setupUI()) 