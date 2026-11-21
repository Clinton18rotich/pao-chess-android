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
    var selectedTab by remember { mutableStateOf(0) }

    val explanations = listOf(
        "PAWN: Moves forward 1 square. Captures diagonally. You can capture the black pawn on e5, or push the other pawn to e8 to promote it to a Queen!",
        "KNIGHT: Moves in an L-shape and jumps over pieces. You can capture the enemy Pawn. Notation: Nf3, Nxf3.",
        "BISHOP: Moves diagonally any number of squares. You can capture the enemy Pawn. Notation: Bc4, Bxc4.",
        "ROOK: Moves horizontally or vertically. You can capture the enemy Pawn. Notation: Ra1, Rxa1.",
        "QUEEN: Moves any direction. You can capture the enemy Pawn. Notation: Qd1, Qxd1.",
        "KING: Moves 1 square in any direction. You can capture the enemy Pawn. Notation: Ke2, Kxe2.",
        "GAME OVER: Win by Checkmate (King is attacked and has no escape). Draw by Stalemate (no legal moves but NOT in check). Notation: # (Checkmate), + (Check).",
        "CASTLING: The King moves 2 squares toward a Rook, and the Rook jumps to the other side. Notation: O-O (King-side), O-O-O (Queen-side).",
        "EN PASSANT: If an enemy pawn moves 2 squares and lands beside your pawn, you can capture it as if it moved 1 square. Notation: exd6 e.p.",
        "CAPTURE: To capture, move your piece onto the enemy piece's square. Watch how every piece captures: Pawn, Knight, Bishop, Rook, Queen, and King.",
    )

    val icons = listOf("♙", "♘", "♗", "♖", "♕", "♔", "🏁", "🏰", "👻", "⚔️")
    val buttonGlyphs = listOf("♙", "♘", "♗", "♖", "♕", "♔", "🏁", "🏰", "👻", "⚔️")

    val showBoard = selectedTab != 6 

    var currentBoard by remember { mutableStateOf(mapOf<Square, Piece>()) }
    var selectedSquare by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<Square>>(emptyList()) }
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

    LaunchedEffect(selectedTab) {
        selectedSquare = null
        legalMoves = emptyList()
        specialStep = 0
        tts?.stop()

        when {
            selectedTab < 6 -> {
                val b = mutableMapOf<Square, Piece>()
                val pieceType = PieceType.values()[selectedTab]
                
                if (pieceType == PieceType.PAWN) {
                    // White pawn on d4 to show move/capture
                    b[Square(3, 3)] = Piece(pieceType, PieceColor.WHITE)
                    // Black pawn on d5 (blocks forward move)
                    b[Square(3, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
                    // Black pawn on e5 (to be captured diagonally)
                    b[Square(4, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
                    // White pawn on e7 to show promotion
                    b[Square(4, 6)] = Piece(pieceType, PieceColor.WHITE)
                } else {
                    // Other pieces start on d4 (3,3) with a target pawn
                    b[Square(3, 3)] = Piece(pieceType, PieceColor.WHITE)
                }

                // Add an enemy pawn for capture practice (except King, who shouldn't be adjacent to checkmate easily)
                if (pieceType == PieceType.KNIGHT) b[Square(5, 5)] = Piece(PieceType.PAWN, PieceColor.BLACK) // e4 capture
                if (pieceType == PieceType.BISHOP) b[Square(6, 6)] = Piece(PieceType.PAWN, PieceColor.BLACK) // g7 capture
                if (pieceType == PieceType.ROOK) b[Square(3, 6)] = Piece(PieceType.PAWN, PieceColor.BLACK)   // d7 capture
                if (pieceType == PieceType.QUEEN) b[Square(3, 6)] = Piece(PieceType.PAWN, PieceColor.BLACK)  // d7 capture
                if (pieceType == PieceType.KING) b[Square(4, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)   // e5 capture

                currentBoard = b
                if (isTtsReady) tts?.speak(explanations[selectedTab], TextToSpeech.QUEUE_FLUSH, null, null)
            }
            selectedTab == 7 -> { // Castling — BOTH types
                currentBoard = mapOf(Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE), Square(7, 0) to Piece(PieceType.ROOK, PieceColor.WHITE))
                specialStep = 0
                if (isTtsReady) tts?.speak("King-side castling, O-O. King moves two squares to g1, Rook jumps to f1.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(2500L)
                specialStep = 1
                delay(1500L)
                specialStep = 2
                delay(3500L)
                currentBoard = mapOf(Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE), Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE))
                specialStep = 3
                if (isTtsReady) tts?.speak("Now queen-side castling, O-O-O. King moves two squares to c1, Rook jumps to d1.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(2500L)
                specialStep = 4
                delay(1500L)
                specialStep = 5
            }
            selectedTab == 8 -> { // En Passant
                currentBoard = mapOf(
                    Square(4, 4) to Piece(PieceType.PAWN, PieceColor.WHITE), 
                    Square(3, 4) to Piece(PieceType.PAWN, PieceColor.BLACK), 
                )
                if (isTtsReady) tts?.speak("Black just moved their pawn two squares. White can capture it En Passant.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(2500L)
                specialStep = 1 
            }
            selectedTab == 9 -> { // Capture — ALL pieces
                currentBoard = mapOf(Square(3, 3) to Piece(PieceType.PAWN, PieceColor.WHITE), Square(4, 4) to Piece(PieceType.PAWN, PieceColor.BLACK))
                specialStep = 0
                if (isTtsReady) tts?.speak("Pawn captures diagonally. Pawn on d4 captures pawn on e5.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500L)
                specialStep = 1
                delay(3500L)
                currentBoard = mapOf(Square(2, 2) to Piece(PieceType.KNIGHT, PieceColor.WHITE), Square(4, 3) to Piece(PieceType.PAWN, PieceColor.BLACK))
                specialStep = 2
                if (isTtsReady) tts?.speak("Knight captures in an L-shape. Knight on c3 captures pawn on e4.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500L)
                specialStep = 3
                delay(3500L)
                currentBoard = mapOf(Square(2, 0) to Piece(PieceType.BISHOP, PieceColor.WHITE), Square(7, 5) to Piece(PieceType.PAWN, PieceColor.BLACK))
                specialStep = 4
                if (isTtsReady) tts?.speak("Bishop captures along diagonal. Bishop on c1 captures pawn on h6.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500L)
                specialStep = 5
                delay(3500L)
                currentBoard = mapOf(Square(0, 0) to Piece(PieceType.ROOK, PieceColor.WHITE), Square(0, 5) to Piece(PieceType.PAWN, PieceColor.BLACK))
                specialStep = 6
                if (isTtsReady) tts?.speak("Rook captures in a straight line. Rook on a1 captures pawn on a6.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500L)
                specialStep = 7
                delay(3500L)
                currentBoard = mapOf(Square(3, 0) to Piece(PieceType.QUEEN, PieceColor.WHITE), Square(3, 5) to Piece(PieceType.PAWN, PieceColor.BLACK))
                specialStep = 8
                if (isTtsReady) tts?.speak("Queen captures any direction. Queen on d1 captures pawn on d6.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500L)
                specialStep = 9
                delay(3500L)
                currentBoard = mapOf(Square(4, 0) to Piece(PieceType.KING, PieceColor.WHITE), Square(4, 1) to Piece(PieceType.PAWN, PieceColor.BLACK))
                specialStep = 10
                if (isTtsReady) tts?.speak("King captures one square in any direction. King on e1 captures pawn on e2.", TextToSpeech.QUEUE_FLUSH, null, null)
                delay(3500L)
                specialStep = 11
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("📖 Learn Chess", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

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
            
            // --- BULLETPROOF BOARD LAYOUT ---
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (rank in 7 downTo 0) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            // 1. The Rank Number (Fixed)
                            Box(modifier = Modifier.width(16.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                Text("${rank + 1}", color = LightGrey, fontSize = 12.sp)
                            }
                            
                            // 2. The 8 Squares for this rank
                            for (file in 0..7) {
                                val square = Square(file, rank)
                                
                                var displayPiece = currentBoard[square]
                                if (selectedTab == 7) { // Castling — BOTH
                                    if (specialStep in 1..2 && square == Square(6, 0)) displayPiece = Piece(PieceType.KING, PieceColor.WHITE)
                                    if (specialStep in 1..2 && square == Square(4, 0)) displayPiece = null
                                    if (specialStep in 2..2 && square == Square(5, 0)) displayPiece = Piece(PieceType.ROOK, PieceColor.WHITE)
                                    if (specialStep in 2..2 && square == Square(7, 0)) displayPiece = null
                                    if (specialStep in 4..5 && square == Square(2, 0)) displayPiece = Piece(PieceType.KING, PieceColor.WHITE)
                                    if (specialStep in 4..5 && square == Square(4, 0)) displayPiece = null
                                    if (specialStep in 5..5 && square == Square(3, 0)) displayPiece = Piece(PieceType.ROOK, PieceColor.WHITE)
                                    if (specialStep in 5..5 && square == Square(0, 0)) displayPiece = null
                                }
                                if (selectedTab == 8) { // En Passant
                                    if (specialStep >= 1 && square == Square(3, 5)) displayPiece = Piece(PieceType.PAWN, PieceColor.WHITE)
                                    if (specialStep >= 1 && square == Square(4, 4)) displayPiece = null
                                    if (specialStep >= 1 && square == Square(3, 4)) displayPiece = null
                                }
                                if (selectedTab == 9) { // Capture — ALL
                                    if (specialStep == 1) { if (square == Square(4, 4)) displayPiece = Piece(PieceType.PAWN, PieceColor.WHITE); if (square == Square(3, 3)) displayPiece = null }
                                    if (specialStep == 3) { if (square == Square(4, 3)) displayPiece = Piece(PieceType.KNIGHT, PieceColor.WHITE); if (square == Square(2, 2)) displayPiece = null }
                                    if (specialStep == 5) { if (square == Square(7, 5)) displayPiece = Piece(PieceType.BISHOP, PieceColor.WHITE); if (square == Square(2, 0)) displayPiece = null }
                                    if (specialStep == 7) { if (square == Square(0, 5)) displayPiece = Piece(PieceType.ROOK, PieceColor.WHITE); if (square == Square(0, 0)) displayPiece = null }
                                    if (specialStep == 9) { if (square == Square(3, 5)) displayPiece = Piece(PieceType.QUEEN, PieceColor.WHITE); if (square == Square(3, 0)) displayPiece = null }
                                    if (specialStep == 11) { if (square == Square(4, 1)) displayPiece = Piece(PieceType.KING, PieceColor.WHITE); if (square == Square(4, 0)) displayPiece = null }
                                }

                                val isDark = (file + rank) % 2 == 1
                                val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                val isLegal = legalMoves.contains(square)
                                val isSelected = selectedSquare == square

                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight().background(squareColor)
                                        .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent)
                                        .clickable {
                                            if (selectedTab < 6) { // Removed == 9 so user can't click during capture demo
                                                if (isLegal) {
                                                    // Use the engine's applyMove to handle promotion & captures
                                                    currentBoard = applyMove(currentBoard, selectedSquare!!, square)
                                                    selectedSquare = null
                                                    legalMoves = emptyList()
                                                    
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
                    
                    // File Letters (a-h) at the bottom of the board
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.width(16.dp)) // align with squares
                        for (file in 0..7) {
                            Text("${'a' + file}", color = LightGrey, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            
            if (selectedTab < 6 || selectedTab == 9) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = { selectedTab = -1; selectedTab = 0 }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)), modifier = Modifier.fillMaxWidth()) {
                    Text("Reset Board")
                }
            }
        }
    }
}


