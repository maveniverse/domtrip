#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

/**
 * Extracts code snippets from Java demo files for use in documentation.
 * 
 * Usage: node extract-snippets.js <source-dir> <output-dir>
 */

function extractSnippets(sourceDir, outputDir) {
    console.log(`Extracting snippets from ${sourceDir} to ${outputDir}`);
    
    if (!fs.existsSync(outputDir)) {
        fs.mkdirSync(outputDir, { recursive: true });
    }
    
    const javaFiles = findJavaFiles(sourceDir);
    
    for (const file of javaFiles) {
        console.log(`Processing ${file}`);
        extractFromFile(file, outputDir);
    }
    
    console.log(`✅ Extracted snippets from ${javaFiles.length} files`);
}

function findJavaFiles(dir) {
    const files = [];
    
    function scan(currentDir) {
        const entries = fs.readdirSync(currentDir);
        
        for (const entry of entries) {
            const fullPath = path.join(currentDir, entry);
            const stat = fs.statSync(fullPath);
            
            if (stat.isDirectory()) {
                scan(fullPath);
            } else if (entry.endsWith('.java')) {
                files.push(fullPath);
            }
        }
    }
    
    scan(dir);
    return files;
}

function extractFromFile(filePath, outputDir) {
    const content = fs.readFileSync(filePath, 'utf8');
    const fileName = path.basename(filePath, '.java');
    
    // Extract method snippets
    const methods = extractMethods(content);
    
    for (const method of methods) {
        const snippetName = `${fileName}_${method.name}.java`;
        const snippetPath = path.join(outputDir, snippetName);
        
        // Clean up the method code
        const cleanCode = cleanMethodCode(method.code);
        
        fs.writeFileSync(snippetPath, cleanCode);
        console.log(`  → ${snippetName}`);
    }
    
    // Extract class-level snippets marked with special comments
    const classSnippets = extractMarkedSnippets(content);
    
    for (const snippet of classSnippets) {
        const snippetName = `${fileName}_${snippet.name}.java`;
        const snippetPath = path.join(outputDir, snippetName);
        
        fs.writeFileSync(snippetPath, snippet.code);
        console.log(`  → ${snippetName} (marked)`);
    }
}

function extractMethods(content) {
    const methods = [];
    const methodRegex = /(?:public|private|protected)?\s*(?:static)?\s*(?:\w+\s+)*(\w+)\s*\([^)]*\)\s*(?:throws\s+[^{]+)?\s*\{/g;
    
    let match;
    while ((match = methodRegex.exec(content)) !== null) {
        const methodName = match[1];
        const startIndex = match.index;
        
        // Skip constructors and common method names
        if (methodName === 'main' || methodName === 'setUp' || methodName === 'tearDown') {
            continue;
        }
        
        // Find the method body
        const methodCode = extractMethodBody(content, startIndex);
        
        if (methodCode && methodCode.length > 50) { // Only include substantial methods
            methods.push({
                name: methodName,
                code: methodCode
            });
        }
    }
    
    return methods;
}

function extractMethodBody(content, startIndex) {
    let braceCount = 0;
    let inMethod = false;
    let methodStart = -1;
    
    for (let i = startIndex; i < content.length; i++) {
        const char = content[i];
        
        if (char === '{') {
            if (!inMethod) {
                inMethod = true;
                methodStart = i;
            }
            braceCount++;
        } else if (char === '}') {
            braceCount--;
            if (braceCount === 0 && inMethod) {
                // Found the end of the method
                return content.substring(startIndex, i + 1);
            }
        }
    }
    
    return null;
}

function extractMarkedSnippets(content) {
    const snippets = [];
    const snippetRegex = /\/\/\s*SNIPPET:\s*(\w+)\s*\n([\s\S]*?)\/\/\s*END_SNIPPET/g;
    
    let match;
    while ((match = snippetRegex.exec(content)) !== null) {
        const snippetName = match[1];
        const snippetCode = match[2].trim();
        
        snippets.push({
            name: snippetName,
            code: snippetCode
        });
    }
    
    return snippets;
}

function cleanMethodCode(code) {
    // Remove excessive whitespace and clean up formatting
    return code
        .split('\n')
        .map(line => line.trimEnd())
        .join('\n')
        .replace(/\n{3,}/g, '\n\n') // Replace multiple newlines with double newlines
        .trim();
}

// Main execution
if (require.main === module) {
    const args = process.argv.slice(2);
    
    if (args.length !== 2) {
        console.error('Usage: node extract-snippets.js <source-dir> <output-dir>');
        process.exit(1);
    }
    
    const [sourceDir, outputDir] = args;
    
    if (!fs.existsSync(sourceDir)) {
        console.error(`Source directory does not exist: ${sourceDir}`);
        process.exit(1);
    }
    
    extractSnippets(sourceDir, outputDir);
}
