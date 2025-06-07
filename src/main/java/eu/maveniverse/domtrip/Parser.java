package eu.maveniverse.domtrip;

import java.util.Stack;

/**
 * A simple XML parser that preserves all formatting information including
 * whitespace, comments, and exact attribute formatting.
 */
public class Parser {
    
    private String xml;
    private int position;
    private int length;
    
    public Parser() {
    }

    /**
     * Parses an XML string into a lossless XML document tree
     */
    public Document parse(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("XML content cannot be null or empty");
        }
        
        this.xml = xml;
        this.position = 0;
        this.length = xml.length();
        
        Document document = new Document();
        Stack<Node> nodeStack = new Stack<>();
        nodeStack.push(document);
        
        StringBuilder precedingWhitespace = new StringBuilder();
        
        while (position < length) {
            char ch = xml.charAt(position);
            
            if (ch == '<') {
                // Save any preceding whitespace/text
                if (!precedingWhitespace.isEmpty()) {
                    String rawText = precedingWhitespace.toString();
                    String decodedText = Text.unescapeTextContent(rawText);
                    Text textNode = new Text(decodedText, rawText);
                    nodeStack.peek().addChildInternal(textNode);
                    precedingWhitespace.setLength(0);
                }
                
                if (position + 1 < length) {
                    char nextChar = xml.charAt(position + 1);
                    
                    if (nextChar == '!') {
                        if (position + 3 < length && xml.startsWith("<!--", position)) {
                            // Parse comment
                            Comment comment = parseComment();
                            nodeStack.peek().addChildInternal(comment);
                        } else if (position + 8 < length && xml.startsWith("<![CDATA[", position)) {
                            // Parse CDATA
                            Text cdata = parseCData();
                            nodeStack.peek().addChildInternal(cdata);
                        } else {
                            // Skip other declarations (DOCTYPE, etc.)
                            skipDeclaration();
                        }
                    } else if (nextChar == '?') {
                        // Parse processing instruction
                        String pi = parseProcessingInstruction();
                        if (pi.startsWith("<?xml")) {
                            document.setXmlDeclaration(pi);
                        } else {
                            // Add other processing instructions as nodes
                            ProcessingInstruction piNode = new ProcessingInstruction(pi);
                            nodeStack.peek().addChildInternal(piNode);
                        }
                    } else if (nextChar == '/') {
                        // Parse closing tag
                        parseClosingTag(nodeStack);
                    } else {
                        // Parse opening tag
                        Element element = parseOpeningTag();
                        nodeStack.peek().addChildInternal(element);

                        if (!element.isSelfClosing()) {
                            nodeStack.push(element);
                        }
                    }
                }
            } else {
                // Collect text content and whitespace
                precedingWhitespace.append(ch);
                position++;
            }
        }
        
        // Add any remaining whitespace/text
        if (!precedingWhitespace.isEmpty()) {
            String rawText = precedingWhitespace.toString();
            String decodedText = Text.unescapeTextContent(rawText);
            Text textNode = new Text(decodedText, rawText);
            document.addChildInternal(textNode);
        }

        // Set the document element (first element child)
        for (Node child : document.getChildren()) {
            if (child instanceof Element) {
                document.setDocumentElementInternal((Element) child);
                break;
            }
        }

        return document;
    }
    
    private Comment parseComment() {
        position += 4; // Skip "<!--"

        StringBuilder content = new StringBuilder();
        while (position + 2 < length) {
            if (xml.startsWith("-->", position)) {
                position += 3;
                break;
            }
            content.append(xml.charAt(position));
            position++;
        }

        return new Comment(content.toString());
    }

    private Text parseCData() {
        position += 9; // Skip "<![CDATA["

        StringBuilder content = new StringBuilder();
        while (position + 2 < length) {
            if (xml.startsWith("]]>", position)) {
                position += 3;
                break;
            }
            content.append(xml.charAt(position));
            position++;
        }

        return new Text(content.toString(), true);
    }
    
    private String parseProcessingInstruction() {
        int start = position;
        position += 2; // Skip "<?"
        
        while (position + 1 < length) {
            if (xml.startsWith("?>", position)) {
                position += 2;
                break;
            }
            position++;
        }
        
        return xml.substring(start, position);
    }
    
    private void skipDeclaration() {
        while (position < length && xml.charAt(position) != '>') {
            position++;
        }
        if (position < length) {
            position++; // Skip '>'
        }
    }
    
    private Element parseOpeningTag() {
        int start = position;
        position++; // Skip '<'

        // Parse element name
        StringBuilder name = new StringBuilder();
        while (position < length && !Character.isWhitespace(xml.charAt(position))
               && xml.charAt(position) != '>' && xml.charAt(position) != '/') {
            name.append(xml.charAt(position));
            position++;
        }

        Element element = new Element(name.toString());

        // Store original opening tag for whitespace preservation

        // Parse attributes and whitespace
        while (position < length && xml.charAt(position) != '>' &&
               !(xml.charAt(position) == '/' && position + 1 < length && xml.charAt(position + 1) == '>')) {

            if (Character.isWhitespace(xml.charAt(position))) {
                // Skip whitespace but preserve it
                position++;
            } else {
                // Parse attribute
                parseAttribute(element);
            }
        }

        // Check for self-closing tag
        if (position < length && xml.charAt(position) == '/') {
            element.setSelfClosing(true);
            position++; // Skip '/'
        }

        if (position < length && xml.charAt(position) == '>') {
            position++; // Skip '>'
        }

        // Store original tag content for formatting preservation
        element.setOriginalOpenTag(xml.substring(start, position));
        return element;
    }
    
    private void parseAttribute(Element element) {
        // Parse attribute name
        StringBuilder name = new StringBuilder();
        while (position < length && xml.charAt(position) != '=' && 
               !Character.isWhitespace(xml.charAt(position))) {
            name.append(xml.charAt(position));
            position++;
        }
        
        // Skip whitespace around '='
        while (position < length && Character.isWhitespace(xml.charAt(position))) {
            position++;
        }
        
        if (position < length && xml.charAt(position) == '=') {
            // Skip whitespace after '='
            do {
                position++;
            } while (position < length && Character.isWhitespace(xml.charAt(position)));
            
            // Parse attribute value
            if (position < length && (xml.charAt(position) == '"' || xml.charAt(position) == '\'')) {
                char quote = xml.charAt(position);
                position++; // Skip opening quote

                StringBuilder value = new StringBuilder();
                while (position < length && xml.charAt(position) != quote) {
                    value.append(xml.charAt(position));
                    position++;
                }

                if (position < length) {
                    position++; // Skip closing quote
                }

                // Set attribute with original quote character and raw value (internal method doesn't mark as modified)
                String rawValue = value.toString();
                String decodedValue = Text.unescapeTextContent(rawValue);
                element.setAttributeInternal(name.toString(), decodedValue, quote, " ", rawValue);
            }
        }
    }
    
    private void parseClosingTag(Stack<Node> nodeStack) {
        position += 2; // Skip "</"

        // Parse tag name
        StringBuilder name = new StringBuilder();
        while (position < length && xml.charAt(position) != '>') {
            name.append(xml.charAt(position));
            position++;
        }

        if (position < length) {
            position++; // Skip '>'
        }

        // Pop from stack if names match
        if (!nodeStack.isEmpty() && nodeStack.peek() instanceof Element element) {
            if (element.getName().equals(name.toString().trim())) {
                nodeStack.pop();
            }
        }
    }
}
