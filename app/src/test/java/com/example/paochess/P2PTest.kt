package com.example.paochess

import com.example.paochess.data.*
import org.junit.Assert.*
import org.junit.Test

class P2PTest {

    @Test
    fun testBoardSerializationRoundTrip() {
        val board = getInitialBoard()
        // Simulate a move (e2 to e4)
        val movedBoard = applyMove(board, Square(4, 1), Square(4, 3))
        
        val serialized = serializeBoardForTest(movedBoard)
        val deserialized = deserializeBoardForTest(serialized)
        
        assertEquals("Boards must match after round trip", movedBoard, deserialized)
    }

    @Test
    fun testPromotionSerialization() {
        val board = mapOf(
            Square(4, 6) to Piece(PieceType.PAWN, PieceColor.WHITE),
            Square(0, 0) to Piece(PieceType.KING, PieceColor.WHITE),
            Square(7, 7) to Piece(PieceType.KING, PieceColor.BLACK)
        )
        // Move pawn from e7 to e8 (Promotion)
        val promotedBoard = applyMove(board, Square(4, 6), Square(4, 7))
        
        val serialized = serializeBoardForTest(promotedBoard)
        val deserialized = deserializeBoardForTest(serialized)
        
        assertEquals(PieceType.QUEEN, deserialized[Square(4, 7)]?.type)
    }
}

// Helper functions for testing (matching the ones in P2PGameScreen)
fun serializeBoardForTest(board: Map<Square, Piece>): String {
    return board.entries.joinToString("|") { "${it.key.file},${it.key.rank},${it.value.type.name},${it.value.color.name}" }
}

fun deserializeBoardForTest(str: String): Map<Square, Piece> {
    if (str.isEmpty()) return getInitialBoard()
    val map = mutableMapOf<Square, Piece>()
    str.split("|").forEach { entry ->
        val parts = entry.split(",")
        if (parts.size == 4) {
            map[Square(parts[0].toInt(), parts[1].toInt())] = Piece(PieceType.valueOf(parts[2]), PieceColor.valueOf(parts[3]))
        }
    }
    return map
}
