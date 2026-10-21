package com.example.paochess

import com.example.paochess.data.*
import com.example.paochess.ui.screens.PUZZLES
import org.junit.Test
import org.junit.Assert.*

class PuzzleAuditTest {

    @Test
    fun `audit every puzzle is legal`() {
        val failures = mutableListOf<String>()

        PUZZLES.forEachIndexed { index, puzzle ->
            val label = "#${index + 1} ${puzzle.name}"
            val move = puzzle.solution

            val piece = puzzle.board[move.from]
            if (piece == null) {
                failures.add("$label: source ${move.from} is empty")
                return@forEachIndexed
            }

            val legal = getLegalMoves(move.from, piece, puzzle.board)
            if (!legal.contains(move.to)) {
                failures.add("$label: ${move.from}->${move.to} ILLEGAL for ${piece.type}")
                return@forEachIndexed
            }

            // If it claims checkmate, verify it's actually mate
            if (puzzle.description.contains("Checkmate", ignoreCase = true)) {
                val after = applyMove(puzzle.board, move.from, move.to)
                val enemy = if (piece.color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                if (!isCheckmate(enemy, after)) {
                    failures.add("$label: ${move.from}->${move.to} NOT mate")
                }
            }
        }

        if (failures.isNotEmpty()) {
            fail("Puzzle audit failures:\n" + failures.joinToString("\n"))
        }
    }
}
