package eu.maveniverse.domtrip;

/**
 * Provides high-level editing operations for XML documents while preserving formatting.
 */
public class Editor {

    private Document document;
    private Parser parser;
    private Serializer serializer;

    public Editor() {
        this.parser = new Parser();
        this.serializer = new Serializer();
    }

    public Editor(String xml) {
        this();
        loadXml(xml);
    }
    
    /**
     * Loads XML content into the editor
     */
    public void loadXml(String xml) {
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
    public Element addElement(Element parent, String elementName) {
        if (parent == null || elementName == null || elementName.trim().isEmpty()) {
            throw new IllegalArgumentException("Parent element and element name cannot be null or empty");
        }

        Element newElement = new Element(elementName.trim());

        // Try to preserve indentation by looking at sibling elements
        String indentation = inferIndentation(parent);
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
                // If it's just whitespace (like "\n"), it's probably closing tag whitespace
                if (content.trim().isEmpty() && content.contains("\n")) {
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
            String parentIndent = inferIndentation(parent.getParent());
            Text closingWhitespace = new Text("\n" + parentIndent);
            parent.addChild(closingWhitespace);
        }

        return newElement;
    }
    
    /**
     * Adds a new element with text content
     */
    public Element addElement(Element parent, String elementName, String textContent) {
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
        
        return element.getParent().removeChild(element);
    }
    
    /**
     * Adds or updates an attribute on an element
     */
    public void setAttribute(Element element, String name, String value) {
        if (element == null || name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Element and attribute name cannot be null or empty");
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
    public void setTextContent(Element element, String content) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        
        element.setTextContent(content);
    }
    
    /**
     * Adds a comment as a child of the specified parent
     */
    public Comment addComment(Node parent, String content) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null");
        }

        Comment comment = new Comment(content != null ? content : "");
        
        // Try to preserve indentation
        String indentation = inferIndentation(parent);
        if (!indentation.isEmpty()) {
            comment.setPrecedingWhitespace("\n" + indentation);
        }
        
        parent.addChild(comment);
        return comment;
    }
    
    /**
     * Finds the first element with the given name in the document
     */
    public Element findElement(String name) {
        return document != null ? document.findElement(name) : null;
    }
    
    /**
     * Finds the first child element with the given name under the specified parent
     */
    public Element findChildElement(Node parent, String name) {
        return parent != null ? parent.findChildElement(name) : null;
    }
    
    /**
     * Creates a new XML document with the specified root element
     */
    public void createDocument(String rootElementName) {
        if (rootElementName == null || rootElementName.trim().isEmpty()) {
            throw new IllegalArgumentException("Root element name cannot be null or empty");
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
     */
    private String inferIndentation(Node parent) {
        if (parent == null || parent.getChildCount() == 0) {
            return "  "; // Default to 2 spaces
        }

        // Look for patterns in existing children's preceding whitespace
        for (Node child : parent.getChildren()) {
            String whitespace = child.getPrecedingWhitespace();
            if (whitespace.contains("\n")) {
                // Extract indentation after the last newline
                int lastNewline = whitespace.lastIndexOf('\n');
                if (lastNewline >= 0 && lastNewline < whitespace.length() - 1) {
                    return whitespace.substring(lastNewline + 1);
                }
            }

            // Also check text node content for whitespace patterns
            if (child instanceof Text) {
                Text textNode = (Text) child;
                String content = textNode.getContent();
                if (content.contains("\n")) {
                    // Extract indentation after the last newline
                    int lastNewline = content.lastIndexOf('\n');
                    if (lastNewline >= 0 && lastNewline < content.length() - 1) {
                        return content.substring(lastNewline + 1);
                    }
                }
            }
        }

        // If parent has indentation, add to it
        if (parent.getParent() != null) {
            String parentIndent = inferIndentation(parent.getParent());
            return parentIndent + "  "; // Add 2 more spaces
        }

        return "  "; // Default fallback
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
        
        return String.format("Document stats: %d elements, %d text nodes, %d comments, %d total nodes",
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
        
        for (Node child : node.getChildren()) {
            countNodes(child, counts);
        }
    }
}
