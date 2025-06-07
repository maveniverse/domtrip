#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

/**
 * Validates that code snippets referenced in documentation exist and are up to date.
 * 
 * Usage: node validate-snippets.js <docs-dir> <snippets-dir>
 */

function validateSnippets(docsDir, snippetsDir) {
    console.log(`Validating snippets in ${docsDir} against ${snippetsDir}`);
    
    if (!fs.existsSync(docsDir)) {
        console.error(`Documentation directory does not exist: ${docsDir}`);
        process.exit(1);
    }
    
    if (!fs.existsSync(snippetsDir)) {
        console.error(`Snippets directory does not exist: ${snippetsDir}`);
        process.exit(1);
    }
    
    const docFiles = findMarkdownFiles(docsDir);
    const snippetFiles = findSnippetFiles(snippetsDir);
    
    let totalReferences = 0;
    let validReferences = 0;
    let errors = [];
    
    for (const docFile of docFiles) {
        const references = findSnippetReferences(docFile);
        totalReferences += references.length;
        
        for (const ref of references) {
            const snippetPath = path.join(snippetsDir, ref.snippet);
            
            if (fs.existsSync(snippetPath)) {
                validReferences++;
                console.log(`✅ ${ref.snippet} (${path.relative(docsDir, docFile)}:${ref.line})`);
            } else {
                errors.push(`❌ Missing snippet: ${ref.snippet} referenced in ${path.relative(docsDir, docFile)}:${ref.line}`);
            }
        }
    }
    
    // Report results
    console.log('\n📊 Validation Results:');
    console.log(`Total snippet references: ${totalReferences}`);
    console.log(`Valid references: ${validReferences}`);
    console.log(`Missing snippets: ${errors.length}`);
    
    if (errors.length > 0) {
        console.log('\n🚨 Errors:');
        errors.forEach(error => console.log(error));
        process.exit(1);
    } else {
        console.log('\n✅ All snippet references are valid!');
    }
    
    // Report unused snippets
    const referencedSnippets = new Set();
    for (const docFile of docFiles) {
        const references = findSnippetReferences(docFile);
        references.forEach(ref => referencedSnippets.add(ref.snippet));
    }
    
    const unusedSnippets = snippetFiles.filter(snippet => !referencedSnippets.has(path.basename(snippet)));
    
    if (unusedSnippets.length > 0) {
        console.log('\n⚠️  Unused snippets:');
        unusedSnippets.forEach(snippet => console.log(`  ${path.basename(snippet)}`));
    }
}

function findMarkdownFiles(dir) {
    const files = [];
    
    function scan(currentDir) {
        const entries = fs.readdirSync(currentDir);
        
        for (const entry of entries) {
            const fullPath = path.join(currentDir, entry);
            const stat = fs.statSync(fullPath);
            
            if (stat.isDirectory()) {
                scan(fullPath);
            } else if (entry.endsWith('.md') || entry.endsWith('.mdx')) {
                files.push(fullPath);
            }
        }
    }
    
    scan(dir);
    return files;
}

function findSnippetFiles(dir) {
    const files = [];
    
    if (fs.existsSync(dir)) {
        const entries = fs.readdirSync(dir);
        
        for (const entry of entries) {
            const fullPath = path.join(dir, entry);
            const stat = fs.statSync(fullPath);
            
            if (stat.isFile() && entry.endsWith('.java')) {
                files.push(fullPath);
            }
        }
    }
    
    return files;
}

function findSnippetReferences(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    const lines = content.split('\n');
    const references = [];
    
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        
        // Look for snippet references in various formats:
        // import CodeBlock from '@theme/CodeBlock';
        // import MySnippet from '!!raw-loader!../snippets/MySnippet.java';
        // <CodeBlock language="java">{MySnippet}</CodeBlock>
        
        // Pattern 1: Direct import of snippet
        const importMatch = line.match(/import\s+(\w+)\s+from\s+['"]!!raw-loader![^'"]*\/([^'"]+\.java)['"]/);
        if (importMatch) {
            references.push({
                snippet: importMatch[2],
                line: i + 1,
                type: 'import'
            });
        }
        
        // Pattern 2: Reference to snippet file in code blocks
        const codeBlockMatch = line.match(/```java.*?\/\/\s*@snippet\s+([^\s]+)/);
        if (codeBlockMatch) {
            references.push({
                snippet: codeBlockMatch[1],
                line: i + 1,
                type: 'reference'
            });
        }
        
        // Pattern 3: Inline snippet reference
        const inlineMatch = line.match(/\{([A-Za-z0-9_]+)\}/);
        if (inlineMatch && line.includes('CodeBlock')) {
            const snippetName = inlineMatch[1] + '.java';
            references.push({
                snippet: snippetName,
                line: i + 1,
                type: 'inline'
            });
        }
    }
    
    return references;
}

// Main execution
if (require.main === module) {
    const args = process.argv.slice(2);
    
    if (args.length !== 2) {
        console.error('Usage: node validate-snippets.js <docs-dir> <snippets-dir>');
        process.exit(1);
    }
    
    const [docsDir, snippetsDir] = args;
    
    validateSnippets(docsDir, snippetsDir);
}
