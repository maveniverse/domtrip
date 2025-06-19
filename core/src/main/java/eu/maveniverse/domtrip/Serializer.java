package eu.maveniverse.domtrip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Serializes XML node trees back to XML string format with configurable
 * formatting options and lossless preservation for unmodified content.
 *
 * <p>The Serializer class is responsible for converting DomTrip's internal
 * XML node tree back into XML text format. It provides intelligent formatting
 * preservation that maintains the original formatting for unchanged content
 * while applying new formatting rules to modified sections.</p>
 *
 * <h3>Serialization Features:</h3>
 * <ul>
 *   <li><strong>Selective Preservation</strong> - Preserves formatting only for unmodified content</li>
 *   <li><strong>Pretty Printing</strong> - Configurable indentation and formatting</li>
 *   <li><strong>Raw Mode</strong> - Completely unformatted output (no line breaks or indentation)</li>
 *   <li><strong>Automatic Detection</strong> - Intelligently detects and preserves existing formatting patterns</li>
 *   <li><strong>Attribute Formatting</strong> - Preserves quote styles and attribute order</li>
 *   <li><strong>Namespace Handling</strong> - Serialization of namespace declarations</li>
 * </ul>
 *
 * <h3>Serialization Modes:</h3>
 * <ul>
 *   <li><strong>Preservation Mode</strong> ({@code prettyPrint = false}, default) -
 *       Maintains original formatting for unchanged content. Automatically detects
 *       formatting patterns in existing XML and preserves them when adding new content.</li>
 *   <li><strong>Pretty Print Mode</strong> ({@code prettyPrint = true}) -
 *       Applies consistent indentation and formatting using configured settings.
 *       When {@code lineEnding = ""} and {@code indentString = ""}, produces raw mode output.</li>
 * </ul>
 *
 * <h3>Raw Mode:</h3>
 * <p>Raw mode produces XML with no line breaks or indentation, resulting in a single
 * continuous line. This is useful for minimizing file size or when formatting is not desired.
 * Raw mode can be achieved by setting both {@code lineEnding} and {@code indentString} to
 * empty strings, or by using {@link DomTripConfig#raw()}.</p>
 *
 * <h3>Automatic Formatting Detection:</h3>
 * <p>When parsing existing XML documents, the system automatically detects the formatting style:
 * <ul>
 *   <li><strong>Raw formatting</strong> - XML with no line breaks or custom spacing is detected
 *       and preserved when adding new content</li>
 *   <li><strong>Pretty formatting</strong> - XML with consistent indentation is detected and preserved</li>
 *   <li><strong>Custom formatting</strong> - XML with custom spacing patterns is detected and preserved</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Basic serialization with preservation (default)
 * Serializer serializer = new Serializer();
 * String xml = serializer.serialize(document);
 *
 * // Pretty printing with custom indentation
 * Serializer prettySerializer = new Serializer();
 * prettySerializer.setPrettyPrint(true);
 * prettySerializer.setIndentString("    "); // 4 spaces
 * prettySerializer.setLineEnding("\n");
 * String prettyXml = prettySerializer.serialize(document);
 *
 * // Raw mode (no formatting)
 * Serializer rawSerializer = new Serializer(DomTripConfig.raw());
 * String rawXml = rawSerializer.serialize(document);
 * // Result: <root><child>content</child></root>
 *
 * // Manual raw mode configuration
 * Serializer manualRaw = new Serializer();
 * manualRaw.setPrettyPrint(true);
 * manualRaw.setIndentString(""); // No indentation
 * manualRaw.setLineEnding("");   // No line endings
 *
 * // Serialize to OutputStream with encoding
 * OutputStream outputStream = new FileOutputStream("output.xml");
 * serializer.serialize(document, outputStream);
 *
 * // Using configuration
 * DomTripConfig config = DomTripConfig.prettyPrint()
 *     .withIndentString("\t")
 *     .withLineEnding("\r\n");
 * Serializer configSerializer = new Serializer(config);
 * }</pre>
 *
 * <h3>Performance Considerations:</h3>
 * <p>The Serializer is optimized for performance:</p>
 * <ul>
 *   <li>Uses StringBuilder for efficient string building</li>
 *   <li>Avoids re-serializing unmodified content when possible</li>
 *   <li>Provides both string and StringBuilder output methods</li>
 * </ul>
 *
 * @see Parser
 * @see DomTripConfig
 * @see DomTripConfig#raw()
 * @see Document
 * @see Node
 */
public class Serializer {

    private String indentString;
    private boolean prettyPrint;
    private boolean preserveComments;
    private boolean preserveProcessingInstructions;
    private boolean omitXmlDeclaration;
    private String lineEnding;

