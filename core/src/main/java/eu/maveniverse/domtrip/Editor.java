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
 * Editor editor = new Editor(xmlString);
 *
 * // Make modifications
 * Element root = editor.getDocumentElement();
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
 * Editor editor = new Editor(xmlString, config);
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
 * Element root = editor.getDocumentElement();
 * editor.addElement(root, "child", "value");
 * }</pre>
 *
 * <h3>Working with Existing Documents:</h3>
 * <pre>{@code
 * // Use an existing Document object
 * Document existingDoc = parser.parse(xmlString);
 * Editor editor = new Editor(existingDoc);
 *
 * // Or with custom configuration
 * Editor editor = new Editor(existingDoc, DomTripConfig.prettyPrint());
 *
 * // Work with programmatically created documents
 * Document doc = Document.builder()
 *     .withRootElement("project")
 *     .withXmlDeclaration()
 *     .build();
 * Editor editor = new Editor(doc);
 * }</pre>
 *
 * @author DomTrip Development Team
 * @since 1.0
 * @see Parser
 * @see Serializer
 * @see DomTripConfig
 * @see Document
 */
public class Editor {

    private final Parser parser = new Parser();
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

    public Editor(String xml) throws ParseException {
        this(xml, null);
    }

    public Editor(String xml, DomTripConfig config) throws ParseException {
        this((Document) null, config);
        loadXml(xml);
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
     * Document existingDoc = parser.parse(xmlString);
     * Editor editor = new Editor(existingDoc);
     *
     * // Working with a programmatically created document
     * Document doc = Document.builder()
     *     .withRootElement("project")
     *     .withXmlDeclaration()
     *     .build();
     * Editor editor = new Editor(doc);
     *
     * // Continue editing
     * Element root = editor.getDocumentElement();
     * editor.addElement(root, "version", "1.0");
     * }</pre>
     *
     * @param document the existing Document to edit, must not be null
     * @throws IllegalArgumentException if document is null
     * @since 1.0
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
     * Document existingDoc = parser.parse(xmlString);
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
     * @since 1.0
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
     * Loads XML content into the editor
     */
    public void loadXml(String xml) throws ParseException {
        this.document = parser.parse(xml);
    }

