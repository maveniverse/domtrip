package eu.maveniverse.domtrip;

import java.awt.*;
import java.util.Map;

/**
 * High-level API for editing XML documents while preserving original formatting.
 *
 * <p>The Editor class provides a convenient, user-friendly interface for parsing,
 * modifying, and serializing XML documents. It combines the functionality of
 * {@link Parser}, and {@link Serializer} to offer
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
    private final DomTripConfig config;
    private Document document;
    private final String lineEnding;

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
        this.document = document; // Can be null for empty editors
        this.lineEnding = detectLineEnding(); // Detect from document or use config default
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

        // Use centralized node addition with automatic whitespace normalization
        insertChild(parent, newElement, parent.nodeCount());

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

        // Create element without automatic namespace declaration
        Element newElement = new Element(qname.qualifiedName());

        // Add namespace declaration if needed and not already declared
        if (qname.hasNamespace() && !isNamespaceDeclaredInHierarchy(parent, qname)) {
            if (qname.hasPrefix()) {
                newElement.namespaceDeclaration(qname.prefix(), qname.namespaceURI());
            } else {
                newElement.namespaceDeclaration(null, qname.namespaceURI());
            }
        }

        // Use centralized node addition with automatic whitespace normalization
        insertChild(parent, newElement, parent.nodeCount());

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
     * Removes an element from its parent with intelligent whitespace handling.
     *
     * <p>This method removes an element while preserving proper formatting by
     * intelligently handling surrounding whitespace text nodes:</p>
     * <ul>
     *   <li>If removing the first element and there's no line before, removes following blank lines</li>
     *   <li>If removing the last element and there's no line after, removes blank lines before</li>
     *   <li>If removing a middle element, removes any following blank lines</li>
     * </ul>
     *
     * @param element the element to remove
     * @return true if the element was successfully removed, false otherwise
     */
    public boolean removeElement(Element element) {
        if (element == null || element.parent() == null) {
            return false;
        }

        Node parent = element.parent();
        if (!(parent instanceof ContainerNode container)) {
            return false;
        }

        // Find the index of the element to remove
        int elementIndex = -1;
        for (int i = 0; i < container.nodes.size(); i++) {
            if (container.nodes.get(i) == element) {
                elementIndex = i;
                break;
            }
        }

        if (elementIndex == -1) {
            return false;
        }

        // Analyze surrounding whitespace and remove appropriately
        removeElementWithWhitespaceHandling(container, elementIndex);
        return true;
    }

    /**
     * Helper method to remove an element with intelligent whitespace handling.
     */
    private void removeElementWithWhitespaceHandling(ContainerNode container, int elementIndex) {
        Element element = (Element) container.nodes.get(elementIndex);

        // Determine position context
        boolean isFirst = isFirstNonWhitespaceElement(container, elementIndex);
        boolean isLast = isLastNonWhitespaceElement(container, elementIndex);

        if (isFirst && !isLast) {
            // First element: remove following whitespace
            removeElementAndFollowingWhitespace(container, elementIndex);
        } else if (isLast && !isFirst) {
            // Last element: remove preceding whitespace
            removeElementAndPrecedingWhitespace(container, elementIndex);
        } else if (!isFirst && !isLast) {
            // Middle element: remove following whitespace
            removeElementAndFollowingWhitespace(container, elementIndex);
        } else {
            // Only element or both first and last: remove element and clean up whitespace
            removeOnlyElementWithWhitespaceCleanup(container, elementIndex);
        }
    }

    /**
     * Checks if the element at the given index is the first non-whitespace element.
     */
    private boolean isFirstNonWhitespaceElement(ContainerNode container, int elementIndex) {
        for (int i = 0; i < elementIndex; i++) {
            Node node = container.nodes.get(i);
            if (node instanceof Element) {
                return false;
            }
            if (node instanceof Text text && !isWhitespaceOnly(text.content())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the element at the given index is the last non-whitespace element.
     */
    private boolean isLastNonWhitespaceElement(ContainerNode container, int elementIndex) {
        for (int i = elementIndex + 1; i < container.nodes.size(); i++) {
            Node node = container.nodes.get(i);
            if (node instanceof Element) {
                return false;
            }
            if (node instanceof Text text && !isWhitespaceOnly(text.content())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes an element and any following whitespace-only text nodes.
     * Also transfers the removed element's preceding whitespace to the next element.
     */
    private void removeElementAndFollowingWhitespace(ContainerNode container, int elementIndex) {
        Element element = (Element) container.nodes.get(elementIndex);

        // Capture the removed element's preceding whitespace to transfer to next element
        String precedingWhitespaceToTransfer = element.precedingWhitespace();

        container.removeNode(element);

        // Remove following whitespace-only text nodes
        while (elementIndex < container.nodes.size()) {
            Node nextNode = container.nodes.get(elementIndex);
            if (nextNode instanceof Text text && isWhitespaceOnly(text.content())) {
                container.removeNode(text);
                // Don't increment elementIndex since we removed a node
            } else {
                break;
            }
        }

        // Transfer the removed element's preceding whitespace to the next element
        if (!precedingWhitespaceToTransfer.isEmpty() && elementIndex < container.nodes.size()) {
            Node nextNode = container.nodes.get(elementIndex);
            if (nextNode instanceof Element nextElement) {
                // If the next element doesn't have preceding whitespace, give it the removed element's
                if (nextElement.precedingWhitespace().isEmpty()) {
                    nextElement.precedingWhitespaceInternal(precedingWhitespaceToTransfer);
                }
            }
        }
    }

    /**
     * Removes an element and any preceding whitespace-only text nodes.
     * Also transfers the removed element's following whitespace to the previous element.
     */
    private void removeElementAndPrecedingWhitespace(ContainerNode container, int elementIndex) {
        Element element = (Element) container.nodes.get(elementIndex);

        // Capture the removed element's following whitespace to transfer to previous element
        String followingWhitespaceToTransfer = element.followingWhitespace();

        // Remove preceding whitespace-only text nodes
        int removeFromIndex = elementIndex - 1;
        while (removeFromIndex >= 0) {
            Node prevNode = container.nodes.get(removeFromIndex);
            if (prevNode instanceof Text text && isWhitespaceOnly(text.content())) {
                removeFromIndex--;
            } else {
                break;
            }
        }

        // Find the previous element before removal to transfer whitespace to
        Element previousElement = null;
        for (int i = removeFromIndex; i >= 0; i--) {
            Node node = container.nodes.get(i);
            if (node instanceof Element elem) {
                previousElement = elem;
                break;
            }
        }

        // Remove nodes from removeFromIndex+1 to elementIndex (inclusive)
        for (int i = elementIndex; i > removeFromIndex; i--) {
            Node nodeToRemove = container.nodes.get(i);
            container.removeNode(nodeToRemove);
        }

        // Transfer the removed element's following whitespace to the previous element
        if (!followingWhitespaceToTransfer.isEmpty() && previousElement != null) {
            // If the previous element doesn't have following whitespace, give it the removed element's
            if (previousElement.followingWhitespace().isEmpty()) {
                previousElement.followingWhitespaceInternal(followingWhitespaceToTransfer);
            }
        }
    }

    /**
     * Removes the only element and cleans up surrounding whitespace appropriately.
     * When removing the only element, we want to preserve the parent's structure
     * but remove the element's indentation.
     */
    private void removeOnlyElementWithWhitespaceCleanup(ContainerNode container, int elementIndex) {
        Element element = (Element) container.nodes.get(elementIndex);

        // For the only element case, we want to preserve the parent's newline structure
        // but remove the element's indentation. The element's preceding and following
        // whitespace should be combined and preserved as a single text node if needed.

        String precedingWs = element.precedingWhitespace();
        String followingWs = element.followingWhitespace();

        // Remove the element
        container.removeNode(element);

        // If there was significant whitespace, preserve a minimal newline structure
        if (!precedingWs.isEmpty() || !followingWs.isEmpty()) {
            // Create a minimal whitespace text node to maintain structure
            String preservedWhitespace = lineEnding;
            Text whitespaceNode = new Text(preservedWhitespace);
            container.addNode(whitespaceNode);
        }

        // Remove any remaining whitespace-only text nodes that are now redundant
        for (int i = container.nodes.size() - 1; i >= 0; i--) {
            Node node = container.nodes.get(i);
            if (node instanceof Text text && isWhitespaceOnly(text.content())) {
                String content = text.content();
                // Remove if it's just spaces/tabs (not structural newlines)
                if (!content.contains(lineEnding) && content.trim().isEmpty()) {
                    container.removeNode(text);
                }
            }
        }
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

        // Try to preserve indentation by inferring from parent context
        String indentation = inferIndentation(parent);
        if (!indentation.isEmpty()) {
            comment.precedingWhitespace(lineEnding + indentation);
        }

        parent.addNode(comment);
        return comment;
    }

    // ========== ELEMENT COMMENTING METHODS ==========

    /**
     * Comments out an element by wrapping it in an XML comment.
     *
     * <p>This method replaces the element with a comment containing the element's XML representation.
     * The original element is preserved within the comment and can be restored using
     * {@link #uncommentElement(Comment)}.</p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Before: <dependency><groupId>junit</groupId></dependency>
     * editor.commentOutElement(dependencyElement);
     * // After: <!-- <dependency><groupId>junit</groupId></dependency> -->
     * }</pre>
     *
     * @param element the element to comment out
     * @return the comment that replaced the element
     * @throws DomTripException if the element cannot be commented out
     */
    public Comment commentOutElement(Element element) throws DomTripException {
        if (element == null) {
            throw new DomTripException("Element cannot be null");
        }

        ContainerNode parent = element.parent();
        if (parent == null || parent instanceof Document) {
            throw new DomTripException("Cannot comment out root element");
        }

        // Serialize the element to XML preserving its original formatting
        String elementXml = element.toXml().trim();

        // Create comment with the element's XML
        Comment comment = new Comment(" " + elementXml + " ");

        // Preserve the element's whitespace
        comment.precedingWhitespace(element.precedingWhitespace());
        comment.followingWhitespace(element.followingWhitespace());

        // Find the element's position and replace it
        int index = parent.nodes.indexOf(element);
        if (index >= 0) {
            parent.removeNode(element);
            parent.insertNode(index, comment);
        } else {
            throw new DomTripException("Element not found in parent");
        }

        return comment;
    }

    /**
     * Comments out multiple elements as a single block comment.
     *
     * <p>This method wraps multiple elements in a single XML comment block.
     * All elements must have the same parent. The elements are replaced with
     * a single comment containing all their XML representations.</p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Before: <dep1/><dep2/><dep3/>
     * editor.commentOutElements(dep1, dep2, dep3);
     * // After: <!-- <dep1/><dep2/><dep3/> -->
     * }</pre>
     *
     * @param elements the elements to comment out as a block
     * @return the comment that replaced the elements
     * @throws DomTripException if the elements cannot be commented out
     */
    public Comment commentOutElements(Element... elements) throws DomTripException {
        if (elements == null || elements.length == 0) {
            throw new DomTripException("At least one element must be provided");
        }

        // Validate all elements have the same parent
        ContainerNode parent = elements[0].parent();
        if (parent == null || parent instanceof Document) {
            throw new DomTripException("Cannot comment out root elements");
        }

        for (Element element : elements) {
            if (element.parent() != parent) {
                throw new DomTripException("All elements must have the same parent");
            }
        }

        // Build the comment content inline (no newlines)
        StringBuilder commentContent = new StringBuilder();
        commentContent.append(" ");

        // Find the range of elements to replace
        int firstIndex = Integer.MAX_VALUE;
        int lastIndex = -1;

        for (Element element : elements) {
            int index = parent.nodes.indexOf(element);
            if (index < 0) {
                throw new DomTripException("Element not found in parent");
            }
            firstIndex = Math.min(firstIndex, index);
            lastIndex = Math.max(lastIndex, index);
            // Serialize element preserving its original formatting but trim whitespace
            commentContent.append(element.toXml().trim());
        }

        commentContent.append(" ");

        // Create the comment
        Comment comment = new Comment(commentContent.toString());

        // Preserve whitespace from the first element
        comment.precedingWhitespace(elements[0].precedingWhitespace());

        // Capture the following whitespace from the last element to preserve it
        String lastElementFollowingWhitespace = elements[elements.length - 1].followingWhitespace();
        comment.followingWhitespace(""); // Comment itself has no following whitespace initially

        // Remove all elements in reverse order to maintain indices
        for (int i = lastIndex; i >= firstIndex; i--) {
            Node node = parent.getNode(i);
            if (node instanceof Element && java.util.Arrays.asList(elements).contains(node)) {
                parent.removeNode(node);
            }
        }

        // Insert the comment at the first position
        parent.insertNode(firstIndex, comment);

        // If the last element had following whitespace, preserve it as a separate text node
        // We preserve any whitespace that contains newlines, even if it's just whitespace
        if (lastElementFollowingWhitespace != null && !lastElementFollowingWhitespace.isEmpty()) {
            Text whitespaceNode = new Text(lastElementFollowingWhitespace);
            parent.insertNode(firstIndex + 1, whitespaceNode);

            // Ensure the next element (if any) has proper preceding whitespace
            if ((firstIndex + 2) < parent.nodeCount()) {
                Node nextNode = parent.getNode(firstIndex + 2);
                if (nextNode instanceof Element nextElement
                        && nextElement.precedingWhitespace().isEmpty()) {
                    nextElement.precedingWhitespaceInternal(lastElementFollowingWhitespace);
                }
            }
        }

        return comment;
    }

    /**
     * Uncomments a previously commented element by parsing the comment content back to XML.
     *
     * <p>This method attempts to parse the content of a comment as XML and replace the comment
     * with the parsed elements. This is the reverse operation of {@link #commentOutElement(Element)}
     * and {@link #commentOutElements(Element...)}.</p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Before: <!-- <dependency><groupId>junit</groupId></dependency> -->
     * Element restored = editor.uncommentElement(comment);
     * // After: <dependency><groupId>junit</groupId></dependency>
     * }</pre>
     *
     * @param comment the comment containing XML to uncomment
     * @return the first element that was restored, or null if no elements were found
     * @throws DomTripException if the comment content cannot be parsed as XML
     */
    public Element uncommentElement(Comment comment) throws DomTripException {
        if (comment == null) {
            throw new DomTripException("Comment cannot be null");
        }

        ContainerNode parent = comment.parent();
        if (parent == null) {
            throw new DomTripException("Comment has no parent");
        }

        String content = comment.content().trim();
        if (content.isEmpty()) {
            throw new DomTripException("Comment is empty");
        }

        try {
            // Parse the comment content as XML
            Document tempDoc = Document.of("<root>" + content + "</root>");
            Element tempRoot = tempDoc.root();

            if (tempRoot.nodeCount() == 0) {
                throw new DomTripException("No elements found in comment");
            }

            // Find the comment's position
            int index = parent.nodes.indexOf(comment);
            if (index < 0) {
                throw new DomTripException("Comment not found in parent");
            }

            // Remove the comment
            parent.removeNode(comment);

            // Insert the parsed elements
            Element firstElement = null;
            int insertIndex = index;

            for (Node node : tempRoot.nodes().toList()) {
                if (node instanceof Element element) {
                    // Preserve the comment's whitespace on the first element
                    if (firstElement == null) {
                        element.precedingWhitespace(comment.precedingWhitespace());
                        element.followingWhitespace(comment.followingWhitespace());
                        firstElement = element;
                    }

                    // Clone the element to avoid parent conflicts
                    Element clonedElement = cloneElement(element);
                    parent.insertNode(insertIndex++, clonedElement);
                }
            }

            return firstElement;

        } catch (Exception e) {
            throw new DomTripException("Failed to parse comment content as XML: " + e.getMessage(), e);
        }
    }

    // ========== ELEMENT POSITIONING METHODS ==========

    /**
     * Inserts a new element at the specified position within the parent.
     *
     * <p>This method provides precise control over element positioning by allowing
     * insertion at a specific index. The index is 0-based, where 0 inserts at the
     * beginning and parent.nodeCount() appends at the end.</p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Insert at position 1 (second position)
     * Element newElement = editor.insertElementAt(parent, 1, "newChild");
     * }</pre>
     *
     * @param parent the parent element to insert into
     * @param index the position to insert at (0-based)
     * @param elementName the name of the new element
     * @return the newly created element
     * @throws DomTripException if the insertion fails
     */
    public Element insertElementAt(Element parent, int index, String elementName) throws DomTripException {
        if (parent == null) {
            throw new DomTripException("Parent element cannot be null");
        }
        if (elementName == null || elementName.trim().isEmpty()) {
            throw new DomTripException("Element name cannot be null or empty");
        }
        if (index < 0 || index > parent.nodeCount()) {
            throw new DomTripException("Index out of bounds: " + index);
        }

        Element newElement = new Element(elementName.trim());

        // Use centralized node insertion with automatic whitespace normalization
        insertChild(parent, newElement, index);

        return newElement;
    }

    /**
     * Inserts a new element with text content at the specified position.
     *
     * @param parent the parent element to insert into
     * @param index the position to insert at (0-based)
     * @param elementName the name of the new element
     * @param textContent the text content for the element
     * @return the newly created element
     * @throws DomTripException if the insertion fails
     */
    public Element insertElementAt(Element parent, int index, String elementName, String textContent)
            throws DomTripException {
        Element element = insertElementAt(parent, index, elementName);
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }
        return element;
    }

    /**
     * Inserts a new element before the specified reference element.
     *
     * <p>This method creates a new element and inserts it immediately before
     * the reference element in the parent's child list. Both elements will
     * have the same parent after insertion.</p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Insert before existing element
     * Element newElement = editor.insertElementBefore(existingElement, "newChild");
     * }</pre>
     *
     * @param referenceElement the element to insert before
     * @param elementName the name of the new element
     * @return the newly created element
     * @throws DomTripException if the insertion fails
     */
    public Element insertElementBefore(Element referenceElement, String elementName) throws DomTripException {
        if (referenceElement == null) {
            throw new DomTripException("Reference element cannot be null");
        }
        if (elementName == null || elementName.trim().isEmpty()) {
            throw new DomTripException("Element name cannot be null or empty");
        }

        ContainerNode parent = referenceElement.parent();
        if (parent == null) {
            throw new DomTripException("Reference element has no parent");
        }

        int index = parent.nodes.indexOf(referenceElement);
        if (index < 0) {
            throw new DomTripException("Reference element not found in parent");
        }

        Element newElement = new Element(elementName.trim());

        // Use centralized node insertion with automatic whitespace normalization
        insertChild(parent, newElement, index);

        return newElement;
    }

    /**
     * Inserts a new element with text content before the specified reference element.
     *
     * @param referenceElement the element to insert before
     * @param elementName the name of the new element
     * @param textContent the text content for the element
     * @return the newly created element
     * @throws DomTripException if the insertion fails
     */
    public Element insertElementBefore(Element referenceElement, String elementName, String textContent)
            throws DomTripException {
        Element element = insertElementBefore(referenceElement, elementName);
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }
        return element;
    }

    /**
     * Inserts a new element after the specified reference element.
     *
     * <p>This method creates a new element and inserts it immediately after
     * the reference element in the parent's child list. Both elements will
     * have the same parent after insertion.</p>
     *
     * <h3>Example:</h3>
     * <pre>{@code
     * // Insert after existing element
     * Element newElement = editor.insertElementAfter(existingElement, "newChild");
     * }</pre>
     *
     * @param referenceElement the element to insert after
     * @param elementName the name of the new element
     * @return the newly created element
     * @throws DomTripException if the insertion fails
     */
    public Element insertElementAfter(Element referenceElement, String elementName) throws DomTripException {
        if (referenceElement == null) {
            throw new DomTripException("Reference element cannot be null");
        }
        if (elementName == null || elementName.trim().isEmpty()) {
            throw new DomTripException("Element name cannot be null or empty");
        }

        ContainerNode parent = referenceElement.parent();
        if (parent == null) {
            throw new DomTripException("Reference element has no parent");
        }

        int index = parent.nodes.indexOf(referenceElement);
        if (index < 0) {
            throw new DomTripException("Reference element not found in parent");
        }

        Element newElement = new Element(elementName.trim());

        // Use centralized node insertion with automatic whitespace normalization
        insertChild(parent, newElement, index + 1);

        return newElement;
    }

    /**
     * Inserts a new element with text content after the specified reference element.
     *
     * @param referenceElement the element to insert after
     * @param elementName the name of the new element
     * @param textContent the text content for the element
     * @return the newly created element
     * @throws DomTripException if the insertion fails
     */
    public Element insertElementAfter(Element referenceElement, String elementName, String textContent)
            throws DomTripException {
        Element element = insertElementAfter(referenceElement, elementName);
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }
        return element;
    }

    // ========== HELPER METHODS ==========

    /**
     * Detects the indentation unit (tab vs spaces) from existing indentation.
     *
     * @param indentation the existing indentation string
     * @return the detected indentation unit to use for one more level
     */
    private String detectIndentationUnit(String indentation) {
        if (indentation == null || indentation.isEmpty()) {
            return config.indentString();
        }

        // If the indentation contains tabs, use tabs
        if (indentation.contains("\t")) {
            return "\t";
        }

        // If it's all spaces, try to detect the unit size
        if (indentation.matches("^ +$")) {
            // Common indentation sizes: 2, 4, 8 spaces
            int length = indentation.length();
            if (length % 4 == 0) {
                return "    "; // 4 spaces
            } else if (length % 2 == 0) {
                return "  "; // 2 spaces
            } else if (length % 8 == 0) {
                return "        "; // 8 spaces
            } else {
                // Use the whole indentation as the unit
                return indentation;
            }
        }

        // Fallback to config default
        return config.indentString();
    }

    /**
     * Creates a deep clone of an element with all its attributes and children.
     *
     * @param element the element to clone
     * @return a new element that is a copy of the original
     */
    private Element cloneElement(Element element) {
        Element clone = new Element(element.name());

        // Copy attributes
        for (String attrName : element.attributes().keySet()) {
            clone.attribute(attrName, element.attribute(attrName));
        }

        // Copy children recursively
        for (Node child : element.nodes().toList()) {
            if (child instanceof Element childElement) {
                clone.addNode(cloneElement(childElement));
            } else if (child instanceof Text textNode) {
                clone.addNode(new Text(textNode.content()));
            } else if (child instanceof Comment commentNode) {
                clone.addNode(new Comment(commentNode.content()));
            }
            // Add other node types as needed
        }

        // Copy whitespace and other properties
        clone.precedingWhitespace(element.precedingWhitespace());
        clone.followingWhitespace(element.followingWhitespace());
        clone.selfClosing(element.selfClosing());

        return clone;
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
            String indentation = editor.inferIndentation(parent);
            if (!indentation.isEmpty()) {
                element.precedingWhitespace(editor.lineEnding + indentation);
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
            String indentation = editor.inferIndentation(parent);
            if (!indentation.isEmpty()) {
                comment.precedingWhitespace(editor.lineEnding + indentation);
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
                if (whitespace.contains(lineEnding)) {
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
        if (existingWhitespace == null || !existingWhitespace.contains(lineEnding)) {
            return " ";
        }

        // Extract the pattern after the last newline (including the newline)
        int lastNewline = existingWhitespace.lastIndexOf(lineEnding.charAt(lineEnding.length() - 1));
        if (lastNewline >= 0) {
            // Return from the newline onwards (including the newline)
            return existingWhitespace.substring(lastNewline);
        }

        // Fallback to newline + some spaces for alignment
        return lineEnding + "         "; // Reasonable default for attribute alignment
    }

    /**
     * Detects the line ending style used in the document.
     *
     * @return the detected line ending (\r\n, \n, or \r), or the config default if none detected
     */
    private String detectLineEnding() {
        if (document == null) {
            return config.lineEnding();
        }

        // Check various places in the document for line endings
        String detected = detectLineEndingInNode(document);
        return detected != null ? detected : config.lineEnding();
    }

    /**
     * Recursively searches for line endings in a node and its children.
     */
    private String detectLineEndingInNode(Node node) {
        // Check preceding whitespace
        String precedingWs = node.precedingWhitespace();
        if (precedingWs != null) {
            String detected = extractLineEnding(precedingWs);
            if (detected != null) {
                return detected;
            }
        }

        // Check following whitespace
        String followingWs = node.followingWhitespace();
        if (followingWs != null) {
            String detected = extractLineEnding(followingWs);
            if (detected != null) {
                return detected;
            }
        }

        // Check text content
        if (node instanceof Text text) {
            String detected = extractLineEnding(text.content());
            if (detected != null) {
                return detected;
            }
        }

        // Check children recursively
        if (node instanceof ContainerNode container) {
            for (Node child : container.nodes) {
                String detected = detectLineEndingInNode(child);
                if (detected != null) {
                    return detected;
                }
            }
        }

        return null;
    }

    /**
     * Extracts the line ending from a string.
     */
    private String extractLineEnding(String text) {
        if (text == null) {
            return null;
        }

        // Check for Windows line ending first (\r\n)
        if (text.contains("\r\n")) {
            return "\r\n";
        }

        // Check for Unix line ending (\n)
        if (text.contains("\n")) {
            return "\n";
        }

        // Check for old Mac line ending (\r)
        if (text.contains("\r")) {
            return "\r";
        }

        return null;
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

        // Start from the element itself and walk up the hierarchy
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

    // ========== NODE INSERTION METHODS ==========
    // Centralized node insertion with automatic whitespace normalization

    /**
     * Inserts a node at the specified index in the parent and normalizes whitespace.
     * This is the central method that all node insertions should go through.
     *
     * @param parent the parent container to insert into
     * @param newElement the node to insert
     * @param index the index at which to insert the node
     */
    private void insertChild(ContainerNode parent, Element newElement, int index) {
        index = normalizeWhitespaces(parent, index);
        int count = parent.nodeCount();
        String properIndentation = inferElementIndentation(parent);

        if (count == 0) {
            // Case 1: Inserting into empty parent
            // Set up proper indentation for the new element and closing whitespace for parent
            if (!properIndentation.isEmpty()) {
                newElement.precedingWhitespace(lineEnding + properIndentation);
                if (parent instanceof Element parentElement) {
                    String parentIndentation = getElementIndentation(parentElement);
                    newElement.followingWhitespace(lineEnding + parentIndentation);
                }
            }

        } else if (index == 0) {
            // Case 2: Inserting at the beginning
            // Transfer any existing preceding whitespace from the first element to the new element
            Node firstNode = parent.nodes.get(0);
            if (firstNode instanceof Element firstElement) {
                String existingPreceding = firstElement.precedingWhitespace();
                if (!existingPreceding.isEmpty()) {
                    newElement.precedingWhitespace(existingPreceding);
                    // Clear the first element's preceding whitespace to avoid duplication
                    firstElement.precedingWhitespace("");
                } else if (!properIndentation.isEmpty()) {
                    // No existing whitespace, set proper indentation
                    newElement.precedingWhitespace(lineEnding + properIndentation);
                }

                // Ensure the first element gets proper preceding whitespace for its new position
                if (!properIndentation.isEmpty()) {
                    firstElement.precedingWhitespace(lineEnding + properIndentation);
                    // Clear new element's following whitespace to avoid duplication
                    newElement.followingWhitespace("");
                }
            }

        } else if (index == count) {
            // Case 3: Inserting at the end
            // Set proper indentation for the new element
            if (!properIndentation.isEmpty()) {
                newElement.precedingWhitespace(lineEnding + properIndentation);
            }

            // Transfer any closing whitespace from the last element to the new element
            Node lastNode = parent.nodes.get(count - 1);
            if (lastNode instanceof Element lastElement) {
                String existingFollowing = lastElement.followingWhitespace();
                if (!existingFollowing.isEmpty()) {
                    newElement.followingWhitespace(existingFollowing);
                    // Clear the last element's following whitespace to avoid duplication
                    lastElement.followingWhitespace("");
                } else if (parent instanceof Element parentElement) {
                    // Set up proper closing whitespace
                    String parentIndentation = getElementIndentation(parentElement);
                    if (!parentIndentation.isEmpty()) {
                        newElement.followingWhitespace(lineEnding + parentIndentation);
                    }
                }

                // Clear previous element's following whitespace since we set new element's preceding
                if (!newElement.precedingWhitespace().isEmpty()) {
                    lastElement.followingWhitespace("");
                }
            }

        } else {
            // Case 4: Inserting in the middle
            // Normalize whitespace: either previous element has followingWhitespace OR new element has
            // precedingWhitespace

            Node prevNode = parent.nodes.get(index - 1);
            Node nextNode = parent.nodes.get(index);

            if (prevNode instanceof Element prevElement && nextNode instanceof Element nextElement) {
                // Check if there's existing whitespace between the elements
                String prevFollowing = prevElement.followingWhitespace();
                String nextPreceding = nextElement.precedingWhitespace();

                if (!prevFollowing.isEmpty()) {
                    // Previous element has following whitespace - transfer it to new element's preceding
                    newElement.precedingWhitespace(prevFollowing);
                    prevElement.followingWhitespace("");
                } else if (!nextPreceding.isEmpty()) {
                    // Next element has preceding whitespace - use it for new element
                    newElement.precedingWhitespace(nextPreceding);
                    nextElement.precedingWhitespace("");
                } else if (!properIndentation.isEmpty()) {
                    // No existing whitespace - set proper indentation
                    newElement.precedingWhitespace(lineEnding + properIndentation);
                }

                // Ensure the next element has proper preceding whitespace
                if (nextElement.precedingWhitespace().isEmpty() && !properIndentation.isEmpty()) {
                    nextElement.precedingWhitespace(lineEnding + properIndentation);
                    // Clear new element's following whitespace to avoid duplication
                    newElement.followingWhitespace("");
                }
            }
        }

        parent.insertNode(index, newElement);
    }

    /**
     * Normalizes whitespace by converting Text node whitespaces to element whitespace properties.
     * This gives us a cleaner state to work with before insertion.
     *
     * @param parent the parent element to normalize
     */
    private int normalizeWhitespaces(ContainerNode parent, int index) {
        if (!(parent instanceof Element parentElement)) {
            return index;
        }

        // Convert whitespace-only Text nodes to element whitespace properties
        for (int i = 0; i < parent.nodes.size(); i++) {
            Node node = parent.nodes.get(i);

            if (node instanceof Text textNode && isWhitespaceOnly(textNode.content())) {
                // This is a whitespace-only text node
                String whitespace = textNode.content();
                if (index > i) {
                    index--;
                }
                if (i == 0) {
                    // First node - this is inner following whitespace for the parent
                    parentElement.innerFollowingWhitespaceInternal(whitespace);
                    parent.removeNode(textNode);
                    i--; // Adjust index since we removed a node
                } else if (i == parent.nodes.size() - 1) {
                    // Last node - this is inner preceding whitespace for the parent
                    parentElement.innerPrecedingWhitespaceInternal(whitespace);
                    parent.removeNode(textNode);
                    i--; // Adjust index since we removed a node
                } else {
                    // Middle node - transfer to preceding whitespace of next element
                    Node nextNode = parent.nodes.get(i + 1);
                    if (nextNode instanceof Element nextElement) {
                        nextElement.precedingWhitespaceInternal(whitespace);
                        parent.removeNode(textNode);
                        i--; // Adjust index since we removed a node
                    }
                }
            }
        }
        return index;
    }

    /**
     * Infers the proper indentation for an element based on its context.
     */
    private String inferElementIndentation(ContainerNode container) {
        // First try to get indentation from sibling elements
        for (Node sibling : container.nodes) {
            if (sibling instanceof Element siblingElement) {
                String siblingIndent = getElementIndentation(siblingElement);
                if (!siblingIndent.isEmpty()) {
                    return siblingIndent;
                }
            }
        }

        // If no siblings, infer from parent context
        if (container instanceof Element parentElement) {
            String parentIndent = getElementIndentation(parentElement);
            if (!parentIndent.isEmpty()) {
                String indentUnit = detectIndentationUnit(parentIndent);
                return parentIndent + indentUnit;
            }
        }

        // Fallback to config default
        return config.indentString();
    }

    public void addBlankLineBefore(Element element) {
        int index = element.parent().nodes.indexOf(element);
        if (index > 0) {
            Node prev = element.parent().nodes.get(index - 1);
            prev.followingWhitespace(lineEnding + prev.followingWhitespace());
        } else if (element.parent() instanceof Element parentElement) {
            parentElement.innerFollowingWhitespace(lineEnding + element.followingWhitespace());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void addBlankLineAfter(Element element) {
        String currentFollowing = element.followingWhitespace();
        // Add an extra newline to create a blank line
        element.followingWhitespace(lineEnding + currentFollowing);
    }

    /**
     * Infers the indentation pattern from the context of a node.
     * This replaces the WhitespaceManager.inferIndentation method.
     */
    private String inferIndentation(Node context) {
        if (context == null) {
            return config.indentString();
        }

        // First, look for indentation patterns in the node's children
        if (context instanceof ContainerNode container) {
            for (Node child : container.nodes) {
                // Check preceding whitespace
                String precedingWs = child.precedingWhitespace();
                if (precedingWs != null && precedingWs.contains("\n")) {
                    // Extract indentation after the last newline
                    int lastNewline = precedingWs.lastIndexOf('\n');
                    if (lastNewline >= 0 && lastNewline < precedingWs.length() - 1) {
                        return precedingWs.substring(lastNewline + 1);
                    }
                }
            }
        }

        // If no children, look at siblings (for when context is a child node)
        Node parent = context.parent();
        if (parent instanceof ContainerNode parentContainer) {
            for (Node sibling : parentContainer.nodes) {
                String precedingWs = sibling.precedingWhitespace();
                if (precedingWs != null && precedingWs.contains("\n")) {
                    // Extract indentation after the last newline
                    int lastNewline = precedingWs.lastIndexOf('\n');
                    if (lastNewline >= 0 && lastNewline < precedingWs.length() - 1) {
                        return precedingWs.substring(lastNewline + 1);
                    }
                }
            }
        }

        // Fallback to configured indent string
        return config.indentString();
    }

    /**
     * Gets the indentation of an element by examining its preceding whitespace.
     */
    private String getElementIndentation(Element element) {
        String whitespace = element.precedingWhitespace();
        if (whitespace.isEmpty()) {
            return "";
        }

        int lastNewline = whitespace.lastIndexOf('\n');
        if (lastNewline >= 0) {
            return whitespace.substring(lastNewline + 1);
        }

        return "";
    }

    /**
     * Checks if the given content contains only whitespace.
     * This replaces the WhitespaceManager.isWhitespaceOnly method.
     */
    boolean isWhitespaceOnly(String content) {
        return content == null || content.trim().isEmpty();
    }
}
