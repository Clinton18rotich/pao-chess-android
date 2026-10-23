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
        "Ladder Mate",
        "White to move. Force the King to the edge.",
        FenParser.parseFen("6k1/5ppp/8/8/8/8/5PPP/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Rook to e8, it checks the King on the back rank.", Square(4, 0), Square(4, 7)),
            MoveStep("Then Black will try to escape by moving the King to f8.", Square(6, 7), Square(5, 7)),
            MoveStep("But White will slide the Rook to f8, capturing the King's cover. This is Checkmate!", Square(4, 7), Square(5, 7))
        ),
        "Push the Rook to the back rank."
    ),
    MultiMovePuzzle(
        "Scholar's Trap",
        "White to move. Punish Black's careless move.",
        FenParser.parseFen("rnbqkbnr/pppp1ppp/8/4p3/2B1P3/8/PPPP1PPP/RNBQK1NR w KQkq - 0 3"),
        listOf(
            MoveStep("If White moves the Queen to h5, it targets the weak f7 pawn and threatens mate.", Square(3, 0), Square(7, 4)),
            MoveStep("Then Black will try to defend by moving the Knight to f6, blocking the threat.", Square(6, 7), Square(5, 5)),
            MoveStep("But White will capture the pawn on f7 with the Queen. This is Scholar's Mate!", Square(7, 4), Square(5, 6))
        ),
        "Strike f7 with the Queen."
    ),
    MultiMovePuzzle(
        "Back Rank Weakness",
        "White to move. Exploit the trapped King.",
        FenParser.parseFen("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Rook to a8, it invades the back rank and checks the King.", Square(0, 0), Square(0, 7)),
            MoveStep("Black's King is completely boxed in by his own pawns, resulting in Checkmate!", Square(0, 7), Square(0, 7))
        ),
        "Push the Rook to the 8th rank."
    ),
    MultiMovePuzzle(
        "Rook Skewer",
        "White to move. Attack the King and Queen.",
        FenParser.parseFen("4k3/4r3/8/8/8/8/8/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Rook to e7, it attacks the Black Rook.", Square(4, 0), Square(4, 6)),
            MoveStep("Then Black will capture the White Rook, but White will take back with the King.", Square(4, 6), Square(4, 6))
        ),
        "Attack the Rook."
    ),
    MultiMovePuzzle(
        "Queen Fork",
        "White to move. Attack two pieces at once.",
        FenParser.parseFen("7k/8/8/8/8/8/6q1/K6Q w - - 0 1"),
        listOf(
            MoveStep("If White moves the Queen to a8, it checks the King.", Square(7, 0), Square(0, 7)),
            MoveStep("Then Black will be forced to move the King, and White will capture the Queen.", Square(7, 7), Square(6, 7))
        ),
        "Check the King."
    ),
    MultiMovePuzzle(
        "The Skewer",
        "White to move. Force the King to move and win the Queen.",
        FenParser.parseFen("4k3/4q3/8/8/8/8/8/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Rook to e7, it checks the King on e8.", Square(4, 0), Square(4, 6)),
            MoveStep("Then Black is forced to move the King, exposing the Queen behind it.", Square(4, 7), Square(3, 7)),
            MoveStep("But White will capture the Queen with Rxe7. That is a Skewer!", Square(4, 6), Square(4, 6))
        ),
        "Check the King to win the Queen."
    ),
    MultiMovePuzzle(
        "The Pin",
        "White to move. Trap the Knight against the King.",
        FenParser.parseFen("4k3/3n4/8/8/8/8/8/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Rook to e7, it pins the Knight to the King.", Square(4, 0), Square(4, 6)),
            MoveStep("Since the Knight cannot move without exposing the King, it is trapped.", Square(4, 6), Square(4, 6)),
            MoveStep("And White will capture it with Rxd7. That is an Absolute Pin!", Square(4, 6), Square(3, 6))
        ),
        "Pin the Knight."
    ),
    MultiMovePuzzle(
        "The Fork",
        "White to move. Attack two pieces at once.",
        FenParser.parseFen("4k3/8/8/4N3/8/8/8/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Knight to f6, it checks the King.", Square(4, 4), Square(5, 5)),
            MoveStep("Then Black will be forced to move the King.", Square(4, 7), Square(3, 7)),
            MoveStep("And White will capture the Rook on the next move. That is a Royal Fork!", Square(5, 5), Square(5, 5))
        ),
        "Fork the King and Rook."
    ),
    MultiMovePuzzle(
        "Discovered Attack",
        "White to move. Reveal an attack on the Queen.",
        FenParser.parseFen("3qk3/8/8/4N3/8/8/8/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Knight to f7, it attacks the Queen and reveals the Rook's attack on the King.", Square(4, 4), Square(5, 5)),
            MoveStep("Then Black will move the King to escape the check.", Square(4, 7), Square(3, 7)),
            MoveStep("But White will capture the Queen with Nxd8. That is a Discovered Attack!", Square(5, 5), Square(3, 7))
        ),
        "Reveal the Rook's attack."
    ),
    MultiMovePuzzle(
        "Anastasia's Mate",
        "White to move. Checkmate in 2.",
        FenParser.parseFen("6kr/6pp/8/8/4N3/8/8/4R1K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Rook to e8, it checks the King.", Square(4, 0), Square(4, 7)),
            MoveStep("Then Black must move the King to g7.", Square(6, 7), Square(6, 6)),
            MoveStep("But White will play Nf6#, supported by the Rook. That is Anastasia's Mate!", Square(4, 4), Square(5, 5))
        ),
        "Check the King."
    ),
    MultiMovePuzzle(
        "Greek Gift Sacrifice",
        "White to move. Sacrifice the Bishop to open the King.",
        FenParser.parseFen("rnbq1rk1/pp2bppp/4pn2/2pp4/3P4/2NBP3/PP3PPP/R1BQK1NR w KQ - 0 1"),
        listOf(
            MoveStep("If White moves the Bishop to h7, it checks the King and sacrifices itself.", Square(3, 3), Square(7, 6)),
            MoveStep("Then Black will capture the Bishop.", Square(6, 7), Square(7, 6)),
            MoveStep("But White will bring the Queen to h5 for a devastating attack. This is the Greek Gift Sacrifice!", Square(3, 0), Square(7, 4))
        ),
        "Sacrifice the Bishop."
    ),
    MultiMovePuzzle(
        "The Windmill",
        "White to move. Unleash a devastating discovered check.",
        FenParser.parseFen("6k1/5ppp/8/8/8/8/5PPP/2R3K1 w - - 0 1"),
        listOf(
            MoveStep("If White moves the Rook to c8, it checks the King.", Square(2, 0), Square(2, 7)),
            MoveStep("Then Black must move the King to g7.", Square(6, 7), Square(6, 6)),
            MoveStep("But White will play Rxh7, setting up the windmill. This is a devastating attack!", Square(2, 7), Square(7, 6))
        ),
        "Check the King."
    )
)
