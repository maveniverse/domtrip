#!/bin/bash

# DomTrip Website Initialization Script
# This script sets up the website for local development

set -e

echo "🚀 Initializing DomTrip Website..."

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: package.json not found. Please run this script from the website directory."
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Error: Node.js 18 or higher is required. Current version: $(node --version)"
    exit 1
fi

echo "✅ Node.js version: $(node --version)"

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p target/static/snippets
mkdir -p static/img

# Make scripts executable
echo "🔧 Setting up scripts..."
chmod +x scripts/*.js

# Extract snippets from demo classes
echo "📝 Extracting code snippets..."
if [ -d "../src/test/java/eu/maveniverse/domtrip/demos" ]; then
    npm run extract-snippets
    echo "✅ Code snippets extracted"
else
    echo "⚠️  Warning: Demo classes not found. Snippets will be empty."
fi

# Validate snippets
echo "🔍 Validating snippets..."
npm run validate-snippets || echo "⚠️  Some snippet references may be invalid"

# Create a simple favicon if it doesn't exist
if [ ! -f "static/img/favicon.ico" ]; then
    echo "🎨 Creating placeholder favicon..."
    # Create a simple text file as placeholder
    echo "# Placeholder favicon - replace with actual ICO file" > static/img/favicon.ico
fi

# Test build
echo "🏗️  Testing build..."
npm run build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🎉 Website initialization complete!"
    echo ""
    echo "Next steps:"
    echo "  1. Start development server: npm start"
    echo "  2. Open http://localhost:3000 in your browser"
    echo "  3. Edit documentation in the docs/ directory"
    echo "  4. Customize the website in src/ directory"
    echo ""
    echo "For more information, see the README.md file."
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi
