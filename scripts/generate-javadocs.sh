#!/bin/bash

# DomTrip Javadoc Generation Script
# This script generates Javadocs for the current version and integrates them into the website

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_info "DomTrip Javadoc Generation Script"
print_info "Project root: $PROJECT_ROOT"

# Change to project root
cd "$PROJECT_ROOT"

# Check if we're in the right directory
if [ ! -f "pom.xml" ] || [ ! -d "core" ] || [ ! -d "website" ]; then
    print_error "This script must be run from the DomTrip project root directory"
    exit 1
fi

# Get current version
CURRENT_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
print_info "Current version: $CURRENT_VERSION"

# Check if this is a release version (not SNAPSHOT)
IS_RELEASE=false
if [[ ! "$CURRENT_VERSION" =~ SNAPSHOT ]]; then
    IS_RELEASE=true
    print_info "This is a release version"
else
    print_info "This is a snapshot version"
fi

# Clean and generate Javadocs for core module
print_info "Generating Javadocs for core module..."
./mvnw clean javadoc:javadoc -pl core -q

# Check if Javadocs were generated
JAVADOC_DIR="core/target/reports/apidocs"
if [ ! -d "$JAVADOC_DIR" ]; then
    print_error "Javadoc generation failed - directory $JAVADOC_DIR not found"
    exit 1
fi

print_success "Javadocs generated successfully"

# Determine target directory based on mode
if [ "$1" = "--static-generation" ]; then
    # For static generation, put in public directory so ROQ can pick it up
    WEBSITE_JAVADOC_DIR="website/public/javadoc"
    print_info "Static generation mode: Javadocs will be included in ROQ static generation"
elif [ "$1" = "--build-time" ]; then
    # For build-time generation, put in website target directory
    WEBSITE_JAVADOC_DIR="website/target/classes/META-INF/resources/javadoc"
    print_info "Build-time mode: Javadocs will be included in website build"
else
    # For development/testing, put in public directory (gitignored)
    WEBSITE_JAVADOC_DIR="website/public/javadoc"
    print_info "Development mode: Javadocs will be in public directory"
fi

# Create website javadoc directories
mkdir -p "$WEBSITE_JAVADOC_DIR/snapshot"
mkdir -p "$WEBSITE_JAVADOC_DIR/latest"

# Copy snapshot javadocs
print_info "Copying snapshot Javadocs..."
cp -r "$JAVADOC_DIR"/* "$WEBSITE_JAVADOC_DIR/snapshot/"
print_success "Snapshot Javadocs copied"

# Handle release vs snapshot
if [ "$IS_RELEASE" = true ]; then
    # For releases, create version-specific directory and update latest
    VERSION_DIR="$WEBSITE_JAVADOC_DIR/$CURRENT_VERSION"
    mkdir -p "$VERSION_DIR"

    print_info "Copying release Javadocs to version-specific directory..."
    cp -r "$JAVADOC_DIR"/* "$VERSION_DIR/"

    print_info "Updating latest Javadocs to point to release $CURRENT_VERSION..."
    rm -rf "$WEBSITE_JAVADOC_DIR/latest"/*
    cp -r "$JAVADOC_DIR"/* "$WEBSITE_JAVADOC_DIR/latest/"

    print_success "Release Javadocs ($CURRENT_VERSION) set as latest"
else
    # For snapshots, update latest to point to snapshot
    print_info "Updating latest Javadocs to point to snapshot..."
    rm -rf "$WEBSITE_JAVADOC_DIR/latest"/*
    cp -r "$JAVADOC_DIR"/* "$WEBSITE_JAVADOC_DIR/latest/"

    print_success "Snapshot Javadocs set as latest"
fi

# Generate index page for Javadocs
print_info "Generating Javadoc index page..."

cat > "$WEBSITE_JAVADOC_DIR/index.html" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DomTrip API Documentation</title>
    <link rel="stylesheet" href="/domtrip/css/docusaurus-style.css">
    <style>
        .javadoc-container {
            max-width: 800px;
            margin: 40px auto;
            padding: 20px;
        }
        .version-card {
            display: block;
            padding: 20px;
            margin: 15px 0;
            background: #f8f9fa;
            text-decoration: none;
            color: #333;
            border-radius: 8px;
            border: 1px solid #e9ecef;
            transition: all 0.2s ease;
        }
        .version-card:hover {
            background: #e9ecef;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .version-card.latest {
            background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
            border-color: #4caf50;
        }
        .version-card.snapshot {
            background: linear-gradient(135deg, #fff3cd, #ffeaa7);
            border-color: #ff9800;
        }
        .version-title {
            font-size: 1.2em;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .version-description {
            font-size: 0.9em;
            color: #666;
        }
        .back-link {
            display: inline-block;
            margin-top: 20px;
            color: #007bff;
            text-decoration: none;
        }
        .back-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="javadoc-container">
        <h1>DomTrip API Documentation</h1>
        <p>Choose a version to view the complete API documentation:</p>

        <a href="latest/" class="version-card latest">
            <div class="version-title">Latest Release</div>
            <div class="version-description">Most recent stable release - recommended for production use</div>
        </a>

        <a href="snapshot/" class="version-card snapshot">
            <div class="version-title">Development Snapshot</div>
            <div class="version-description">Latest development version - may contain unreleased features</div>
        </a>

        <a href="/domtrip/" class="back-link">← Back to Documentation</a>
    </div>
</body>
</html>
EOF

print_success "Javadoc index page generated"

print_success "Javadoc integration completed successfully!"
print_info "Available at: /domtrip/javadoc/"
print_info "- Latest: /domtrip/javadoc/latest/"
print_info "- Snapshot: /domtrip/javadoc/snapshot/"
