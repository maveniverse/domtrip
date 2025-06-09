// Simple syntax highlighting initialization
(function() {
    console.log('Syntax highlighting script loaded');

    // Wait for DOM and Prism.js to be ready
    function initializeHighlighting() {
        console.log('Initializing syntax highlighting');

        // Add language classes to code blocks that don't have them
        var codeBlocks = document.querySelectorAll('pre code');
        console.log('Found', codeBlocks.length, 'code blocks');

        codeBlocks.forEach(function(codeBlock, index) {
            var pre = codeBlock.parentElement;
            console.log('Processing code block', index + 1);

            // Skip if already has language class
            if (pre.className.includes('language-')) {
                console.log('Code block already has language class:', pre.className);
                return;
            }

            // Try to detect language from content
            var content = codeBlock.textContent || codeBlock.innerText;
            var language = detectLanguage(content);
            console.log('Detected language:', language);

            // Add language class
            pre.className = 'language-' + language + ' line-numbers';
            codeBlock.className = 'language-' + language;
        });

        // Highlight all code blocks
        if (window.Prism) {
            console.log('Running Prism.highlightAll()');
            window.Prism.highlightAll();
        } else {
            console.error('Prism.js not available');
        }
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeHighlighting);
    } else {
        // DOM already loaded
        setTimeout(initializeHighlighting, 100);
    }

    // Function to initialize syntax highlighting
    function initializeHighlighting() {
        console.log('Initializing syntax highlighting');

        // Add language classes to code blocks that don't have them
        var codeBlocks = document.querySelectorAll('pre code');
        console.log('Found', codeBlocks.length, 'code blocks');

        codeBlocks.forEach(function(codeBlock, index) {
            var pre = codeBlock.parentElement;
            console.log('Processing code block', index + 1);

            // Skip if already has language class
            if (pre.className.includes('language-')) {
                console.log('Code block already has language class:', pre.className);
                return;
            }

            // Try to detect language from content
            var content = codeBlock.textContent || codeBlock.innerText;
            var language = detectLanguage(content);
            console.log('Detected language:', language);

            // Add language class
            pre.className = 'language-' + language + ' line-numbers';
            codeBlock.className = 'language-' + language;
        });

        // Highlight all code blocks
        if (window.Prism) {
            console.log('Running Prism.highlightAll()');
            window.Prism.highlightAll();
        } else {
            console.error('Prism.js not available');
        }
    }

    // Simple language detection
    function detectLanguage(content) {
        // Java detection
        if (content.includes('public class') || content.includes('import ') || content.includes('package ')) {
            return 'java';
        }
        
        // XML detection
        if (content.includes('<?xml') || content.includes('<dependency>') || content.includes('</')) {
            return 'xml';
        }
        
        // JSON detection
        if (content.trim().startsWith('{') && content.includes('"')) {
            return 'json';
        }
        
        // YAML detection
        if (content.includes('---') || content.includes(': ')) {
            return 'yaml';
        }
        
        // Bash detection
        if (content.includes('#!/bin/bash') || content.includes('$ ') || content.includes('cd ')) {
            return 'bash';
        }
        
        // Properties detection
        if (content.includes('=') && content.includes('.')) {
            return 'properties';
        }
        
        // Default to text
        return 'text';
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeHighlighting);
    } else {
        initializeHighlighting();
    }
})();
