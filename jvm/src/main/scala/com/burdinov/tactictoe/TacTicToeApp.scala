package com.burdinov.tactictoe

import com.burdinov.tactictoe.Player.*
import com.burdinov.tactictoe.bots.RandomBot
import scala.io.StdIn.readLine
import scala.util.Random

@main
def main(): Unit =
  var game = Game.emptyNested

  println("Welcome to Tac-Tic-Toe!")
  println("Do you want to play against a bot? (y/n): ")
  val playBot = readLine().trim.toLowerCase.startsWith("y")

  val (humanPlayer, botPlayer) =
    if playBot then
      if Random.nextBoolean() then (X, O) else (O, X)
    else (X, O) // default, not used if not playing bot

  if playBot then
    println(s"You are $humanPlayer. Bot is $botPlayer. ${if (humanPlayer == X) "You start." else "Bot starts."}")

  println("Initial board:")
  println(gameToString(game))

  def humanMove(): Unit = {
    println(s"\nCurrent player: ${game.currentPlayer}")
    val board = getValidInput("Enter board number (0-8): ", 0, 8)
    val position = getValidInput("Enter position (0-8): ", 0, 8)
    game.makeMove(board, position) match
      case Some(newGame) =>
        game = newGame
        println("\nBoard after move:")
        println(gameToString(game))
        game.getWinner.foreach(winner => println(s"\nGame won by $winner!"))
        if game.isFull && game.getWinner.isEmpty then
          println("\nGame ended in a tie!")
      case None =>
        println(s"\nInvalid move: board $board, position $position")
        println("Please try again.")
        humanMove()
  }

  def botMove(): Unit = {
    println(s"\nBot ($botPlayer) is thinking...")
    Thread.sleep(500)
    RandomBot.randomMove(game) match
      case Some((board, position)) =>
        println(s"Bot plays in board $board, position $position")
        game.makeMove(board, position) match
          case Some(newGame) =>
            game = newGame
            println("\nBoard after move:")
            println(gameToString(game))
            game.getWinner.foreach(winner => println(s"\nGame won by $winner!"))
            if game.isFull && game.getWinner.isEmpty then
              println("\nGame ended in a tie!")
          case None =>
            println("Bot tried an invalid move! This should not happen.")
            botMove()
      case None =>
        println("Bot cannot make a move.")
  }

  if playBot then
    while game.getWinner.isEmpty && !game.isFull do
      if game.currentPlayer == humanPlayer then
        humanMove()
      else
        botMove()
  else
    while game.getWinner.isEmpty && !game.isFull do
      humanMove()

def getValidInput(prompt: String, min: Int, max: Int): Int =
  var input: Option[Int] = None
  while input.isEmpty do
    print(prompt)
    try
      val value = readLine().toInt
      if value >= min && value <= max then
        input = Some(value)
      else
        println(s"Please enter a number between $min and $max")
    catch
      case _: NumberFormatException =>
        println("Please enter a valid number")
  input.get

def gameToString(game: Game, depth: Int = 0): String =
  val indent = "  " * depth
  
  // Convert each cell to a list of lines
  val cellLines = game.cells.map {
    case v: Vector[Player] => 
      val winner = getBoardWinner(v).map(_.asString).getOrElse("")
      if winner.nonEmpty then
        // Create a 3x3 board with the winner in the middle, no grid lines
        val emptyRow = Vector("   ", " ", "   ")
        val middleRow = Vector("   ", winner, "   ")
        Vector(emptyRow, emptyRow, middleRow, emptyRow, emptyRow).map(_.mkString(" ", " ", " "))
      else
        // Split inner board into lines and add proper indentation
        val innerLines = v.grouped(3).map { row =>
          row.map(_.asString).mkString(" ", " │ ", " ")
        }.toVector
        // Add horizontal separators between rows
        val withSeparators = innerLines.zipWithIndex.flatMap { (line, idx) =>
          if idx < innerLines.length - 1 then
            val separator = line.map { c =>
              if c == '│' then "┼" else "─"
            }.mkString
            Vector(line, separator)
          else
            Vector(line)
        }
        withSeparators
    case p: Player => 
        // Create a 3x3 board with the winner in the middle, no grid lines
        val emptyRow = Vector("   ", " ", "   ")
        val middleRow = Vector("   ", p, "   ")
        Vector(emptyRow, emptyRow, middleRow, emptyRow, emptyRow).map(_.mkString(" ", " ", " "))

  }

  // Group cells into rows of 3 (outer board rows)
  val rowsOfCells = cellLines.grouped(3).toVector

  // For each row, combine the lines of its cells side by side with ║
  val formattedRows = rowsOfCells.map { row =>
    val maxHeight = row.map(_.length).max
    val paddedCells = row.map(cell => cell.padTo(maxHeight, " " * cell.head.length))
    (0 until maxHeight).map { lineIdx =>
      indent + paddedCells.map(_(lineIdx)).mkString(" ", " ║ ", " ")
    }.toVector
  }

  // Create the outer horizontal separator (═ and ╬)
  val rowWidth = formattedRows.head.head.length
  val cellWidths = formattedRows.head.head.split("║").map(_.length)
  val outerSeparator = cellWidths
    .map(w => "═" * w)
    .mkString("╬")
    .prependedAll(indent)
    .mkString

  // Insert the outer separator between each row of inner boards
  val boardWithSeparators = formattedRows.zipWithIndex.flatMap { case (row, idx) =>
    if idx < formattedRows.length - 1 then
      row :+ outerSeparator
    else
      row
  }.mkString("\n")

  // Add the game state information at the bottom of the main board
  if depth == 0 then
    val nextBoardInfo = game.nextValidBoard match
      case Some(pos) => 
        if game.isTargetBoardPlayable(pos) then
          s"\nNext move must be in board $pos"
        else
          "\nNext player can play in any available board (target board is full or won)"
      case None => "\nNext player can play in any available board"
    boardWithSeparators + nextBoardInfo
  else
    boardWithSeparators