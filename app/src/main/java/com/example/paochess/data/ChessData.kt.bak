package com.example.paochess.data

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }
enum class PieceColor { WHITE, BLACK }

data class Piece(val type: PieceType, val color: PieceColor)

data class Square(val file: Int, val rank: Int) {
    override fun toString(): String = "${'a' + file}${rank + 1}"
}

data class ChessMove(val from: Square, val to: Square, val piece: Piece)

// Initial board setup
fun getInitialBoard(): Map<Square, Piece> {
    val board = mutableMapOf<Square, Piece>()
    // Pawns
    for (i in 0..7) {
        board[Square(i, 1)] = Piece(PieceType.PAWN, PieceColor.WHITE)
        board[Square(i, 6)] = Piece(PieceType.PAWN, PieceColor.BLACK)
    }
    // Back ranks
    val backRank = listOf(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK)
    for (i in 0..7) {
        board[Square(i, 0)] = Piece(backRank[i], PieceColor.WHITE)
        board[Square(i, 7)] = Piece(backRank[i], PieceColor.BLACK)
    }
    return board
}

// Simplified legal moves generator (ignores check/checkmate for now)
fun getLegalMoves(from: Square, piece: Piece, board: Map<Square, Piece>): List<Square> {
    val moves = mutableListOf<Square>()
    val (file, rank) = from

    fun isSafe(f: Int, r: Int): Boolean = f in 0..7 && r in 0..7
    fun getPieceAt(f: Int, r: Int): Piece? = board[Square(f, r)]

    when (piece.type) {
        PieceType.PAWN -> {
            val dir = if (piece.color == PieceColor.WHITE) 1 else -1
            if (isSafe(file, rank + dir) && getPieceAt(file, rank + dir) == null) moves.add(Square(file, rank + dir))
            if (rank == (if (piece.color == PieceColor.WHITE) 1 else 6) && isSafe(file, rank + 2 * dir) && getPieceAt(file, rank + 2 * dir) == null) moves.add(Square(file, rank + 2 * dir))
            // Diagonal captures
            if (isSafe(file - 1, rank + dir) && getPieceAt(file - 1, rank + dir)?.color != piece.color) moves.add(Square(file - 1, rank + dir))
            if (isSafe(file + 1, rank + dir) && getPieceAt(file + 1, rank + dir)?.color != piece.color) moves.add(Square(file + 1, rank + dir))
        }
        PieceType.KNIGHT -> {
            val offsets = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
            offsets.forEach { (df, dr) -> if (isSafe(file + df, rank + dr) && getPieceAt(file + df, rank + dr)?.color != piece.color) moves.add(Square(file + df, rank + dr)) }
        }
        PieceType.BISHOP -> {
            val dirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
            dirs.forEach { (df, dr) ->
                var f = file + df; var r = rank + dr
                while (isSafe(f, r)) {
                    if (getPieceAt(f, r) == null) moves.add(Square(f, r))
                    else { if (getPieceAt(f, r)?.color != piece.color) moves.add(Square(f, r)); break }
                    f += df; r += dr
                }
            }
        }
        PieceType.ROOK -> {
            val dirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
            dirs.forEach { (df, dr) ->
                var f = file + df; var r = rank + dr
                while (isSafe(f, r)) {
                    if (getPieceAt(f, r) == null) moves.add(Square(f, r))
                    else { if (getPieceAt(f, r)?.color != piece.color) moves.add(Square(f, r)); break }
                    f += df; r += dr
                }
            }
        }
        PieceType.QUEEN -> {
            val dirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1)
            dirs.forEach { (df, dr) ->
                var f = file + df; var r = rank + dr
                while (isSafe(f, r)) {
                    if (getPieceAt(f, r) == null) moves.add(Square(f, r))
                    else { if (getPieceAt(f, r)?.color != piece.color) moves.add(Square(f, r)); break }
                    f += df; r += dr
                }
            }
        }
        PieceType.KING -> {
            val dirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1)
            dirs.forEach { (df, dr) -> if (isSafe(file + df, rank + dr) && getPieceAt(file + df, rank + dr)?.color != piece.color) moves.add(Square(file + df, rank + dr)) }
        }
    }
    return moves
}
