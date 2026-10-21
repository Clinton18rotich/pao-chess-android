package com.example.paochess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paochess.data.*
import com.example.paochess.ui.theme.*

@Composable
fun LearnScreen() {
    // 0 to 5 are pieces, 6 is Endgame
    var selectedTab by remember { mutableStateOf(0) } 

    val explanations = listOf(
        "The Pawn moves forward 1 square. On its very first move it may advance 2 squares. It captures diagonally. If it reaches the far rank, it promotes.",
        "The Knight moves in an 'L' shape: two squares in one direction, then one square perpendicular. It is the only piece that can jump over others.",
        "The Bishop moves diagonally any number of squares. It stays on the same color square for the whole game.",
        "The Rook moves in straight lines (horizontal or vertical) any number of squares. It is very powerful on open files.",
        "The Queen is the most powerful piece. She moves in straight lines or diagonally, any number of squares.",
        "The King moves 1 square in any direction. You cannot move the King into check.",
        "How the game ends: You win by Checkmate (trapping the King). The game is a Draw if it's a Stalemate (no legal moves but not in check), or if there is insufficient material to mate, or a 50-move rule."
    )

    val icons = listOf("♙", "♘", "♗", "♖", "♕", "♔", "🏁")

    val buttonGlyphs = listOf("♙", "♘", "♗", "♖", "♕", "♔", "🏁")

    // The board only shows for pieces (0 to 5)
    val showBoard = selectedTab < 6

    val board = remember(selectedTab) {
        val b = mutableMapOf<Square, Piece>()
        if (showBoard) {
            val pieceType = PieceType.values()[selectedTab]
            b[Square(4, 3)] = Piece(pieceType, PieceColor.WHITE)
            if (pieceType == PieceType.PAWN) {
                b[Square(3, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
                b[Square(5, 4)] = Piece(PieceType.PAWN, PieceColor.BLACK)
            }
        }
        b
    }
    
    val legalMoves = remember(selectedTab) {
        if (!showBoard) emptyList()
        else {
            val pieceType = PieceType.values()[selectedTab]
            getLegalMoves(Square(4, 3), Piece(pieceType, PieceColor.WHITE), board)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("📖 Learn Chess", color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Piece Buttons (Row 1)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 0..2) {
                Button(
                    onClick = { selectedTab = i },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == i) Orange else DarkSurface),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(buttonGlyphs[i], fontSize = 24.sp, color = if (selectedTab == i) Color.Black else White)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        
        // Piece Buttons (Row 2 + Endgame)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 3..6) {
                Button(
                    onClick = { selectedTab = i },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == i) Orange else DarkSurface),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(buttonGlyphs[i], fontSize = 24.sp, color = if (selectedTab == i) Color.Black else White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = DarkSurface), shape = RoundedCornerShape(10.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icons[selectedTab], color = Orange, fontSize = 56.sp, modifier = Modifier.padding(end = 16.dp))
                Text(explanations[selectedTab], color = Color(0xFFDDDDDD), fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
            }
        }

        if (showBoard) {
            Spacer(Modifier.height(24.dp))
            
            Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 16.dp)) {
                // Rank numbers (8 down to 1) aligned perfectly with the rows
                Column(
                    modifier = Modifier.fillMaxHeight().width(16.dp).align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (rank in 7 downTo 0) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("${rank + 1}", color = LightGrey, fontSize = 12.sp)
                        }
                    }
                }
                
                // Board and File letters
                Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF2B1A10)).padding(4.dp)) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            for (rank in 7 downTo 0) {
                                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                    for (file in 0..7) {
                                        val square = Square(file, rank)
                                        val piece = board[square]
                                        val isDark = (file + rank) % 2 == 1
                                        val squareColor = if (isDark) Color(0xFF8D6E63) else Color(0xFFF5F5F5)
                                        val isLegal = legalMoves.contains(square)
                                        val isSelected = square == Square(4, 3)
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxHeight().background(squareColor)
                                                .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Orange else Color.Transparent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isLegal) Box(modifier = Modifier.size(12.dp).background(Color(0x8000FF00), CircleShape))
                                            if (piece != null) {
                                                val glyph = when (piece.type) {
                                                    PieceType.PAWN -> "♟"; PieceType.KNIGHT -> "♞"; PieceType.BISHOP -> "♝"
                                                    PieceType.ROOK -> "♜"; PieceType.QUEEN -> "♛"; PieceType.KING -> "♚"
                                                }
                                                val text = if (piece.color == PieceColor.WHITE) glyph
                                                    .replace("♟","♙").replace("♞","♘").replace("♝","♗")
                                                    .replace("♜","♖").replace("♛","♕").replace("♚","♔") else glyph
                                                Text(text = text, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // File letters (a to h) at the bottom
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        for (file in 0..7) {
                            Text(
                                "${'a' + file}", 
                                color = LightGrey, 
                                fontSize = 12.sp, 
                                modifier = Modifier.weight(1f), 
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
