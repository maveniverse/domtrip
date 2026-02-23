/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create documents using factory methods
 * Document doc = Document.of(); // Empty document
 * Document parsed = Document.of(xmlString); // Parse XML from String
 * Document fromStream = Document.of(inputStream); // Parse XML from InputStream
 * Document fromFile = Document.of(Paths.get("config.xml")); // Parse XML from file
 * Document withDecl = Document.withXmlDeclaration("1.0", "UTF-8");
 * Document complete = Document.withRootElement("project");
 *
 * // Set the root element
 * Element root = Element.of("root");
 * doc.root(root);
 *
 * // Access document properties
 * String encoding = doc.encoding(); // "UTF-8"
 * String version = doc.version();   // "1.0"
 *
 * // Complex documents using fluent API
 * Document complex = Document.of()
 *     .version("1.1")
 *     .encoding("UTF-8")
 *     .standalone(true)
 *     .root(Element.of("project"))
 *     .withXmlDeclaration();
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
    private String doctypePrecedingWhitespace;
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
        this.doctypePrecedingWhitespace = "";
        this.encoding = "UTF-8";
        this.version = "1.0";
        this.standalone = false;
    }

    /**
     * Private copy constructor for cloning.
     *
     * @param original the document to copy from
     */
    private Document(Document original) {
        super(); // Initialize ContainerNode with empty nodes list
        this.xmlDeclaration = original.xmlDeclaration;
        this.doctype = original.doctype;
        this.doctypePrecedingWhitespace = original.doctypePrecedingWhitespace;
        this.encoding = original.encoding;
        this.version = original.version;
        this.standalone = original.standalone;

        // Copy inherited Node properties
        this.precedingWhitespace = original.precedingWhitespace;

        // Copy root element if it exists
        if (original.root != null) {
            this.root = original.root.clone();
            this.root.parent(this); // Set parent directly
        }

        // Deep copy children directly to avoid addNode() side effects
        for (Node child : original.nodes().collect(Collectors.toList())) {
            Node clonedChild = child.clone();
            clonedChild.parent(this); // Set parent directly
            this.nodes.add(clonedChild); // Add directly to list
        }

        // Note: parent is intentionally not copied - clone has no parent
        // Note: modified flag is not copied - clone starts as unmodified
    }
    /**
     * {@inheritDoc}
     *
     * @return this document for method chaining
     */
    @Override
    public Document parent(ContainerNode parent) {
        this.parent = parent;
        return this;
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
     * @see #xmlDeclaration(String)
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
     * @return this document for method chaining
     * @see #xmlDeclaration()
     */
    public Document xmlDeclaration(String xmlDeclaration) {
        this.xmlDeclaration = xmlDeclaration != null ? xmlDeclaration : "";
        markModified();
        return this;
    }

    /**
     * Gets the DOCTYPE declaration for this document.
     *
     * <p>The DOCTYPE declaration defines the document type and may include
     * references to external DTD files or inline DTD definitions.</p>
     *
     * @return the DOCTYPE declaration string, or empty string if none is set
     * @see #doctype(String)
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
     * @return this document for method chaining
     * @see #doctype()
     */
    public Document doctype(String doctype) {
        this.doctype = doctype != null ? doctype : "";
        markModified();
        return this;
    }

    /**
     * Sets the whitespace before the DOCTYPE declaration (for internal use during parsing).
     */
    void doctypePrecedingWhitespace(String whitespace) {
        this.doctypePrecedingWhitespace = whitespace != null ? whitespace : "";
    }

    /**
     * Gets the root element of this document.
     *
     * <p>The document element is the top-level element that contains all other
     * elements in the document. Every well-formed XML document must have exactly
     * one document element.</p>
     *
     * @return the root element, or null if none is set
     * @see #root(Element)
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
     * @return this document for method chaining
     * @see #root()
     * @see #addNode(Node)
     */
    public Document root(Element root) {
        this.root = root;
        if (root != null) {
            root.parent(this);
        }
        markModified();
        return this;
    }

    /**
     * Sets the document element without marking the document as modified.
     *
     * <p>This method is used internally during parsing to avoid unnecessary
     * modification flags while still establishing the proper parent-child relationship.</p>
     *
     * @param documentElement the element to set as the document root
     */
    void rootInternal(Element documentElement) {
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
     * @see #encoding(String)
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
     * @return this document for method chaining
     * @see #encoding()
     */
    public Document encoding(String encoding) {
        this.encoding = encoding != null ? encoding : "UTF-8";
        markModified();
        return this;
    }

    /**
     * Gets the XML version for this document.
     *
     * <p>The XML version indicates which version of the XML specification
     * this document conforms to. Common values are "1.0" and "1.1".</p>
     *
     * @return the XML version, defaults to "1.0"
     * @see #version(String)
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
     * @return this document for method chaining
     * @see #version()
     */
    public Document version(String version) {
        this.version = version != null ? version : "1.0";
        markModified();
        return this;
    }

    /**
     * Gets the standalone flag for this document.
     *
     * <p>The standalone flag indicates whether the document is self-contained
     * or depends on external markup declarations. When true, the document
     * declares that it has no external dependencies.</p>
     *
     * @return true if the document is standalone, false otherwise
     * @see #standalone(boolean)
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
     * @return this document for method chaining
     * @see #isStandalone()
     */
    public Document standalone(boolean standalone) {
        this.standalone = standalone;
        markModified();
        return this;
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

        // Add DOCTYPE if present (with its preceding whitespace)
        if (!doctype.isEmpty()) {
            sb.append(doctypePrecedingWhitespace).append(doctype);
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
    }

    /**
     * Serializes this document to an OutputStream using the document's encoding.
     *
     * <p>This method uses the document's encoding property to determine the character
     * encoding for the output stream. If the document has no encoding specified,
     * UTF-8 is used as the default.</p>
     *
     * @param outputStream the OutputStream to write to
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void toXml(OutputStream outputStream) throws DomTripException {
        Serializer serializer = new Serializer();
        serializer.serialize(this, outputStream);
    }

    /**
     * Serializes this document to an OutputStream using the specified charset.
     *
     * <p>This method allows explicit control over the character encoding used
     * for serialization, regardless of the document's encoding property.</p>
     *
     * @param outputStream the OutputStream to write to
     * @param charset the character encoding to use
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void toXml(OutputStream outputStream, Charset charset) throws DomTripException {
        Serializer serializer = new Serializer();
        serializer.serialize(this, outputStream, charset);
    }

    /**
     * Serializes this document to an OutputStream using the specified encoding.
     *
     * <p>This method allows explicit control over the character encoding used
     * for serialization, regardless of the document's encoding property.</p>
     *
     * @param outputStream the OutputStream to write to
     * @param encoding the character encoding name to use
     * @throws DomTripException if serialization fails or I/O errors occur
     */
    public void toXml(OutputStream outputStream, String encoding) throws DomTripException {
        Serializer serializer = new Serializer();
        serializer.serialize(this, outputStream, encoding);
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
     * @see #version()
     * @see #encoding()
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

    // Factory methods for common document patterns

    /**
     * Creates an empty document with default settings.
     *
     * @return a new empty Document
     */
    public static Document of() {
        return new Document();
    }

    /**
     * Creates a document by parsing the provided XML string.
     *
     * <p>This is a convenience method that combines document creation and XML parsing
     * in a single call. It uses the default parser configuration.</p>
     *
     * @param xml the XML string to parse
     * @return a new Document containing the parsed XML
     * @throws DomTripException if the XML is malformed or cannot be parsed
     */
    public static Document of(String xml) throws DomTripException {
        if (xml == null || xml.trim().isEmpty()) {
            throw new DomTripException("XML string cannot be null or empty");
        }
        return new Parser().parse(xml);
    }

    /**
     * Parses an XML fragment into a list of nodes.
     *
     * <p>This method parses an XML fragment that may contain multiple root-level
     * elements, comments, processing instructions, and text nodes. Unlike
     * {@link #of(String)}, which expects a well-formed XML document, this method
     * handles fragments that don't have a single root element.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Parse a fragment with multiple elements
     * List<Node> nodes = Document.parseFragment("<foo>bar</foo><bar>baz</bar>");
     *
     * // Parse a fragment with comments and elements
     * List<Node> nodes = Document.parseFragment(
     *     "<!-- comment -->\n<foo>bar</foo>\n<bar>baz</bar>");
     * }</pre>
     *
     * @param xml the XML fragment string to parse
     * @return a list of parsed nodes
     * @throws DomTripException if the XML fragment is malformed
     */
    public static List<Node> parseFragment(String xml) throws DomTripException {
        if (xml == null || xml.isEmpty()) {
            return Collections.emptyList();
        }
        Document doc = new Parser().parse(xml);
        return doc.nodes().collect(Collectors.toList());
    }

    /**
     * Creates a document by parsing XML from an InputStream with automatic encoding detection.
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
     * @return a new Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the XML is malformed, cannot be parsed, or I/O errors occur
     */
    public static Document of(InputStream inputStream) throws DomTripException {
        return new Parser().parse(inputStream);
    }

    /**
     * Creates a document by parsing XML from an InputStream with encoding detection and fallback.
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
     * @return a new Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the XML is malformed, cannot be parsed, or I/O errors occur
     */
    public static Document of(InputStream inputStream, Charset defaultCharset) throws DomTripException {
        return new Parser().parse(inputStream, defaultCharset);
    }

    /**
     * Creates a document by parsing XML from an InputStream with encoding detection and fallback.
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
     * @return a new Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the XML is malformed, cannot be parsed, or I/O errors occur
     */
    public static Document of(InputStream inputStream, String defaultEncoding) throws DomTripException {
        return new Parser().parse(inputStream, defaultEncoding);
    }

    /**
     * Creates a document by parsing XML from a file path with automatic encoding detection.
     *
     * <p>This is a convenience method that combines file reading and XML parsing in a single call.
     * It leverages the InputStream-based parsing with automatic encoding detection to properly
     * handle various character encodings.</p>
     *
     * <p>The method automatically detects the character encoding by:</p>
     * <ol>
     *   <li>Checking for a Byte Order Mark (BOM)</li>
     *   <li>Reading the XML declaration to extract the encoding attribute</li>
     *   <li>Falling back to UTF-8 if no encoding is specified</li>
     * </ol>
     *
     * <p>This method provides the most robust way to parse XML files as it properly handles
     * character encoding detection and avoids potential encoding issues.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Parse XML file with automatic encoding detection
     * Document doc = Document.of(Paths.get("config.xml"));
     *
     * // Works with various encodings
     * Document utf8Doc = Document.of(Paths.get("utf8-file.xml"));
     * Document utf16Doc = Document.of(Paths.get("utf16-file.xml"));
     * Document isoDoc = Document.of(Paths.get("iso-8859-1-file.xml"));
     *
     * // Use with try-with-resources for proper resource management
     * try {
     *     Document doc = Document.of(configPath);
     *     Editor editor = new Editor(doc);
     *     // ... edit document
     * } catch (DomTripException e) {
     *     System.err.println("Failed to parse XML: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param path the path to the XML file to parse
     * @return a new Document containing the parsed XML with preserved formatting
     * @throws DomTripException if the file cannot be read, the XML is malformed, or cannot be parsed
     * @see #of(InputStream) for InputStream-based parsing
     * @see java.nio.file.Files#newInputStream(Path, java.nio.file.OpenOption...) for the underlying file reading
     */
    public static Document of(Path path) throws DomTripException {
        if (path == null) {
            throw new DomTripException("Path cannot be null");
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            return of(inputStream);
        } catch (IOException e) {
            throw new DomTripException("Failed to read file: " + path, e);
        }
    }

    @Override
    public Document clone() {
        return new Document(this);
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
        return new Document()
                .version(version != null ? version : "1.0")
                .encoding(encoding != null ? encoding : "UTF-8")
                .withXmlDeclaration();
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
        return new Document()
                .version(version != null ? version : "1.0")
                .encoding(encoding != null ? encoding : "UTF-8")
                .standalone(standalone)
                .withXmlDeclaration();
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
    public static Document withRootElement(String rootElementName) throws DomTripException {
        return new Document()
                .version("1.0")
                .encoding("UTF-8")
                .root(new Element(rootElementName))
                .withXmlDeclaration();
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
        return new Document()
                .version(version != null ? version : "1.0")
                .encoding(encoding != null ? encoding : "UTF-8")
                .doctype(doctype)
                .withXmlDeclaration();
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
    public static Document minimal(String rootElementName) throws DomTripException {
        return new Document().root(new Element(rootElementName));
    }

    /**
     * Generates and sets an XML declaration based on current document settings.
     *
     * <p>The XML declaration will include the version, encoding, and standalone
     * flag (if true) based on the current document configuration.</p>
     *
     * @return this document for method chaining
     */
    public Document withXmlDeclaration() {
        StringBuilder xmlDecl = new StringBuilder("<?xml version=\"");
        xmlDecl.append(version()).append("\"");
        xmlDecl.append(" encoding=\"").append(encoding()).append("\"");
        if (isStandalone()) {
            xmlDecl.append(" standalone=\"yes\"");
        }
        xmlDecl.append("?>");
        xmlDeclaration(xmlDecl.toString());
        return this;
    }
}
