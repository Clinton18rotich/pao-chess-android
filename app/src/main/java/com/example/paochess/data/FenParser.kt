package com.example.paochess.data

object FenParser {

    /**
     * Parses a FEN string into the app's Map<Square, Piece>.
     * FEN rank order is 8..1, but our board uses rank 7..0.
     */
    fun parseFen(fen: String): Map<Square, Piece> {
        val board = mutableMapOf<Square, Piece>()
        val ranks = fen.split(" ")[0].split("/")

        for (rankIndex in 0..7) {
            val actualRank = 7 - rankIndex
            var fileIndex = 0
            for (char in ranks[rankIndex]) {
                if (char.isDigit()) {
                    fileIndex += char.digitToInt()
                } else {
                    val type = when (char.lowercaseChar()) {
                        'p' -> PieceType.PAWN
                        'n' -> PieceType.KNIGHT
                        'b' -> PieceType.BISHOP
                        'r' -> PieceType.ROOK
                        'q' -> PieceType.QUEEN
                        'k' -> PieceType.KING
                        else -> continue
                    }
                    val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                    board[Square(fileIndex, actualRank)] = Piece(type, color)
                    fileIndex++
                }
            }
        }
        return board
    }

    /**
     * Converts a UCI move (e.g. "e2e4") into from/to Squares.
     * Returns null if the UCI string is malformed.
     */
    fun parseUciMove(uci: String): Pair<Square, Square>? {
        if (uci.length < 4) return null
        val from = Square(uci[0] - 'a', uci[1] - '1')
        val to = Square(uci[2] - 'a', uci[3] - '1')
        return Pair(from, to)
    }
}
