package com.example.paochess.data

import com.example.paochess.ui.screens.MoveStep

/**
 * Minimal PGN parser. Handles SAN notation (e4, Nf3, Bb5, O-O, e8=Q, Qxf7#).
 * Returns a list of MoveSteps from an initial board.
 */
object PgnParser {

    fun parse(pgn: String): List<MoveStep> {
        val board = getInitialBoard().toMutableMap()
        val moves = mutableListOf<MoveStep>()

        // Strip comments between braces using non-brace pattern
        val moveText = pgn
            .lines()
            .filter { !it.startsWith("[") }
            .joinToString(" ")
            .replace(Regex("\\d+\\.\\.\\."), "")
            .replace(Regex("\\d+\\."), " ")
            .replace(Regex("\\x7B[^\\x7D]*\\x7D"), "")
            .replace(Regex("\\x28[^\\x29]*\\x29"), "")

        val tokens = moveText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        for (token in tokens) {
            val clean = token.replace("+", "").replace("#", "").replace("!", "").replace("?", "")
            if (clean.isBlank()) continue

            val move = parseSingleMove(clean, board) ?: continue
            moves.add(move)

            val piece = board[move.from] ?: continue
            board.remove(move.from)
            board[move.to] = piece
        }

        return moves
    }

    private fun parseSingleMove(san: String, board: Map<Square, Piece>): MoveStep? {
        if (san == "O-O" || san == "0-0") {
            val king = board.entries.firstOrNull { it.value.type == PieceType.KING && it.value.color == PieceColor.WHITE }?.key
                ?: board.entries.firstOrNull { it.value.type == PieceType.KING }?.key
                ?: return null
            return MoveStep("O-O castling", king, Square(king.file + 2, king.rank))
        }
        if (san == "O-O-O" || san == "0-0-0") {
            val king = board.entries.firstOrNull { it.value.type == PieceType.KING }?.key ?: return null
            return MoveStep("O-O-O castling", king, Square(king.file - 2, king.rank))
        }

        if (san.contains("=")) return null

        var pieceType: PieceType
        var rest = san

        when {
            rest.startsWith("K") -> { pieceType = PieceType.KING; rest = rest.drop(1) }
            rest.startsWith("Q") -> { pieceType = PieceType.QUEEN; rest = rest.drop(1) }
            rest.startsWith("R") -> { pieceType = PieceType.ROOK; rest = rest.drop(1) }
            rest.startsWith("B") -> { pieceType = PieceType.BISHOP; rest = rest.drop(1) }
            rest.startsWith("N") -> { pieceType = PieceType.KNIGHT; rest = rest.drop(1) }
            else -> { pieceType = PieceType.PAWN }
        }

        rest = rest.replace("x", "")

        if (rest.length < 2) return null
        val toSquare = parseSquareNotation(rest.takeLast(2)) ?: return null

        val disambig = rest.dropLast(2)

        val candidates = board.entries.filter { (_, p) ->
            p.type == pieceType
        }.filter { (from, piece) ->
            getLegalMoves(from, piece, board).contains(toSquare)
        }.filter { (from, _) ->
            disambig.isEmpty() ||
            (disambig.length >= 1 && from.file == disambig[0] - 'a') ||
            (disambig.length >= 1 && disambig[0].isDigit() && from.rank == disambig[0] - '1')
        }

        if (candidates.size != 1) return null

        val fromSquare = candidates.first().key
        return MoveStep(san, fromSquare, toSquare)
    }

    private fun parseSquareNotation(s: String): Square? {
        if (s.length != 2) return null
        val file = s[0] - 'a'
        val rank = s[1] - '1'
        if (file !in 0..7 || rank !in 0..7) return null
        return Square(file, rank)
    }
}
