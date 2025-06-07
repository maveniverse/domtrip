package eu.maveniverse.domtrip;

/**
 * Represents the root of an XML document, containing the document element
 * and preserving document-level formatting like XML declarations and DTDs.
 *
 * <p>The Document class serves as the top-level container for an XML document,
 * maintaining the document element along with document-level metadata such as
 * XML declarations, DOCTYPE declarations, and encoding information. It preserves
 * the exact formatting of these elements during round-trip parsing and serialization.</p>
 *
 * <h3>Document Properties:</h3>
 * <ul>
 *   <li><strong>XML Declaration</strong> - Maintains original XML declaration formatting</li>
 *   <li><strong>DOCTYPE Support</strong> - Preserves DOCTYPE declarations exactly as written</li>
 *   <li><strong>Encoding</strong> - Tracks document encoding information</li>
 *   <li><strong>Version</strong> - Maintains XML version information</li>
 *   <li><strong>Standalone Flag</strong> - Preserves standalone document declarations</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Create a new document
 * Document doc = new Document();
 * doc.setXmlDeclaration("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 *
 * // Set the root element
 * Element root = new Element("root");
 * doc.setDocumentElement(root);
 *
 * // Access document properties
 * String encoding = doc.getEncoding(); // "UTF-8"
 * String version = doc.getVersion();   // "1.0"
 * }</pre>
 *
 * <h3>Document Structure:</h3>
 * <p>A Document can contain:</p>
 * <ul>
 *   <li>Exactly one document element (root element)</li>
 *   <li>Zero or more comments and processing instructions</li>
 *   <li>Whitespace between top-level nodes</li>
 *   <li>An optional XML declaration</li>
 *   <li>An optional DOCTYPE declaration</li>
 * </ul>
 *
 * @author DomTrip Development Team
 * @since 1.0
 * @see Element
 * @see ContainerNode
 * @see Parser
 */
public class Document extends ContainerNode {

    private String xmlDeclaration;
    private String doctype;
    private Element documentElement;
    private String encoding;
    private String version;
    private boolean standalone;

    /**
     * Creates a new empty XML document with default settings.
     *
     * <p>Initializes the document with UTF-8 encoding, XML version 1.0,
     * and standalone set to false. The XML declaration and DOCTYPE are
     * initially empty.</p>
     *
     * @since 1.0
     */
    public Document() {
        super();
        this.xmlDeclaration = "";
        this.doctype = "";
        this.encoding = "UTF-8";
        this.version = "1.0";
        this.standalone = false;
    }

    /**
     * Returns the node type for this document.
     *
     * @return {@link NodeType#DOCUMENT}
     * @since 1.0
     */
    @Override
    public NodeType getNodeType() {
        return NodeType.DOCUMENT;
    }

    /**
     * Gets the XML declaration string for this document.
     *
     * <p>The XML declaration typically contains version, encoding, and standalone
     * information, formatted as: {@code <?xml version="1.0" encoding="UTF-8"?>}</p>
     *
     * @return the XML declaration string, or empty string if none is set
     * @since 1.0
     * @see #setXmlDeclaration(String)
     */
    public String getXmlDeclaration() {
        return xmlDeclaration;
    }

    /**
     * Sets the XML declaration for this document.
     *
     * <p>The XML declaration should be a complete declaration including the
     * opening {@code <?xml} and closing {@code ?>} tags. Setting this value
     * marks the document as modified.</p>
     *
     * <p>Example: {@code <?xml version="1.0" encoding="UTF-8" standalone="yes"?>}</p>
     *
     * @param xmlDeclaration the XML declaration string, or null to clear it
     * @since 1.0
     * @see #getXmlDeclaration()
     */
    public void setXmlDeclaration(String xmlDeclaration) {
        this.xmlDeclaration = xmlDeclaration != null ? xmlDeclaration : "";
        markModified();
    }

    /**
     * Gets the DOCTYPE declaration for this document.
     *
     * <p>The DOCTYPE declaration defines the document type and may include
     * references to external DTD files or inline DTD definitions.</p>
     *
     * @return the DOCTYPE declaration string, or empty string if none is set
     * @since 1.0
     * @see #setDoctype(String)
     */
    public String getDoctype() {
        return doctype;
    }

