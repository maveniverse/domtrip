#!/usr/bin/env python3
"""
Script to convert absolute internal links to relative links in markdown files.
This ensures that links work correctly in both development and production environments.
"""

import os
import re
import sys
from pathlib import Path

# Pattern to match markdown links: [text](/internal/path) but not [text](http://external)
MARKDOWN_LINK_PATTERN = re.compile(r'\[([^\]]+)\]\((/[^)]+)\)', re.IGNORECASE)

# Pattern to match external links
EXTERNAL_LINK_PATTERN = re.compile(r'^(https?://|mailto:|#)', re.IGNORECASE)

def calculate_relative_path(from_file, to_path):
    """Calculate the relative path from one file to another."""
    # Remove the leading slash from to_path
    to_path = to_path.lstrip('/')

    # Get the directory of the source file relative to content root
    from_dir = from_file.parent.relative_to(Path("website/content"))

    # Calculate how many levels up we need to go
    levels_up = len(from_dir.parts)

    # Build the relative path
    if levels_up == 0:
        # We're in the root, just use the path as-is
        return to_path
    else:
        # Go up the required number of levels, then down to the target
        up_path = "../" * levels_up
        return up_path + to_path

def fix_links_in_file(file_path):
    """Convert absolute internal links to relative links in a single markdown file."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content
        fixed_links = 0

        def replace_link(match):
            nonlocal fixed_links
            link_text = match.group(1)
            link_url = match.group(2)

            # Skip external links
            if EXTERNAL_LINK_PATTERN.match(link_url):
                return match.group(0)

            # Skip links that are already relative (don't start with /)
            if not link_url.startswith('/'):
                return match.group(0)

            # Convert absolute link to relative
            relative_url = calculate_relative_path(file_path, link_url)
            fixed_links += 1

            print(f"  Fixed: {link_url} -> {relative_url}")
            return f"[{link_text}]({relative_url})"

        content = MARKDOWN_LINK_PATTERN.sub(replace_link, content)

        if fixed_links > 0:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Fixed {fixed_links} links in {file_path}")

        return fixed_links

    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return 0

def main():
    content_dir = Path("website/content")

    if not content_dir.exists():
        print(f"Content directory not found: {content_dir}")
        sys.exit(1)

    print(f"Converting absolute internal links to relative links")
    print(f"Processing markdown files in: {content_dir.absolute()}")

    total_fixed = 0
    total_files = 0

    # Find all markdown files
    for md_file in content_dir.rglob("*.md"):
        total_files += 1
        fixed_count = fix_links_in_file(md_file)
        total_fixed += fixed_count

    print(f"\nSummary:")
    print(f"  Processed {total_files} markdown files")
    print(f"  Fixed {total_fixed} internal links")

if __name__ == "__main__":
    main()
