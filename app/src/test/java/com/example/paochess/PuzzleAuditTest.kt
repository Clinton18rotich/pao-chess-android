package com.example.paochess

import com.example.paochess.data.*
import com.example.paochess.ui.screens.PUZZLES
import org.junit.Test
import org.junit.Assert.*

class PuzzleAuditTest {

    /**
     * For each puzzle, verify:
     *   - the solution move is legal
     *   - if the label says "Checkmate", the resulting position IS mate
     *   - if not mate, FIND a real mate-in-1 on that board (so we can fix it)
     */
    @Test
    fun `audit every puzzle`() {
        val failures = mutableListOf<String>()

        PUZZLES.forEachIndexed { index, puzzle ->
            val label = "#${index + 1} ${puzzle.name}"
            val piece = puzzle.board[puzzle.solution.from]
            if (piece == null) {
                failures.add("$label: source ${puzzle.solution.from} is empty")
                return@forEachIndexed
            }

            val legal = getLegalMoves(puzzle.solution.from, piece, puzzle.board)
            if (!legal.contains(puzzle.solution.to)) {
                failures.add("$label: ${puzzle.solution.from}->${puzzle.solution.to} is ILLEGAL for ${piece.type}")
                return@forEachIndexed
            }

            val after = applyMove(puzzle.board, puzzle.solution.from, puzzle.solution.to)
            val enemy = if (piece.color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE

            if (puzzle.description.contains("Checkmate", ignoreCase = true)) {
                if (!isCheckmate(enemy, after)) {
                    val alt = findMateInOne(piece.color, puzzle.board)
                    failures.add("$label: ${puzzle.solution.from}->${puzzle.solution.to} NOT mate. Real mate-in-1: $alt")
                }
            } else {
                // Non-mate tactics: just confirm the move gives check or wins material (attacks a valuable piece)
                val givesCheck = isInCheck(enemy, after)
                if (!givesCheck) {
                    failures.add("$label: ${puzzle.solution.from}->${puzzle.solution.to} does not give check (label='${puzzle.description}')")
                }
            }
        }

        if (failures.isNotEmpty()) {
            fail("Puzzle audit failures:\n" + failures.joinToString("\n"))
        }
    }

    /** Brute-force: find ANY legal move by `color` that delivers checkmate. */
    private fun findMateInOne(color: PieceColor, board: Map<Square, Piece>): String {
        val enemy = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for ((from, piece) in board) {
            if (piece.color != color) continue
            for (to in getLegalMoves(from, piece, board)) {
                val after = applyMove(board, from, to)
                if (isCheckmate(enemy, after)) return "$from -> $to (${piece.type})"
            }
        }
        return "none found — redesign this puzzle"
    }
}
