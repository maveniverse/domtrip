package eu.maveniverse.domtrip;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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
 * // Create elements using factory methods
 * Element root = Element.of("root").attribute("id", "main");
 * Element textElement = Element.text("child", "Hello World");
 * Element selfClosing = Element.selfClosing("br");
 *
 * // Add child elements
 * root.addNode(textElement);
 * root.addNode(Element.text("version", "1.0.0"));
 *
 * // Navigate children
 * Optional<Element> found = root.child("child");
 * Stream<Element> children = root.children("item");
 *
 * // Namespace handling
 * QName soapEnvelope = QName.of("http://schemas.xmlsoap.org/soap/envelope/", "Envelope", "soap");
 * Element nsElement = Element.of(soapEnvelope);
 *
 * // Complex elements with fluent API
 * Element dependency = Element.of("dependency")
 *     .attribute("scope", "test");
 * dependency.addNode(Element.text("groupId", "junit"));
 * dependency.addNode(Element.text("artifactId", "junit"));
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
 * String value = element.attribute("class");         // Returns "updated"
 *
 * // For advanced formatting control, use attribute objects directly
 * element.attributeObject("class").value("manual");
 * }</pre>
 *
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
     */
    @Override
    public NodeType type() {
        return NodeType.ELEMENT;
    }

    /**
     * Gets the name (tag name) of this element.
     *
     * <p>For namespaced elements, this returns the full qualified name
     * including the prefix (e.g., "soap:Envelope"). Use {@link #localName()}
     * to get just the local part.</p>
     *
     * @return the element name
     * @see #localName()
     * @see #prefix()
     */
    public String name() {
        return name;
    }

    /**
     * Sets the element name.
     *
     * @param name the new element name
     * @return this element for method chaining
     */
    public Element name(String name) {
        this.name = name;
        markModified();
        return this;
    }

    // Attribute management

    /**
     * Gets the value of the specified attribute.
     *
     * @param name the attribute name
     * @return the attribute value, or null if the attribute doesn't exist
     */
    public String attribute(String name) {
        Attribute attr = attributes.get(name);
        return attr != null ? attr.value() : null;
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
     * element.attribute("attr1", "updated");
     * // Result:   <element attr1='updated' />  (preserves single quotes)
     *
     * element.attribute("attr2", "new");
     * // Result:   <element attr1='updated' attr2="new" />  (uses default double quotes)
     * }</pre>
     *
     * @param name the attribute name
     * @param value the attribute value
     * @return this element for method chaining
     * @see #attribute(String, String, char)
     * @see #attributeObject(String)
     */
    public Element attribute(String name, String value) {
        Attribute existingAttr = attributes.get(name);
        if (existingAttr != null) {
            // Preserve existing formatting by updating the existing attribute
            existingAttr.value(value);
        } else {
            // Create new attribute with default formatting
            attributes.put(name, new Attribute(name, value));
        }
        markModified();
        return this;
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
     * @return this element for method chaining
     * @see #attribute(String, String)
     */
    public Element attribute(String name, String value, char quoteChar) {
        Attribute existingAttr = attributes.get(name);
        if (existingAttr != null) {
            // Preserve existing whitespace but update quote style and value
            existingAttr.value(value);
            existingAttr.quoteStyle(QuoteStyle.fromChar(quoteChar));
        } else {
            // Create new attribute with specified quote character
            attributes.put(name, new Attribute(name, value, quoteChar, " "));
        }
        markModified();
        return this;
    }

    /**
     * Sets attribute without marking as modified (for use during parsing)
     */
    void attributeInternal(String name, String value, char quoteChar, String precedingWhitespace, String rawValue) {
        attributes.put(name, new Attribute(name, value, quoteChar, precedingWhitespace, rawValue));
        // Don't call markModified() here
    }

    /**
     * Removes the specified attribute from this element.
     *
     * @param name the name of the attribute to remove
     */
    public void removeAttribute(String name) {
        if (attributes.remove(name) != null) {
            markModified();
        }
    }

    /**
     * Gets all attributes as a map of names to values.
     *
     * @return a map containing all attribute names and their values
     */
    public Map<String, String> attributes() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            result.put(entry.getKey(), entry.getValue().value());
        }
        return result;
    }

    /**
     * Gets all attribute objects with their formatting information.
     *
     * @return a map of attribute names to Attribute objects
     */
    public Map<String, Attribute> attributeObjects() {
        return new LinkedHashMap<>(attributes);
    }

    /**
     * Checks if this element has the specified attribute.
     *
     * @param name the attribute name to check
     * @return true if the attribute exists, false otherwise
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Gets the Attribute object for the specified attribute name.
     *
     * @param name the attribute name
     * @return the Attribute object, or null if the attribute doesn't exist
     */
    public Attribute attributeObject(String name) {
        return attributes.get(name);
    }

    /**
     * Sets an attribute using an Attribute object.
     *
     * @param name the attribute name
     * @param attribute the Attribute object to set
     * @return this element for method chaining
     */
    public Element attributeObject(String name, Attribute attribute) {
        if (name != null && attribute != null) {
            attributes.put(name, attribute);
            markModified();
        }
        return this;
    }

    // Attribute formatting management

    /**
     * Sets the preceding whitespace for the specified attribute.
     *
     * @param attributeName the name of the attribute
     * @param whitespace the whitespace to set before the attribute
     * @return this element for method chaining
     */
    public Element attributeWhitespace(String attributeName, String whitespace) {
        Attribute attr = attributes.get(attributeName);
        if (attr != null) {
            attr.precedingWhitespace(whitespace);
        }
        return this;
    }

    /**
     * Gets the preceding whitespace for the specified attribute.
     *
     * @param attributeName the name of the attribute
     * @return the preceding whitespace, or a single space if not set
     */
    public String attributeWhitespace(String attributeName) {
        Attribute attr = attributes.get(attributeName);
        return attr != null ? attr.precedingWhitespace() : " ";
    }

    /**
     * Sets the quote character for the specified attribute.
     *
     * @param attributeName the name of the attribute
     * @param quoteChar the quote character to use (single or double quote)
     * @return this element for method chaining
     */
    public Element attributeQuote(String attributeName, char quoteChar) {
        Attribute attr = attributes.get(attributeName);
        if (attr != null) {
            attr.quoteStyle(QuoteStyle.fromChar(quoteChar));
        }
        return this;
    }

    /**
     * Gets the quote character for the specified attribute.
     *
     * @param attributeName the name of the attribute
     * @return the quote character, or double quote if not set
     */
    public char attributeQuote(String attributeName) {
        Attribute attr = attributes.get(attributeName);
        return attr != null ? attr.quoteStyle().getCharacter() : '"';
    }

    // Tag formatting

    /**
     * Gets the whitespace within the opening tag (before the closing &gt;).
     *
     * @return the whitespace within the opening tag
     */
    public String openTagWhitespace() {
        return openTagWhitespace;
    }

    /**
     * Sets the whitespace within the opening tag (before the closing &gt;).
     *
     * @param whitespace the whitespace to set
     * @return this element for method chaining
     */
    public Element openTagWhitespace(String whitespace) {
        this.openTagWhitespace = whitespace != null ? whitespace : "";
        return this;
    }

    /**
     * Gets the whitespace within the closing tag (before the element name).
     *
     * @return the whitespace within the closing tag
     */
    public String closeTagWhitespace() {
        return closeTagWhitespace;
    }

    /**
     * Sets the whitespace within the closing tag (before the element name).
     *
     * @param whitespace the whitespace to set
     * @return this element for method chaining
     */
    public Element closeTagWhitespace(String whitespace) {
        this.closeTagWhitespace = whitespace != null ? whitespace : "";
        return this;
    }

    /**
     * Checks if this element is self-closing.
     *
     * @return true if the element is self-closing, false otherwise
     */
    public boolean selfClosing() {
        return selfClosing;
    }

    /**
     * Sets whether this element should be self-closing.
     *
     * @param selfClosing true to make the element self-closing, false otherwise
     * @return this element for method chaining
     */
    public Element selfClosing(boolean selfClosing) {
        this.selfClosing = selfClosing;
        markModified();
        return this;
    }

    /**
     * Sets self-closing flag without marking as modified (for internal use)
     */
    void selfClosingInternal(boolean selfClosing) {
        this.selfClosing = selfClosing;
    }

    // Original tag preservation

    /**
     * Gets the original opening tag as it appeared in the source XML.
     *
     * @return the original opening tag string
     */
    public String originalOpenTag() {
        return originalOpenTag;
    }

    /**
     * Sets the original opening tag for formatting preservation.
     *
     * @param originalOpenTag the original opening tag string
     * @return this element for method chaining
     */
    public Element originalOpenTag(String originalOpenTag) {
        this.originalOpenTag = originalOpenTag != null ? originalOpenTag : "";
        return this;
    }

    /**
     * Gets the original closing tag as it appeared in the source XML.
     *
     * @return the original closing tag string
     */
    public String originalCloseTag() {
        return originalCloseTag;
    }

    /**
     * Sets the original closing tag for formatting preservation.
     *
     * @param originalCloseTag the original closing tag string
     * @return this element for method chaining
     */
    public Element originalCloseTag(String originalCloseTag) {
        this.originalCloseTag = originalCloseTag != null ? originalCloseTag : "";
        return this;
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
                for (Node child : nodes) {
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
            for (Node child : nodes) {
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
    public String textContent() {
        StringBuilder sb = new StringBuilder();
        for (Node child : nodes) {
            if (child instanceof Text) {
                sb.append(((Text) child).content());
            }
        }
        return sb.toString();
    }

    /**
     * Sets the text content, replacing all existing text children.
     *
     * <p><strong>Note:</strong> This method replaces all text children and does not
     * preserve existing whitespace patterns. For whitespace-preserving updates,
     * use {@link #textPreservingWhitespace(String)} instead.</p>
     *
     * @param content the new text content
     * @return this element for method chaining
     * @see #textPreservingWhitespace(String)
     * @see #textContent()
     */
    public Element textContent(String content) {
        // Remove all existing text children
        nodes.removeIf(child -> child instanceof Text);

        // Add new text content if not empty
        if (content != null && !content.isEmpty()) {
            Text textNode = new Text(content);
            addNode(textNode);
        }

        markModified();
        return this;
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
     * element.textPreservingWhitespace("new value");
     * // Result:   <item>   new value   </item>
     *
     * // Original: <item>old value</item>
     * element.textPreservingWhitespace("new value");
     * // Result:   <item>new value</item>
     * }</pre>
     *
     * @param content the new text content
     * @return this element for method chaining
     * @see #textContent(String)
     * @see #textContent()
     * @see #trimmedTextContent()
     */
    public Element textPreservingWhitespace(String content) {
        if (content == null) {
            content = "";
        }

        // Find existing text node to preserve its whitespace pattern
        Text existingText = getFirstTextChild();

        if (existingText != null) {
            // Use the Text node's whitespace-preserving method
            existingText.contentPreservingWhitespace(content);
        } else {
            // No existing text, just set normally
            textContent(content);
        }
        return this;
    }

    /**
     * Gets the text content with leading and trailing whitespace removed.
     *
     * <p>This is a convenience method that returns the trimmed text content
     * without modifying the original content. Useful for getting clean content
     * for processing while preserving the original formatting.</p>
     *
     * @return the text content with leading and trailing whitespace removed
     * @see #textContent()
     * @see #textPreservingWhitespace(String)
     */
    public String trimmedTextContent() {
        String content = textContent();
        return content != null ? content.trim() : "";
    }

    /**
     * Gets the first Text child node, if any.
     *
     * @return the first Text child, or null if none exists
     */
    private Text getFirstTextChild() {
        for (Node child : nodes) {
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
    public String localName() {
        String[] parts = NamespaceResolver.splitQualifiedName(name);
        return parts[1];
    }

    /**
     * Gets the namespace prefix of this element.
     * Returns null if the element has no prefix.
     */
    public String prefix() {
        String[] parts = NamespaceResolver.splitQualifiedName(name);
        return parts[0];
    }

    /**
     * Gets the qualified name of this element (prefix:localName or just localName).
     */
    public String qualifiedName() {
        return name;
    }

    /**
     * Gets the namespace URI of this element.
     * Returns null if the element is not in any namespace.
     */
    public String namespaceURI() {
        String prefix = prefix();
        return NamespaceResolver.resolveNamespaceURI(this, prefix);
    }

    /**
     * Checks if this element is in the specified namespace.
     */
    public boolean inNamespace(String namespaceURI) {
        String elementNamespaceURI = namespaceURI();
        return namespaceURI != null && namespaceURI.equals(elementNamespaceURI);
    }

    /**
     * Gets the namespace context for this element.
     * Includes all namespace declarations from this element and its ancestors.
     */
    public NamespaceContext namespaceContext() {
        return NamespaceResolver.buildNamespaceContext(this);
    }

    /**
     * Sets a namespace declaration attribute (xmlns or xmlns:prefix).
     *
     * @param prefix the namespace prefix, or null/empty for default namespace
     * @param namespaceURI the namespace URI
     * @return this element for method chaining
     */
    public Element namespaceDeclaration(String prefix, String namespaceURI) {
        if (prefix == null || prefix.isEmpty()) {
            attribute("xmlns", namespaceURI);
        } else {
            attribute("xmlns:" + prefix, namespaceURI);
        }
        return this;
    }

    /**
     * Gets a namespace declaration for the given prefix.
     * Returns null if no declaration is found on this element.
     */
    public String namespaceDeclaration(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return attribute("xmlns");
        } else {
            return attribute("xmlns:" + prefix);
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

    /**
     * Returns a stream of all descendant elements (depth-first traversal).
     */
    public Stream<Element> descendants() {
        return nodes.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .flatMap(element -> Stream.concat(Stream.of(element), element.descendants()));
    }

    // Enhanced navigation methods with QName support

    /**
     * Finds the first child element with the given QName.
     *
     * @param qname the QName to match
     * @return an Optional containing the first matching child element, or empty if none found
     */
    public Optional<Element> child(QName qname) {
        if (qname == null) {
            return Optional.empty();
        }
        return nodes.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> qname.matches(element.namespaceURI(), element.localName()))
                .findFirst();
    }

    /**
     * Finds the first child element with the given name.
     *
     * @param name the element name to match
     * @return an Optional containing the first matching child element, or empty if none found
     */
    public Optional<Element> child(String name) {
        return nodes.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> name.equals(element.name()))
                .findFirst();
    }

    /**
     * Finds all child elements with the given QName.
     *
     * @param qname the QName to match
     * @return a Stream of matching child elements
     */
    public Stream<Element> children(QName qname) {
        if (qname == null) {
            return Stream.empty();
        }
        return nodes.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> qname.matches(element.namespaceURI(), element.localName()));
    }

    /**
     * Finds all child elements with the given name.
     *
     * @param name the element name to match
     * @return a Stream of matching child elements
     */
    public Stream<Element> children(String name) {
        return nodes.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> name.equals(element.name()));
    }

    /**
     * Finds all child elements.
     *
     * @return a Stream of all child elements
     */
    public Stream<Element> children() {
        return nodes.stream().filter(child -> child instanceof Element).map(child -> (Element) child);
    }

    /**
     * Finds the first descendant element with the given QName.
     *
     * @param qname the QName to match
     * @return an Optional containing the first matching descendant element, or empty if none found
     */
    public Optional<Element> descendant(QName qname) {
        if (qname == null) {
            return Optional.empty();
        }
        return descendants()
                .filter(element -> qname.matches(element.namespaceURI(), element.localName()))
                .findFirst();
    }

    /**
     * Finds the first descendant element with the given name.
     *
     * @param name the element name to match
     * @return an Optional containing the first matching descendant element, or empty if none found
     */
    public Optional<Element> descendant(String name) {
        Objects.requireNonNull(name);
        return descendants().filter(element -> name.equals(element.name())).findFirst();
    }

    /**
     * Finds all descendant elements with the given QName.
     *
     * @param qname the QName to match
     * @return a Stream of matching descendant elements
     */
    public Stream<Element> descendants(QName qname) {
        if (qname == null) {
            return Stream.empty();
        }
        return descendants().filter(element -> qname.matches(element.namespaceURI(), element.localName()));
    }

    /**
     * Finds all descendant elements with the given name (convenience method).
     *
     * @param name the element name to match
     * @return a Stream of matching descendant elements
     */
    public Stream<Element> descendants(String name) {
        return descendants().filter(element -> name.equals(element.name()));
    }

    /**
     * Finds the first text child node.
     *
     * @return an Optional containing the first text child, or empty if none found
     */
    public Optional<Text> textChild() {
        return nodes.stream()
                .filter(child -> child instanceof Text)
                .map(child -> (Text) child)
                .findFirst();
    }

    /**
     * Creates a fluent query builder for finding elements.
     *
     * @return a new ElementQuery for fluent element searching
     */
    public ElementQuery query() {
        return new ElementQuery(this);
    }

    // Path-based navigation methods

    /**
     * Finds an element by following a path of element names from this element.
     *
     * <p>Example: {@code element.path("dependencies", "dependency")} will find
     * the first dependency element under this element's dependencies child.</p>
     *
     * @param path the path of element names to follow
     * @return an Optional containing the element at the end of the path, or empty if not found
     */
    public Optional<Element> path(String... path) {
        if (path == null || path.length == 0) {
            return Optional.of(this);
        }

        return Arrays.stream(path)
                .reduce(
                        Optional.of(this),
                        (current, elementName) -> current.flatMap(el -> el.child(elementName)),
                        (a, b) -> b);
    }

    /**
     * Finds an element by following a path of QNames from this element.
     *
     * @param path the path of QNames to follow
     * @return an Optional containing the element at the end of the path, or empty if not found
     */
    public Optional<Element> path(QName... path) {
        if (path == null || path.length == 0) {
            return Optional.of(this);
        }

        return Arrays.stream(path)
                .reduce(Optional.of(this), (current, qname) -> current.flatMap(el -> el.child(qname)), (a, b) -> b);
    }

    /**
     * Checks if a namespace is already declared in the element hierarchy.
     *
     * @param element the element to check from
     * @param qname the QName to check for namespace declaration
     * @return true if the namespace is already declared
     */
    private static boolean isNamespaceDeclared(Element element, QName qname) {
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

    @Override
    public String toString() {
        return "Element{name='" + name + "', attributes=" + attributes.size() + ", children=" + nodes.size() + "}";
    }

    // Factory methods for common element patterns

    /**
     * Creates a simple element.
     *
     * @param name the element name
     * @return a new Element
     */
    public static Element of(String name) {
        return new Element(name);
    }

    /**
     * Creates an element from a QName.
     *
     * @param qname the QName for the element
     * @return a new Element with namespace configuration
     * @throws IllegalArgumentException if qname is null
     */
    public static Element of(QName qname) {
        if (qname == null) {
            throw new IllegalArgumentException("QName cannot be null");
        }

        Element element = new Element(qname.qualifiedName());

        // Add namespace declaration if needed
        if (qname.hasNamespace()) {
            if (qname.hasPrefix()) {
                element.namespaceDeclaration(qname.prefix(), qname.namespaceURI());
            } else {
                element.namespaceDeclaration(null, qname.namespaceURI());
            }
        }

        return element;
    }

    /**
     * Creates a simple text element.
     *
     * <p>Creates an element with the specified name and text content.
     * This is a convenience method for creating elements that contain only text.</p>
     *
     * @param name the element name
     * @param content the text content to add
     * @return a new Element with text content
     */
    public static Element text(String name, String content) {
        Element element = new Element(name);
        if (content != null && !content.isEmpty()) {
            element.addNode(new Text(content));
        }
        return element;
    }

    /**
     * Creates a self-closing element.
     *
     * <p>Creates an element that will be serialized as a self-closing tag
     * (e.g., {@code <br/>} instead of {@code <br></br>}).</p>
     *
     * @param name the element name
     * @return a new self-closing Element
     */
    public static Element selfClosing(String name) {
        return new Element(name).selfClosing(true);
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
     */
    public static Element withAttributes(String name, Map<String, String> attributes) {
        Element element = new Element(name);
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                element.attribute(entry.getKey(), entry.getValue());
            }
        }
        return element;
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
     */
    public static Element withTextAndAttributes(String name, String content, Map<String, String> attributes) {
        Element element = new Element(name);
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                element.attribute(entry.getKey(), entry.getValue());
            }
        }
        if (content != null && !content.isEmpty()) {
            element.addNode(new Text(content));
        }
        return element;
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
     */
    public static Element cdata(String name, String content) {
        Element element = new Element(name);
        if (content != null) {
            element.addNode(new Text(content, true));
        }
        return element;
    }

    // Namespace factory methods

    /**
     * Creates an element from a QName with text content.
     *
     * @param qname the QName for the element
     * @param content the text content to add
     * @return a new Element with namespace configuration and text content
     * @throws IllegalArgumentException if qname is null
     */
    public static Element text(QName qname, String content) {
        Element element = of(qname);
        if (content != null && !content.isEmpty()) {
            element.addNode(new Text(content));
        }
        return element;
    }

    // Factory methods for other node types (for convenience)

    /**
     * Creates a comment node.
     *
     * <p>Creates a Comment node with the specified content. This is a convenience
     * method equivalent to {@link Comment#of(String)}.</p>
     *
     * @param content the comment content
     * @return a new Comment node
     * @see Comment#of(String)
     */
    public static Comment comment(String content) {
        return Comment.of(content);
    }

    /**
     * Creates a processing instruction.
     *
     * <p>Creates a ProcessingInstruction node with the specified target and data.
     * This is a convenience method equivalent to {@link ProcessingInstruction#of(String, String)}.</p>
     *
     * @param target the processing instruction target
     * @param data the processing instruction data
     * @return a new ProcessingInstruction node
     * @see ProcessingInstruction#of(String, String)
     */
    public static ProcessingInstruction processingInstruction(String target, String data) {
        return ProcessingInstruction.of(target, data);
    }
}
