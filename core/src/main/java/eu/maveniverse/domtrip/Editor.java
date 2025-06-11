package eu.maveniverse.domtrip;

import java.util.Map;

/**
 * High-level API for editing XML documents while preserving original formatting.
 *
 * <p>The Editor class provides a convenient, user-friendly interface for parsing,
 * modifying, and serializing XML documents. It combines the functionality of
 * {@link Parser}, {@link Serializer}, and {@link WhitespaceManager} to offer
 * a complete XML editing solution with lossless formatting preservation.</p>
 *
 * <h3>Capabilities:</h3>
 * <ul>
 *   <li><strong>Lossless Parsing</strong> - Preserves all formatting, whitespace, and comments</li>
 *   <li><strong>Simple Editing</strong> - Methods for common XML modifications</li>
 *   <li><strong>Format Preservation</strong> - Maintains original formatting for unchanged content</li>
 *   <li><strong>Flexible Output</strong> - Support for different serialization styles</li>
 *   <li><strong>Builder Pattern</strong> - Fluent API for document construction</li>
 * </ul>
 *
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * // Parse existing XML
 * Document doc = Document.of(xmlString);
 * Editor editor = new Editor(doc);
 *
 * // Make modifications
 * Element root = editor.root();
 * editor.addElement(root, "newChild", "content");
 * editor.setAttribute(root, "version", "2.0");  // Intelligently preserves formatting
 *
 * // Serialize with preserved formatting
 * String result = editor.toXml();
 * }</pre>
 *
 * <h3>Intelligent Formatting Preservation:</h3>
 * <p>The Editor automatically preserves and infers appropriate formatting for new content:</p>
 * <pre>{@code
 * // For XML with aligned attributes:
 * // <element attr1="value1"
 * //          attr2="value2"/>
 *
 * editor.setAttribute(element, "attr3", "value3");
 * // Result: <element attr1="value1"
 * //                  attr2="value2"
 * //                  attr3="value3"/>  // Maintains alignment
 * }</pre>
 *
 * <h3>Configuration Options:</h3>
 * <pre>{@code
 * // Use custom configuration
 * DomTripConfig config = DomTripConfig.prettyPrint()
 *     .withIndentation("  ")
 *     .withPreserveComments(true);
 *
 * Document doc = Document.of(xmlString);
 * Editor editor = new Editor(doc, config);
 *
 * // Different output styles
 * String pretty = editor.toXml(DomTripConfig.prettyPrint());
 * String minimal = editor.toXml(DomTripConfig.minimal());
 * }</pre>
 *
 * <h3>Document Creation:</h3>
 * <pre>{@code
 * // Create new document
 * Editor editor = new Editor();
 * editor.createDocument("root");
 *
 * // Build document structure
 * Element root = editor.root();
 * editor.addElement(root, "child", "value");
 * }</pre>
 *
 * <h3>Working with Existing Documents:</h3>
 * <pre>{@code
 * // Use an existing Document object
 * Document existingDoc = Document.of(xmlString);
 * Editor editor = new Editor(existingDoc);
 *
 * // Or with custom configuration
 * Editor editor = new Editor(existingDoc, DomTripConfig.prettyPrint());
 *
 * // Work with programmatically created documents
 * Document doc = Document.withRootElement("project");
 * Editor editor = new Editor(doc);
 * }</pre>
 *
 * @see Parser
 * @see Serializer
 * @see DomTripConfig
 * @see Document
 */
public class Editor {

    private final Serializer serializer;
    private final WhitespaceManager whitespaceManager;
    private final DomTripConfig config;
    private Document document;

    public Editor() {
        this((Document) null, DomTripConfig.defaults());
    }

    public Editor(DomTripConfig config) {
        this((Document) null, config);
    }

