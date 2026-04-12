/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 *   <li><strong>Automatic Whitespace Normalization</strong> - Never creates Text nodes with only whitespace</li>
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
 *   <li>Automatically normalizes whitespace-only content to element properties</li>
 *   <li>Builds a complete node tree with parent-child relationships</li>
 *   <li>Maintains modification flags for selective formatting preservation</li>
 * </ol>
 *
 * <h3>Whitespace Normalization:</h3>
 * <p>The parser automatically normalizes whitespace during parsing to ensure a clean tree structure:</p>
 * <ul>
 *   <li><strong>No Whitespace-Only Text Nodes</strong> - Whitespace between elements is captured in element properties</li>
 *   <li><strong>Mixed Content Preservation</strong> - Text nodes with actual content preserve their whitespace</li>
 *   <li><strong>Lossless Round-Trip</strong> - All whitespace is preserved for perfect XML reconstruction</li>
 *   <li><strong>Element Properties</strong> - Whitespace stored in precedingWhitespace, innerPrecedingWhitespace, etc.</li>
 * </ul>
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
 *     // Parse from String
 *     Document document = parser.parse(xmlString);
 *
 *     // Parse from InputStream with encoding detection
 *     Document document2 = parser.parse(inputStream);
 *
 *     // Parse from InputStream with fallback encoding
 *     Document document3 = parser.parse(inputStream, "UTF-8");
 *
 *     // Use the parsed document
 * } catch (DomTripException e) {
 *     // Handle parsing errors
 *     System.err.println("Parse error at position " + e.position() + ": " + e.getMessage());
 * }
 * }</pre>
 *
 * @implNote This class is not thread-safe. It uses instance fields to track parse state.
 * A single instance may be reused for sequential parses but must not be shared across threads.
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

    /** Prefix for XML declarations. */
    private static final String XML_DECL_PREFIX = "<?xml";

    // Pattern for parsing XML declaration attributes
    private static final Pattern XML_DECLARATION_PATTERN = Pattern.compile(
            "\\s*<\\?xml\\s+version\\s*=\\s*[\"']([^\"']+)[\"'](?:\\s+encoding\\s*=\\s*[\"']([^\"']+)[\"'])?(?:\\s+standalone\\s*=\\s*[\"']([^\"']+)[\"'])?\\s*\\?>");

    /**
     * Creates a new Parser instance with default settings.
     *
     * <p>No initialization is needed here because the parser state (xml, position, length)
     * is initialized at the start of each {@link #parse(String)} call.</p>
     */
    public Parser() {
        // Parser state is initialized in parse() method
    }

    /**
     * Parses XML from an InputStream with automatic encoding detection.
     *
     * <p>This method automatically detects the character encoding by:</p>
     * <ol>
     *   <li>Checking for a Byte Order Mark (BOM)</li>
     *   <li>Reading the XML declaration to extract the encoding attribute</li>
     *   <li>Falling back to UTF-8 if no encoding is specified</li>
     * </ol>
     *
     * <p>The resulting Document will have its encoding property set to the detected
     * or declared encoding.</p>
     *
     * @param inputStream the InputStream containing XML data
     * @return a Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the XML is malformed, cannot be parsed, or I/O errors occur
     */
    public Document parse(InputStream inputStream) throws DomTripException {
        return parse(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * Parses XML from an InputStream with encoding detection and fallback.
     *
     * <p>This method attempts to detect the character encoding by:</p>
     * <ol>
     *   <li>Checking for a Byte Order Mark (BOM)</li>
     *   <li>Reading the XML declaration to extract the encoding attribute</li>
     *   <li>Using the provided default encoding if detection fails</li>
     * </ol>
     *
     * <p>The resulting Document will have its encoding property set to the detected,
     * declared, or default encoding.</p>
     *
     * @param inputStream the InputStream containing XML data
     * @param defaultEncoding the encoding name to use if detection fails
     * @return a Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the XML is malformed, cannot be parsed, or I/O errors occur
     */
    public Document parse(InputStream inputStream, String defaultEncoding) throws DomTripException {
        if (defaultEncoding == null || defaultEncoding.trim().isEmpty()) {
            return parse(inputStream, StandardCharsets.UTF_8);
        }
        try {
            Charset charset = Charset.forName(defaultEncoding);
            return parse(inputStream, charset);
        } catch (Exception e) {
            throw new DomTripException("Invalid encoding name: " + defaultEncoding, e);
        }
    }

    /**
     * Parses XML from an InputStream with encoding detection and fallback.
     *
     * <p>This method attempts to detect the character encoding by:</p>
     * <ol>
     *   <li>Checking for a Byte Order Mark (BOM)</li>
     *   <li>Reading the XML declaration to extract the encoding attribute</li>
     *   <li>Using the provided default charset if detection fails</li>
     * </ol>
     *
     * <p>The resulting Document will have its encoding property set to the detected,
     * declared, or default encoding.</p>
     *
     * @param inputStream the InputStream containing XML data
     * @param defaultCharset the charset to use if detection fails
     * @return a Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the XML is malformed, cannot be parsed, or I/O errors occur
     */
    public Document parse(InputStream inputStream, Charset defaultCharset) throws DomTripException {
        if (inputStream == null) {
            throw new DomTripException("InputStream cannot be null");
        }
        if (defaultCharset == null) {
            defaultCharset = StandardCharsets.UTF_8;
        }

        try {
            // Read the entire stream into a byte array for encoding detection
            byte[] xmlBytes = readAllBytes(inputStream);
            if (xmlBytes.length == 0) {
                throw new DomTripException("InputStream is empty");
            }

            // Detect encoding
            Charset detectedCharset = detectEncoding(xmlBytes, defaultCharset);

            // Convert bytes to string using detected encoding
            String xmlString = new String(xmlBytes, detectedCharset);

            // Strip BOM character (U+FEFF) if present at the beginning of the string.
            // The BOM is encoding-level metadata that has already been used for charset detection.
            // It should not appear in the parsed XML content.
            boolean hasBom = !xmlString.isEmpty() && xmlString.charAt(0) == '\uFEFF';
            if (hasBom) {
                xmlString = xmlString.substring(1);
            }

            // Parse the XML string
            Document document = parse(xmlString);
            document.bom(hasBom);

            // Parse XML declaration attributes and update document properties
            updateDocumentFromXmlDeclaration(document, xmlString);

            // Update document encoding based on byte-level detection.
            // This must happen AFTER updateDocumentFromXmlDeclaration, because the detected
            // encoding (from BOM or byte patterns) takes precedence over the declared encoding
            // in the XML declaration (which may be inaccurate).
            document.encoding(detectedCharset.name());

            return document;

        } catch (IOException e) {
            throw new DomTripException("Failed to read from InputStream: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof DomTripException) {
                throw e;
            }
            throw new DomTripException("Failed to parse XML from InputStream: " + e.getMessage(), e);
        }
    }

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
        StringBuilder pendingWhitespace = new StringBuilder();

        while (position < length) {
            char ch = xml.charAt(position);

            if (ch == '<') {
                flushPrecedingText(precedingWhitespace, pendingWhitespace, nodeStack);
                parseTagStart(document, nodeStack, pendingWhitespace);
            } else {
                // Collect text content and whitespace
                precedingWhitespace.append(ch);
                position++;
            }
        }

        // Handle any remaining whitespace/text
        flushRemainingContent(document, precedingWhitespace, pendingWhitespace);

        // Check for unclosed elements
        if (nodeStack.size() > 1) {
            Node unclosed = nodeStack.peek();
            if (unclosed instanceof Element) {
                throw new DomTripException("Unclosed element '<" + ((Element) unclosed).name() + ">'");
            }
        }

        // Set the document element (first element child)
        for (Node child : document.children) {
            if (child instanceof Element) {
                document.rootInternal((Element) child);
                break;
            }
        }

        return document;
    }

    /**
     * Flushes any accumulated text/whitespace before a tag into the appropriate nodes.
     */
    private void flushPrecedingText(
            StringBuilder precedingWhitespace, StringBuilder pendingWhitespace, Deque<Node> nodeStack) {
        if (precedingWhitespace.length() == 0) {
            return;
        }

        String rawText = precedingWhitespace.toString();
        String decodedText = Text.unescapeTextContent(rawText);

        if (isWhitespaceOnly(decodedText)) {
            pendingWhitespace.append(decodedText);
        } else {
            Text textNode = new Text(decodedText, rawText);
            applyPendingWhitespace(textNode, pendingWhitespace);
            ContainerNode current = (ContainerNode) nodeStack.peek();
            current.addChildInternal(textNode);
        }
        precedingWhitespace.setLength(0);
    }

    /**
     * Parses the content starting at a '<' character and dispatches to the appropriate handler.
     */
    private void parseTagStart(Document document, Deque<Node> nodeStack, StringBuilder pendingWhitespace)
            throws DomTripException {
        if (position + 1 >= length) {
            throw new DomTripException("Unexpected end of XML: truncated '<' character", position, xml);
        }

        char nextChar = xml.charAt(position + 1);

        if (nextChar == '!') {
            parseDeclarationOrSpecial(document, nodeStack, pendingWhitespace);
        } else if (nextChar == '?') {
            parseProcessingInstructionTag(document, nodeStack, pendingWhitespace);
        } else if (nextChar == '/') {
            parseClosingTagWithWhitespace(nodeStack, pendingWhitespace);
        } else {
            parseOpeningTagAndPush(nodeStack, pendingWhitespace);
        }
    }

    /**
     * Parses declarations (comments, CDATA, DOCTYPE, etc.) starting with '<!'.
     */
    private void parseDeclarationOrSpecial(Document document, Deque<Node> nodeStack, StringBuilder pendingWhitespace)
            throws DomTripException {
        if (position + 3 < length && xml.startsWith("<!--", position)) {
            Comment comment = parseComment();
            applyPendingWhitespace(comment, pendingWhitespace);
            ((ContainerNode) nodeStack.peek()).addChildInternal(comment);
        } else if (position + 8 < length && xml.startsWith("<![CDATA[", position)) {
            Text cdata = parseCData();
            applyPendingWhitespace(cdata, pendingWhitespace);
            ((ContainerNode) nodeStack.peek()).addChildInternal(cdata);
        } else if (position + 9 < length && xml.startsWith("<!DOCTYPE", position)) {
            String doctype = parseDoctype();
            document.doctype(doctype);
            if (pendingWhitespace.length() > 0) {
                document.doctypePrecedingWhitespace(pendingWhitespace.toString());
                pendingWhitespace.setLength(0);
            }
        } else {
            skipDeclaration();
        }
    }

    /**
     * Parses a processing instruction tag starting with '<?'.
     */
    private void parseProcessingInstructionTag(
            Document document, Deque<Node> nodeStack, StringBuilder pendingWhitespace) throws DomTripException {
        String pi = parseProcessingInstruction();
        if (pi.startsWith(XML_DECL_PREFIX + " ") && pi.contains("version=")) {
            document.xmlDeclaration(pi);
            updateDocumentFromXmlDeclaration(document, pi);
        } else {
            ProcessingInstruction piNode = new ProcessingInstruction(pi);
            applyPendingWhitespace(piNode, pendingWhitespace);
            ((ContainerNode) nodeStack.peek()).addChildInternal(piNode);
        }
    }

    /**
     * Handles a closing tag, applying pending whitespace as inner whitespace to the current element.
     */
    private void parseClosingTagWithWhitespace(Deque<Node> nodeStack, StringBuilder pendingWhitespace) {
        if (!nodeStack.isEmpty() && nodeStack.peek() instanceof Element) {
            Element currentElement = (Element) nodeStack.peek();
            if (pendingWhitespace.length() > 0) {
                currentElement.innerPrecedingWhitespaceInternal(pendingWhitespace.toString());
                pendingWhitespace.setLength(0);
            }
        }
        parseClosingTag(nodeStack);
    }

    /**
     * Parses an opening tag, applies pending whitespace, and pushes onto the stack if non-self-closing.
     */
    private void parseOpeningTagAndPush(Deque<Node> nodeStack, StringBuilder pendingWhitespace)
            throws DomTripException {
        Element element = parseOpeningTag();
        applyPendingWhitespace(element, pendingWhitespace);
        ((ContainerNode) nodeStack.peek()).addChildInternal(element);
        if (!element.selfClosing()) {
            nodeStack.push(element);
        }
    }

    /**
     * Applies any accumulated pending whitespace as preceding whitespace on a node.
     */
    private void applyPendingWhitespace(Node node, StringBuilder pendingWhitespace) {
        if (pendingWhitespace.length() > 0) {
            node.precedingWhitespaceInternal(pendingWhitespace.toString());
            pendingWhitespace.setLength(0);
        }
    }

    /**
     * Flushes any remaining text/whitespace at the end of parsing into the document.
     */
    private void flushRemainingContent(
            Document document, StringBuilder precedingWhitespace, StringBuilder pendingWhitespace) {
        if (precedingWhitespace.length() > 0) {
            String rawText = precedingWhitespace.toString();
            String decodedText = Text.unescapeTextContent(rawText);

            if (isWhitespaceOnly(decodedText)) {
                pendingWhitespace.append(decodedText);
            } else {
                Text textNode = new Text(decodedText, rawText);
                if (pendingWhitespace.length() > 0) {
                    textNode.precedingWhitespaceInternal(pendingWhitespace.toString());
                    pendingWhitespace.setLength(0);
                }
                document.addChildInternal(textNode);
            }
        }

        if (pendingWhitespace.length() > 0) {
            Text trailingWhitespace = new Text(pendingWhitespace.toString());
            document.addChildInternal(trailingWhitespace);
        }
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

    /**
     * Parses a DOCTYPE declaration beginning at the current parser position and returns the full declaration text.
     *
     * Advances the parser position to just after the closing '>' of the declaration.
     *
     * @return the DOCTYPE declaration substring including the leading "<!DOCTYPE" and the trailing ">"
     * @throws DomTripException if the DOCTYPE declaration is not closed before the end of input
     */
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

    /**
     * Advances the parser position past the next `>` character that terminates a declaration.
     *
     * Stops at and consumes the first `>` found after the current position. If no `>` is found
     * before the end of the input, a `DomTripException` is thrown.
     *
     * @throws DomTripException if the declaration is not terminated before the end of the input
     */
    private void skipDeclaration() throws DomTripException {
        while (position < length && xml.charAt(position) != '>') {
            position++;
        }
        if (position < length) {
            position++; // Skip '>'
        } else {
            throw new DomTripException("Unclosed declaration", position, xml);
        }
    }

    /**
     * Parses an opening element tag at the current parser position and returns the created Element.
     *
     * The returned Element contains the element name, parsed attributes, self-closing flag, and preserved
     * original opening-tag text/whitespace for formatting fidelity.
     *
     * @return the parsed Element corresponding to the opening tag
     * @throws DomTripException if the element name is empty or the opening tag is not properly closed
     */
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

        // Capture any remaining whitespace before the closing > or />
        // This is the openTagWhitespace
        if (currentWhitespace.length() > 0) {
            element.openTagWhitespaceInternal(currentWhitespace.toString());
        }

        // Check for self-closing tag
        if (position < length && xml.charAt(position) == '/') {
            element.selfClosingInternal(true);
            position++; // Skip '/'
        }

        if (position < length && xml.charAt(position) == '>') {
            position++; // Skip '>'
        } else {
            throw new DomTripException("Unclosed opening tag '" + elementName + "'", position, xml);
        }

        // Store original tag content for formatting preservation
        element.originalOpenTag(xml.substring(start, position));
        return element;
    }

    /**
     * Parses a single attribute at the parser's current position and attaches it to the given element.
     *
     * The method consumes the attribute name, the `=` separator and a quoted attribute value, decodes
     * the raw value, and calls Element.attributeInternal(...) with the attribute name, decoded value,
     * the quote character used, the whitespace to associate with the attribute (uses `precedingWhitespace`
     * or a single space when that is empty), and the raw attribute value.
     *
     * @param element the Element to which the parsed attribute will be added
     * @param precedingWhitespace the exact whitespace string that immediately preceded this attribute in the open tag;
     *                            if empty, a single space is used when associating whitespace with the attribute
     * @throws DomTripException if the attribute value is not terminated with a matching quote or if a quoted value is missing
     */
    private void parseAttribute(Element element, String precedingWhitespace) throws DomTripException {
        String name = parseAttributeName();
        skipWhitespace();

        if (position < length && xml.charAt(position) == '=') {
            skipEqualsAndWhitespace();
            parseAttributeValue(element, name, precedingWhitespace);
        }
    }

    private String parseAttributeName() {
        StringBuilder name = new StringBuilder();
        while (position < length && xml.charAt(position) != '=' && !Character.isWhitespace(xml.charAt(position))) {
            name.append(xml.charAt(position));
            position++;
        }
        return name.toString();
    }

    private void skipWhitespace() {
        while (position < length && Character.isWhitespace(xml.charAt(position))) {
            position++;
        }
    }

    private void skipEqualsAndWhitespace() {
        // Skip '=' and any whitespace after it
        do {
            position++;
        } while (position < length && Character.isWhitespace(xml.charAt(position)));
    }

    private void parseAttributeValue(Element element, String name, String precedingWhitespace) throws DomTripException {
        if (position >= length || (xml.charAt(position) != '"' && xml.charAt(position) != '\'')) {
            throw new DomTripException("Missing attribute value quote", position, xml);
        }

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

        String rawValue = value.toString();
        String decodedValue = Text.unescapeTextContent(rawValue);
        String actualWhitespace = precedingWhitespace.isEmpty() ? " " : precedingWhitespace;
        element.attributeInternal(name, decodedValue, quote, actualWhitespace, rawValue);
    }

    /**
     * Parses a closing tag at the current parser position, validates it against the provided node stack,
     * finalizes the corresponding Element (captures original close-tag text and its close-tag whitespace),
     * removes it from the stack, and returns it.
     *
     * @param nodeStack the stack of open nodes used to validate and pop the matching Element
     * @return the Element that was closed by this tag
     * @throws DomTripException if the closing tag is truncated/unterminated, does not match the top Element on the stack,
     *                          or appears when there is no open Element to match
     */
    private Element parseClosingTag(Deque<Node> nodeStack) throws DomTripException {
        int start = position; // Remember start position for original tag capture
        position += 2; // Skip "</"

        // Capture whitespace before the element name
        StringBuilder closeTagWhitespace = new StringBuilder();
        while (position < length && Character.isWhitespace(xml.charAt(position))) {
            closeTagWhitespace.append(xml.charAt(position));
            position++;
        }

        // Parse tag name
        StringBuilder name = new StringBuilder();
        while (position < length && xml.charAt(position) != '>' && !Character.isWhitespace(xml.charAt(position))) {
            name.append(xml.charAt(position));
            position++;
        }

        // Skip any trailing whitespace after the element name (but don't capture it)
        while (position < length && Character.isWhitespace(xml.charAt(position))) {
            position++;
        }

        if (position >= length) {
            throw new DomTripException("Unclosed closing tag '</" + name + ">'", position, xml);
        }
        position++; // Skip '>'

        // Pop from stack if names match
        String closingName = name.toString();
        if (!nodeStack.isEmpty() && nodeStack.peek() instanceof Element) {
            Element element = (Element) nodeStack.peek();
            if (element.name().equals(closingName)) {
                // Capture the original close tag for whitespace preservation (AFTER consuming all content)
                element.originalCloseTag(xml.substring(start, position));

                // Capture the close tag whitespace
                if (closeTagWhitespace.length() > 0) {
                    element.closeTagWhitespaceInternal(closeTagWhitespace.toString());
                }

                nodeStack.pop();
                return element; // Return the closed element
            }
            throw new DomTripException(
                    "Mismatched closing tag: expected '</" + element.name() + ">' but found '</" + closingName + ">'",
                    position,
                    xml);
        }
        throw new DomTripException("Unexpected closing tag '</" + closingName + ">'", position, xml);
    }

    /**
     * Checks if the given content contains only whitespace characters.
     */
    private boolean isWhitespaceOnly(String content) {
        return content != null && content.trim().isEmpty();
    }

    /**
     * Reads all bytes from an InputStream.
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }

    /**
     * Detects the character encoding of XML content from byte array.
     *
     * @param xmlBytes the XML content as bytes
     * @param defaultCharset fallback charset if detection fails
     * @return the detected or default charset
     */
    private Charset detectEncoding(byte[] xmlBytes, Charset defaultCharset) {
        // Check for BOM first
        Charset bomCharset = detectBOM(xmlBytes);
        if (bomCharset != null) {
            return bomCharset;
        }

        // Try to read XML declaration with different encodings
        Charset[] charsetsToTry = {
            StandardCharsets.UTF_8,
            StandardCharsets.UTF_16,
            StandardCharsets.UTF_16BE,
            StandardCharsets.UTF_16LE,
            StandardCharsets.ISO_8859_1,
            defaultCharset
        };

        for (Charset charset : charsetsToTry) {
            if (charset == null) continue;
            try {
                String xmlString = new String(xmlBytes, charset);
                String declaredEncoding = extractEncodingFromXmlDeclaration(xmlString);
                if (declaredEncoding != null) {
                    // Verify the declared encoding is valid
                    try {
                        Charset declaredCharset = Charset.forName(declaredEncoding);
                        return declaredCharset;
                    } catch (Exception e) {
                        // Invalid encoding name, continue with detection
                    }
                }
                // If we can read the XML declaration but no encoding is specified,
                // and we're trying UTF-8, use it
                if (StandardCharsets.UTF_8.equals(charset) && xmlString.trim().startsWith(XML_DECL_PREFIX)) {
                    return charset;
                }
            } catch (Exception e) {
                // Try next encoding
            }
        }

        return defaultCharset;
    }

    /**
     * Detects Byte Order Mark (BOM) and returns corresponding charset.
     */
    private Charset detectBOM(byte[] bytes) {
        Charset charset = detectUtf32BOM(bytes);
        if (charset != null) {
            return charset;
        }
        charset = detectUtf8BOM(bytes);
        if (charset != null) {
            return charset;
        }
        return detectUtf16BOM(bytes);
    }

    private Charset detectUtf8BOM(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        }
        return null;
    }

    private Charset detectUtf16BOM(byte[] bytes) {
        if (bytes.length >= 2) {
            if (bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
                return StandardCharsets.UTF_16BE;
            }
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
                return StandardCharsets.UTF_16LE;
            }
        }
        return null;
    }

    private Charset detectUtf32BOM(byte[] bytes) {
        if (bytes.length >= 4) {
            if (bytes[0] == 0x00 && bytes[1] == 0x00 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) {
                return Charset.forName("UTF-32BE");
            }
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE && bytes[2] == 0x00 && bytes[3] == 0x00) {
                return Charset.forName("UTF-32LE");
            }
        }
        return null;
    }

    /**
     * Extracts the encoding attribute from an XML declaration.
     *
     * @param xmlString the XML content as string
     * @return the encoding value from XML declaration, or null if not found
     */
    private String extractEncodingFromXmlDeclaration(String xmlString) {
        // Look for XML declaration at the beginning of the document
        String trimmed = xmlString.trim();
        if (!trimmed.startsWith(XML_DECL_PREFIX)) {
            return null;
        }

        // Find the end of the XML declaration
        int endIndex = trimmed.indexOf("?>");
        if (endIndex == -1) {
            return null;
        }

        String xmlDeclaration = trimmed.substring(0, endIndex + 2);
        Matcher matcher = XML_DECLARATION_PATTERN.matcher(xmlDeclaration);

        if (matcher.matches()) {
            return matcher.group(2); // encoding is the second group
        }

        return null;
    }

    /**
     * Updates Document properties based on parsed XML declaration attributes.
     *
     * @param document the document to update
     * @param xmlString the XML content to parse declaration from
     */
    private void updateDocumentFromXmlDeclaration(Document document, String xmlString) {
        String trimmed = xmlString.trim();
        if (!trimmed.startsWith(XML_DECL_PREFIX)) {
            return;
        }

        // Find the end of the XML declaration
        int endIndex = trimmed.indexOf("?>");
        if (endIndex == -1) {
            return;
        }

        String xmlDeclaration = trimmed.substring(0, endIndex + 2);
        Matcher matcher = XML_DECLARATION_PATTERN.matcher(xmlDeclaration);

        if (matcher.matches()) {
            String version = matcher.group(1);
            String encoding = matcher.group(2);
            String standalone = matcher.group(3);

            if (version != null) {
                document.version(version);
            }
            if (encoding != null) {
                document.encoding(encoding);
            }
            if (standalone != null) {
                document.standalone("yes".equalsIgnoreCase(standalone));
            }
        }
    }
}