    /**
     * Creates a new Serializer with default settings.
     *
     * <p>Default settings include two-space indentation, pretty printing disabled
     * (which preserves original formatting), and standard line endings.</p>
     */
    public Serializer() {
        this.indentString = "  "; // Two spaces
        this.prettyPrint = false;
        this.preserveComments = true;
        this.preserveProcessingInstructions = true;
        this.omitXmlDeclaration = false;
        this.lineEnding = "\n";
    }

    /**
     * Creates a new Serializer with the specified configuration.
     *
     * @param config the DomTripConfig to use for serialization settings
     */
    public Serializer(DomTripConfig config) {
        this.prettyPrint = config.isPrettyPrint();
        this.indentString = config.indentString();
        this.preserveComments = config.isPreserveComments();
        this.preserveProcessingInstructions = config.isPreserveProcessingInstructions();
        this.omitXmlDeclaration = config.isOmitXmlDeclaration();
        this.lineEnding = config.lineEnding();
    }

    /**
     * Gets the indentation string used for pretty printing.
     *
     * @return the indentation string
     */
    public String getIndentString() {
        return indentString;
    }

    /**
     * Sets the indentation string for pretty printing.
     *
     * @param indentString the indentation string, or null for default
     */
    public void setIndentString(String indentString) {
        this.indentString = indentString != null ? indentString : "  ";
    }

    /**
     * Checks if pretty printing is enabled.
     *
     * @return true if pretty printing is enabled, false otherwise
     */
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    /**
     * Sets whether to enable pretty printing.
     *
     * @param prettyPrint true to enable pretty printing, false otherwise
     */
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * Gets the line ending string used for pretty printing.
     *
     * @return the line ending string
     */
    public String getLineEnding() {
        return lineEnding;
    }

    /**
     * Sets the line ending string for pretty printing.
     *
     * @param lineEnding the line ending string, or null for default
     */
    public void setLineEnding(String lineEnding) {
        this.lineEnding = lineEnding != null ? lineEnding : "\n";
    }

    /**
     * Serializes an XML document to string with custom configuration.
     *
     * <p>Creates a temporary serializer with the specified configuration
     * and uses it to serialize the document. This allows one-time use
     * of different serialization settings without modifying this serializer.</p>
     *
     * @param document the document to serialize
     * @param config the configuration to use for serialization
     * @return the serialized XML string, or empty string if document is null
     */
    public String serialize(Document document, DomTripConfig config) {
        if (document == null) {
            return "";
        }

        // Create temporary serializer with config
        Serializer tempSerializer = new Serializer(config);
        return tempSerializer.serialize(document);
    }

    /**
     * Serializes an XML document to string using this serializer's configuration.
     *
     * <p>If pretty printing is disabled and the document is unmodified,
     * the original formatting will be preserved. Otherwise, the document will
     * be serialized according to this serializer's configuration.</p>
     *
     * @param document the document to serialize
     * @return the serialized XML string, or empty string if document is null
     */
    public String serialize(Document document) {
        if (document == null) {
            return "";
        }

        if (!prettyPrint && !document.isModified()) {
            // If pretty printing is disabled and document is unmodified, use original formatting
            return document.toXml();
        }

        StringBuilder sb = new StringBuilder();

        // Add XML declaration only if it was present in original and not omitted by config
        if (!document.xmlDeclaration().isEmpty() && !omitXmlDeclaration) {
            sb.append(document.xmlDeclaration());
        }

        // Add DOCTYPE if present
        if (!document.doctype().isEmpty()) {
            if (prettyPrint && !lineEnding.isEmpty()) {
                sb.append(lineEnding);
            }
            sb.append(document.doctype());
        }

        // Add document element and other children
        if (prettyPrint) {
            if (!lineEnding.isEmpty()) {
                sb.append(lineEnding);
            }
            serializeNodePretty(document, sb, 0);
        } else {
            serializeNode(document, sb);
        }

        return sb.toString();
    }

