package com.example.paochess

import com.example.paochess.data.*
import org.junit.Test
import org.junit.Assert.*

class FenParserTest {

    @Test
    fun `parse standard starting position`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val board = FenParser.parseFen(fen)

        // 32 pieces total
        assertEquals(32, board.size)

        // Verify a few specific squares
        assertEquals(Piece(PieceType.ROOK, PieceColor.BLACK), board[Square(0, 7)])   // a8
        assertEquals(Piece(PieceType.KING, PieceColor.WHITE), board[Square(4, 0)])   // e1
        assertEquals(Piece(PieceType.PAWN, PieceColor.WHITE), board[Square(0, 1)])   // a2
        assertEquals(Piece(PieceType.PAWN, PieceColor.BLACK), board[Square(7, 6)])   // h7
    }

    @Test
    fun `parse sparse endgame position`() {
        val fen = "6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1"
        val board = FenParser.parseFen(fen)

        assertEquals(Piece(PieceType.KING, PieceColor.BLACK), board[Square(6, 7)])   // g8
        assertEquals(Piece(PieceType.ROOK, PieceColor.WHITE), board[Square(4, 0)])   // e1
        assertEquals(Piece(PieceType.KING, PieceColor.WHITE), board[Square(6, 0)])   // g1
        assertNull(board[Square(0, 0)])                                               // a1 empty
    }

    @Test
    fun `parse UCI move`() {
        val result = FenParser.parseUciMove("e1e8")
        assertNotNull(result)
        assertEquals(Square(4, 0), result!!.first)   // e1
        assertEquals(Square(4, 7), result.second)    // e8
    }

    @Test
    fun `parse UCI with promotion suffix`() {
        // Some UCI moves include a 5th character like e7e8q for promotion
        val result = FenParser.parseUciMove("e7e8q")
        assertNotNull(result)
        assertEquals(Square(4, 6), result!!.first)   // e7
        assertEquals(Square(4, 7), result.second)    // e8
    }

    @Test
    fun `Lichess puzzle board matches expected legal moves`() {
        val fen = "6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1"
        val board = FenParser.parseFen(fen)

        // White rook on e1 should be able to reach e8
        val rook = board[Square(4, 0)]
        assertNotNull(rook)
        val legal = getLegalMoves(Square(4, 0), rook!!, board)
        assertTrue("Rook should reach e8", legal.contains(Square(4, 7)))
    }
}
