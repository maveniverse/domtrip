package eu.maveniverse.domtrip;

import java.util.Map;

/**
 * Provides high-level editing operations for XML documents while preserving formatting.
 */
public class Editor {

    private Document document;
    private Parser parser;
    private Serializer serializer;
    private DomTripConfig config;
    private WhitespaceManager whitespaceManager;

    public Editor() {
        this(DomTripConfig.defaults());
    }

    public Editor(DomTripConfig config) {
        this.config = config != null ? config : DomTripConfig.defaults();
        this.parser = new Parser();
        this.serializer = new Serializer();
        this.whitespaceManager = new WhitespaceManager(this.config);
    }

    public Editor(String xml) throws ParseException {
        this();
        loadXml(xml);
    }

    public Editor(String xml, DomTripConfig config) throws ParseException {
        this(config);
        loadXml(xml);
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
            if (lastChild instanceof Text) {
                Text lastText = (Text) lastChild;
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
     * Adds or updates an attribute on an element
     */
    public void setAttribute(Element element, String name, String value) throws InvalidXmlException {
        if (element == null) {
            throw new InvalidXmlException("Element cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidXmlException("Attribute name cannot be null or empty");
        }

        element.setAttribute(name.trim(), value != null ? value : "");
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
     * Adds a comment as a child of the specified parent (backward compatibility)
     * @deprecated Use addComment(ContainerNode, String) instead
     */
    @Deprecated
    public Comment addComment(Node parent, String content) throws InvalidXmlException {
        if (parent == null) {
            throw new InvalidXmlException("Parent cannot be null");
        }
        if (!(parent instanceof ContainerNode)) {
            throw new InvalidXmlException("Parent must be a container node (Document or Element)");
        }
        return addComment((ContainerNode) parent, content);
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
     * Finds the first child element with the given name under the specified parent (backward compatibility)
     * @deprecated Use findChildElement(Element, String) instead or call parent.findChild(name) directly
     */
    @Deprecated
    public Element findChildElement(Node parent, String name) {
        if (parent instanceof Element element) {
            return element.findChild(name).orElse(null);
        }
        return null;
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
    public Element getRootElement() {
        return document != null ? document.getDocumentElement() : null;
    }

    /**
     * Infers the indentation pattern by examining existing children
     * @deprecated Use WhitespaceManager.inferIndentation() instead
     */
    @Deprecated
    private String inferIndentation(Node parent) {
        return whitespaceManager.inferIndentation(parent);
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
        if (element == null && getRootElement() != null) {
            element = addElement(getRootElement(), name);
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
     * Creates a fluent builder for adding nodes.
     */
    public NodeBuilder add() {
        return new NodeBuilder(this);
    }

    /**
     * Fluent builder for creating and adding nodes to the document.
     */
    public static class NodeBuilder {
        private final Editor editor;

        private NodeBuilder(Editor editor) {
            this.editor = editor;
        }

        /**
         * Creates an element builder.
         */
        public ElementBuilder element(String name) {
            return new ElementBuilder(editor, name);
        }

        /**
         * Creates a comment builder.
         */
        public CommentBuilder comment() {
            return new CommentBuilder(editor);
        }

        /**
         * Creates a text builder.
         */
        public TextBuilder text() {
            return new TextBuilder(editor);
        }
    }

    /**
     * Builder for creating and configuring elements.
     */
    public static class ElementBuilder {
        private final Editor editor;
        private final String name;
        private Node parent;
        private String textContent;
        private Map<String, String> attributes;
        private boolean selfClosing = false;

        private ElementBuilder(Editor editor, String name) {
            this.editor = editor;
            this.name = name;
        }

        /**
         * Sets the parent node for this element.
         */
        public ElementBuilder to(Node parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Sets text content for this element.
         */
        public ElementBuilder withText(String content) {
            this.textContent = content;
            return this;
        }

        /**
         * Adds an attribute to this element.
         */
        public ElementBuilder withAttribute(String name, String value) {
            if (this.attributes == null) {
                this.attributes = new java.util.HashMap<>();
            }
            this.attributes.put(name, value);
            return this;
        }

        /**
         * Adds multiple attributes to this element.
         */
        public ElementBuilder withAttributes(Map<String, String> attributes) {
            if (this.attributes == null) {
                this.attributes = new java.util.HashMap<>();
            }
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        /**
         * Makes this element self-closing.
         */
        public ElementBuilder selfClosing() {
            this.selfClosing = true;
            return this;
        }

        /**
         * Builds and adds the element to the document.
         */
        public Element build() throws InvalidXmlException {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            Element element;
            if (textContent != null && !textContent.isEmpty()) {
                element = editor.addElement((Element) parent, name, textContent);
            } else {
                element = editor.addElement((Element) parent, name);
            }

            if (attributes != null) {
                editor.setAttributes(element, attributes);
            }

            if (selfClosing) {
                element.setSelfClosing(true);
            }

            return element;
        }
    }

    /**
     * Builder for creating comments.
     */
    public static class CommentBuilder {
        private final Editor editor;
        private Node parent;
        private String content;

        private CommentBuilder(Editor editor) {
            this.editor = editor;
        }

        /**
         * Sets the parent node for this comment.
         */
        public CommentBuilder to(Node parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Sets the content of this comment.
         */
        public CommentBuilder withContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * Builds and adds the comment to the document.
         */
        public Comment build() throws InvalidXmlException {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }
            if (!(parent instanceof ContainerNode)) {
                throw new IllegalStateException("Parent must be a container node (Document or Element)");
            }

            return editor.addComment((ContainerNode) parent, content);
        }
    }

    /**
     * Builder for creating text nodes.
     */
    public static class TextBuilder {
        private final Editor editor;
        private Node parent;
        private String content;
        private boolean cdata = false;

        private TextBuilder(Editor editor) {
            this.editor = editor;
        }

        /**
         * Sets the parent node for this text.
         */
        public TextBuilder to(Node parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Sets the content of this text node.
         */
        public TextBuilder withContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * Makes this text node a CDATA section.
         */
        public TextBuilder asCData() {
            this.cdata = true;
            return this;
        }

        /**
         * Builds and adds the text node to the document.
         */
        public Text build() {
            if (parent == null) {
                throw new IllegalStateException("Parent node must be specified");
            }

            Text textNode = new Text(content != null ? content : "", cdata);
            if (parent instanceof ContainerNode container) {
                container.addChild(textNode);
            }
            return textNode;
        }
    }
}
