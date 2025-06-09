package eu.maveniverse.domtrip;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents an XML element with attributes and children, preserving original
 * formatting including attribute spacing, quote styles, and element structure.
 *
 * <p>The Element class is the core building block for XML documents, representing
 * XML elements with their attributes, child nodes, and namespace information.
 * It maintains lossless formatting preservation during round-trip parsing and
 * serialization operations.</p>
 *
 * <h3>Capabilities:</h3>
 * <ul>
 *   <li><strong>Attribute Management</strong> - Preserves attribute order, quote styles, and whitespace</li>
 *   <li><strong>Namespace Support</strong> - XML namespace handling with prefix resolution</li>
 *   <li><strong>Child Navigation</strong> - Methods for finding and manipulating child elements</li>
 *   <li><strong>Self-Closing Support</strong> - Handles self-closing tags</li>
 *   <li><strong>Formatting Preservation</strong> - Maintains original tag formatting and whitespace</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a new element
 * Element root = new Element("root");
 * root.setAttribute("id", "main");
 *
 * // Add child elements
 * Element child = new Element("child");
 * child.setTextContent("Hello World");
 * root.addChild(child);
 *
 * // Navigate children
 * Optional<Element> found = root.findChild("child");
 * Stream<Element> children = root.findChildren("item");
 *
 * // Namespace handling
 * Element nsElement = new Element("soap:Envelope");
 * nsElement.setNamespaceURI("http://schemas.xmlsoap.org/soap/envelope/");
 *
 * // Use builder pattern for complex elements
 * Element complex = Element.builder("dependency")
 *     .withAttribute("scope", "test")
 *     .withChild(Element.builder("groupId").withText("junit").build())
 *     .withChild(Element.builder("artifactId").withText("junit").build())
 *     .build();
 * }</pre>
 *
 * <h3>Attribute Handling:</h3>
 * <p>Elements maintain attributes using {@link Attribute} objects that preserve
 * the original quote style, whitespace, and raw values. The {@code setAttribute}
 * methods automatically preserve existing formatting when updating attributes:</p>
 * <pre>{@code
 * // Setting new attributes uses default formatting
 * element.setAttribute("class", "important");           // Uses default double quotes
 * element.setAttribute("style", "color: red", '\'');    // Uses single quotes
 *
 * // Updating existing attributes preserves original formatting
 * element.setAttribute("class", "updated");             // Preserves original quotes/whitespace
 * String value = element.getAttribute("class");         // Returns "updated"
 *
 * // For advanced formatting control, use attribute objects directly
 * element.getAttributeObject("class").setValue("manual");
 * }</pre>
 *
 * @author DomTrip Development Team
 * @since 1.0
 * @see ContainerNode
 * @see Attribute
 * @see NamespaceContext
 */
public class Element extends ContainerNode {

    private String name;
    private Map<String, Attribute> attributes;
    private String openTagWhitespace; // Whitespace within the opening tag
    private String closeTagWhitespace; // Whitespace within the closing tag
    private boolean selfClosing;
    private String originalOpenTag; // Original opening tag for reference
    private String originalCloseTag; // Original closing tag for reference

