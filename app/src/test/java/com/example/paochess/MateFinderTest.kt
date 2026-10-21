package com.example.paochess

import com.example.paochess.data.*
import com.example.paochess.ui.screens.PUZZLES
import org.junit.Test

class MateFinderTest {

    @Test
    fun `dump all mate-in-1 moves per puzzle board`() {
        println("=== MATE FINDER START ===")
        PUZZLES.forEachIndexed { i, p ->
            println("=== #${i + 1} ${p.name} ===")
            val color = if (p.solution.description.contains("White", ignoreCase = true))
                PieceColor.WHITE else PieceColor.BLACK
            val enemy = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            var found = 0
            for ((from, piece) in p.board) {
                if (piece.color != color) continue
                for (to in getLegalMoves(from, piece, p.board)) {
                    val after = applyMove(p.board, from, to)
                    if (isCheckmate(enemy, after)) {
                        println("  MATE: $from -> $to (${piece.type})")
                        found++
                    }
                }
            }
            if (found == 0) println("  NO MATE-IN-1 AVAILABLE — redesign board")
            println("  Currently claims: ${p.solution.from} -> ${p.solution.to}")
        }
        println("=== MATE FINDER END ===")
    }
}