    /**
     * Sets the DOCTYPE declaration for this document.
     *
     * <p>The DOCTYPE declaration should be a complete declaration including
     * the opening {@code <!DOCTYPE} and closing {@code >} tags. Setting this
     * value marks the document as modified.</p>
     *
     * <p>Example: {@code <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     * "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">}</p>
     *
     * @param doctype the DOCTYPE declaration string, or null to clear it
     * @since 1.0
     * @see #getDoctype()
     */
    public void setDoctype(String doctype) {
        this.doctype = doctype != null ? doctype : "";
        markModified();
    }

    /**
     * Gets the root element of this document.
     *
     * <p>The document element is the top-level element that contains all other
     * elements in the document. Every well-formed XML document must have exactly
     * one document element.</p>
     *
     * @return the document element, or null if none is set
     * @since 1.0
     * @see #setDocumentElement(Element)
     */
    public Element getDocumentElement() {
        return documentElement;
    }

    /**
     * Sets the root element of this document.
     *
     * <p>The document element becomes the top-level element containing all other
     * elements. Setting this value marks the document as modified and establishes
     * the parent-child relationship.</p>
     *
     * @param documentElement the element to set as the document root, or null to clear it
     * @since 1.0
     * @see #getDocumentElement()
     * @see #addChild(Node)
     */
    public void setDocumentElement(Element documentElement) {
        this.documentElement = documentElement;
        if (documentElement != null) {
            documentElement.setParent(this);
        }
        markModified();
    }

    /**
     * Sets the document element without marking the document as modified.
     *
     * <p>This method is used internally during parsing to avoid unnecessary
     * modification flags while still establishing the proper parent-child relationship.</p>
     *
     * @param documentElement the element to set as the document root
     * @since 1.0
     */
    void setDocumentElementInternal(Element documentElement) {
        this.documentElement = documentElement;
        if (documentElement != null) {
            documentElement.setParent(this);
        }
        // Don't call markModified() here
    }

    /**
     * Gets the character encoding for this document.
     *
     * <p>The encoding specifies how the document's characters are encoded.
     * Common values include "UTF-8", "UTF-16", "ISO-8859-1", etc.</p>
     *
     * @return the document encoding, defaults to "UTF-8"
     * @since 1.0
     * @see #setEncoding(String)
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding for this document.
     *
     * <p>Setting this value marks the document as modified. The encoding
     * affects how the document is serialized and should match the actual
     * character encoding used.</p>
     *
     * @param encoding the character encoding to use, or null to use default "UTF-8"
     * @since 1.0
     * @see #getEncoding()
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding != null ? encoding : "UTF-8";
        markModified();
    }

    /**
     * Gets the XML version for this document.
     *
     * <p>The XML version indicates which version of the XML specification
     * this document conforms to. Common values are "1.0" and "1.1".</p>
     *
     * @return the XML version, defaults to "1.0"
     * @since 1.0
     * @see #setVersion(String)
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the XML version for this document.
     *
     * <p>Setting this value marks the document as modified. Most documents
     * should use version "1.0" unless specific XML 1.1 features are required.</p>
     *
     * @param version the XML version to use, or null to use default "1.0"
     * @since 1.0
     * @see #getVersion()
     */
    public void setVersion(String version) {
        this.version = version != null ? version : "1.0";
        markModified();
    }

    /**
     * Gets the standalone flag for this document.
     *
     * <p>The standalone flag indicates whether the document is self-contained
     * or depends on external markup declarations. When true, the document
     * declares that it has no external dependencies.</p>
     *
     * @return true if the document is standalone, false otherwise
     * @since 1.0
     * @see #setStandalone(boolean)
     */
    public boolean isStandalone() {
        return standalone;
    }

