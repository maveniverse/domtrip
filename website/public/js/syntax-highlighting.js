// Syntax highlighting functionality for DomTrip documentation

// Enhanced language detection
function detectLanguage(content) {
    content = content.trim();

    // Java detection - more comprehensive
    if (content.includes('public class') ||
        content.includes('import ') ||
        content.includes('System.out.println') ||
        content.includes('Editor editor') ||
        content.includes('Element ') ||
        content.includes('DomTripConfig') ||
        content.includes('new Editor(') ||
        (content.includes('public ') && content.includes('{')) ||
        content.includes('throws Exception')) {
        return 'java';
    }

    // XML detection
    if (content.includes('<?xml') ||
        content.includes('<dependency>') ||
        content.includes('<config>') ||
        content.includes('<project>') ||
        (content.includes('<') && content.includes('</') && content.includes('>'))) {
        return 'xml';
    }

    // JSON detection
    if ((content.startsWith('{') && content.endsWith('}')) ||
        (content.startsWith('[') && content.endsWith(']')) ||
        (content.includes('"') && content.includes(':') && content.includes('{'))) {
        return 'json';
    }

    // YAML detection
    if (content.includes('---') ||
        (content.includes(':') && !content.includes(';') && !content.includes('<'))) {
        return 'yaml';
    }

    // Bash/Shell detection
    if (content.includes('#!/bin/bash') ||
        content.includes('$ ') ||
        content.includes('./mvnw') ||
        content.includes('mvn ') ||
        content.includes('npm ')) {
        return 'bash';
    }

    // Properties detection
    if (content.includes('=') && content.includes('.') && !content.includes('<')) {
        return 'properties';
    }

    return 'text';
}

function initSyntaxHighlighting() {
    console.log('Initializing syntax highlighting...');

    // First, try to find existing pre/code blocks
    var existingCodeBlocks = document.querySelectorAll('pre code');
    console.log('Found', existingCodeBlocks.length, 'existing pre/code blocks');

    existingCodeBlocks.forEach(function(codeBlock, index) {
        var pre = codeBlock.parentElement;
        if (pre.classList.contains('language-processed')) return;

        var content = codeBlock.textContent || codeBlock.innerText;
        var language = detectLanguage(content);
        console.log('Code block ' + index + ' detected as: ' + language);

        pre.className = 'language-' + language + ' line-numbers';
        codeBlock.className = 'language-' + language;
        pre.classList.add('language-processed');
    });

    // If no proper code blocks found, look for indented code-like content
    if (existingCodeBlocks.length === 0) {
        console.log('No pre/code blocks found, looking for indented code content...');
        findAndConvertCodeBlocks();
    }

    if (window.Prism) {
        console.log('Running Prism.highlightAll()...');
        window.Prism.highlightAll();
    }
}

// Function to find and convert plain text that looks like code
function findAndConvertCodeBlocks() {
    // Look for indented text blocks that look like code
    var textNodes = [];
    var walker = document.createTreeWalker(
        document.querySelector('.content-wrapper') || document.body,
        NodeFilter.SHOW_TEXT,
        {
            acceptNode: function(node) {
                var text = node.textContent.trim();
                // Look for code patterns in text nodes
                if (text.length > 30 && (
                    text.includes('import ') ||
                    text.includes('public class') ||
                    text.includes('<?xml') ||
                    text.includes('Editor editor') ||
                    text.includes('System.out.println') ||
                    (text.includes('    ') && text.includes(';')) // indented with semicolons
                )) {
                    return NodeFilter.FILTER_ACCEPT;
                }
                return NodeFilter.FILTER_REJECT;
            }
        },
        false
    );

    var node;
    while (node = walker.nextNode()) {
        textNodes.push(node);
    }

    console.log('Found', textNodes.length, 'potential code text nodes');

    // Convert text nodes to proper code blocks
    textNodes.forEach(function(textNode, index) {
        var content = textNode.textContent.trim();
        var language = detectLanguage(content);

        console.log('Converting text node', index, 'to', language, 'code block');

        // Create proper code block structure
        var pre = document.createElement('pre');
        var code = document.createElement('code');

        pre.className = 'language-' + language + ' line-numbers';
        code.className = 'language-' + language;
        code.textContent = content;

        pre.appendChild(code);

        // Replace the text node with the code block
        var parent = textNode.parentNode;
        parent.replaceChild(pre, textNode);

        // Add some spacing
        pre.style.margin = '1.5em 0';
    });
}

