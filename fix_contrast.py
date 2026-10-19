import os
import re

screens_dir = 'app/src/main/java/com/example/paochess/ui/screens/'
files = ['ChessBoardScreen.kt', 'LearnScreen.kt', 'PuzzlesScreen.kt']

for filename in files:
    path = os.path.join(screens_dir, filename)
    if not os.path.exists(path):
        continue
    
    with open(path, 'r') as f:
        content = f.read()

    # Find the Text block that renders the chess symbol and add a shadow
    # We look for the Text( text = if (piece.color == ...) block
    # and wrap the Text in a Box with a shadow.
    
    # This is a complex replacement, so we'll do a simpler approach:
    # Add a black border around the text by using a custom modifier.
    # Actually, Compose Text doesn't have a native outline, so we'll use a shadow on the text style.
    
    # Let's replace the textStyle parameter to include a shadow.
    # We find the Text(...) block and add textStyle = TextStyle(shadow = Shadow(...))
    
    # Add import for TextStyle and Shadow if not present
    if 'import androidx.compose.ui.text.TextStyle' not in content:
        content = content.replace(
            'import androidx.compose.ui.text.font.FontWeight',
            'import androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.TextStyle\nimport androidx.compose.ui.graphics.Shadow\nimport androidx.compose.ui.geometry.Offset'
        )
    
    # Add shadow to the Text that renders the piece
    # We look for the Text( text = if (piece.color... block
    # and add the textStyle parameter before the closing )
    # The pattern is usually: fontSize = 24.sp, fontWeight = FontWeight.Bold)
    
    content = re.sub(
        r'(fontSize = \d+\.sp,\s*fontWeight = FontWeight\.Bold)(\s*\))',
        r'\1,\n                                        textStyle = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f))\2',
        content
    )

    with open(path, 'w') as f:
        f.write(content)
    print(f"✅ Added shadow to pieces in {filename}")

print("Done!")
