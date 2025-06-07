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
 * }</pre>
 *
 * <h3>Attribute Handling:</h3>
 * <p>Elements maintain attributes using {@link Attribute} objects that preserve
 * the original quote style, whitespace, and raw values:</p>
 * <pre>{@code
 * element.setAttribute("class", "important");           // Uses default quotes
 * element.setAttribute("style", "color: red", '\'');    // Uses single quotes
 * String value = element.getAttribute("class");         // Returns "important"
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

    public void setAttribute(String name, String value) {
        attributes.put(name, new Attribute(name, value));
        markModified();
    }

    public void setAttribute(String name, String value, char quoteChar) {
        attributes.put(name, new Attribute(name, value, quoteChar, " "));
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
     * Sets the text content, replacing all existing text children
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
}
