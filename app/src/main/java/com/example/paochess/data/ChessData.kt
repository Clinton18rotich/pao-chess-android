package com.example.paochess.data

enum class PieceType { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }
enum class PieceColor { WHITE, BLACK }

data class Piece(val type: PieceType, val color: PieceColor)

data class Square(val file: Int, val rank: Int) {
    override fun toString(): String = "${'a' + file}${rank + 1}"
    fun inBounds() = file in 0..7 && rank in 0..7
}

data class ChessMove(val from: Square, val to: Square, val piece: Piece)

// ---------- Initial position ----------

fun getInitialBoard(): Map<Square, Piece> {
    val board = mutableMapOf<Square, Piece>()
    for (i in 0..7) {
        board[Square(i, 1)] = Piece(PieceType.PAWN, PieceColor.WHITE)
        board[Square(i, 6)] = Piece(PieceType.PAWN, PieceColor.BLACK)
    }
    val backRank = listOf(
        PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
        PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
    )
    for (i in 0..7) {
        board[Square(i, 0)] = Piece(backRank[i], PieceColor.WHITE)
        board[Square(i, 7)] = Piece(backRank[i], PieceColor.BLACK)
    }
    return board
}

// ---------- Pseudo-legal move generation (piece movement rules only) ----------
// Does NOT consider whether the move leaves own King in check. Use getLegalMoves for that.

fun getPseudoLegalMoves(from: Square, piece: Piece, board: Map<Square, Piece>): List<Square> {
    val moves = mutableListOf<Square>()
    val (file, rank) = from

    fun canLand(f: Int, r: Int): Boolean {
        val sq = Square(f, r)
        if (!sq.inBounds()) return false
        val occupant = board[sq]
        return occupant == null || occupant.color != piece.color
    }

    when (piece.type) {
        PieceType.PAWN -> {
            val dir = if (piece.color == PieceColor.WHITE) 1 else -1
            val startRank = if (piece.color == PieceColor.WHITE) 1 else 6
            val oneStep = Square(file, rank + dir)
            if (oneStep.inBounds() && board[oneStep] == null) {
                moves.add(oneStep)
                val twoStep = Square(file, rank + 2 * dir)
                if (rank == startRank && twoStep.inBounds() && board[twoStep] == null) {
                    moves.add(twoStep)
                }
            }
            // Captures: only onto enemy pieces
            for (df in listOf(-1, 1)) {
                val target = Square(file + df, rank + dir)
                if (target.inBounds()) {
                    val occ = board[target]
                    if (occ != null && occ.color != piece.color) moves.add(target)
                }
            }
        }

        PieceType.KNIGHT -> {
            val offsets = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
            offsets.forEach { (df, dr) -> if (canLand(file + df, rank + dr)) moves.add(Square(file + df, rank + dr)) }
        }

        PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN -> {
            val dirs = when (piece.type) {
                PieceType.BISHOP -> listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
                PieceType.ROOK   -> listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                else             -> listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1)
            }
            dirs.forEach { (df, dr) ->
                var f = file + df
                var r = rank + dr
                while (f in 0..7 && r in 0..7) {
                    val sq = Square(f, r)
                    val occ = board[sq]
                    if (occ == null) moves.add(sq)
                    else {
                        if (occ.color != piece.color) moves.add(sq)
                        break
                    }
                    f += df; r += dr
                }
            }
        }

        PieceType.KING -> {
            val dirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1)
            dirs.forEach { (df, dr) -> if (canLand(file + df, rank + dr)) moves.add(Square(file + df, rank + dr)) }

            // ═══ CASTLING — both types, both colors ═══
            val homeRank = if (piece.color == PieceColor.WHITE) 0 else 7
            if (file == 4 && rank == homeRank) {
                // KING-SIDE (O-O): rook at h1/h8 = file 7
                val rookK = board[Square(7, homeRank)]
                if (rookK != null && rookK.type == PieceType.ROOK && rookK.color == piece.color
                    && board[Square(5, homeRank)] == null
                    && board[Square(6, homeRank)] == null
                ) {
                    moves.add(Square(6, homeRank))
                }
                // QUEEN-SIDE (O-O-O): rook at a1/a8 = file 0
                val rookQ = board[Square(0, homeRank)]
                if (rookQ != null && rookQ.type == PieceType.ROOK && rookQ.color == piece.color
                    && board[Square(1, homeRank)] == null
                    && board[Square(2, homeRank)] == null
                    && board[Square(3, homeRank)] == null
                ) {
                    moves.add(Square(2, homeRank))
                }
            }
        }
    }
    return moves
}

