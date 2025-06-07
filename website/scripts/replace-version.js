#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { replaceInFile } = require('replace-in-file');

/**
 * Replaces version placeholders in documentation with actual version numbers.
 * 
 * Usage: 
 *   node replace-version.js                    # Replace in built docs
 *   node replace-version.js --source          # Replace in source docs
 *   node replace-version.js --source --revert # Revert source docs
 */

const VERSION_PLACEHOLDER = '{{DOMTRIP_VERSION}}';
const ACTUAL_VERSION = '0.1-SNAPSHOT'; // This should be updated when releasing

async function replaceVersions(options = {}) {
    const { source = false, revert = false } = options;
    
    let targetDir;
    let fromPattern;
    let toPattern;
    
    if (source) {
        targetDir = path.join(__dirname, '..', 'docs');
        if (revert) {
            fromPattern = new RegExp(escapeRegExp(ACTUAL_VERSION), 'g');
            toPattern = VERSION_PLACEHOLDER;
            console.log(`Reverting version ${ACTUAL_VERSION} → ${VERSION_PLACEHOLDER} in source docs`);
        } else {
            fromPattern = new RegExp(escapeRegExp(VERSION_PLACEHOLDER), 'g');
            toPattern = ACTUAL_VERSION;
            console.log(`Replacing ${VERSION_PLACEHOLDER} → ${ACTUAL_VERSION} in source docs`);
        }
    } else {
        targetDir = path.join(__dirname, '..', 'target', 'build');
        fromPattern = new RegExp(escapeRegExp(VERSION_PLACEHOLDER), 'g');
        toPattern = ACTUAL_VERSION;
        console.log(`Replacing ${VERSION_PLACEHOLDER} → ${ACTUAL_VERSION} in built docs`);
    }
    
    if (!fs.existsSync(targetDir)) {
        console.log(`Target directory does not exist: ${targetDir}`);
        return;
    }
    
    try {
        const results = await replaceInFile({
            files: [
                path.join(targetDir, '**', '*.html'),
                path.join(targetDir, '**', '*.js'),
                path.join(targetDir, '**', '*.css'),
                path.join(targetDir, '**', '*.md'),
                path.join(targetDir, '**', '*.mdx'),
            ],
            from: fromPattern,
            to: toPattern,
            allowEmptyPaths: true,
        });
        
        const changedFiles = results.filter(result => result.hasChanged);
        
        if (changedFiles.length > 0) {
            console.log(`✅ Updated ${changedFiles.length} files:`);
            changedFiles.forEach(result => {
                const relativePath = path.relative(targetDir, result.file);
                console.log(`  ${relativePath}`);
            });
        } else {
            console.log('ℹ️  No files needed updating');
        }
        
    } catch (error) {
        console.error('❌ Error replacing versions:', error);
        process.exit(1);
    }
}

function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function parseArgs() {
    const args = process.argv.slice(2);
    const options = {};
    
    for (const arg of args) {
        switch (arg) {
            case '--source':
                options.source = true;
                break;
            case '--revert':
                options.revert = true;
                break;
            case '--help':
            case '-h':
                console.log(`
Usage: node replace-version.js [options]

Options:
  --source    Replace versions in source documentation (docs/)
  --revert    Revert version replacements (use with --source)
  --help, -h  Show this help message

Examples:
  node replace-version.js                    # Replace in built docs
  node replace-version.js --source          # Replace in source docs  
  node replace-version.js --source --revert # Revert source docs
`);
                process.exit(0);
                break;
            default:
                console.error(`Unknown option: ${arg}`);
                process.exit(1);
        }
    }
    
    return options;
}

// Main execution
if (require.main === module) {
    const options = parseArgs();
    replaceVersions(options);
}
