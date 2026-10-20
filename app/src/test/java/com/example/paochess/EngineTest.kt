package com.example.paochess

import com.example.paochess.data.*
import org.junit.Test
import org.junit.Assert.*

class EngineTest {

    @Test
    fun `start position sanity`() {
        val b = getInitialBoard()
        assertNotNull(findKing(PieceColor.WHITE, b))
        assertNotNull(findKing(PieceColor.BLACK, b))
        assertFalse(isInCheck(PieceColor.WHITE, b))
        assertFalse(isInCheck(PieceColor.BLACK, b))
    }

    @Test
    fun `back rank mate is real`() {
        // Board from the puzzle
        val b = mapOf(
            Square(4, 0) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(0, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(1, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(2, 1) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 7) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.WHITE)
        )
        val after = applyMove(b, Square(0, 7), Square(4, 0))
        assertTrue("Re8 should be mate", isCheckmate(PieceColor.BLACK, after))
    }

    @Test
    fun `knight cannot fork king to itself`() {
        // Whatever puzzle is called "Royal Fork" must produce real mate or check after the move.
        val b = mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(3, 6) to Piece(PieceType.QUEEN, PieceColor.BLACK),
            Square(3, 4) to Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        )
        val after = applyMove(b, Square(3, 4), Square(5, 5))
        assertTrue("Nf6+ should give check", isInCheck(PieceColor.BLACK, after))
    }
}
