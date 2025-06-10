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
 * String encoding = doc.encoding(); // "UTF-8"
 * String version = doc.version();   // "1.0"
 *
 * // Use builder pattern for complex documents
 * Document complex = Document.builder()
 *     .withVersion("1.1")
 *     .withEncoding("UTF-8")
 *     .withStandalone(true)
 *     .withRootElement("project")
 *     .withXmlDeclaration()
 *     .build();
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
 * @see Element
 * @see ContainerNode
 * @see Parser
 */
public class Document extends ContainerNode {

    private String xmlDeclaration;
    private String doctype;
    private Element root;
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
     */
    @Override
    public NodeType type() {
        return NodeType.DOCUMENT;
    }

    /**
     * Gets the XML declaration string for this document.
     *
     * <p>The XML declaration typically contains version, encoding, and standalone
     * information, formatted as: {@code <?xml version="1.0" encoding="UTF-8"?>}</p>
     *
     * @return the XML declaration string, or empty string if none is set
     * @see #setXmlDeclaration(String)
     */
    public String xmlDeclaration() {
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
     * @see #setDoctype(String)
     */
    public String doctype() {
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
     * @return the root element, or null if none is set
     * @see #setRoot(Element)
     */
    public Element root() {
        return root;
    }

    /**
     * Sets the root element of this document.
     *
     * <p>The document element becomes the top-level element containing all other
     * elements. Setting this value marks the document as modified and establishes
     * the parent-child relationship.</p>
     *
     * @param root the element to set as the document root, or null to clear it
     * @see #getDocumentElement()
     * @see #addChild(Node)
     */
    public void setRoot(Element root) {
        this.root = root;
        if (root != null) {
            root.parent(this);
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
     */
    void setDocumentElementInternal(Element documentElement) {
        this.root = documentElement;
        if (documentElement != null) {
            documentElement.parent(this);
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
     * @see #setEncoding(String)
     */
    public String encoding() {
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
     * @see #setVersion(String)
     */
    public String version() {
        return version;
    }

    /**
     * Sets the XML version for this document.
     *
     * <p>Setting this value marks the document as modified. Most documents
     * should use version "1.0" unless specific XML 1.1 features are required.</p>
     *
     * @param version the XML version to use, or null to use default "1.0"
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
        for (Node child : nodes) {
            child.toXml(sb);
        }

        // Add document element if set and not already in children
        if (root != null && !nodes.contains(root)) {
            root.toXml(sb);
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
     * @throws NullPointerException if name is null
     * @see Element#findChild(String)
     */
    public Element findElement(String name) {
        if (name == null) {
            throw new NullPointerException("Element name cannot be null");
        }

        // First check document element
        if (root != null && name.equals(root.name())) {
            return root;
        }

        // Then search in children
        for (Node child : nodes) {
            if (child instanceof Element) {
                Element element = (Element) child;
                if (name.equals(element.name())) {
                    return element;
                }
            }
        }

        // Finally search recursively in document element
        return findElementRecursive(root, name);
    }

    /**
     * Finds the first element with the specified name in the document.
     *
     * @param name the name of the element to find
     * @return the first element with the specified name, or null if not found
     * @throws NullPointerException if name is null
     */
    public Element element(String name) {
        return findElement(name);
    }

    /**
     * Recursively searches for an element with the given name within a node tree.
     *
     * @param node the node to search within
     * @param name the name of the element to find
     * @return the first element with the specified name, or null if not found
     */
    private Element findElementRecursive(Node node, String name) {
        if (node == null) return null;

        if (node instanceof ContainerNode container) {
            for (Node child : container.nodes) {
                if (child instanceof Element) {
                    Element element = (Element) child;
                    if (name.equals(element.name())) {
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
     */
    @Override
    public String toString() {
        return "Document{version='" + version + "', encoding='" + encoding + "', documentElement="
                + (root != null ? root.name() : "null") + "}";
    }

    /**
     * Builder for creating complex document structures with fluent API.
     *
     * <p>The Document.Builder provides a convenient way to construct XML documents
     * with proper configuration of version, encoding, DOCTYPE, and root elements.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Simple document
     * Document doc = Document.builder()
     *     .withRootElement("html")
     *     .build();
     *
     * // Complex document with full configuration
     * Document complex = Document.builder()
     *     .withVersion("1.1")
     *     .withEncoding("UTF-8")
     *     .withStandalone(true)
     *     .withDoctype("<!DOCTYPE html>")
     *     .withRootElement("html")
     *     .withXmlDeclaration()
     *     .build();
     * }</pre>
     *
     */
    public static class Builder {
        private final Document document;

        private Builder() {
            this.document = new Document();
        }

        /**
         * Sets the XML version for the document.
         *
         * @param version the XML version (e.g., "1.0", "1.1")
         * @return this builder for method chaining
         */
        public Builder withVersion(String version) {
            document.setVersion(version);
            return this;
        }

        /**
         * Sets the character encoding for the document.
         *
         * @param encoding the character encoding (e.g., "UTF-8", "ISO-8859-1")
         * @return this builder for method chaining
         */
        public Builder withEncoding(String encoding) {
            document.setEncoding(encoding);
            return this;
        }

        /**
         * Sets the standalone flag for the document.
         *
         * @param standalone true if the document is standalone, false otherwise
         * @return this builder for method chaining
         */
        public Builder withStandalone(boolean standalone) {
            document.setStandalone(standalone);
            return this;
        }

        /**
         * Sets the DOCTYPE declaration for the document.
         *
         * @param doctype the DOCTYPE declaration string
         * @return this builder for method chaining
         */
        public Builder withDoctype(String doctype) {
            document.setDoctype(doctype);
            return this;
        }

        /**
         * Sets the root element using an existing Element instance.
         *
         * @param rootElement the root element to set
         * @return this builder for method chaining
         */
        public Builder withRootElement(Element rootElement) {
            document.setRoot(rootElement);
            return this;
        }

        /**
         * Creates and sets the root element with the specified name.
         *
         * @param rootElementName the name of the root element to create
         * @return this builder for method chaining
         */
        public Builder withRootElement(String rootElementName) {
            document.setRoot(new Element(rootElementName));
            return this;
        }

        /**
         * Generates and sets an XML declaration based on current document settings.
         *
         * <p>The XML declaration will include the version, encoding, and standalone
         * flag (if true) based on the current document configuration.</p>
         *
         * @return this builder for method chaining
         */
        public Builder withXmlDeclaration() {
            StringBuilder xmlDecl = new StringBuilder("<?xml version=\"");
            xmlDecl.append(document.version()).append("\"");
            xmlDecl.append(" encoding=\"").append(document.encoding()).append("\"");
            if (document.isStandalone()) {
                xmlDecl.append(" standalone=\"yes\"");
            }
            xmlDecl.append("?>");
            document.setXmlDeclaration(xmlDecl.toString());
            return this;
        }

        /**
         * Builds and returns the configured Document instance.
         *
         * @return the constructed Document
         */
        public Document build() {
            return document;
        }
    }

    /**
     * Creates a new Document builder instance.
     *
     * @return a new Document.Builder for fluent document construction
     */
    public static Builder builder() {
        return new Builder();
    }

    // Factory methods for common document patterns

    /**
     * Creates an empty document with default settings.
     *
     * <p>Creates a document with UTF-8 encoding, XML version 1.0, and no XML declaration.</p>
     *
     * @return a new empty Document
     */
    public static Document empty() {
        return new Document();
    }

    /**
     * Creates a document with XML declaration.
     *
     * <p>Creates a document with the specified version and encoding, automatically
     * generating an appropriate XML declaration.</p>
     *
     * @param version the XML version (e.g., "1.0", "1.1"), or null for default "1.0"
     * @param encoding the character encoding (e.g., "UTF-8"), or null for default "UTF-8"
     * @return a new Document with XML declaration
     */
    public static Document withXmlDeclaration(String version, String encoding) {
        return Document.builder()
                .withVersion(version != null ? version : "1.0")
                .withEncoding(encoding != null ? encoding : "UTF-8")
                .withXmlDeclaration()
                .build();
    }

    /**
     * Creates a document with XML declaration and standalone attribute.
     *
     * <p>Creates a document with the specified version, encoding, and standalone flag,
     * automatically generating an appropriate XML declaration.</p>
     *
     * @param version the XML version, or null for default "1.0"
     * @param encoding the character encoding, or null for default "UTF-8"
     * @param standalone true if the document is standalone, false otherwise
     * @return a new Document with XML declaration and standalone attribute
     */
    public static Document withXmlDeclaration(String version, String encoding, boolean standalone) {
        return Document.builder()
                .withVersion(version != null ? version : "1.0")
                .withEncoding(encoding != null ? encoding : "UTF-8")
                .withStandalone(standalone)
                .withXmlDeclaration()
                .build();
    }

    /**
     * Creates a document with a root element and XML declaration.
     *
     * <p>Creates a complete document with XML declaration (version 1.0, UTF-8 encoding)
     * and the specified root element.</p>
     *
     * @param rootElementName the name of the root element
     * @return a new Document with XML declaration and root element
     */
    public static Document withRootElement(String rootElementName) {
        return Document.builder()
                .withVersion("1.0")
                .withEncoding("UTF-8")
                .withRootElement(rootElementName)
                .withXmlDeclaration()
                .build();
    }

    /**
     * Creates a document with XML declaration and DOCTYPE.
     *
     * <p>Creates a document with the specified version, encoding, and DOCTYPE declaration,
     * automatically generating an appropriate XML declaration.</p>
     *
     * @param version the XML version, or null for default "1.0"
     * @param encoding the character encoding, or null for default "UTF-8"
     * @param doctype the DOCTYPE declaration string
     * @return a new Document with XML declaration and DOCTYPE
     */
    public static Document withDoctype(String version, String encoding, String doctype) {
        return Document.builder()
                .withVersion(version != null ? version : "1.0")
                .withEncoding(encoding != null ? encoding : "UTF-8")
                .withDoctype(doctype)
                .withXmlDeclaration()
                .build();
    }

    /**
     * Creates a minimal document with just a root element (no XML declaration).
     *
     * <p>Creates a simple document containing only the specified root element,
     * without any XML declaration or DOCTYPE.</p>
     *
     * @param rootElementName the name of the root element
     * @return a new minimal Document with only a root element
     */
    public static Document minimal(String rootElementName) {
        return Document.builder().withRootElement(rootElementName).build();
    }
}