    /**
     * Sets the standalone flag for this document.
     *
     * <p>Setting this value marks the document as modified. The standalone
     * flag affects the XML declaration output.</p>
     *
     * @param standalone true if the document is standalone, false otherwise
     * @since 1.0
     * @see #isStandalone()
     */
    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
        markModified();
    }

    /**
     * Serializes this document to an XML string.
     *
     * <p>Creates a complete XML representation of the document including
     * XML declaration, DOCTYPE, and all child nodes with preserved formatting.</p>
     *
     * @return the complete XML string representation of this document
     * @since 1.0
     * @see #toXml(StringBuilder)
     */
    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder();
        toXml(sb);
        return sb.toString();
    }

    /**
     * Serializes this document to XML, appending to the provided StringBuilder.
     *
     * <p>This method preserves the original formatting including XML declaration,
     * DOCTYPE declaration, whitespace, and all child nodes. The output includes:</p>
     * <ul>
     *   <li>XML declaration (if present)</li>
     *   <li>DOCTYPE declaration (if present)</li>
     *   <li>Preceding whitespace</li>
     *   <li>All child nodes (comments, processing instructions, elements)</li>
     *   <li>Document element (if not already included in children)</li>
     *   <li>Following whitespace</li>
     * </ul>
     *
     * @param sb the StringBuilder to append the XML content to
     * @since 1.0
     * @see #toXml()
     */
    @Override
    public void toXml(StringBuilder sb) {
        // Add XML declaration only if it was present in original
        if (!xmlDeclaration.isEmpty()) {
            sb.append(xmlDeclaration);
        }

        // Add DOCTYPE if present
        if (!doctype.isEmpty()) {
            sb.append("\n").append(doctype);
        }

        // Add preceding whitespace
        sb.append(precedingWhitespace);

        // Add all children (comments, processing instructions, document element)
        for (Node child : children) {
            child.toXml(sb);
        }

        // Add document element if set and not already in children
        if (documentElement != null && !children.contains(documentElement)) {
            documentElement.toXml(sb);
        }

        // Add following whitespace
        sb.append(followingWhitespace);
    }

    /**
     * Finds the first element with the given name in the document.
     *
     * <p>This method searches for an element by name in the following order:</p>
     * <ol>
     *   <li>The document element itself</li>
     *   <li>Direct children of the document</li>
     *   <li>Recursively within the document element tree</li>
     * </ol>
     *
     * @param name the name of the element to find
     * @return the first element with the specified name, or null if not found
     * @throws IllegalArgumentException if name is null
     * @since 1.0
     * @see Element#findChild(String)
     */
    public Element findElement(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Element name cannot be null");
        }

        // First check document element
        if (documentElement != null && name.equals(documentElement.getName())) {
            return documentElement;
        }

        // Then search in children
        for (Node child : children) {
            if (child instanceof Element) {
                Element element = (Element) child;
                if (name.equals(element.getName())) {
                    return element;
                }
            }
        }

        // Finally search recursively in document element
        return findElementRecursive(documentElement, name);
    }

    /**
     * Recursively searches for an element with the given name within a node tree.
     *
     * @param node the node to search within
     * @param name the name of the element to find
     * @return the first element with the specified name, or null if not found
     * @since 1.0
     */
    private Element findElementRecursive(Node node, String name) {
        if (node == null) return null;

        if (node instanceof ContainerNode container) {
            for (Node child : container.getChildren()) {
                if (child instanceof Element) {
                    Element element = (Element) child;
                    if (name.equals(element.getName())) {
                        return element;
                    }
                    Element found = findElementRecursive(element, name);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Creates a minimal XML declaration based on current document settings.
     *
     * <p>Generates an XML declaration using the current version, encoding, and
     * standalone settings. The declaration follows the standard format:</p>
     * <p>{@code <?xml version="1.0" encoding="UTF-8" standalone="yes"?>}</p>
     *
     * <p>The standalone attribute is only included if the standalone flag is true.</p>
     *
     * @return a properly formatted XML declaration string
     * @since 1.0
     * @see #getVersion()
     * @see #getEncoding()
     * @see #isStandalone()
     */
    public String generateXmlDeclaration() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"").append(version).append("\"");
        sb.append(" encoding=\"").append(encoding).append("\"");
        if (standalone) {
            sb.append(" standalone=\"yes\"");
        }
        sb.append("?>");
        return sb.toString();
    }

    /**
     * Returns a string representation of this document for debugging purposes.
     *
     * <p>The string includes the XML version, encoding, and the name of the
     * document element (if present).</p>
     *
     * @return a string representation of this document
     * @since 1.0
     */
    @Override
    public String toString() {
        return "Document{version='" + version + "', encoding='" + encoding + "', documentElement="
                + (documentElement != null ? documentElement.getName() : "null") + "}";
    }
}
