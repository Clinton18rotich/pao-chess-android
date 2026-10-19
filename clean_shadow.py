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

    # Remove the broken textStyle injection
    # We look for the pattern: textStyle = TextStyle(shadow = Shadow(...))
    content = re.sub(r',\s*textStyle\s*=\s*TextStyle\([^)]*Shadow[^)]*\)[^)]*\)?', '', content)
    content = re.sub(r'textStyle\s*=\s*TextStyle\([^)]*Shadow[^)]*\)[^)]*\)?,', '', content)

    # Also remove the now-unused imports we added
    content = content.replace('import androidx.compose.ui.text.TextStyle\n', '')
    content = content.replace('import androidx.compose.ui.graphics.Shadow\n', '')
    content = content.replace('import androidx.compose.ui.geometry.Offset\n', '')

    with open(path, 'w') as f:
        f.write(content)
    print(f"✅ Cleaned shadow code from {filename}")

print("Done!")
