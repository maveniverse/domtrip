package eu.maveniverse.domtrip;

import java.util.Map;

/**
 * Factory class for creating common Element patterns.
 */
public class Elements {

    private Elements() {
        // Utility class
    }

    /**
     * Creates an element with text content.
     */
    public static Element textElement(String name, String content) {
        Element element = new Element(name);
        if (content != null && !content.isEmpty()) {
            Text textNode = new Text(content);
            element.addChild(textNode);
        }
        return element;
    }

    /**
     * Creates an empty element.
     */
    public static Element emptyElement(String name) {
        return new Element(name);
    }

    /**
     * Creates a self-closing element.
     */
    public static Element selfClosingElement(String name) {
        Element element = new Element(name);
        element.setSelfClosing(true);
        return element;
    }

    /**
     * Creates an element with attributes.
     */
    public static Element elementWithAttributes(String name, Map<String, String> attributes) {
        Element element = new Element(name);
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                element.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        return element;
    }

    /**
     * Creates an element with text content and attributes.
     */
    public static Element elementWithTextAndAttributes(String name, String content, Map<String, String> attributes) {
        Element element = elementWithAttributes(name, attributes);
        if (content != null && !content.isEmpty()) {
            Text textNode = new Text(content);
            element.addChild(textNode);
        }
        return element;
    }

    /**
     * Creates a CDATA element.
     */
    public static Element cdataElement(String name, String content) {
        Element element = new Element(name);
        if (content != null) {
            Text textNode = new Text(content, true); // true for CDATA
            element.addChild(textNode);
        }
        return element;
    }

    /**
     * Creates an element with a namespace prefix.
     */
    public static Element namespacedElement(String prefix, String localName) {
        return new Element(prefix + ":" + localName);
    }

    /**
     * Creates an element with a namespace prefix and URI attribute.
     */
    public static Element namespacedElement(String prefix, String localName, String namespaceUri) {
        Element element = namespacedElement(prefix, localName);
        element.setAttribute("xmlns:" + prefix, namespaceUri);
        return element;
    }

    /**
     * Creates an element in the specified namespace with no prefix (default namespace).
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
     * If the prefix is null or empty, creates an element with default namespace.
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
     */
    public static Element elementWithDefaultNamespace(String namespaceURI, String localName) {
        return elementInNamespace(namespaceURI, localName);
    }

    /**
     * Creates an element with namespace and text content.
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
     */
    public static Element textElementInNamespace(String namespaceURI, String localName, String content) {
        Element element = elementInNamespace(namespaceURI, localName);
        if (content != null && !content.isEmpty()) {
            element.addChild(new Text(content));
        }
        return element;
    }

    /**
     * Creates a comment element (actually returns a Comment node).
     */
    public static Comment comment(String content) {
        return new Comment(content != null ? content : "");
    }

    /**
     * Creates a processing instruction.
     */
    public static ProcessingInstruction processingInstruction(String target, String data) {
        return new ProcessingInstruction(target, data);
    }

    /**
     * Builder for creating complex element structures.
     */
    public static class Builder {
        private final Element element;

        private Builder(String name) {
            this.element = new Element(name);
        }

        public Builder withText(String content) {
            if (content != null && !content.isEmpty()) {
                element.addChild(new Text(content));
            }
            return this;
        }

        public Builder withCData(String content) {
            if (content != null) {
                element.addChild(new Text(content, true));
            }
            return this;
        }

        public Builder withAttribute(String name, String value) {
            element.setAttribute(name, value);
            return this;
        }

        public Builder withAttributes(Map<String, String> attributes) {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    element.setAttribute(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder withChild(Element child) {
            element.addChild(child);
            return this;
        }

        public Builder withComment(String comment) {
            element.addChild(new Comment(comment != null ? comment : ""));
            return this;
        }

        public Builder selfClosing() {
            element.setSelfClosing(true);
            return this;
        }

        public Builder withNamespace(String prefix, String namespaceURI) {
            element.setNamespaceDeclaration(prefix, namespaceURI);
            return this;
        }

        public Builder withDefaultNamespace(String namespaceURI) {
            element.setNamespaceDeclaration(null, namespaceURI);
            return this;
        }

        public Element build() {
            return element;
        }
    }

    /**
     * Creates a new element builder.
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }
}