    /**
     * Creates a new XML element with the specified name.
     *
     * <p>Initializes the element with an empty attribute map, no whitespace,
     * and sets it as a non-self-closing element. The attribute order is
     * preserved using a LinkedHashMap.</p>
     *
     * @param name the element name (tag name)
     * @throws IllegalArgumentException if name is null or empty
     * @since 1.0
     */
    public Element(String name) {
        super();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Element name cannot be null or empty");
        }
        this.name = name.trim();
        this.attributes = new LinkedHashMap<>(); // Preserve attribute order
        this.openTagWhitespace = "";
        this.closeTagWhitespace = "";
        this.selfClosing = false;
        this.originalOpenTag = "";
        this.originalCloseTag = "";
    }

    /**
     * Returns the node type for this element.
     *
     * @return {@link NodeType#ELEMENT}
     * @since 1.0
     */
    @Override
    public NodeType getNodeType() {
        return NodeType.ELEMENT;
    }

    /**
     * Gets the name (tag name) of this element.
     *
     * <p>For namespaced elements, this returns the full qualified name
     * including the prefix (e.g., "soap:Envelope"). Use {@link #getLocalName()}
     * to get just the local part.</p>
     *
     * @return the element name
     * @since 1.0
     * @see #getLocalName()
     * @see #getPrefix()
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markModified();
    }

    // Attribute management
    public String getAttribute(String name) {
        Attribute attr = attributes.get(name);
        return attr != null ? attr.getValue() : null;
    }

    /**
     * Sets an attribute value, preserving existing formatting when the attribute already exists.
     *
     * <p>When setting an attribute that already exists, this method preserves the original
     * quote style, whitespace, and other formatting properties. For new attributes, it uses
     * default formatting (double quotes, single space preceding whitespace).</p>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * // Original: <element attr1='existing' />
     * element.setAttribute("attr1", "updated");
     * // Result:   <element attr1='updated' />  (preserves single quotes)
     *
     * element.setAttribute("attr2", "new");
     * // Result:   <element attr1='updated' attr2="new" />  (uses default double quotes)
     * }</pre>
     *
     * @param name the attribute name
     * @param value the attribute value
     * @since 1.0
     * @see #setAttribute(String, String, char)
     * @see #getAttributeObject(String)
     */
    public void setAttribute(String name, String value) {
        Attribute existingAttr = attributes.get(name);
        if (existingAttr != null) {
            // Preserve existing formatting by updating the existing attribute
            existingAttr.setValue(value);
        } else {
            // Create new attribute with default formatting
            attributes.put(name, new Attribute(name, value));
        }
        markModified();
    }

    /**
     * Sets an attribute value with a specific quote character.
     *
     * <p>When setting an attribute that already exists, this method preserves the original
     * preceding whitespace but uses the specified quote character. For new attributes, it uses
     * the specified quote character with default whitespace (single space).</p>
     *
     * @param name the attribute name
     * @param value the attribute value
     * @param quoteChar the quote character to use (' or ")
     * @since 1.0
     * @see #setAttribute(String, String)
     */
    public void setAttribute(String name, String value, char quoteChar) {
        Attribute existingAttr = attributes.get(name);
        if (existingAttr != null) {
            // Preserve existing whitespace but update quote style and value
            existingAttr.setValue(value);
            existingAttr.setQuoteChar(quoteChar);
        } else {
            // Create new attribute with specified quote character
            attributes.put(name, new Attribute(name, value, quoteChar, " "));
        }
        markModified();
    }

    /**
     * Sets attribute without marking as modified (for use during parsing)
     */
    void setAttributeInternal(String name, String value, char quoteChar, String precedingWhitespace, String rawValue) {
        attributes.put(name, new Attribute(name, value, quoteChar, precedingWhitespace, rawValue));
        // Don't call markModified() here
    }

    public void removeAttribute(String name) {
        if (attributes.remove(name) != null) {
            markModified();
        }
    }

    public Map<String, String> getAttributes() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getValue());
        }
        return result;
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public Attribute getAttributeObject(String name) {
        return attributes.get(name);
    }

    /**
     * Sets an attribute using an Attribute object.
     */
    public void setAttributeObject(String name, Attribute attribute) {
        if (name != null && attribute != null) {
            attributes.put(name, attribute);
            markModified();
        }
    }

    // Attribute formatting management (for backward compatibility)
    public void setAttributeWhitespace(String attributeName, String whitespace) {
        Attribute attr = attributes.get(attributeName);
        if (attr != null) {
            attr.setPrecedingWhitespace(whitespace);
        }
    }

    public String getAttributeWhitespace(String attributeName) {
        Attribute attr = attributes.get(attributeName);
        return attr != null ? attr.getPrecedingWhitespace() : " ";
    }

    public void setAttributeQuote(String attributeName, char quoteChar) {
        Attribute attr = attributes.get(attributeName);
        if (attr != null) {
            attr.setQuoteChar(quoteChar);
        }
    }

    public char getAttributeQuote(String attributeName) {
        Attribute attr = attributes.get(attributeName);
        return attr != null ? attr.getQuoteChar() : '"';
    }

    // Tag formatting
    public String getOpenTagWhitespace() {
        return openTagWhitespace;
    }

    public void setOpenTagWhitespace(String whitespace) {
        this.openTagWhitespace = whitespace != null ? whitespace : "";
    }

    public String getCloseTagWhitespace() {
        return closeTagWhitespace;
    }

    public void setCloseTagWhitespace(String whitespace) {
        this.closeTagWhitespace = whitespace != null ? whitespace : "";
    }

    public boolean isSelfClosing() {
        return selfClosing;
    }

    public void setSelfClosing(boolean selfClosing) {
        this.selfClosing = selfClosing;
        markModified();
    }

    /**
     * Sets self-closing flag without marking as modified (for internal use)
     */
    void setSelfClosingInternal(boolean selfClosing) {
        this.selfClosing = selfClosing;
    }

    // Original tag preservation
    public String getOriginalOpenTag() {
        return originalOpenTag;
    }

    public void setOriginalOpenTag(String originalOpenTag) {
        this.originalOpenTag = originalOpenTag != null ? originalOpenTag : "";
    }

    public String getOriginalCloseTag() {
        return originalCloseTag;
    }

    public void setOriginalCloseTag(String originalCloseTag) {
        this.originalCloseTag = originalCloseTag != null ? originalCloseTag : "";
    }

    @Override
    public String toXml() {
        StringBuilder sb = new StringBuilder();
        toXml(sb);
        return sb.toString();
    }

    @Override
    public void toXml(StringBuilder sb) {
        // If not modified and we have original formatting, use it
        if (!isModified() && !originalOpenTag.isEmpty()) {
            sb.append(precedingWhitespace);

            if (selfClosing) {
                sb.append(originalOpenTag);
            } else {
                // Extract opening tag from original
                int closeIndex = originalOpenTag.indexOf('>');
                if (closeIndex > 0) {
                    sb.append(originalOpenTag.substring(0, closeIndex + 1));
                } else {
                    sb.append(originalOpenTag);
                }

                // Add children
                for (Node child : children) {
                    child.toXml(sb);
                }

                // Add closing tag
                sb.append("</").append(name).append(">");
            }

            sb.append(followingWhitespace);
            return;
        }

        // Build tag from scratch
        sb.append(precedingWhitespace);
        sb.append("<").append(name);

        // Add attributes
        for (Attribute attr : attributes.values()) {
            attr.toXml(sb, !isModified());
        }

        if (selfClosing) {
            sb.append(openTagWhitespace).append("/>");
        } else {
            sb.append(openTagWhitespace).append(">");

            // Add children
            for (Node child : children) {
                child.toXml(sb);
            }

            // Add closing tag
            sb.append("</").append(closeTagWhitespace).append(name).append(">");
        }

        sb.append(followingWhitespace);
    }

    /**
     * Escapes special characters in attribute values
     */
    private String escapeAttributeValue(String value) {
        return escapeAttributeValue(value, '"');
    }

    /**
     * Escapes special characters in attribute values with specific quote character
     */
    private String escapeAttributeValue(String value, char quoteChar) {
        if (value == null) return "";
        String result = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Only escape the quote character that's being used
        if (quoteChar == '"') {
            result = result.replace("\"", "&quot;");
        } else if (quoteChar == '\'') {
            result = result.replace("'", "&apos;");
        }

        return result;
    }

    /**
     * Gets the text content of this element (concatenates all text children)
     */
    public String getTextContent() {
        StringBuilder sb = new StringBuilder();
        for (Node child : children) {
            if (child instanceof Text) {
                sb.append(((Text) child).getContent());
            }
        }
        return sb.toString();
    }

    /**
     * Sets the text content, replacing all existing text children.
     *
     * <p><strong>Note:</strong> This method replaces all text children and does not
     * preserve existing whitespace patterns. For whitespace-preserving updates,
     * use {@link #setTextContentPreservingWhitespace(String)} instead.</p>
     *
     * @param content the new text content
     * @see #setTextContentPreservingWhitespace(String)
     * @see #getTextContent()
     */
    public void setTextContent(String content) {
        // Remove all existing text children
        children.removeIf(child -> child instanceof Text);

        // Add new text content if not empty
        if (content != null && !content.isEmpty()) {
            Text textNode = new Text(content);
            addChild(textNode);
        }

        markModified();
    }

    /**
     * Sets the text content while preserving existing whitespace patterns.
     *
     * <p>This method attempts to preserve the whitespace structure of the existing
     * text content when updating to new content. If the element has existing text
     * with leading/trailing whitespace, the same pattern will be applied to the
     * new content.</p>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * // Original: <item>   old value   </item>
     * element.setTextContentPreservingWhitespace("new value");
     * // Result:   <item>   new value   </item>
     *
     * // Original: <item>old value</item>
     * element.setTextContentPreservingWhitespace("new value");
     * // Result:   <item>new value</item>
     * }</pre>
     *
     * @param content the new text content
     * @since 1.0
     * @see #setTextContent(String)
     * @see #getTextContent()
     * @see #getTrimmedTextContent()
     */
    public void setTextContentPreservingWhitespace(String content) {
        if (content == null) {
            content = "";
        }

        // Find existing text node to preserve its whitespace pattern
        Text existingText = getFirstTextChild();

        if (existingText != null) {
            // Use the Text node's whitespace-preserving method
            existingText.setContentPreservingWhitespace(content);
        } else {
            // No existing text, just set normally
            setTextContent(content);
        }
    }

    /**
     * Gets the text content with leading and trailing whitespace removed.
     *
     * <p>This is a convenience method that returns the trimmed text content
     * without modifying the original content. Useful for getting clean content
     * for processing while preserving the original formatting.</p>
     *
     * @return the text content with leading and trailing whitespace removed
     * @since 1.0
     * @see #getTextContent()
     * @see #setTextContentPreservingWhitespace(String)
     */
    public String getTrimmedTextContent() {
        String content = getTextContent();
        return content != null ? content.trim() : "";
    }

    /**
     * Gets the first Text child node, if any.
     *
     * @return the first Text child, or null if none exists
     */
    private Text getFirstTextChild() {
        for (Node child : children) {
            if (child instanceof Text) {
                return (Text) child;
            }
        }
        return null;
    }

    // Namespace-aware methods

    /**
     * Gets the local name part of this element (without namespace prefix).
     */
    public String getLocalName() {
        String[] parts = NamespaceResolver.splitQualifiedName(name);
        return parts[1];
    }

    /**
     * Gets the namespace prefix of this element.
     * Returns null if the element has no prefix.
     */
    public String getPrefix() {
        String[] parts = NamespaceResolver.splitQualifiedName(name);
        return parts[0];
    }

    /**
     * Gets the qualified name of this element (prefix:localName or just localName).
     */
    public String getQualifiedName() {
        return name;
    }

    /**
     * Gets the namespace URI of this element.
     * Returns null if the element is not in any namespace.
     */
    public String getNamespaceURI() {
        String prefix = getPrefix();
        return NamespaceResolver.resolveNamespaceURI(this, prefix);
    }

    /**
     * Checks if this element is in the specified namespace.
     */
    public boolean isInNamespace(String namespaceURI) {
        String elementNamespaceURI = getNamespaceURI();
        return namespaceURI != null && namespaceURI.equals(elementNamespaceURI);
    }

    /**
     * Gets the namespace context for this element.
     * Includes all namespace declarations from this element and its ancestors.
     */
    public NamespaceContext getNamespaceContext() {
        return NamespaceResolver.buildNamespaceContext(this);
    }

    /**
     * Finds the first child element with the given namespace URI and local name.
     */
    public Optional<Element> findChildByNamespace(String namespaceURI, String localName) {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> localName.equals(element.getLocalName())
                        && namespaceURI != null
                        && namespaceURI.equals(element.getNamespaceURI()))
                .findFirst();
    }

    /**
     * Finds all child elements with the given namespace URI and local name.
     */
    public Stream<Element> findChildrenByNamespace(String namespaceURI, String localName) {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> localName.equals(element.getLocalName())
                        && namespaceURI != null
                        && namespaceURI.equals(element.getNamespaceURI()));
    }

    /**
     * Finds all descendant elements with the given namespace URI and local name.
     */
    public Stream<Element> descendantsByNamespace(String namespaceURI, String localName) {
        return descendants()
                .filter(element -> localName.equals(element.getLocalName())
                        && namespaceURI != null
                        && namespaceURI.equals(element.getNamespaceURI()));
    }

    /**
     * Sets a namespace declaration attribute (xmlns or xmlns:prefix).
     */
    public void setNamespaceDeclaration(String prefix, String namespaceURI) {
        if (prefix == null || prefix.isEmpty()) {
            setAttribute("xmlns", namespaceURI);
        } else {
            setAttribute("xmlns:" + prefix, namespaceURI);
        }
    }

    /**
     * Gets a namespace declaration for the given prefix.
     * Returns null if no declaration is found on this element.
     */
    public String getNamespaceDeclaration(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAttribute("xmlns");
        } else {
            return getAttribute("xmlns:" + prefix);
        }
    }

    /**
     * Removes a namespace declaration.
     */
    public void removeNamespaceDeclaration(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            removeAttribute("xmlns");
        } else {
            removeAttribute("xmlns:" + prefix);
        }
    }

    // Element-specific navigation methods

    /**
     * Finds the first child element with the given name.
     */
    public Optional<Element> findChild(String name) {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> name.equals(element.getName()))
                .findFirst();
    }

    /**
     * Finds all child elements with the given name.
     */
    public Stream<Element> findChildren() {
        return children.stream().filter(child -> child instanceof Element).map(child -> (Element) child);
    }

    /**
     * Finds all child elements with the given name.
     */
    public Stream<Element> findChildren(String name) {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> name.equals(element.getName()));
    }

    /**
     * Finds the first descendant element with the given name.
     */
    public Optional<Element> findDescendant(String name) {
        return descendants().filter(element -> name.equals(element.getName())).findFirst();
    }

    /**
     * Returns a stream of all descendant elements (depth-first traversal).
     */
    public Stream<Element> descendants() {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .flatMap(element -> Stream.concat(Stream.of(element), element.descendants()));
    }

    @Override
    public String toString() {
        return "Element{name='" + name + "', attributes=" + attributes.size() + ", children=" + children.size() + "}";
    }

    /**
     * Builder for creating complex element structures with fluent API.
     *
     * <p>The Element.Builder provides a convenient way to construct XML elements
     * with attributes, child nodes, and namespace declarations using method chaining.</p>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Simple element with text content
     * Element version = Element.builder("version")
     *     .withText("1.0.0")
     *     .build();
     *
     * // Element with attributes and children
     * Element dependency = Element.builder("dependency")
     *     .withAttribute("scope", "test")
     *     .withAttribute("optional", "true")
     *     .withChild(Element.builder("groupId").withText("junit").build())
     *     .withChild(Element.builder("artifactId").withText("junit").build())
     *     .build();
     *
     * // Self-closing element
     * Element input = Element.builder("input")
     *     .withAttribute("type", "text")
     *     .withAttribute("name", "username")
     *     .selfClosing()
     *     .build();
     * }</pre>
     *
     * @since 1.0
     */
    public static class Builder {
        private final Element element;

        private Builder(String name) {
            this.element = new Element(name);
        }

        /**
         * Adds text content to the element.
         *
         * @param content the text content to add
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withText(String content) {
            if (content != null && !content.isEmpty()) {
                element.addChild(new Text(content));
            }
            return this;
        }

        /**
         * Adds CDATA content to the element.
         *
         * @param content the CDATA content to add
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withCData(String content) {
            if (content != null) {
                element.addChild(new Text(content, true));
            }
            return this;
        }

        /**
         * Adds an attribute to the element.
         *
         * @param name the attribute name
         * @param value the attribute value
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withAttribute(String name, String value) {
            element.setAttribute(name, value);
            return this;
        }

        /**
         * Adds multiple attributes to the element.
         *
         * @param attributes a map of attribute names to values
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withAttributes(Map<String, String> attributes) {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    element.setAttribute(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        /**
         * Adds a child element.
         *
         * @param child the child element to add
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withChild(Element child) {
            element.addChild(child);
            return this;
        }

        /**
         * Adds multiple child elements.
         *
         * @param children the child elements to add
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withChildren(java.util.List<Element> children) {
            if (children != null) {
                for (Element child : children) {
                    element.addChild(child);
                }
            }
            return this;
        }

        /**
         * Adds a comment as a child node.
         *
         * @param comment the comment text
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withComment(String comment) {
            element.addChild(new Comment(comment != null ? comment : ""));
            return this;
        }

        /**
         * Makes this element self-closing.
         *
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder selfClosing() {
            element.setSelfClosing(true);
            return this;
        }

        /**
         * Adds a namespace declaration to the element.
         *
         * @param prefix the namespace prefix (null for default namespace)
         * @param namespaceURI the namespace URI
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withNamespace(String prefix, String namespaceURI) {
            element.setNamespaceDeclaration(prefix, namespaceURI);
            return this;
        }

        /**
         * Sets the default namespace for the element.
         *
         * @param namespaceURI the default namespace URI
         * @return this builder for method chaining
         * @since 1.0
         */
        public Builder withDefaultNamespace(String namespaceURI) {
            element.setNamespaceDeclaration(null, namespaceURI);
            return this;
        }

        /**
         * Builds and returns the configured Element instance.
         *
         * @return the constructed Element
         * @since 1.0
         */
        public Element build() {
            return element;
        }

        /**
         * Builds the element and adds it to the specified parent using Editor's whitespace management.
         *
         * <p>This method integrates with the Editor's whitespace management to properly
         * format the element when adding it to the document tree.</p>
         *
         * @param editor the Editor instance for whitespace management
         * @param parent the parent container to add this element to
         * @return the constructed and added Element
         * @throws IllegalArgumentException if editor or parent is null
         * @throws IllegalStateException if parent is not a container node
         * @since 1.0
         */
        public Element buildAndAddTo(Editor editor, ContainerNode parent) {
            if (editor == null) {
                throw new IllegalArgumentException("Editor cannot be null");
            }
            if (parent == null) {
                throw new IllegalArgumentException("Parent cannot be null");
            }

            Element builtElement = build();

            // Use Editor's whitespace management
            String indentation = editor.getWhitespaceManager().inferIndentation(parent);
            if (!indentation.isEmpty()) {
                builtElement.setPrecedingWhitespace("\n" + indentation);
            }

            parent.addChild(builtElement);
            return builtElement;
        }
    }

    /**
     * Creates a new Element builder instance.
     *
     * @param name the element name
     * @return a new Element.Builder for fluent element construction
     * @throws IllegalArgumentException if name is null or empty
     * @since 1.0
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    // Factory methods for common element patterns

    /**
     * Creates an element with text content.
     *
     * <p>Creates an element with the specified name and adds a text node
     * containing the provided content.</p>
     *
     * @param name the element name
     * @param content the text content to add
     * @return a new Element with text content
     * @since 1.0
     */
    public static Element textElement(String name, String content) {
        return Element.builder(name).withText(content).build();
    }

    /**
     * Creates an empty element.
     *
     * <p>Creates an element with the specified name and no content or attributes.</p>
     *
     * @param name the element name
     * @return a new empty Element
     * @since 1.0
     */
    public static Element emptyElement(String name) {
        return new Element(name);
    }

    /**
     * Creates a self-closing element.
     *
     * <p>Creates an element that will be serialized as a self-closing tag
     * (e.g., {@code <br/>} instead of {@code <br></br>}).</p>
     *
     * @param name the element name
     * @return a new self-closing Element
     * @since 1.0
     */
    public static Element selfClosingElement(String name) {
        return Element.builder(name).selfClosing().build();
    }

    /**
     * Creates an element with attributes.
     *
     * <p>Creates an element with the specified name and adds all the
     * provided attributes.</p>
     *
     * @param name the element name
     * @param attributes a map of attribute names to values
     * @return a new Element with attributes
     * @since 1.0
     */
    public static Element elementWithAttributes(String name, Map<String, String> attributes) {
        return Element.builder(name).withAttributes(attributes).build();
    }

    /**
     * Creates an element with text content and attributes.
     *
     * <p>Creates an element with the specified name, adds all the provided
     * attributes, and includes the specified text content.</p>
     *
     * @param name the element name
     * @param content the text content to add
     * @param attributes a map of attribute names to values
     * @return a new Element with text content and attributes
     * @since 1.0
     */
    public static Element elementWithTextAndAttributes(String name, String content, Map<String, String> attributes) {
        return Element.builder(name)
                .withAttributes(attributes)
                .withText(content)
                .build();
    }

    /**
     * Creates a CDATA element.
     *
     * <p>Creates an element with the specified name and adds a CDATA section
     * containing the provided content. CDATA sections preserve content exactly
     * without XML entity escaping.</p>
     *
     * @param name the element name
     * @param content the CDATA content to add
     * @return a new Element with CDATA content
     * @since 1.0
     */
    public static Element cdataElement(String name, String content) {
        return Element.builder(name).withCData(content).build();
    }

    // Namespace factory methods

    /**
     * Creates an element with a namespace prefix.
     *
     * <p>Creates an element with a qualified name using the specified prefix
     * and local name (e.g., "soap:Envelope").</p>
     *
     * @param prefix the namespace prefix
     * @param localName the local element name
     * @return a new Element with namespace prefix
     * @since 1.0
     */
    public static Element namespacedElement(String prefix, String localName) {
        return new Element(prefix + ":" + localName);
    }

    /**
     * Creates an element with a namespace prefix and URI attribute.
     *
     * <p>Creates an element with a qualified name and automatically adds
     * the appropriate namespace declaration attribute.</p>
     *
     * @param prefix the namespace prefix
     * @param localName the local element name
     * @param namespaceUri the namespace URI
     * @return a new Element with namespace prefix and declaration
     * @since 1.0
     */
    public static Element namespacedElement(String prefix, String localName, String namespaceUri) {
        Element element = namespacedElement(prefix, localName);
        element.setAttribute("xmlns:" + prefix, namespaceUri);
        return element;
    }

    /**
     * Creates an element in the specified namespace with no prefix (default namespace).
     *
     * <p>Creates an element in the specified namespace using the default namespace
     * declaration (xmlns attribute).</p>
     *
     * @param namespaceURI the namespace URI
     * @param localName the local element name
     * @return a new Element in the default namespace
     * @since 1.0
     */
    public static Element elementInNamespace(String namespaceURI, String localName) {
        Element element = new Element(localName);
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            element.setAttribute("xmlns", namespaceURI);
        }
        return element;
    }

    /**
     * Creates an element with the specified namespace URI and preferred prefix.
     *
     * <p>If the prefix is null or empty, creates an element with default namespace.
     * Otherwise, creates an element with the specified prefix and namespace declaration.</p>
     *
     * @param namespaceURI the namespace URI
     * @param localName the local element name
     * @param preferredPrefix the preferred namespace prefix, or null for default namespace
     * @return a new Element with appropriate namespace handling
     * @since 1.0
     */
    public static Element elementWithNamespace(String namespaceURI, String localName, String preferredPrefix) {
        if (preferredPrefix == null || preferredPrefix.isEmpty()) {
            return elementInNamespace(namespaceURI, localName);
        } else {
            return namespacedElement(preferredPrefix, localName, namespaceURI);
        }
    }

    /**
     * Creates an element with default namespace declaration.
     *
     * <p>This is an alias for {@link #elementInNamespace(String, String)} for clarity.</p>
     *
     * @param namespaceURI the namespace URI
     * @param localName the local element name
     * @return a new Element with default namespace
     * @since 1.0
     */
    public static Element elementWithDefaultNamespace(String namespaceURI, String localName) {
        return elementInNamespace(namespaceURI, localName);
    }

    /**
     * Creates an element with namespace and text content.
     *
     * <p>Creates a namespaced element with the specified prefix, namespace URI,
     * and text content.</p>
     *
     * @param prefix the namespace prefix
     * @param localName the local element name
     * @param namespaceURI the namespace URI
     * @param content the text content to add
     * @return a new Element with namespace and text content
     * @since 1.0
     */
    public static Element namespacedTextElement(String prefix, String localName, String namespaceURI, String content) {
        Element element = namespacedElement(prefix, localName, namespaceURI);
        if (content != null && !content.isEmpty()) {
            element.addChild(new Text(content));
        }
        return element;
    }

    /**
     * Creates an element in default namespace with text content.
     *
     * <p>Creates an element in the specified default namespace with text content.</p>
     *
     * @param namespaceURI the namespace URI
     * @param localName the local element name
     * @param content the text content to add
     * @return a new Element in default namespace with text content
     * @since 1.0
     */
    public static Element textElementInNamespace(String namespaceURI, String localName, String content) {
        Element element = elementInNamespace(namespaceURI, localName);
        if (content != null && !content.isEmpty()) {
            element.addChild(new Text(content));
        }
        return element;
    }

    // Factory methods for other node types (for convenience)

    /**
     * Creates a comment node.
     *
     * <p>Creates a Comment node with the specified content. This is a convenience
     * method equivalent to {@link Comment#builder()}.</p>
     *
     * @param content the comment content
     * @return a new Comment node
     * @since 1.0
     * @see Comment#builder()
     */
    public static Comment comment(String content) {
        return Comment.builder().withContent(content).build();
    }

    /**
     * Creates a processing instruction.
     *
     * <p>Creates a ProcessingInstruction node with the specified target and data.
     * This is a convenience method equivalent to {@link ProcessingInstruction#builder()}.</p>
     *
     * @param target the processing instruction target
     * @param data the processing instruction data
     * @return a new ProcessingInstruction node
     * @since 1.0
     * @see ProcessingInstruction#builder()
     */
    public static ProcessingInstruction processingInstruction(String target, String data) {
        return ProcessingInstruction.builder().withTarget(target).withData(data).build();
    }
}
