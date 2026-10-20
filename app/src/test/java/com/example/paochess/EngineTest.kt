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
        // Same board as puzzle #6 — engine already proved this is mate
        val b = mapOf(
            Square(6, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(5, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(6, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(7, 6) to Piece(PieceType.PAWN, PieceColor.BLACK),
            Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        )
        val after = applyMove(b, Square(0, 0), Square(0, 7))
        assertTrue("Ra8 should be mate", isCheckmate(PieceColor.BLACK, after))
    }

    @Test
    fun `pawns cannot capture onto empty squares`() {
        val b = mapOf(
            Square(4, 1) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(4, 7) to Piece(PieceType.KING, PieceColor.BLACK),
            Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE)
        )
        val moves = getPseudoLegalMoves(Square(4, 1), Piece(PieceType.PAWN, PieceColor.WHITE), b)
        assertTrue(moves.contains(Square(4, 2)))
        assertTrue(moves.contains(Square(4, 3)))
        assertFalse(moves.contains(Square(3, 2)))
        assertFalse(moves.contains(Square(5, 2)))
    }
}
