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
 * } catch (DomTripException e) {
 *     // Handle parsing errors
 *     System.err.println("Parse error at position " + e.getPosition() + ": " + e.getMessage());
 * }
 * }</pre>
 *
 * @see Document
 * @see Element
 * @see DomTripException
 * @see Serializer
 */
public class Parser {

    private String xml;
    private int position;
    private int length;

    /**
     * Creates a new Parser instance with default settings.
     */
    public Parser() {}

    /**
     * Parses an XML string into a lossless XML document tree.
     *
     * <p>This method performs complete XML parsing while preserving all formatting
     * information including whitespace, comments, attribute styles, and entity encoding.
     * The resulting Document can be used for lossless round-trip editing.</p>
     *
     * @param xml the XML string to parse
     * @return a Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the XML is malformed or cannot be parsed
     */
    public Document parse(String xml) throws DomTripException {
        if (xml == null || xml.trim().isEmpty()) {
            throw new DomTripException("XML content cannot be null or empty");
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
                    current.addNodeInternal(textNode);
                    precedingWhitespace.setLength(0);
                }

                if (position + 1 < length) {
                    char nextChar = xml.charAt(position + 1);

                    if (nextChar == '!') {
                        if (position + 3 < length && xml.startsWith("<!--", position)) {
                            // Parse comment
                            Comment comment = parseComment();
                            ContainerNode current = (ContainerNode) nodeStack.peek();
                            current.addNodeInternal(comment);
                        } else if (position + 8 < length && xml.startsWith("<![CDATA[", position)) {
                            // Parse CDATA
                            Text cdata = parseCData();
                            ContainerNode current = (ContainerNode) nodeStack.peek();
                            current.addNodeInternal(cdata);
                        } else if (position + 9 < length && xml.startsWith("<!DOCTYPE", position)) {
                            // Parse DOCTYPE declaration
                            String doctype = parseDoctype();
                            document.doctype(doctype);
                        } else {
                            // Skip other declarations
                            skipDeclaration();
                        }
                    } else if (nextChar == '?') {
                        // Parse processing instruction
                        String pi = parseProcessingInstruction();
                        if (pi.startsWith("<?xml")) {
                            document.xmlDeclaration(pi);
                        } else {
                            // Add other processing instructions as nodes
                            ProcessingInstruction piNode = new ProcessingInstruction(pi);
                            ContainerNode current = (ContainerNode) nodeStack.peek();
                            current.addNodeInternal(piNode);
                        }
                    } else if (nextChar == '/') {
                        // Parse closing tag
                        parseClosingTag(nodeStack);
                    } else {
                        // Parse opening tag
                        Element element = parseOpeningTag();
                        ContainerNode current = (ContainerNode) nodeStack.peek();
                        current.addNodeInternal(element);

                        if (!element.selfClosing()) {
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
            document.addNodeInternal(textNode);
        }

        // Set the document element (first element child)
        for (Node child : document.nodes) {
            if (child instanceof Element) {
                document.rootInternal((Element) child);
                break;
            }
        }

        return document;
    }

    private Comment parseComment() throws DomTripException {
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

        throw new DomTripException("Unclosed comment", position, xml);
    }

    private Text parseCData() throws DomTripException {
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

        throw new DomTripException("Unclosed CDATA section", position, xml);
    }

    private String parseProcessingInstruction() throws DomTripException {
        int start = position;
        position += 2; // Skip "<?"

        while (position + 1 < length) {
            if (xml.startsWith("?>", position)) {
                position += 2;
                return xml.substring(start, position);
            }
            position++;
        }

        throw new DomTripException("Unclosed processing instruction", position, xml);
    }

    private String parseDoctype() throws DomTripException {
        int start = position;
        position += 9; // Skip "<!DOCTYPE"

        int bracketCount = 0;
        boolean inQuotes = false;
        char quoteChar = 0;

        while (position < length) {
            char ch = xml.charAt(position);

            if (!inQuotes) {
                if (ch == '"' || ch == '\'') {
                    inQuotes = true;
                    quoteChar = ch;
                } else if (ch == '[') {
                    bracketCount++;
                } else if (ch == ']') {
                    bracketCount--;
                } else if (ch == '>' && bracketCount == 0) {
                    position++; // Include the closing '>'
                    return xml.substring(start, position);
                }
            } else {
                if (ch == quoteChar) {
                    inQuotes = false;
                }
            }
            position++;
        }

        throw new DomTripException("Unclosed DOCTYPE declaration", position, xml);
    }

    private void skipDeclaration() {
        while (position < length && xml.charAt(position) != '>') {
            position++;
        }
        if (position < length) {
            position++; // Skip '>'
        }
    }

    private Element parseOpeningTag() throws DomTripException {
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
            throw new DomTripException("Empty element name", position, xml);
        }

        Element element = new Element(elementName);

        // Store original opening tag for whitespace preservation

        // Parse attributes and whitespace
        StringBuilder currentWhitespace = new StringBuilder();
        while (position < length
                && xml.charAt(position) != '>'
                && !(xml.charAt(position) == '/' && position + 1 < length && xml.charAt(position + 1) == '>')) {

            if (Character.isWhitespace(xml.charAt(position))) {
                // Collect whitespace that precedes the next attribute
                currentWhitespace.append(xml.charAt(position));
                position++;
            } else {
                // Parse attribute with the collected whitespace
                parseAttribute(element, currentWhitespace.toString());
                currentWhitespace.setLength(0); // Reset for next attribute
            }
        }

        // Check for self-closing tag
        if (position < length && xml.charAt(position) == '/') {
            element.selfClosing(true);
            position++; // Skip '/'
        }

        if (position < length && xml.charAt(position) == '>') {
            position++; // Skip '>'
        }

        // Store original tag content for formatting preservation
        element.originalOpenTag(xml.substring(start, position));
        return element;
    }

    private void parseAttribute(Element element, String precedingWhitespace) throws DomTripException {
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
                    throw new DomTripException("Unclosed attribute value", position, xml);
                }

                // Set attribute with original quote character and raw value (internal method doesn't mark as modified)
                String rawValue = value.toString();
                String decodedValue = Text.unescapeTextContent(rawValue);
                // Use the actual preceding whitespace, or default to single space if empty
                String actualWhitespace = precedingWhitespace.isEmpty() ? " " : precedingWhitespace;
                element.attributeInternal(name.toString(), decodedValue, quote, actualWhitespace, rawValue);
            } else {
                throw new DomTripException("Missing attribute value quote", position, xml);
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
            if (element.name().equals(name.toString().trim())) {
                nodeStack.pop();
            }
        }
    }
}
