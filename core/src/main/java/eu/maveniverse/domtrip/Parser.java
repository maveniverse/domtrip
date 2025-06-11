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

    // Pattern for parsing XML declaration attributes
    private static final Pattern XML_DECLARATION_PATTERN = Pattern.compile(
        "\\s*<\\?xml\\s+version\\s*=\\s*[\"']([^\"']+)[\"'](?:\\s+encoding\\s*=\\s*[\"']([^\"']+)[\"'])?(?:\\s+standalone\\s*=\\s*[\"']([^\"']+)[\"'])?\\s*\\?>");

    /**
     * Creates a new Parser instance with default settings.
     */
    public Parser() {}

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

            // Parse the XML string
            Document document = parse(xmlString);

            // Update document encoding based on detection
            document.encoding(detectedCharset.name());

            // Parse XML declaration attributes and update document properties
            updateDocumentFromXmlDeclaration(document, xmlString);

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
                if (StandardCharsets.UTF_8.equals(charset) && xmlString.trim().startsWith("<?xml")) {
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
        if (bytes.length >= 3) {
            // UTF-8 BOM: EF BB BF
            if (bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8;
            }
        }

        if (bytes.length >= 2) {
            // UTF-16 BE BOM: FE FF
            if (bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
                return StandardCharsets.UTF_16BE;
            }
            // UTF-16 LE BOM: FF FE
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
                return StandardCharsets.UTF_16LE;
            }
        }

        if (bytes.length >= 4) {
            // UTF-32 BE BOM: 00 00 FE FF
            if (bytes[0] == 0x00 && bytes[1] == 0x00 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) {
                return Charset.forName("UTF-32BE");
            }
            // UTF-32 LE BOM: FF FE 00 00
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE && bytes[2] == 0x00 && bytes[3] == 0x00) {
                return Charset.forName("UTF-32LE");
            }
        }

        return null; // No BOM detected
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
        if (!trimmed.startsWith("<?xml")) {
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
        if (!trimmed.startsWith("<?xml")) {
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
