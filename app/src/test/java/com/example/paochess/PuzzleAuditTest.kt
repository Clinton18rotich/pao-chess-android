package com.example.paochess

import com.example.paochess.data.*
import com.example.paochess.ui.screens.PUZZLES
import com.example.paochess.ui.screens.MoveStep
import org.junit.Test
import org.junit.Assert.*

class PuzzleAuditTest {

    /**
     * For each puzzle, verify:
     *   - Each move in the solution sequence is legal from the current board state
     *   - The board state evolves correctly
     *   - The sequence doesn't reference empty source squares
     */
    @Test
    fun `audit every puzzle sequence is legal`() {
        val failures = mutableListOf<String>()

        PUZZLES.forEachIndexed { index, puzzle ->
            val label = "#${index + 1} ${puzzle.name}"
            var board = puzzle.board
            var stepIndex = 0

            for (move in puzzle.solution) {
                stepIndex++
                val piece = board[move.from]
                if (piece == null) {
                    failures.add("$label step $stepIndex: source ${move.from} is empty (\"${move.description}\")")
                    break
                }

                val legal = getLegalMoves(move.from, piece, board)
                if (!legal.contains(move.to)) {
                    failures.add("$label step $stepIndex: ${move.from}->${move.to} ILLEGAL for ${piece.type}")
                    break
                }

                board = applyMove(board, move.from, move.to)
            }
        }

        if (failures.isNotEmpty()) {
            fail("Puzzle audit failures:\n" + failures.joinToString("\n"))
        }
    }
}
