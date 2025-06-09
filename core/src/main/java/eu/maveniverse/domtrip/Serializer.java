package eu.maveniverse.domtrip;

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
 *   <li><strong>Minimal Output</strong> - Option to minimize whitespace for size optimization</li>
 *   <li><strong>Attribute Formatting</strong> - Preserves quote styles and attribute order</li>
 *   <li><strong>Namespace Handling</strong> - Serialization of namespace declarations</li>
 * </ul>
 *
 * <h3>Serialization Modes:</h3>
 * <ul>
 *   <li><strong>Preservation Mode</strong> - Maintains original formatting for unchanged content</li>
 *   <li><strong>Pretty Print Mode</strong> - Applies consistent indentation and formatting</li>
 *   <li><strong>Compact Mode</strong> - Minimizes whitespace for smaller output</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Basic serialization with preservation
 * Serializer serializer = new Serializer();
 * String xml = serializer.serialize(document);
 *
 * // Pretty printing
 * Serializer prettySerializer = new Serializer(DomTripConfig.prettyPrint());
 * String prettyXml = prettySerializer.serialize(document);
 *
 * // Custom configuration
 * DomTripConfig config = DomTripConfig.defaults()
 *     .withIndentation("  ")
 *     .withPreserveWhitespace(false);
 * Serializer customSerializer = new Serializer(config);
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
 * @author DomTrip Development Team
 * @since 1.0
 * @see Parser
 * @see DomTripConfig
 * @see Document
 * @see Node
 */
public class Serializer {

    private boolean preserveFormatting;
    private String indentString;
    private boolean prettyPrint;

    public Serializer() {
        this.preserveFormatting = true;
        this.indentString = "  "; // Two spaces
        this.prettyPrint = false;
    }

    public Serializer(boolean preserveFormatting) {
        this();
        this.preserveFormatting = preserveFormatting;
    }

    public Serializer(DomTripConfig config) {
        this.preserveFormatting = config.isPreserveWhitespace();
        this.prettyPrint = config.isPrettyPrint();
        this.indentString = config.getIndentString();
    }

    public boolean isPreserveFormatting() {
        return preserveFormatting;
    }

    public void setPreserveFormatting(boolean preserveFormatting) {
        this.preserveFormatting = preserveFormatting;
    }

    public String getIndentString() {
        return indentString;
    }

