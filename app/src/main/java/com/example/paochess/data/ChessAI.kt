package com.example.paochess.data

import kotlin.math.min

data class AIMove(val from: Square, val to: Square, val score: Int, val taunt: String)

object ChessAI {
    private const val PAWN_VALUE = 100
    private const val KNIGHT_VALUE = 320
    private const val BISHOP_VALUE = 330
    private const val ROOK_VALUE = 500
    private const val QUEEN_VALUE = 900
    private const val KING_VALUE = 20000

    private val blunderTaunts = listOf(
        "Did you even look at the board before moving?",
        "I didn't know we were playing with a handicap.",
        "Are you trying to lose on purpose?",
        "My grandmother calculates faster than that.",
        "That was a beautiful move... if you were trying to help me.",
        "Free real estate! Thank you.",
        "You left that piece undefended. Amateur mistake."
    )

    private val queenTaunts = listOf(
        "Your Queen is mine! Do you even know how to protect your pieces?",
        "The Queen is off the board. You might as well resign.",
        "Take your Queen back to the toy box."
    )

    private val checkmateTaunts = listOf(
        "Checkmate. Go back to the tutorials.",
        "You lost. Maybe try Tic-Tac-Toe next time?",
        "Game over. That was embarrassing to watch."
    )

    fun getBestMove(board: Map<Square, Piece>, aiColor: PieceColor = PieceColor.BLACK): AIMove? {
        val aiMoves = getAllLegalMoves(aiColor, board)
        if (aiMoves.isEmpty()) return null

        var bestMove: AIMove? = null
        var bestScore = Int.MIN_VALUE

        for (move in aiMoves) {
            val boardAfterAI = applyMove(board, move.first, move.second)
            
            // Check if AI delivered checkmate
            val playerColor = if (aiColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            if (isCheckmate(playerColor, boardAfterAI)) {
                return AIMove(move.first, move.second, Int.MAX_VALUE, checkmateTaunts.random())
            }

            val playerMoves = getAllLegalMoves(playerColor, boardAfterAI)
            var worstPlayerScore = Int.MAX_VALUE
            
            if (playerMoves.isEmpty()) {
                worstPlayerScore = evaluateBoard(boardAfterAI, aiColor)
            } else {
                for (pMove in playerMoves) {
                    val boardAfterPlayer = applyMove(boardAfterAI, pMove.first, pMove.second)
                    val score = evaluateBoard(boardAfterPlayer, aiColor)
                    worstPlayerScore = min(worstPlayerScore, score)
                }
            }

            if (worstPlayerScore > bestScore) {
                bestScore = worstPlayerScore
                bestMove = AIMove(move.first, move.second, bestScore, "")
            }
        }

        // Generate taunt based on what was captured
        if (bestMove != null) {
            val capturedPiece = board[bestMove.to]
            var taunt = ""
            if (capturedPiece != null) {
                taunt = when (capturedPiece.type) {
                    PieceType.QUEEN -> queenTaunts.random()
                    PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT -> "You just hung your ${capturedPiece.type.name.lowercase()}! ${blunderTaunts.random()}"
                    PieceType.PAWN -> "Free pawn. ${blunderTaunts.random()}"
                    else -> blunderTaunts.random()
                }
            }
            bestMove = bestMove.copy(taunt = taunt)
        }

        return bestMove
    }

    fun evaluateBoard(board: Map<Square, Piece>, forColor: PieceColor): Int {
        var score = 0
        for ((_, piece) in board) {
            val value = when (piece.type) {
                PieceType.PAWN -> PAWN_VALUE
                PieceType.KNIGHT -> KNIGHT_VALUE
                PieceType.BISHOP -> BISHOP_VALUE
                PieceType.ROOK -> ROOK_VALUE
                PieceType.QUEEN -> QUEEN_VALUE
                PieceType.KING -> KING_VALUE
            }
            if (piece.color == forColor) score += value else score -= value
        }
        return score
    }

    private fun getAllLegalMoves(color: PieceColor, board: Map<Square, Piece>): List<Pair<Square, Square>> {
        val moves = mutableListOf<Pair<Square, Square>>()
        for ((from, piece) in board) {
            if (piece.color == color) {
                for (to in getLegalMoves(from, piece, board)) {
                    moves.add(Pair(from, to))
                }
            }
        }
        return moves
    }
}