// Alternative approach: look for code patterns in paragraphs and divs
function findCodeInElements() {
    console.log('Looking for code patterns in regular elements...');

    // Look in paragraphs and divs for code-like content
    var elements = document.querySelectorAll('.content-wrapper p, .content-wrapper div');

    elements.forEach(function(element, index) {
        var text = element.textContent.trim();

        // Skip if already processed or too short
        if (element.classList.contains('code-processed') || text.length < 30) return;

        // Check if this looks like code
        var isCode = false;
        var language = 'text';

        if (text.includes('import ') && text.includes('public class')) {
            isCode = true;
            language = 'java';
        } else if (text.includes('Editor editor') || text.includes('new Editor(')) {
            isCode = true;
            language = 'java';
        } else if (text.includes('<?xml') || (text.includes('<') && text.includes('</'))) {
            isCode = true;
            language = 'xml';
        } else if (text.includes('    ') && (text.includes(';') || text.includes('{'))) {
            // Indented content with semicolons or braces
            isCode = true;
            language = detectLanguage(text);
        }

        if (isCode) {
            console.log('Converting element', index, 'to', language, 'code block');

            // Create code block
            var pre = document.createElement('pre');
            var code = document.createElement('code');

            pre.className = 'language-' + language + ' line-numbers';
            code.className = 'language-' + language;
            code.textContent = text;

            pre.appendChild(code);
            pre.style.margin = '1.5em 0';

            // Replace the element
            element.parentNode.replaceChild(pre, element);
        } else {
            element.classList.add('code-processed');
        }
    });
}

// Copy button functionality for code blocks
function initCopyButtons() {
    console.log('Initializing copy buttons...');

    // Add copy buttons to all code blocks
    const codeBlocks = document.querySelectorAll('pre');

    codeBlocks.forEach(function(pre) {
        // Skip if already has copy button
        if (pre.querySelector('.copy-button')) return;

        // Create copy button
        const copyButton = document.createElement('button');
        copyButton.className = 'copy-button';
        copyButton.innerHTML = '<i class="fas fa-copy"></i>';
        copyButton.title = 'Copy code';
        copyButton.setAttribute('aria-label', 'Copy code to clipboard');

        // Add click handler
        copyButton.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const codeElement = pre.querySelector('code');
            const text = codeElement ? codeElement.textContent : pre.textContent;

            // Copy to clipboard
            navigator.clipboard.writeText(text).then(function() {
                // Show success feedback
                copyButton.innerHTML = '<i class="fas fa-check"></i>';
                copyButton.classList.add('copied');

                // Reset after 2 seconds
                setTimeout(function() {
                    copyButton.innerHTML = '<i class="fas fa-copy"></i>';
                    copyButton.classList.remove('copied');
                }, 2000);
            }).catch(function(err) {
                console.error('Failed to copy text: ', err);
                // Fallback for older browsers
                fallbackCopyTextToClipboard(text, copyButton);
            });
        });

        // Create wrapper for positioning
        pre.style.position = 'relative';
        pre.appendChild(copyButton);
    });
}

// Fallback copy function for older browsers
function fallbackCopyTextToClipboard(text, button) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        const successful = document.execCommand('copy');
        if (successful) {
            button.innerHTML = '<i class="fas fa-check"></i>';
            button.classList.add('copied');
            setTimeout(function() {
                button.innerHTML = '<i class="fas fa-copy"></i>';
                button.classList.remove('copied');
            }, 2000);
        }
    } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
    }

    document.body.removeChild(textArea);
}

// Initialize syntax highlighting functionality
function initSyntaxHighlightingPage() {
    initSyntaxHighlighting();

    // Try alternative approach if no code blocks found
    setTimeout(function() {
        var codeBlocks = document.querySelectorAll('pre code');
        if (codeBlocks.length < 5) { // If we found very few code blocks
            console.log('Found only', codeBlocks.length, 'code blocks, trying alternative approach...');
            findCodeInElements();

            // Re-run syntax highlighting
            setTimeout(function() {
                if (window.Prism) {
                    window.Prism.highlightAll();
                }
                // Initialize copy buttons after syntax highlighting
                initCopyButtons();
            }, 100);
        } else {
            // Initialize copy buttons for existing code blocks
            initCopyButtons();
        }
    }, 1000);
}

// Run syntax highlighting initialization
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSyntaxHighlightingPage);
} else {
    initSyntaxHighlightingPage();
}

// Re-run syntax highlighting after a delay to catch any late-loaded content
setTimeout(function() {
    initSyntaxHighlighting();
    // Also initialize copy buttons after late syntax highlighting
    setTimeout(initCopyButtons, 200);
}, 500);
