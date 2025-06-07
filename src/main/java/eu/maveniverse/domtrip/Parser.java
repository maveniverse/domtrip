package eu.maveniverse.domtrip;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A lossless XML parser that preserves all formatting information including
 * whitespace, comments, attribute quote styles, and entity encoding.
 *
 * <p>The Parser class is responsible for converting XML text into DomTrip's
 * internal node tree representation. Unlike traditional XML parsers that
 * normalize content and lose formatting information, this parser meticulously
 * preserves every aspect of the original XML formatting to enable perfect
 * round-trip processing.</p>
 *
 * <h3>Parsing Features:</h3>
 * <ul>
 *   <li><strong>Whitespace Preservation</strong> - Maintains all whitespace exactly as written</li>
 *   <li><strong>Attribute Formatting</strong> - Preserves quote styles, order, and spacing</li>
 *   <li><strong>Comment Preservation</strong> - Keeps all XML comments in their original positions</li>
 *   <li><strong>Entity Preservation</strong> - Maintains entity references in their original form</li>
 *   <li><strong>Processing Instructions</strong> - Preserves PIs including XML declarations</li>
 *   <li><strong>CDATA Sections</strong> - Maintains CDATA boundaries and content</li>
 * </ul>
 *
 * <h3>Parsing Process:</h3>
 * <p>The parser uses a stack-based approach to build the XML tree:</p>
 * <ol>
 *   <li>Tokenizes the input XML character by character</li>
 *   <li>Identifies XML constructs (elements, comments, text, etc.)</li>
 *   <li>Preserves original formatting information for each construct</li>
 *   <li>Builds a complete node tree with parent-child relationships</li>
 *   <li>Maintains modification flags for selective formatting preservation</li>
 * </ol>
 *
 * <h3>Error Handling:</h3>
 * <p>The parser provides detailed error information for malformed XML:</p>
 * <ul>
 *   <li>Precise error positions within the source text</li>
 *   <li>Descriptive error messages for common XML problems</li>
 *   <li>Context information to help locate and fix issues</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * Parser parser = new Parser();
 * try {
 *     Document document = parser.parse(xmlString);
 *     // Use the parsed document
 * } catch (ParseException e) {
 *     // Handle parsing errors
 *     System.err.println("Parse error at position " + e.getPosition() + ": " + e.getMessage());
 * }
 * }</pre>
 *
 * @author DomTrip Development Team
 * @since 1.0
 * @see Document
 * @see Element
 * @see ParseException
 * @see Serializer
 */
class Parser {

    private String xml;
    private int position;
    private int length;

    public Parser() {}

    /**
     * Parses an XML string into a lossless XML document tree
     */
    public Document parse(String xml) throws ParseException {
        if (xml == null || xml.trim().isEmpty()) {
            throw new ParseException("XML content cannot be null or empty");
        }

        this.xml = xml;
        this.position = 0;
        this.length = xml.length();

        Document document = new Document();
        Deque<Node> nodeStack = new ArrayDeque<>();
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
                    ContainerNode current = (ContainerNode) nodeStack.peek();
                    current.addChildInternal(textNode);
                    precedingWhitespace.setLength(0);
                }

                if (position + 1 < length) {
                    char nextChar = xml.charAt(position + 1);

                    if (nextChar == '!') {
                        if (position + 3 < length && xml.startsWith("<!--", position)) {
                            // Parse comment
                            Comment comment = parseComment();
                            ContainerNode current = (ContainerNode) nodeStack.peek();
                            current.addChildInternal(comment);
                        } else if (position + 8 < length && xml.startsWith("<![CDATA[", position)) {
                            // Parse CDATA
                            Text cdata = parseCData();
                            ContainerNode current = (ContainerNode) nodeStack.peek();
                            current.addChildInternal(cdata);
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
                            ContainerNode current = (ContainerNode) nodeStack.peek();
                            current.addChildInternal(piNode);
                        }
                    } else if (nextChar == '/') {
                        // Parse closing tag
                        parseClosingTag(nodeStack);
                    } else {
                        // Parse opening tag
                        Element element = parseOpeningTag();
                        ContainerNode current = (ContainerNode) nodeStack.peek();
                        current.addChildInternal(element);

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

    private Comment parseComment() throws ParseException {
        position += 4; // Skip "<!--"

        StringBuilder content = new StringBuilder();
        while (position + 2 < length) {
            if (xml.startsWith("-->", position)) {
                position += 3;
                return new Comment(content.toString());
            }
            content.append(xml.charAt(position));
            position++;
        }

        throw new ParseException("Unclosed comment", position, xml);
    }

    private Text parseCData() throws ParseException {
        position += 9; // Skip "<![CDATA["

        StringBuilder content = new StringBuilder();
        while (position + 2 < length) {
            if (xml.startsWith("]]>", position)) {
                position += 3;
                return new Text(content.toString(), true);
            }
            content.append(xml.charAt(position));
            position++;
        }

        throw new ParseException("Unclosed CDATA section", position, xml);
    }

    private String parseProcessingInstruction() throws ParseException {
        int start = position;
        position += 2; // Skip "<?"

        while (position + 1 < length) {
            if (xml.startsWith("?>", position)) {
                position += 2;
                return xml.substring(start, position);
            }
            position++;
        }

        throw new ParseException("Unclosed processing instruction", position, xml);
    }

    private void skipDeclaration() {
        while (position < length && xml.charAt(position) != '>') {
            position++;
        }
        if (position < length) {
            position++; // Skip '>'
        }
    }

    private Element parseOpeningTag() throws ParseException {
        int start = position;
        position++; // Skip '<'

        // Parse element name
        StringBuilder name = new StringBuilder();
        while (position < length
                && !Character.isWhitespace(xml.charAt(position))
                && xml.charAt(position) != '>'
                && xml.charAt(position) != '/') {
            name.append(xml.charAt(position));
            position++;
        }

        String elementName = name.toString();
        if (elementName.isEmpty()) {
            throw new ParseException("Empty element name", position, xml);
        }

        Element element = new Element(elementName);

        // Store original opening tag for whitespace preservation

        // Parse attributes and whitespace
        while (position < length
                && xml.charAt(position) != '>'
                && !(xml.charAt(position) == '/' && position + 1 < length && xml.charAt(position + 1) == '>')) {

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

    private void parseAttribute(Element element) throws ParseException {
        // Parse attribute name
        StringBuilder name = new StringBuilder();
        while (position < length && xml.charAt(position) != '=' && !Character.isWhitespace(xml.charAt(position))) {
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
                } else {
                    throw new ParseException("Unclosed attribute value", position, xml);
                }

                // Set attribute with original quote character and raw value (internal method doesn't mark as modified)
                String rawValue = value.toString();
                String decodedValue = Text.unescapeTextContent(rawValue);
                element.setAttributeInternal(name.toString(), decodedValue, quote, " ", rawValue);
            } else {
                throw new ParseException("Missing attribute value quote", position, xml);
            }
        }
    }

    private void parseClosingTag(Deque<Node> nodeStack) {
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