    /**
     * Serializes an XML document to an OutputStream using the document's encoding.
     *
     * <p>This method uses the document's encoding property to determine the character
     * encoding for the output stream. If the document has no encoding specified,
     * UTF-8 is used as the default.</p>
     *
     * @param document the document to serialize
     * @param outputStream the OutputStream to write to
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void serialize(Document document, OutputStream outputStream) throws DomTripException {
        if (document == null) {
            return;
        }

        String encodingName = document.encoding();
        Charset charset;
        if (encodingName == null || encodingName.trim().isEmpty()) {
            charset = StandardCharsets.UTF_8;
        } else {
            try {
                charset = Charset.forName(encodingName);
            } catch (Exception e) {
                charset = StandardCharsets.UTF_8;
            }
        }

        serialize(document, outputStream, charset);
    }

    /**
     * Serializes an XML document to an OutputStream using the specified charset.
     *
     * <p>This method allows explicit control over the character encoding used
     * for serialization, regardless of the document's encoding property.</p>
     *
     * @param document the document to serialize
     * @param outputStream the OutputStream to write to
     * @param charset the character encoding to use
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void serialize(Document document, OutputStream outputStream, Charset charset) throws DomTripException {
        if (document == null || outputStream == null) {
            return;
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        try {
            try (Writer writer = new OutputStreamWriter(outputStream, charset)) {
                String xmlContent = serialize(document);
                writer.write(xmlContent);
                writer.flush();
            }
        } catch (IOException e) {
            throw new DomTripException("Failed to serialize document to OutputStream: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DomTripException(
                    "Failed to serialize document with charset '" + charset.name() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Serializes an XML document to an OutputStream using the specified encoding.
     *
     * <p>This method allows explicit control over the character encoding used
     * for serialization, regardless of the document's encoding property.</p>
     *
     * @param document the document to serialize
     * @param outputStream the OutputStream to write to
     * @param encoding the character encoding name to use
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void serialize(Document document, OutputStream outputStream, String encoding) throws DomTripException {
        if (encoding == null || encoding.trim().isEmpty()) {
            serialize(document, outputStream, StandardCharsets.UTF_8);
            return;
        }

        try {
            Charset charset = Charset.forName(encoding);
            serialize(document, outputStream, charset);
        } catch (Exception e) {
            throw new DomTripException("Invalid encoding name: " + encoding, e);
        }
    }

    /**
     * Serializes a single node to an OutputStream using UTF-8 encoding.
     *
     * @param node the node to serialize
     * @param outputStream the OutputStream to write to
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void serialize(Node node, OutputStream outputStream) throws DomTripException {
        serialize(node, outputStream, StandardCharsets.UTF_8);
    }

    /**
     * Serializes a single node to an OutputStream using the specified charset.
     *
     * @param node the node to serialize
     * @param outputStream the OutputStream to write to
     * @param charset the character encoding to use
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void serialize(Node node, OutputStream outputStream, Charset charset) throws DomTripException {
        if (node == null || outputStream == null) {
            return;
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        try {
            try (Writer writer = new OutputStreamWriter(outputStream, charset)) {
                String xmlContent = serialize(node);
                writer.write(xmlContent);
                writer.flush();
            }
        } catch (IOException e) {
            throw new DomTripException("Failed to serialize node to OutputStream: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DomTripException(
                    "Failed to serialize node with charset '" + charset.name() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Serializes a single node to an OutputStream using the specified encoding.
     *
     * @param node the node to serialize
     * @param outputStream the OutputStream to write to
     * @param encoding the character encoding name to use
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void serialize(Node node, OutputStream outputStream, String encoding) throws DomTripException {
        if (encoding == null || encoding.trim().isEmpty()) {
            serialize(node, outputStream, StandardCharsets.UTF_8);
            return;
        }

        try {
            Charset charset = Charset.forName(encoding);
            serialize(node, outputStream, charset);
        } catch (Exception e) {
            throw new DomTripException("Invalid encoding name: " + encoding, e);
        }
    }

    /**
     * Serializes a single node to string.
     *
     * <p>If pretty printing is disabled and the node is unmodified,
     * the original formatting will be preserved. Otherwise, the node will
     * be serialized according to this serializer's configuration.</p>
     *
     * @param node the node to serialize
     * @return the serialized XML string, or empty string if node is null
     */
    public String serialize(Node node) {
        if (node == null) {
            return "";
        }

        if (!prettyPrint && !node.isModified()) {
            return node.toXml();
        }

        StringBuilder sb = new StringBuilder();
        if (prettyPrint) {
            serializeNodePretty(node, sb, 0);
        } else {
            serializeNode(node, sb);
        }

        return sb.toString();
    }

    private void serializeNode(Node node, StringBuilder sb) {
        if (!node.isModified()) {
            // Use original formatting for unmodified nodes when not pretty printing
            node.toXml(sb);
            return;
        }

        switch (node.type()) {
            case DOCUMENT:
                serializeDocument((Document) node, sb);
                break;
            case ELEMENT:
                serializeElement((Element) node, sb);
                break;
            case TEXT:
                serializeText((Text) node, sb);
                break;
            case COMMENT:
                if (preserveComments) {
                    serializeComment((Comment) node, sb);
                }
                break;
            case PROCESSING_INSTRUCTION:
                if (preserveProcessingInstructions) {
                    serializeProcessingInstruction((ProcessingInstruction) node, sb);
                }
                break;
        }
    }