// ---------- Attack detection ----------
// True if `sq` is attacked by any piece of `byColor`.

fun isSquareAttacked(sq: Square, byColor: PieceColor, board: Map<Square, Piece>): Boolean {
    for ((from, piece) in board) {
        if (piece.color != byColor) continue
        // For pawns, we must NOT use getPseudoLegalMoves because it filters by "capture onto enemy".
        // Instead, check pawn attack pattern explicitly.
        if (piece.type == PieceType.PAWN) {
            val dir = if (piece.color == PieceColor.WHITE) 1 else -1
            for (df in listOf(-1, 1)) {
                if (from.file + df == sq.file && from.rank + dir == sq.rank) return true
            }
        } else if (getPseudoLegalMoves(from, piece, board).contains(sq)) {
            return true
        }
    }
    return false
}

fun findKing(color: PieceColor, board: Map<Square, Piece>): Square? =
    board.entries.firstOrNull { it.value.type == PieceType.KING && it.value.color == color }?.key

fun isInCheck(color: PieceColor, board: Map<Square, Piece>): Boolean {
    val king = findKing(color, board) ?: return false
    val enemy = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
    return isSquareAttacked(king, enemy, board)
}

// ---------- Legal moves (filters pseudo-legal by "does not leave King in check") ----------

fun applyMove(board: Map<Square, Piece>, from: Square, to: Square): Map<Square, Piece> {
    val nb = board.toMutableMap()
    val piece = nb[from] ?: return board
    nb.remove(from)

    // ═══ PROMOTION FIX: pawn reaching far rank becomes Queen ═══
    if (piece.type == PieceType.PAWN) {
        val promotionRank = if (piece.color == PieceColor.WHITE) 7 else 0
        if (to.rank == promotionRank) {
            nb[to] = Piece(PieceType.QUEEN, piece.color)
            return nb
        }
    }

    nb[to] = piece

    // ═══ CASTLING: move the rook too ═══
    if (piece.type == PieceType.KING) {
        val homeRank = if (piece.color == PieceColor.WHITE) 0 else 7
        // King-side: e→g, rook h→f
        if (from.file == 4 && from.rank == homeRank && to.file == 6 && to.rank == homeRank) {
            val rook = nb[Square(7, homeRank)]
            if (rook != null && rook.type == PieceType.ROOK && rook.color == piece.color) {
                nb.remove(Square(7, homeRank))
                nb[Square(5, homeRank)] = rook
            }
        }
        // Queen-side: e→c, rook a→d
        if (from.file == 4 && from.rank == homeRank && to.file == 2 && to.rank == homeRank) {
            val rook = nb[Square(0, homeRank)]
            if (rook != null && rook.type == PieceType.ROOK && rook.color == piece.color) {
                nb.remove(Square(0, homeRank))
                nb[Square(3, homeRank)] = rook
            }
        }
    }

    return nb
}

fun getLegalMoves(from: Square, piece: Piece, board: Map<Square, Piece>): List<Square> {
    val pseudo = getPseudoLegalMoves(from, piece, board)
    return pseudo.filter { to ->
        val after = applyMove(board, from, to)
        !isInCheck(piece.color, after)
    }
}

// ---------- Checkmate / stalemate ----------

// ═══ Public helper: get all legal moves for a color ═══
fun allLegalMoves(color: PieceColor, board: Map<Square, Piece>): List<Pair<Square, Square>> {
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

fun isCheckmate(color: PieceColor, board: Map<Square, Piece>): Boolean {
    if (!isInCheck(color, board)) return false
    // Try every legal move for `color`
    for ((from, piece) in board) {
        if (piece.color != color) continue
        if (getLegalMoves(from, piece, board).isNotEmpty()) return false
    }
    return true
}

fun isStalemate(color: PieceColor, board: Map<Square, Piece>): Boolean {
    if (isInCheck(color, board)) return false
    for ((from, piece) in board) {
        if (piece.color != color) continue
        if (getLegalMoves(from, piece, board).isNotEmpty()) return false
    }
    return true
}