    /**
     * Creates a new editor with an existing Document.
     *
     * <p>This constructor allows you to create an Editor instance from an existing
     * Document object, which is useful when you already have a parsed document or
     * when working with documents created programmatically.</p>
     *
     * <p>The editor will use default configuration settings. If you need custom
     * configuration, use {@link #Editor(Document, DomTripConfig)} instead.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Working with an existing document
     * Document existingDoc = Document.of(xmlString);
     * Editor editor = new Editor(existingDoc);
     *
     * // Working with a programmatically created document
     * Document doc = Document.withRootElement("project");
     * Editor editor = new Editor(doc);
     *
     * // Continue editing
     * Element root = editor.root();
     * editor.addElement(root, "version", "1.0");
     * }</pre>
     *
     * @param document the existing Document to edit, must not be null
     * @throws IllegalArgumentException if document is null
     * @see #Editor(Document, DomTripConfig)
     * @see Document
     */
    public Editor(Document document) {
        this(document, DomTripConfig.defaults());
    }

    /**
     * Creates a new editor with an existing Document and custom configuration.
     *
     * <p>This constructor allows you to create an Editor instance from an existing
     * Document object with custom configuration settings. This is useful when you
     * need specific serialization or formatting behavior for an existing document.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Working with existing document and custom config
     * Document existingDoc = Document.of(xmlString);
     * DomTripConfig config = DomTripConfig.prettyPrint()
     *     .withIndentString("  ")
     *     .withCommentPreservation(true);
     * Editor editor = new Editor(existingDoc, config);
     *
     * // Working with builder-created document
     * Document doc = Document.withRootElement("maven");
     * Editor editor = new Editor(doc, DomTripConfig.minimal());
     * }</pre>
     *
     * @param document the existing Document to edit, must not be null
     * @param config the configuration to use, or null for default configuration
     * @throws IllegalArgumentException if document is null
     * @see #Editor(Document)
     * @see DomTripConfig
     */
    public Editor(Document document, DomTripConfig config) {
        this.config = config != null ? config : DomTripConfig.defaults();
        this.serializer = new Serializer();
        this.whitespaceManager = new WhitespaceManager(this.config);
        this.document = document; // Can be null for empty editors
    }

    /**
     * Gets the current XML document being edited.
     *
     * @return the current Document, or null if no document is loaded
     */
    public Document document() {
        return document;
    }

    /**
     * Serializes the current document back to XML
     */
    public String toXml() {
        return document != null ? serializer.serialize(document) : "";
    }

    /**
     * Serializes with pretty printing
     */
    public String toXmlPretty() {
        if (document == null) return "";

        Serializer prettySerializer = new Serializer();
        prettySerializer.setPrettyPrint(true);
        prettySerializer.setPreserveFormatting(false);
        return prettySerializer.serialize(document);
    }

    /**
     * Adds a new element as a child of the specified parent element
     */
    public Element addElement(Element parent, String elementName) throws DomTripException {
        if (parent == null) {
            throw new DomTripException("Parent element cannot be null");
        }
        if (elementName == null || elementName.trim().isEmpty()) {
            throw new DomTripException("Element name cannot be null or empty");
        }

        Element newElement = new Element(elementName.trim());

        // Try to preserve indentation using WhitespaceManager
        String indentation = whitespaceManager.inferIndentation(parent);
        if (!indentation.isEmpty()) {
            newElement.precedingWhitespace("\n" + indentation);
        }

        // Check if the last child is a text node with just whitespace (closing tag whitespace)
        // If so, insert the new element before it, and add proper closing whitespace after
        int childCount = parent.nodeCount();
        if (childCount > 0) {
            Node lastChild = parent.getNode(childCount - 1);
            if (lastChild instanceof Text lastText) {
                String content = lastText.content();
                // Use WhitespaceManager to check if it's whitespace only
                if (whitespaceManager.isWhitespaceOnly(content) && content.contains("\n")) {
                    // Insert before the last text node
                    parent.insertNode(childCount - 1, newElement);
                    return newElement;
                }
            }
        }

        // Default: add at the end
        parent.addNode(newElement);

        // Add closing whitespace if parent has indentation
        if (!indentation.isEmpty() && parent.parent() != null) {
            String parentIndent = whitespaceManager.inferIndentation(parent.parent());
            Text closingWhitespace = new Text("\n" + parentIndent);
            parent.addNode(closingWhitespace);
        }

        return newElement;
    }

