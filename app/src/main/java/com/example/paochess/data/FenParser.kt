package com.example.paochess.data

import com.example.paochess.ui.screens.MoveStep

object FenParser {

    /**
     * Converts a FEN string into your app's Map<Square, Piece> format.
     * FEN ranks go from 8 down to 1, while your board uses rank 7 down to 0.
     */
    fun parseFen(fen: String): Map<Square, Piece> {
        val board = mutableMapOf<Square, Piece>()
        val ranks = fen.split(" ")[0].split("/")
        
        // FEN starts at rank 8 (index 0 in the array, but rank 7 in our system)
        for (rankIndex in 0..7) {
            var fileIndex = 0
            val rankString = ranks[rankIndex]
            val actualRank = 7 - rankIndex // Convert FEN rank (8..1) to board rank (7..0)

            for (char in rankString) {
                if (char.isDigit()) {
                    fileIndex += char.digitToInt() // Skip empty squares
                } else {
                    val pieceType = when (char.lowercaseChar()) {
                        'p' -> PieceType.PAWN
                        'n' -> PieceType.KNIGHT
                        'b' -> PieceType.BISHOP
                        'r' -> PieceType.ROOK
                        'q' -> PieceType.QUEEN
                        'k' -> PieceType.KING
                        else -> continue
                    }
                    val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                    
                    board[Square(fileIndex, actualRank)] = Piece(pieceType, color)
                    fileIndex++
                }
            }
        }
        return board
    }

    /**
     * Converts a UCI move string (e.g., "e2e4") into a MoveStep.
     * We use a generic description since the database doesn't provide text.
     */
    fun parseUciMove(uci: String, moveNumber: Int): MoveStep {
        val fromFile = uci[0] - 'a'
        val fromRank = uci[1] - '1'
        val toFile = uci[2] - 'a'
        val toRank = uci[3] - '1'
        
        return MoveStep(
            description = "Move $moveNumber: Play the best move.",
            from = Square(fromFile, fromRank),
            to = Square(toFile, toRank)
        )
    }

    /**
     * Parses the full Lichess puzzle format (FEN + UCI moves).
     * Note: In Lichess, the first move is the opponent's blunder.
     * The actual solution starts from the *second* move.
     */
    fun parseLichessPuzzle(fen: String, uciMoves: String): Pair<Map<Square, Piece>, List<MoveStep>> {
        val board = parseFen(fen)
        val allMoves = uciMoves.split(" ")
        
        // Apply the opponent's blunder first (the first move in the list)
        val opponentMove = allMoves[0]
        val updatedBoard = applyMove(board, 
            Square(opponentMove[0] - 'a', opponentMove[1] - '1'),
            Square(opponentMove[2] - 'a', opponentMove[3] - '1')
        )
        
        // The solution is the remaining moves
        val solutionMoves = allMoves.drop(1).mapIndexed { index, uci ->
            parseUciMove(uci, index + 1)
        }
        
        return Pair(updatedBoard, solutionMoves)
    }
}
