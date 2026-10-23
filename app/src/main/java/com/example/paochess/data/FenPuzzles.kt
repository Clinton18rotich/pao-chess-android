package com.example.paochess.data

import com.example.paochess.ui.screens.MoveStep

data class MultiMovePuzzle(
    val name: String,
    val description: String,
    val board: Map<Square, Piece>,
    val solution: List<MoveStep>,
    val hint: String
)

val FEN_PUZZLES = listOf(
    MultiMovePuzzle(
        "Lichess Mate in 2",
        "White to move. Find the forced mate.",
        FenParser.parseFen("6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("Move 1 (White): Re8+. Push the Rook to the back rank.", Square(4, 0), Square(4, 7)),
            MoveStep("Move 2 (Black): Kf8. Forced king move.", Square(6, 7), Square(5, 7)),
            MoveStep("Move 3 (White): Rxf8#. Checkmate!", Square(4, 7), Square(5, 7))
        ),
        "Push the Rook to the back rank."
    ),
    MultiMovePuzzle(
        "Scholar's Mate from FEN",
        "White to move. Trap the King.",
        FenParser.parseFen("rnbqkbnr/pppp1ppp/8/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 0 3"),
        listOf(
            MoveStep("Move 1 (White): Qh5. Threaten f7.", Square(3, 0), Square(7, 4)),
            MoveStep("Move 2 (Black): Nf6. The final blunder.", Square(6, 7), Square(5, 5)),
            MoveStep("Move 3 (White): Qxf7#. Checkmate!", Square(7, 4), Square(5, 6))
        ),
        "Strike f7 with the Queen."
    ),
    MultiMovePuzzle(
        "Back Rank Mate",
        "White to move. Checkmate in 1.",
        FenParser.parseFen("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1"),
        listOf(
            MoveStep("Move 1 (White): Ra8#. Back rank mate!", Square(0, 0), Square(0, 7))
        ),
        "Push the Rook to the 8th rank."
    )
)
