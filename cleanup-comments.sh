#!/bin/bash

# Safe Java Comment Removal Script
# Removes line comments (//) and Javadoc comments (/** ... */)
# while preserving code integrity

echo "🧹 Starting Java comment cleanup..."

# List of directories to clean
DIRS=(
    "diplom-test-service/src/main/java"
    "diplom-shop/src/main/java"
    "diplom-demographic-service/src/main/java"
    "diplom-selector-service/src/main/java"
    "diplom-participant-service/src/main/java"
)

for dir in "${DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "📁 Processing: $dir"

        find "$dir" -name "*.java" -type f | while read file; do
            # Create backup
            cp "$file" "$file.bak"

            # Remove line comments (but preserve empty lines and indentation)
            sed -i.tmp1 's/^\([^"]*\)\/\/.*$/\1/' "$file"

            # Remove Javadoc comments (/** ... */)
            sed -i.tmp2 '/^[[:space:]]*\/\*\*/,/^[[:space:]]*\*\//d' "$file"

            # Remove block comments (/* ... */)
            sed -i.tmp3 '/^[[:space:]]*\/\*/,/^[[:space:]]*\*\//d' "$file"

            # Clean up temporary files
            rm -f "$file.tmp1" "$file.tmp2" "$file.tmp3"

            # Check if file was modified
            if ! diff -q "$file" "$file.bak" > /dev/null 2>&1; then
                echo "  ✓ Cleaned: $(basename $file)"
            fi
        done
    else
        echo "⚠️  Directory not found: $dir"
    fi
done

echo "✅ Comment cleanup complete!"
echo "💾 Backups created with .bak extension"
echo ""
echo "To verify changes:"
echo "  git diff --stat"
echo ""
echo "To revert if needed:"
echo "  find . -name '*.bak' -exec sh -c 'mv \"\$1\" \"\${1%.bak}\"' _ {} \\;"