    /**
     * Adds a new element with text content
     */
    public Element addElement(Element parent, String elementName, String textContent) throws DomTripException {
        Element element = addElement(parent, elementName);
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }
        return element;
    }

    /**
     * Adds a new element using a QName.
     *
     * @param parent the parent element
     * @param qname the QName for the new element
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    public Element addElement(Element parent, QName qname) throws DomTripException {
        if (parent == null) {
            throw new DomTripException("Parent element cannot be null");
        }
        if (qname == null) {
            throw new DomTripException("QName cannot be null");
        }

        Element newElement = Element.of(qname);

        // Add namespace declaration if needed and not already declared
        if (qname.hasNamespace() && !isNamespaceDeclaredInHierarchy(parent, qname)) {
            if (qname.hasPrefix()) {
                newElement.namespaceDeclaration(qname.prefix(), qname.namespaceURI());
            } else {
                newElement.namespaceDeclaration(null, qname.namespaceURI());
            }
        }

        // Try to preserve indentation using WhitespaceManager
        String indentation = whitespaceManager.inferIndentation(parent);
        if (!indentation.isEmpty()) {
            newElement.precedingWhitespace("\n" + indentation);
        }

        parent.addNode(newElement);

        // Add closing whitespace if parent has indentation
        if (!indentation.isEmpty() && parent.parent() != null) {
            String parentIndent = whitespaceManager.inferIndentation(parent.parent());
            Text closingWhitespace = new Text("\n" + parentIndent);
            parent.addNode(closingWhitespace);
        }

        return newElement;
    }

    /**
     * Adds a new element using a QName with text content.
     *
     * @param parent the parent element
     * @param qname the QName for the new element
     * @param textContent the text content for the element
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    public Element addElement(Element parent, QName qname, String textContent) throws DomTripException {
        Element element = addElement(parent, qname);
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }
        return element;
    }

    /**
     * Removes an element from its parent
     */
    public boolean removeElement(Element element) {
        if (element == null || element.parent() == null) {
            return false;
        }

        Node parent = element.parent();
        if (parent instanceof ContainerNode container) {
            return container.removeNode(element);
        }
        return false;
    }

    /**
     * Adds or updates an attribute on an element with intelligent formatting preservation.
     *
     * <p>When updating an existing attribute, this method preserves the original formatting.
     * When adding a new attribute, it analyzes existing attributes on the element to infer
     * appropriate formatting patterns (quote style and whitespace alignment).</p>
     *
     * <h3>Formatting Inference for New Attributes:</h3>
     * <ul>
     *   <li><strong>Quote Style</strong> - Uses the most common quote style from existing attributes</li>
     *   <li><strong>Whitespace</strong> - Analyzes existing attribute spacing patterns for alignment</li>
     *   <li><strong>Multi-line Support</strong> - Preserves newline-based attribute alignment</li>
     * </ul>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * // XML with aligned attributes:
     * // <element attr1="value1"
     * //          attr2="value2"/>
     *
     * editor.setAttribute(element, "attr3", "value3");
     * // Result: <element attr1="value1"
     * //                  attr2="value2"
     * //                  attr3="value3"/>
     * }</pre>
     *
     * @param element the element to modify
     * @param name the attribute name
     * @param value the attribute value
     * @throws DomTripException if element is null or name is invalid
     * @see #setAttributes(Element, Map)
     */
    public void setAttribute(Element element, String name, String value) throws DomTripException {
        if (element == null) {
            throw new DomTripException("Element cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new DomTripException("Attribute name cannot be null or empty");
        }

        String trimmedName = name.trim();
        String safeValue = value != null ? value : "";

        // Check if attribute already exists
        if (element.hasAttribute(trimmedName)) {
            // Use Element's setAttribute which preserves existing formatting
            element.attribute(trimmedName, safeValue);
        } else {
            // New attribute - infer formatting from existing attributes
            AttributeFormatting formatting = inferAttributeFormatting(element);
            Attribute newAttr =
                    new Attribute(trimmedName, safeValue, formatting.quoteStyle, formatting.precedingWhitespace);
            element.attributeObject(trimmedName, newAttr);
        }
    }

    /**
     * Removes an attribute from an element
     */
    public boolean removeAttribute(Element element, String name) {
        if (element == null || name == null) {
            return false;
        }

        boolean hadAttribute = element.hasAttribute(name);
        element.removeAttribute(name);
        return hadAttribute;
    }

    /**
     * Sets the text content of an element
     */
    public void setTextContent(Element element, String content) throws DomTripException {
        if (element == null) {
            throw new DomTripException("Element cannot be null");
        }

        element.textContent(content);
    }

    /**
     * Adds a comment as a child of the specified parent
     */
    public Comment addComment(ContainerNode parent, String content) throws DomTripException {
        if (parent == null) {
            throw new DomTripException("Parent cannot be null");
        }

        Comment comment = new Comment(content != null ? content : "");

        // Try to preserve indentation using WhitespaceManager
        String indentation = whitespaceManager.inferIndentation(parent);
        if (!indentation.isEmpty()) {
            comment.precedingWhitespace("\n" + indentation);
        }

        parent.addNode(comment);
        return comment;
    }

    /**
     * Creates a new XML document with the specified root element.
     *
     * <p>This method creates a new document with a default XML declaration
     * and the specified root element. Any existing document will be replaced.</p>
     *
     * @param rootElementName the name of the root element
     * @throws DomTripException if the root element name is null or empty
     */
    public void createDocument(String rootElementName) throws DomTripException {
        if (rootElementName == null || rootElementName.trim().isEmpty()) {
            throw new DomTripException("Root element name cannot be null or empty");
        }

        this.document = new Document();
        // Add default XML declaration for new documents
        document.xmlDeclaration("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        Element rootElement = new Element(rootElementName.trim());
        document.root(rootElement);
        document.addNode(rootElement);
    }

    /**
     * Gets the root element of the document.
     *
     * @return the root element of the document
     * @throws IllegalStateException if no document is loaded or document has no root element
     */
    public Element root() {
        if (document == null) {
            throw new IllegalStateException("No document loaded");
        }
        Element root = document.root();
        if (root == null) {
            throw new IllegalStateException("Document has no root element");
        }
        return root;
    }

    /**
     * Validates that the document is well-formed.
     *
     * <p>Performs basic well-formedness validation including checking
     * for the presence of a root element. Additional validation rules
     * may be added in the future.</p>
     *
     * @return true if the document is well-formed, false otherwise
     */
    public boolean isWellFormed() {
        if (document == null) {
            return false;
        }

        // Basic validation - check that we have a root element
        Element root = document.root();
        if (root == null) {
            return false;
        }

        // Could add more validation rules here
        return true;
    }

    /**
     * Gets statistics about the document structure.
     *
     * <p>Returns a formatted string containing counts of different
     * node types in the document including elements, text nodes,
     * comments, and total nodes.</p>
     *
     * @return a string containing document statistics
     */
    public String documentStats() {
        if (document == null) {
            return "No document loaded";
        }

        int[] counts = new int[4]; // elements, text nodes, comments, total
        countNodes(document, counts);

        return String.format(
                "Document stats: %d elements, %d text nodes, %d comments, %d total nodes",
                counts[0], counts[1], counts[2], counts[3]);
    }

    private void countNodes(Node node, int[] counts) {
        switch (node.type()) {
            case ELEMENT:
                counts[0]++;
                break;
            case TEXT:
                counts[1]++;
                break;
            case COMMENT:
                counts[2]++;
                break;
        }
        counts[3]++;

        if (node instanceof ContainerNode container) {
            for (Node child : container.nodes) {
                countNodes(child, counts);
            }
        }
    }

    // Convenience methods for common operations

    /**
     * Batch operation to set multiple attributes on an element.
     *
     * <p>Sets multiple attributes at once with intelligent formatting preservation.
     * Each attribute uses inferred formatting based on existing patterns on the element.</p>
     *
     * @param element the element to modify
     * @param attributes a map of attribute names to values
     * @throws DomTripException if any attribute operation is invalid
     */
    public void setAttributes(Element element, Map<String, String> attributes) throws DomTripException {
        if (element != null && attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                setAttribute(element, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Batch operation to add multiple child elements with text content.
     *
     * <p>Creates multiple child elements at once, each with the specified text content.
     * This is more efficient than adding elements individually.</p>
     *
     * @param parent the parent element to add children to
     * @param nameValuePairs a map of element names to text content values
     * @throws DomTripException if any element creation is invalid
     */
    public void addElements(Element parent, Map<String, String> nameValuePairs) throws DomTripException {
        if (parent != null && nameValuePairs != null) {
            for (Map.Entry<String, String> entry : nameValuePairs.entrySet()) {
                addElement(parent, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Batch operation to add multiple child elements with text content using QNames.
     *
     * @param parent the parent element
     * @param qnameValuePairs a map of QNames to text content
     * @throws DomTripException if any element cannot be added
     */
    public void addQNameElements(Element parent, Map<QName, String> qnameValuePairs) throws DomTripException {
        if (parent != null && qnameValuePairs != null) {
            qnameValuePairs.forEach((qname, value) -> {
                try {
                    addElement(parent, qname, value);
                } catch (DomTripException e) {
                    throw new RuntimeException("Failed to add element: " + qname, e);
                }
            });
        }
    }

    /**
     * Serializes the document to XML string with custom configuration.
     *
     * <p>Uses the provided configuration instead of the editor's default
     * configuration for this serialization operation only.</p>
     *
     * @param config the configuration to use for serialization
     * @return the XML string, or empty string if no document is loaded
     */
    public String toXml(DomTripConfig config) {
        if (document == null) {
            return "";
        }

        return new Serializer(config).serialize(document);
    }

    /**
     * Gets the configuration used by this editor.
     *
     * @return the DomTripConfig instance used by this editor
     */
    public DomTripConfig config() {
        return config;
    }

    /**
     * Gets the whitespace manager used by this editor.
     *
     * @return the WhitespaceManager instance
     */
    public WhitespaceManager whitespaceManager() {
        return whitespaceManager;
    }

    /**
     * Creates a fluent builder for adding nodes.
     */
    public NodeBuilder add() {
        return new NodeBuilder(this);
    }

    /**
     * Fluent builder for creating and adding nodes to the document.
     *
     * <p>This builder provides a convenient way to add nodes to existing documents
     * while maintaining the Editor's whitespace management and configuration.</p>
     *
     */
    public static class NodeBuilder {
        private final Editor editor;

        private NodeBuilder(Editor editor) {
            this.editor = editor;
        }

        /**
         * Creates an element builder that will be added to the document.
         *
         * @param name the element name
         * @return a new EditorElementBuilder for fluent element construction
         */
        public EditorElementBuilder element(String name) {
            return new EditorElementBuilder(editor, name);
        }

        /**
         * Creates an element builder using a QName that will be added to the document.
         *
         * @param qname the element QName
         * @return a new EditorElementBuilder for fluent element construction
         */
        public EditorElementBuilder element(QName qname) {
            return new EditorElementBuilder(editor, qname);
        }

        /**
         * Creates a comment builder that will be added to the document.
         *
         * @return a new EditorCommentBuilder for fluent comment construction
         */
        public EditorCommentBuilder comment() {
            return new EditorCommentBuilder(editor);
        }

        /**
         * Creates a text builder that will be added to the document.
         *
         * @return a new EditorTextBuilder for fluent text construction
         */
        public EditorTextBuilder text() {
            return new EditorTextBuilder(editor);
        }
    }

    /**
     * Builder for creating and configuring elements within the Editor context.
     *
     * <p>This builder integrates with the Editor's whitespace management and
     * automatically adds the created element to the specified parent.</p>
     *
     */
    public static class EditorElementBuilder {
        private final Editor editor;
        private final Element element;
        private ContainerNode parent;

        private EditorElementBuilder(Editor editor, String name) {
            this.editor = editor;
            this.element = new Element(name);
        }

        private EditorElementBuilder(Editor editor, QName qname) {
            this.editor = editor;
            this.element = Element.of(qname);
        }

        /**
         * Sets the parent node for this element.
         *
         * @param parent the parent node
         * @return this builder for method chaining
         */
        public EditorElementBuilder to(ContainerNode parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Sets text content for this element.
         *
         * @param content the text content
         * @return this builder for method chaining
         */
        public EditorElementBuilder withText(String content) {
            if (content != null && !content.isEmpty()) {
                element.addNode(new Text(content));
            }
            return this;
        }

        /**
         * Adds an attribute to this element.
         *
         * @param name the attribute name
         * @param value the attribute value
         * @return this builder for method chaining
         */
        public EditorElementBuilder withAttribute(String name, String value) {
            element.attribute(name, value);
            return this;
        }

        /**
         * Adds an attribute to this element using a QName.
         *
         * @param qname the attribute QName
         * @param value the attribute value
         * @return this builder for method chaining
         */
        public EditorElementBuilder withAttribute(QName qname, String value) {
            if (qname != null) {
                element.attribute(qname.qualifiedName(), value);
            }
            return this;
        }

        /**
         * Adds multiple attributes to this element.
         *
         * @param attributes a map of attribute names to values
         * @return this builder for method chaining
         */
        public EditorElementBuilder withAttributes(Map<String, String> attributes) {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    element.attribute(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        /**
         * Adds multiple attributes to this element using QNames.
         *
         * @param qnameAttributes a map of attribute QNames to values
         * @return this builder for method chaining
         */
        public EditorElementBuilder withQNameAttributes(Map<QName, String> qnameAttributes) {
            if (qnameAttributes != null) {
                qnameAttributes.forEach((qname, value) -> {
                    if (qname != null) {
                        element.attribute(qname.qualifiedName(), value);
                    }
                });
            }
            return this;
        }

        /**
         * Makes this element self-closing.
         *
         * @return this builder for method chaining
         */
        public EditorElementBuilder selfClosing() {
            element.selfClosing(true);
            return this;
        }

        /**
         * Builds and adds the element to the document.
         *
         * @return the created and added element
         * @throws DomTripException if the element cannot be added
         */
        public Element build() throws DomTripException {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            // Use Editor's whitespace management
            String indentation = editor.whitespaceManager().inferIndentation(parent);
            if (!indentation.isEmpty()) {
                element.precedingWhitespace("\n" + indentation);
            }

            parent.addNode(element);
            return element;
        }
    }

    /**
     * Builder for creating comments within the Editor context.
     *
     * <p>This builder integrates with the Editor's whitespace management and
     * automatically adds the created comment to the specified parent.</p>
     *
     */
    public static class EditorCommentBuilder {
        private final Editor editor;
        private final Comment comment;
        private ContainerNode parent;

        private EditorCommentBuilder(Editor editor) {
            this.editor = editor;
            this.comment = new Comment("");
        }

        /**
         * Sets the parent node for this comment.
         *
         * @param parent the parent node
         * @return this builder for method chaining
         */
        public EditorCommentBuilder to(ContainerNode parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Sets the content of this comment.
         *
         * @param content the comment content
         * @return this builder for method chaining
         */
        public EditorCommentBuilder withContent(String content) {
            comment.content(content != null ? content : "");
            return this;
        }

        /**
         * Builds and adds the comment to the document.
         *
         * @return the created and added comment
         * @throws DomTripException if the comment cannot be added
         */
        public Comment build() throws DomTripException {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            // Use Editor's whitespace management
            String indentation = editor.whitespaceManager().inferIndentation(parent);
            if (!indentation.isEmpty()) {
                comment.precedingWhitespace("\n" + indentation);
            }

            parent.addNode(comment);
            return comment;
        }
    }

    /**
     * Builder for creating text nodes within the Editor context.
     *
     * <p>This builder integrates with the Editor's configuration and
     * automatically adds the created text node to the specified parent.</p>
     *
     */
    public static class EditorTextBuilder {
        private final Editor editor;
        private final Text text;
        private ContainerNode parent;

        private EditorTextBuilder(Editor editor) {
            this.editor = editor;
            this.text = new Text("");
        }

        /**
         * Sets the parent node for this text.
         *
         * @param parent the parent node
         * @return this builder for method chaining
         */
        public EditorTextBuilder to(ContainerNode parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Sets the content of this text node.
         *
         * @param content the text content
         * @return this builder for method chaining
         */
        public EditorTextBuilder withContent(String content) {
            text.content(content != null ? content : "");
            return this;
        }

        /**
         * Makes this text node a CDATA section.
         *
         * @return this builder for method chaining
         */
        public EditorTextBuilder asCData() {
            text.cdata(true);
            return this;
        }

        /**
         * Builds and adds the text node to the document.
         *
         * @return the created and added text node
         */
        public Text build() {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            parent.addNode(text);
            return text;
        }
    }

    /**
     * Infers appropriate formatting for new attributes based on existing attributes.
     */
    private AttributeFormatting inferAttributeFormatting(Element element) {
        Map<String, Attribute> existingAttrs = element.attributeObjects();

        if (existingAttrs.isEmpty()) {
            // No existing attributes - use config defaults
            return new AttributeFormatting(config.defaultQuoteStyle(), " ");
        }

        // Analyze quote style preferences
        QuoteStyle inferredQuoteStyle = inferQuoteStyle(existingAttrs.values());

        // Analyze whitespace patterns
        String inferredWhitespace = inferAttributeWhitespace(existingAttrs.values());

        return new AttributeFormatting(inferredQuoteStyle, inferredWhitespace);
    }

    /**
     * Infers the preferred quote style from existing attributes.
     */
    private QuoteStyle inferQuoteStyle(java.util.Collection<Attribute> attributes) {
        int doubleQuoteCount = 0;
        int singleQuoteCount = 0;

        for (Attribute attr : attributes) {
            if (attr.quoteStyle() == QuoteStyle.DOUBLE) {
                doubleQuoteCount++;
            } else if (attr.quoteStyle() == QuoteStyle.SINGLE) {
                singleQuoteCount++;
            }
        }

        // Return the most common style, defaulting to config default
        if (singleQuoteCount == doubleQuoteCount) {
            return config.defaultQuoteStyle();
        }
        return singleQuoteCount > doubleQuoteCount ? QuoteStyle.SINGLE : QuoteStyle.DOUBLE;
    }

    /**
     * Infers appropriate whitespace for new attributes based on existing patterns.
     */
    private String inferAttributeWhitespace(java.util.Collection<Attribute> attributes) {
        String bestMultiLinePattern = null;
        String bestCustomPattern = null;

        // Look for patterns in existing attributes
        for (Attribute attr : attributes) {
            String whitespace = attr.precedingWhitespace();
            if (whitespace != null) {
                // Prioritize multi-line patterns
                if (whitespace.contains("\n")) {
                    bestMultiLinePattern = whitespace;
                    // Don't break - look for the best multi-line pattern
                } else if (!whitespace.equals(" ")) {
                    // Found custom spacing - use the last one found (prefer later attributes)
                    bestCustomPattern = whitespace;
                }
            }
        }

        // Use multi-line pattern if found
        if (bestMultiLinePattern != null) {
            return inferAlignmentWhitespace(bestMultiLinePattern);
        }

        // Use custom pattern if found
        if (bestCustomPattern != null) {
            return bestCustomPattern;
        }

        // Default to single space
        return " ";
    }

    /**
     * Infers alignment whitespace for multi-line attribute formatting.
     */
    private String inferAlignmentWhitespace(String existingWhitespace) {
        if (existingWhitespace == null || !existingWhitespace.contains("\n")) {
            return " ";
        }

        // Extract the pattern after the last newline (including the newline)
        int lastNewline = existingWhitespace.lastIndexOf('\n');
        if (lastNewline >= 0) {
            // Return from the newline onwards (including the newline)
            return existingWhitespace.substring(lastNewline);
        }

        // Fallback to newline + some spaces for alignment
        return "\n         "; // Reasonable default for attribute alignment
    }

    /**
     * Helper class to hold inferred attribute formatting information.
     */
    private static class AttributeFormatting {
        final QuoteStyle quoteStyle;
        final String precedingWhitespace;

        AttributeFormatting(QuoteStyle quoteStyle, String precedingWhitespace) {
            this.quoteStyle = quoteStyle;
            this.precedingWhitespace = precedingWhitespace;
        }
    }

    /**
     * Checks if a namespace is already declared in the element hierarchy.
     *
     * @param element the element to check from
     * @param qname the QName to check for namespace declaration
     * @return true if the namespace is already declared
     */
    private boolean isNamespaceDeclaredInHierarchy(Element element, QName qname) {
        if (!qname.hasNamespace()) {
            return true; // No namespace needed
        }

        Element current = element;
        while (current != null) {
            // Check if this element declares the namespace
            String declaredURI = NamespaceResolver.resolveNamespaceURI(current, qname.prefix());
            if (qname.namespaceURI().equals(declaredURI)) {
                return true;
            }

            // Move up to parent element
            Node parent = current.parent();
            current = (parent instanceof Element) ? (Element) parent : null;
        }

        return false; // Namespace not found in hierarchy
    }
}