    private void serializeNodePretty(Node node, StringBuilder sb, int depth) {
        // Always apply pretty printing formatting, ignoring original formatting
        switch (node.type()) {
            case DOCUMENT:
                serializeDocumentPretty((Document) node, sb, depth);
                break;
            case ELEMENT:
                serializeElementPretty((Element) node, sb, depth);
                break;
            case TEXT:
                serializeTextPretty((Text) node, sb, depth);
                break;
            case COMMENT:
                if (preserveComments) {
                    serializeCommentPretty((Comment) node, sb, depth);
                }
                break;
            case PROCESSING_INSTRUCTION:
                if (preserveProcessingInstructions) {
                    serializeProcessingInstructionPretty((ProcessingInstruction) node, sb, depth);
                }
                break;
        }
    }

    private void serializeDocument(Document document, StringBuilder sb) {
        for (Node child : document.nodes) {
            serializeNode(child, sb);
        }

        if (document.root() != null && !document.nodes.contains(document.root())) {
            serializeNode(document.root(), sb);
        }
    }

    private void serializeDocumentPretty(Document document, StringBuilder sb, int depth) {
        for (Node child : document.nodes) {
            serializeNodePretty(child, sb, depth);
        }

        if (document.root() != null && !document.nodes.contains(document.root())) {
            serializeNodePretty(document.root(), sb, depth);
        }
    }

    private void serializeElement(Element element, StringBuilder sb) {
        // Use the Element's own toXml method to ensure all whitespace fields are respected
        element.toXml(sb);
    }

    private void serializeElementPretty(Element element, StringBuilder sb, int depth) {
        // Indentation
        if (depth > 0 && !lineEnding.isEmpty()) {
            sb.append(lineEnding);
            for (int i = 0; i < depth; i++) {
                sb.append(indentString);
            }
        }

        // Opening tag
        sb.append("<").append(element.name());

        // Attributes - use Attribute objects with pretty print formatting
        for (String attrName : element.attributes().keySet()) {
            Attribute attr = element.attributeObject(attrName);
            if (attr != null) {
                attr.toXml(sb, false); // Don't preserve original formatting in pretty print mode
            }
        }

        if (element.selfClosing()) {
            sb.append("/>");
        } else {
            sb.append(">");

            boolean hasElementChildren = element.nodes.stream().anyMatch(child -> child instanceof Element);

            // Children
            for (Node child : element.nodes) {
                if (hasElementChildren && child instanceof Element) {
                    serializeNodePretty(child, sb, depth + 1);
                } else {
                    serializeNode(child, sb);
                }
            }

            // Closing tag
            if (hasElementChildren && !lineEnding.isEmpty()) {
                sb.append(lineEnding);
                for (int i = 0; i < depth; i++) {
                    sb.append(indentString);
                }
            }
            sb.append("</").append(element.name()).append(">");
        }
    }

    private void serializeText(Text text, StringBuilder sb) {
        if (text.cdata()) {
            sb.append("<![CDATA[").append(text.content()).append("]]>");
        } else {
            sb.append(escapeTextContent(text.content()));
        }
    }

    private void serializeTextPretty(Text text, StringBuilder sb, int depth) {
        // For pretty printing, we might want to trim whitespace-only text nodes
        if (text.isWhitespaceOnly() && prettyPrint) {
            return; // Skip whitespace-only text in pretty print mode
        }
        serializeText(text, sb);
    }

    private void serializeComment(Comment comment, StringBuilder sb) {
        sb.append("<!--").append(comment.content()).append("-->");
    }

    private void serializeCommentPretty(Comment comment, StringBuilder sb, int depth) {
        if (depth > 0 && !lineEnding.isEmpty()) {
            sb.append(lineEnding);
            for (int i = 0; i < depth; i++) {
                sb.append(indentString);
            }
        }
        serializeComment(comment, sb);
    }

    private void serializeProcessingInstruction(ProcessingInstruction pi, StringBuilder sb) {
        sb.append(pi.precedingWhitespace());
        sb.append("<?").append(pi.target());
        if (!pi.data().isEmpty()) {
            sb.append(" ").append(pi.data());
        }
        sb.append("?>");
    }

    private void serializeProcessingInstructionPretty(ProcessingInstruction pi, StringBuilder sb, int depth) {
        if (depth > 0 && !lineEnding.isEmpty()) {
            sb.append(lineEnding);
            for (int i = 0; i < depth; i++) {
                sb.append(indentString);
            }
        }
        sb.append("<?").append(pi.target());
        if (!pi.data().isEmpty()) {
            sb.append(" ").append(pi.data());
        }
        sb.append("?>");
    }

    private String escapeAttributeValue(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String escapeTextContent(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
