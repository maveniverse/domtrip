#!/bin/bash

# Script to fix editor.root() calls after API change

echo "Fixing editor.root() calls in test files..."

# Find all Java test files and fix the root() calls
find core/src/test/java -name "*.java" -type f | while read file; do
    echo "Processing $file..."
    
    # Fix .root().orElseThrow() -> .root()
    sed -i 's/\.root()\.orElseThrow()/\.root()/g' "$file"
    
    # Fix .root().orElse(null) -> .root()
    sed -i 's/\.root()\.orElse(null)/\.root()/g' "$file"
    
    # Fix .root().orElse(something) -> .root() (might need manual review)
    sed -i 's/\.root()\.orElse([^)]*)/\.root()/g' "$file"
    
    # Fix .root().isPresent() -> true (since root() now always returns non-null)
    sed -i 's/\.root()\.isPresent()/true/g' "$file"
    
    # Fix .root().isEmpty() -> false
    sed -i 's/\.root()\.isEmpty()/false/g' "$file"
done

echo "Root call fixes applied."