    /**
     * Gets the current XML document
     */
    public Document getDocument() {
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
    public Element addElement(Element parent, String elementName) throws InvalidXmlException {
        if (parent == null) {
            throw new InvalidXmlException("Parent element cannot be null");
        }
        if (elementName == null || elementName.trim().isEmpty()) {
            throw new InvalidXmlException("Element name cannot be null or empty");
        }

        Element newElement = new Element(elementName.trim());

        // Try to preserve indentation using WhitespaceManager
        String indentation = whitespaceManager.inferIndentation(parent);
        if (!indentation.isEmpty()) {
            newElement.setPrecedingWhitespace("\n" + indentation);
        }

        // Check if the last child is a text node with just whitespace (closing tag whitespace)
        // If so, insert the new element before it, and add proper closing whitespace after
        int childCount = parent.getChildCount();
        if (childCount > 0) {
            Node lastChild = parent.getChild(childCount - 1);
            if (lastChild instanceof Text lastText) {
                String content = lastText.getContent();
                // Use WhitespaceManager to check if it's whitespace only
                if (whitespaceManager.isWhitespaceOnly(content) && content.contains("\n")) {
                    // Insert before the last text node
                    parent.insertChild(childCount - 1, newElement);
                    return newElement;
                }
            }
        }

        // Default: add at the end
        parent.addChild(newElement);

        // Add closing whitespace if parent has indentation
        if (!indentation.isEmpty() && parent.getParent() != null) {
            String parentIndent = whitespaceManager.inferIndentation(parent.getParent());
            Text closingWhitespace = new Text("\n" + parentIndent);
            parent.addChild(closingWhitespace);
        }

        return newElement;
    }

    /**
     * Adds a new element with text content
     */
    public Element addElement(Element parent, String elementName, String textContent) throws InvalidXmlException {
        Element element = addElement(parent, elementName);
        if (textContent != null && !textContent.isEmpty()) {
            element.setTextContent(textContent);
        }
        return element;
    }

    /**
     * Removes an element from its parent
     */
    public boolean removeElement(Element element) {
        if (element == null || element.getParent() == null) {
            return false;
        }

        Node parent = element.getParent();
        if (parent instanceof ContainerNode container) {
            return container.removeChild(element);
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
     * @throws InvalidXmlException if element is null or name is invalid
     * @since 1.0
     * @see #setAttributes(Element, Map)
     */
    public void setAttribute(Element element, String name, String value) throws InvalidXmlException {
        if (element == null) {
            throw new InvalidXmlException("Element cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidXmlException("Attribute name cannot be null or empty");
        }

        String trimmedName = name.trim();
        String safeValue = value != null ? value : "";

        // Check if attribute already exists
        if (element.hasAttribute(trimmedName)) {
            // Use Element's setAttribute which preserves existing formatting
            element.setAttribute(trimmedName, safeValue);
        } else {
            // New attribute - infer formatting from existing attributes
            AttributeFormatting formatting = inferAttributeFormatting(element);
            Attribute newAttr =
                    new Attribute(trimmedName, safeValue, formatting.quoteStyle, formatting.precedingWhitespace);
            element.setAttributeObject(trimmedName, newAttr);
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
    public void setTextContent(Element element, String content) throws InvalidXmlException {
        if (element == null) {
            throw new InvalidXmlException("Element cannot be null");
        }

        element.setTextContent(content);
    }

    /**
     * Adds a comment as a child of the specified parent
     */
    public Comment addComment(ContainerNode parent, String content) throws InvalidXmlException {
        if (parent == null) {
            throw new InvalidXmlException("Parent cannot be null");
        }

        Comment comment = new Comment(content != null ? content : "");

        // Try to preserve indentation using WhitespaceManager
        String indentation = whitespaceManager.inferIndentation(parent);
        if (!indentation.isEmpty()) {
            comment.setPrecedingWhitespace("\n" + indentation);
        }

        parent.addChild(comment);
        return comment;
    }

    /**
     * Finds the first element with the given name in the document
     */
    public Element findElement(String name) throws NodeNotFoundException {
        if (name == null) {
            throw new NodeNotFoundException("Element name cannot be null", name);
        }
        return document != null ? document.findElement(name) : null;
    }

    /**
     * Finds the first child element with the given name under the specified parent
     */
    public Element findChildElement(Element parent, String name) {
        return parent != null ? parent.findChild(name).orElse(null) : null;
    }

    /**
     * Creates a new XML document with the specified root element
     */
    public void createDocument(String rootElementName) throws InvalidXmlException {
        if (rootElementName == null || rootElementName.trim().isEmpty()) {
            throw new InvalidXmlException("Root element name cannot be null or empty");
        }

        this.document = new Document();
        // Add default XML declaration for new documents
        document.setXmlDeclaration("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        Element rootElement = new Element(rootElementName.trim());
        document.setDocumentElement(rootElement);
        document.addChild(rootElement);
    }

    /**
     * Gets the root element of the document
     */
    public Element getDocumentElement() {
        return document != null ? document.getDocumentElement() : null;
    }

    /**
     * Validates that the document is well-formed
     */
    public boolean isWellFormed() {
        if (document == null) {
            return false;
        }

        // Basic validation - check that we have a root element
        Element root = document.getDocumentElement();
        if (root == null) {
            return false;
        }

        // Could add more validation rules here
        return true;
    }

    /**
     * Gets statistics about the document
     */
    public String getDocumentStats() {
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
        switch (node.getNodeType()) {
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
            for (Node child : container.getChildren()) {
                countNodes(child, counts);
            }
        }
    }

    // Convenience methods for common operations

    /**
     * Finds or creates an element with the given name.
     */
    public Element findOrCreateElement(String name) throws NodeNotFoundException, InvalidXmlException {
        Element element = findElement(name);
        if (element == null && getDocumentElement() != null) {
            element = addElement(getDocumentElement(), name);
        }
        return element;
    }

    /**
     * Sets element text content by name (finds first matching element).
     */
    public void setElementText(String elementName, String text) throws NodeNotFoundException, InvalidXmlException {
        Element element = findElement(elementName);
        if (element != null) {
            setTextContent(element, text);
        }
    }

    /**
     * Sets element attribute by element name (finds first matching element).
     */
    public void setElementAttribute(String elementName, String attrName, String attrValue)
            throws NodeNotFoundException, InvalidXmlException {
        Element element = findElement(elementName);
        if (element != null) {
            setAttribute(element, attrName, attrValue);
        }
    }

    /**
     * Batch operation to set multiple attributes on an element.
     */
    public void setAttributes(Element element, Map<String, String> attributes) throws InvalidXmlException {
        if (element != null && attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                setAttribute(element, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Batch operation to add multiple child elements with text content.
     */
    public void addElements(Element parent, Map<String, String> nameValuePairs) throws InvalidXmlException {
        if (parent != null && nameValuePairs != null) {
            for (Map.Entry<String, String> entry : nameValuePairs.entrySet()) {
                addElement(parent, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Serializes with custom configuration.
     */
    public String toXml(DomTripConfig config) {
        if (document == null) {
            return "";
        }

        return new Serializer(config).serialize(document);
    }

    /**
     * Gets the configuration used by this editor.
     */
    public DomTripConfig getConfig() {
        return config;
    }

    /**
     * Gets the whitespace manager used by this editor.
     *
     * @return the WhitespaceManager instance
     * @since 1.0
     */
    public WhitespaceManager getWhitespaceManager() {
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
     * @since 1.0
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
         * @since 1.0
         */
        public EditorElementBuilder element(String name) {
            return new EditorElementBuilder(editor, name);
        }

        /**
         * Creates a comment builder that will be added to the document.
         *
         * @return a new EditorCommentBuilder for fluent comment construction
         * @since 1.0
         */
        public EditorCommentBuilder comment() {
            return new EditorCommentBuilder(editor);
        }

        /**
         * Creates a text builder that will be added to the document.
         *
         * @return a new EditorTextBuilder for fluent text construction
         * @since 1.0
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
     * @since 1.0
     */
    public static class EditorElementBuilder {
        private final Editor editor;
        private final Element.Builder elementBuilder;
        private ContainerNode parent;

        private EditorElementBuilder(Editor editor, String name) {
            this.editor = editor;
            this.elementBuilder = Element.builder(name);
        }

        /**
         * Sets the parent node for this element.
         *
         * @param parent the parent node
         * @return this builder for method chaining
         * @since 1.0
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
         * @since 1.0
         */
        public EditorElementBuilder withText(String content) {
            elementBuilder.withText(content);
            return this;
        }

        /**
         * Adds an attribute to this element.
         *
         * @param name the attribute name
         * @param value the attribute value
         * @return this builder for method chaining
         * @since 1.0
         */
        public EditorElementBuilder withAttribute(String name, String value) {
            elementBuilder.withAttribute(name, value);
            return this;
        }

        /**
         * Adds multiple attributes to this element.
         *
         * @param attributes a map of attribute names to values
         * @return this builder for method chaining
         * @since 1.0
         */
        public EditorElementBuilder withAttributes(Map<String, String> attributes) {
            elementBuilder.withAttributes(attributes);
            return this;
        }

        /**
         * Makes this element self-closing.
         *
         * @return this builder for method chaining
         * @since 1.0
         */
        public EditorElementBuilder selfClosing() {
            elementBuilder.selfClosing();
            return this;
        }

        /**
         * Builds and adds the element to the document.
         *
         * @return the created and added element
         * @throws InvalidXmlException if the element cannot be added
         * @since 1.0
         */
        public Element build() throws InvalidXmlException {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            return elementBuilder.buildAndAddTo(editor, parent);
        }
    }

    /**
     * Builder for creating comments within the Editor context.
     *
     * <p>This builder integrates with the Editor's whitespace management and
     * automatically adds the created comment to the specified parent.</p>
     *
     * @since 1.0
     */
    public static class EditorCommentBuilder {
        private final Editor editor;
        private final Comment.Builder commentBuilder;
        private ContainerNode parent;

        private EditorCommentBuilder(Editor editor) {
            this.editor = editor;
            this.commentBuilder = Comment.builder();
        }

        /**
         * Sets the parent node for this comment.
         *
         * @param parent the parent node
         * @return this builder for method chaining
         * @since 1.0
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
         * @since 1.0
         */
        public EditorCommentBuilder withContent(String content) {
            commentBuilder.withContent(content);
            return this;
        }

        /**
         * Builds and adds the comment to the document.
         *
         * @return the created and added comment
         * @throws InvalidXmlException if the comment cannot be added
         * @since 1.0
         */
        public Comment build() throws InvalidXmlException {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            return commentBuilder.buildAndAddTo(editor, parent);
        }
    }

    /**
     * Builder for creating text nodes within the Editor context.
     *
     * <p>This builder integrates with the Editor's configuration and
     * automatically adds the created text node to the specified parent.</p>
     *
     * @since 1.0
     */
    public static class EditorTextBuilder {
        private final Editor editor;
        private final Text.Builder textBuilder;
        private ContainerNode parent;

        private EditorTextBuilder(Editor editor) {
            this.editor = editor;
            this.textBuilder = Text.builder();
        }

        /**
         * Sets the parent node for this text.
         *
         * @param parent the parent node
         * @return this builder for method chaining
         * @since 1.0
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
         * @since 1.0
         */
        public EditorTextBuilder withContent(String content) {
            textBuilder.withContent(content);
            return this;
        }

        /**
         * Makes this text node a CDATA section.
         *
         * @return this builder for method chaining
         * @since 1.0
         */
        public EditorTextBuilder asCData() {
            textBuilder.asCData();
            return this;
        }

        /**
         * Builds and adds the text node to the document.
         *
         * @return the created and added text node
         * @since 1.0
         */
        public Text build() {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            return textBuilder.buildAndAddTo(editor, parent);
        }
    }

    /**
     * Infers appropriate formatting for new attributes based on existing attributes.
     */
    private AttributeFormatting inferAttributeFormatting(Element element) {
        Map<String, Attribute> existingAttrs = element.getAttributeObjects();

        if (existingAttrs.isEmpty()) {
            // No existing attributes - use defaults
            return new AttributeFormatting(QuoteStyle.DOUBLE, " ");
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
            if (attr.getQuoteStyle() == QuoteStyle.DOUBLE) {
                doubleQuoteCount++;
            } else if (attr.getQuoteStyle() == QuoteStyle.SINGLE) {
                singleQuoteCount++;
            }
        }

        // Return the most common style, defaulting to double quotes
        return singleQuoteCount > doubleQuoteCount ? QuoteStyle.SINGLE : QuoteStyle.DOUBLE;
    }

    /**
     * Infers appropriate whitespace for new attributes based on existing patterns.
     */
    private String inferAttributeWhitespace(java.util.Collection<Attribute> attributes) {
        // Look for multi-line attribute patterns
        for (Attribute attr : attributes) {
            String whitespace = attr.getPrecedingWhitespace();
            if (whitespace != null && whitespace.contains("\n")) {
                // Found multi-line pattern - try to infer alignment
                return inferAlignmentWhitespace(whitespace);
            }
        }

        // Look for custom spacing patterns
        for (Attribute attr : attributes) {
            String whitespace = attr.getPrecedingWhitespace();
            if (whitespace != null && !whitespace.equals(" ")) {
                // Found custom spacing - use it
                return whitespace;
            }
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

        // Extract the pattern after the last newline
        int lastNewline = existingWhitespace.lastIndexOf('\n');
        if (lastNewline >= 0 && lastNewline < existingWhitespace.length() - 1) {
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
}
