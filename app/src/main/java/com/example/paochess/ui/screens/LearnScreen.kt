package com.example.paochess.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun LearnScreen() {
    // 0-5 = Pieces, 6 = Endgame, 7 = Castling, 8 = En Passant, 9 = Capture
    var selectedTab by remember { mutableStateOf(0) }

    val explanations = listOf(
        "The Pawn moves forward 1 square. On its very first move it may advance 2 squares. It captures diagonally. If it reaches the far rank, it promotes.",
        "The Knight moves in an 'L' shape: two squares in one direction, then one square perpendicular. It is the only piece that can jump over others.",
        "The Bishop moves diagonally any number of squares. It stays on the same color square for the whole game.",
        "The Rook moves in straight lines (horizontal or vertical) any number of squares. It is very powerful on open files.",
        "The Queen is the most powerful piece. She moves in straight lines or diagonally, any number of squares.",
        "The King moves 1 square in any direction. You cannot move the King into check.",
        "How the game ends: You win by Checkmate (trapping the King). The game is a Draw if it's a Stalemate (no legal moves but not in check), or if there is insufficient material to mate, or a 50-move rule.",
        "Castling: The King moves two squares toward the Rook, and the Rook jumps to the other side. It is a great way to protect your King.",
        "En Passant: If an enemy Pawn moves two squares and lands next to your Pawn, you can capture it as if it had only moved one square.",
        "Capture: To capture a piece, you move your piece onto the square occupied by the enemy piece. Tap the White Knight, then tap the Black Pawn to capture it!"
    )

    val icons = listOf("♙", "♘", "♗", "♖", "♕", "♔", "🏁", "🏰", "👻", "⚔️")
    val buttonGlyphs = listOf("♙", "♘", "♗", "♖", "♕", "♔", "🏁", "🏰", "👻", "⚔️")

    val showBoard = selectedTab != 6 // Show board for all except Endgame

    // --- INTERACTIVE STATE ---
    var currentBoard by remember { mutableStateOf(mapOf<Square, Piece>()) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }

    // --- SPECIAL MOVE ANIMATION STATE ---
    var specialStep by remember { mutableStateOf(0) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setPitch(1.15f)
                tts?.setSpeechRate(0.95f)
                isTtsReady = true
            }
        }
    }

    // Setup the board based on the selected tab
    LaunchedEffect(selectedTab) {
        selectedSquare = null
        legalMoves = emptyList()
        specialStep = 0
        tts?.stop()

        when {
            selectedTab < 6 -> {
                val b = mutableMapOf<Square, Piece>()
                val pieceType = PieceType.values()[selectedTab]
                b[Square(4, 3)] = Piece(pieceType, PieceColor.WHITE)
                if (pieceType == PieceType.PAWN) {
                    b[Square(3, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
                    b[Square(5, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
                }
                currentBoard = b
                if (isTtsReady) tts?.speak(explanations[selectedTab], TextToSpeech.QUEUE_FLUSH, null, null)
            }
            selectedTab == 7 -> { // Castling
                currentBoard = mapOf(
                    Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE), // e1
                    Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE), // h1
                )
                if (isTtsReady) tts?.speak("White castles kingside. The King moves two squares, and the Rook jumps over.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(2500L)
                specialStep = 1 // King moves to g1
                delay(1500L)
                specialStep = 2 // Rook moves to f1
            }
            selectedTab == 8 -> { // En Passant
                currentBoard = mapOf(
                    Square(4, 4) to Piece(PieceType.PAWN, PieceColor.WHITE), // e5
                    Square(3, 4) to Piece(PieceType.PAWN, PieceColor.BLACK), // d5
                )
                if (isTtsReady) tts?.speak("Black just moved their pawn two squares. White can capture it En Passant.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(2500L)
                specialStep = 1 // White pawn captures to d6, black pawn removed
            }
            selectedTab == 9 -> { // Capture Practice
                currentBoard = mapOf(
                    Square(4, 3) to Piece(PieceType.KNIGHT, PieceColor.WHITE), // e4
                    Square(6, 5) to Piece(PieceType.PAWN, PieceColor.BLACK),   // g6
                )
                if (isTtsReady) tts?.speak("To capture, move your piece onto the enemy piece. Tap the White Knight, then tap the Black Pawn.", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("📖 Learn Chess", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Buttons (Rows of 5)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 0..4) {
                Button(onClick = { selectedTab = i }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == i) Orange else DarkSurface), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)) {
                    Text(buttonGlyphs[i], fontSize = 16.sp, color = if (selectedTab == i) Color.Black else White)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 5..9) {
                Button(onClick = { selectedTab = i }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == i) Orange else DarkSurface), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)) {
                    Text(buttonGlyphs[i], fontSize = 16.sp, color = if (selectedTab == i) Color.Black else White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(10.dp)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icons[selectedTab], color = Orange, fontSize = 36.sp, modifier = Modifier.padding(end = 12.dp))
                Text(explanations[selectedTab], color = Color(0xFFDDDDDD), fontSize = 14.sp, lineHeight = 18.sp, modifier = Modifier.weight(1f))
            }
        }

        if (showBoard) {
            Spacer(Modifier.height(16.dp))
            
            // --- BOARD ---
            Box(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, bottom = 20.dp)) {
                // Rank Numbers
                Column(modifier = Modifier.fillMaxHeight().width(20.dp).align(Alignment.CenterStart), verticalArrangement = Arrangement.SpaceEvenly) {
                    for (rank in 7 downTo 0) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("${rank + 1}", color = LightGrey, fontSize = 12.sp) }
                    }
                }

                // The Board
                Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (rank in 7 downTo 0) {
                                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                    for (file in 0..7) {
                                        val square = Square(file, rank)
                                        
                                        // Animation logic for Special Moves
                                        var displayPiece = currentBoard[square]
                                        if (selectedTab == 7) { // Castling
                                            if (specialStep >= 1 && square == Square(6, 0)) displayPiece = Piece(PieceType.KING, PieceColor.WHITE)
                                            if (specialStep >= 1 && square == Square(4, 0)) displayPiece = null
                                            if (specialStep >= 2 && square == Square(5, 0)) displayPiece = Piece(PieceType.ROOK, PieceColor.WHITE)
                                            if (specialStep >= 2 && square == Square(7, 0)) displayPiece = null
                                        }
                                        if (selectedTab == 8) { // En Passant
                                            if (specialStep >= 1 && square == Square(3, 5)) displayPiece = Piece(PieceType.PAWN, PieceColor.WHITE) // White pawn to d6
                                            if (specialStep >= 1 && square == Square(4, 4)) displayPiece = null // White pawn left e5
                                            if (specialStep >= 1 && square == Square(3, 4)) displayPiece = null // Black pawn captured
                                        }

                                        val isDark = (file + rank) % 2 == 1
                                        val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                        val isLegal = legalMoves.contains(square)
                                        val isSelected = selectedSquare == square

                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxHeight().background(squareColor)
                                                .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                                .clickable {
                                                    if (selectedTab < 6 || selectedTab == 9) {
                                                        if (isLegal) {
                                                            val newB = currentBoard.toMutableMap()
                                                            newB[square] = currentBoard[selectedSquare!!]!!
                                                            newB.remove(selectedSquare!!)
                                                            currentBoard = newB
                                                            selectedSquare = null
                                                            legalMoves = emptyList()
                                                            
                                                            // If they just completed the capture tutorial
                                                            if (selectedTab == 9 && square == Square(6, 5)) {
                                                                if (isTtsReady) tts?.speak("Excellent! You captured the pawn.", TextToSpeech.QUEUE_FLUSH, null, null)
                                                            }
                                                        } else if (displayPiece != null && displayPiece.color == PieceColor.WHITE) {
                                                            selectedSquare = square
                                                            legalMoves = getLegalMoves(square, displayPiece, currentBoard)
                                                        } else {
                                                            selectedSquare = null
                                                            legalMoves = emptyList()
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isLegal) Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                            if (displayPiece != null) {
                                                val glyph = when (displayPiece.type) {
                                                    PieceType.PAWN -> "♟"; PieceType.KNIGHT -> "♞"; PieceType.BISHOP -> "♝"
                                                    PieceType.ROOK -> "♜"; PieceType.QUEEN -> "♛"; PieceType.KING -> "♚"
                                                }
                                                val text = if (displayPiece.color == PieceColor.WHITE) glyph.replace("♟","♙").replace("♞","♘").replace("♝","♗").replace("♜","♖").replace("♛","♕").replace("♚","♔") else glyph
                                                val shadowColor = if (displayPiece.color == PieceColor.WHITE) Color.White else Color(0xFFAAAAAA)
                                                Text(text = text, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold, style = TextStyle(shadow = Shadow(color = shadowColor, offset = Offset(1.5f, 1.5f), blurRadius = 3f)))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // File Letters
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        for (file in 0..7) {
                            Text("${'a' + file}", color = LightGrey, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            
            if (selectedTab < 6 || selectedTab == 9) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { selectedTab = -1; selectedTab = 0 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)), modifier = Modifier.fillMaxWidth()) {
                    Text("Reset Board")
                }
            }
        }
    }
}