    public void setIndentString(String indentString) {
        this.indentString = indentString != null ? indentString : "  ";
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * Serializes an XML document to string with custom configuration
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
     * Serializes an XML document to string
     */
    public String serialize(Document document) {
        if (document == null) {
            return "";
        }

        if (preserveFormatting && !document.isModified()) {
            // If document is unmodified, use original formatting
            return document.toXml();
        }

        StringBuilder sb = new StringBuilder();

        // Add XML declaration only if it was present in original
        if (!document.getXmlDeclaration().isEmpty()) {
            sb.append(document.getXmlDeclaration());
        }

        // Add DOCTYPE if present
        if (!document.getDoctype().isEmpty()) {
            sb.append("\n").append(document.getDoctype());
        }

        // Add document element and other children
        if (prettyPrint) {
            sb.append("\n");
            serializeNodePretty(document, sb, 0);
        } else {
            serializeNode(document, sb);
        }

        return sb.toString();
    }

    /**
     * Serializes a single node
     */
    public String serialize(Node node) {
        if (node == null) {
            return "";
        }

        if (preserveFormatting && !node.isModified()) {
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
        if (preserveFormatting && !node.isModified()) {
            // Use original formatting for unmodified nodes
            node.toXml(sb);
            return;
        }

        switch (node.getNodeType()) {
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
                serializeComment((Comment) node, sb);
                break;
            case PROCESSING_INSTRUCTION:
                serializeProcessingInstruction((ProcessingInstruction) node, sb);
                break;
        }
    }

    private void serializeNodePretty(Node node, StringBuilder sb, int depth) {
        if (preserveFormatting && !node.isModified()) {
            // Use original formatting for unmodified nodes
            node.toXml(sb);
            return;
        }

        switch (node.getNodeType()) {
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
                serializeCommentPretty((Comment) node, sb, depth);
                break;
            case PROCESSING_INSTRUCTION:
                serializeProcessingInstructionPretty((ProcessingInstruction) node, sb, depth);
                break;
        }
    }

    private void serializeDocument(Document document, StringBuilder sb) {
        for (Node child : document.getChildren()) {
            serializeNode(child, sb);
        }

        if (document.getDocumentElement() != null && !document.getChildren().contains(document.getDocumentElement())) {
            serializeNode(document.getDocumentElement(), sb);
        }
    }

    private void serializeDocumentPretty(Document document, StringBuilder sb, int depth) {
        for (Node child : document.getChildren()) {
            serializeNodePretty(child, sb, depth);
        }

        if (document.getDocumentElement() != null && !document.getChildren().contains(document.getDocumentElement())) {
            serializeNodePretty(document.getDocumentElement(), sb, depth);
        }
    }

    private void serializeElement(Element element, StringBuilder sb) {
        // Add preceding whitespace
        sb.append(element.getPrecedingWhitespace());

        // Opening tag
        sb.append("<").append(element.getName());

        // Attributes - use Attribute objects to preserve formatting
        for (String attrName : element.getAttributes().keySet()) {
            Attribute attr = element.getAttributeObject(attrName);
            if (attr != null) {
                attr.toXml(sb, preserveFormatting && !element.isModified());
            }
        }

        if (element.isSelfClosing()) {
            sb.append("/>");
        } else {
            sb.append(">");

            // Children
            for (Node child : element.getChildren()) {
                serializeNode(child, sb);
            }

            // Closing tag
            sb.append("</").append(element.getName()).append(">");
        }

        // Add following whitespace
        sb.append(element.getFollowingWhitespace());
    }

    private void serializeElementPretty(Element element, StringBuilder sb, int depth) {
        // Indentation
        if (depth > 0) {
            sb.append("\n");
            for (int i = 0; i < depth; i++) {
                sb.append(indentString);
            }
        }

        // Opening tag
        sb.append("<").append(element.getName());

        // Attributes - use Attribute objects to preserve formatting
        for (String attrName : element.getAttributes().keySet()) {
            Attribute attr = element.getAttributeObject(attrName);
            if (attr != null) {
                attr.toXml(sb, preserveFormatting && !element.isModified());
            }
        }

        if (element.isSelfClosing()) {
            sb.append("/>");
        } else {
            sb.append(">");

            boolean hasElementChildren = element.getChildren().stream().anyMatch(child -> child instanceof Element);

            // Children
            for (Node child : element.getChildren()) {
                if (hasElementChildren && child instanceof Element) {
                    serializeNodePretty(child, sb, depth + 1);
                } else {
                    serializeNode(child, sb);
                }
            }

            // Closing tag
            if (hasElementChildren) {
                sb.append("\n");
                for (int i = 0; i < depth; i++) {
                    sb.append(indentString);
                }
            }
            sb.append("</").append(element.getName()).append(">");
        }
    }

    private void serializeText(Text text, StringBuilder sb) {
        if (text.isCData()) {
            sb.append("<![CDATA[").append(text.getContent()).append("]]>");
        } else {
            sb.append(escapeTextContent(text.getContent()));
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
        sb.append("<!--").append(comment.getContent()).append("-->");
    }

    private void serializeCommentPretty(Comment comment, StringBuilder sb, int depth) {
        if (depth > 0) {
            sb.append("\n");
            for (int i = 0; i < depth; i++) {
                sb.append(indentString);
            }
        }
        serializeComment(comment, sb);
    }

    private void serializeProcessingInstruction(ProcessingInstruction pi, StringBuilder sb) {
        sb.append(pi.getPrecedingWhitespace());
        sb.append("<?").append(pi.getTarget());
        if (!pi.getData().isEmpty()) {
            sb.append(" ").append(pi.getData());
        }
        sb.append("?>");
        sb.append(pi.getFollowingWhitespace());
    }

    private void serializeProcessingInstructionPretty(ProcessingInstruction pi, StringBuilder sb, int depth) {
        if (depth > 0) {
            sb.append("\n");
            for (int i = 0; i < depth; i++) {
                sb.append(indentString);
            }
        }
        sb.append("<?").append(pi.getTarget());
        if (!pi.getData().isEmpty()) {
            sb.append(" ").append(pi.getData());
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
