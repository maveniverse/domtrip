package eu.maveniverse.domtrip;

/**
 * Base class for all XML nodes in the lossless XML tree, providing core
 * functionality for formatting preservation and tree navigation.
 *
 * <p>The Node class serves as the foundation for DomTrip's type-safe XML node
 * hierarchy. It provides essential functionality for maintaining parent-child
 * relationships, tracking modifications, and preserving whitespace formatting
 * during round-trip parsing and serialization.</p>
 *
 * <h3>Node Hierarchy:</h3>
 * <ul>
 *   <li><strong>Container Nodes</strong> - {@link Document} and {@link Element} can contain children</li>
 *   <li><strong>Leaf Nodes</strong> - {@link Text}, {@link Comment}, and {@link ProcessingInstruction} cannot contain children</li>
 * </ul>
 *
 * <h3>Core Functionality:</h3>
 * <ul>
 *   <li><strong>Whitespace Preservation</strong> - Maintains preceding and following whitespace</li>
 *   <li><strong>Modification Tracking</strong> - Tracks changes for selective formatting preservation</li>
 *   <li><strong>Parent-Child Relationships</strong> - Maintains bidirectional tree navigation</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Access node properties
 * NodeType type = node.getNodeType();
 * ContainerNode parent = node.parent();
 * Element parentElement = node.getParentElement();
 * Document document = node.document();
 * int depth = node.getDepth();
 *
 * // Check modification status
 * if (node.isModified()) {
 *     // Node has been changed since parsing
 * }
 *
 * // Serialize to XML
 * String xml = node.toXml();
 * }</pre>
 *
 * @see ContainerNode
 * @see Element
 * @see Text
 * @see Comment
 * @see ProcessingInstruction
 */
public abstract class Node {

    /**
     * Enumeration of XML node types supported by DomTrip.
     *
     * <p>Each node type corresponds to a specific XML construct and determines
     * the node's behavior and capabilities within the XML tree.</p>
     *
     */
    public enum NodeType {
        /** XML element nodes with attributes and potential children */
        ELEMENT,
        /** Text content nodes including CDATA sections */
        TEXT,
        /** XML comment nodes */
        COMMENT,
        /** Document root nodes */
        DOCUMENT,
        /** XML processing instruction nodes */
        PROCESSING_INSTRUCTION
    }

    /** The parent node of this node in the XML tree */
    protected ContainerNode parent;
    /** Whitespace that appears before this node in the original XML */
    protected String precedingWhitespace;
    /** Whitespace that appears after this node in the original XML */
    protected String followingWhitespace;
    /** Flag indicating whether this node has been modified since parsing */
    protected boolean modified;

    /**
     * Creates a new XML node with default settings.
     *
     * <p>Initializes the node with empty whitespace strings and sets the
     * modification flag to false.</p>
     *
     */
    public Node() {
        this.precedingWhitespace = "";
        this.followingWhitespace = "";
        this.modified = false;
    }

    /**
     * Returns the type of this XML node.
     *
     * <p>The node type determines the node's behavior and capabilities.
     * This method must be implemented by all concrete node classes.</p>
     *
     * @return the {@link NodeType} of this node
     */
    public abstract NodeType type();

    /**
     * Serializes this node to an XML string.
     *
     * <p>Creates a complete XML representation of this node and its children
     * (if any), preserving original formatting for unmodified content.</p>
     *
     * @return the XML string representation of this node
     * @see #toXml(StringBuilder)
     */
    public abstract String toXml();

    /**
     * Serializes this node to XML, appending to the provided StringBuilder.
     *
     * <p>This method is more efficient than {@link #toXml()} when building
     * larger XML documents as it avoids string concatenation overhead.</p>
     *
     * @param sb the StringBuilder to append the XML content to
     * @see #toXml()
     */
    public abstract void toXml(StringBuilder sb);

    /**
     * Gets the parent container node of this node.
     *
     * <p>Returns the parent container node in the XML tree, or null if this is the
     * root node or if the node has not been added to a tree. Only Document and
     * Element nodes can be parents since they are the only container nodes.</p>
     *
     * @return the parent container node, or null if this node has no parent
     * @see #parentElement()
     * @see #document()
     */
    public ContainerNode parent() {
        return parent;
    }

    /**
     * Sets the parent container node of this node.
     *
     * <p>This method is typically called automatically when adding nodes
     * to containers. Manual use should be done carefully to maintain
     * tree consistency.</p>
     *
     * @param parent the parent container node to set, or null to clear the parent
     * @return this node for method chaining
     * @see #parent()
     */
    public Node parent(ContainerNode parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Gets the whitespace that precedes this node in the original XML.
     *
     * <p>This includes any whitespace characters (spaces, tabs, newlines)
     * that appeared before this node in the source XML. Preserving this
     * whitespace enables lossless round-trip processing.</p>
     *
     * @return the preceding whitespace string, never null
     * @see #precedingWhitespace(String)
     */
    public String precedingWhitespace() {
        return precedingWhitespace;
    }

    /**
     * Sets the whitespace that precedes this node.
     *
     * <p>This method allows control over the whitespace formatting
     * before this node when serializing to XML.</p>
     *
     * @param whitespace the whitespace string to set, null is treated as empty string
     * @return this node for method chaining
     * @see #precedingWhitespace()
     */
    public Node precedingWhitespace(String whitespace) {
        this.precedingWhitespace = whitespace != null ? whitespace : "";
        markModified();
        return this;
    }

    /**
     * Sets preceding whitespace without marking as modified (for use during parsing)
     */
    void precedingWhitespaceInternal(String whitespace) {
        this.precedingWhitespace = whitespace != null ? whitespace : "";
    }

    /**
     * Gets the whitespace that follows this node in the original XML.
     *
     * <p>This includes any whitespace characters that appeared after this
     * node in the source XML, enabling preservation of original formatting.</p>
     *
     * @return the following whitespace string, never null
     * @see #followingWhitespace(String)
     */
    public String followingWhitespace() {
        return followingWhitespace;
    }

    /**
     * Sets the whitespace that follows this node.
     *
     * <p>This method allows control over the whitespace formatting
     * after this node when serializing to XML.</p>
     *
     * @param whitespace the whitespace string to set, null is treated as empty string
     * @return this node for method chaining
     * @see #followingWhitespace()
     */
    public Node followingWhitespace(String whitespace) {
        this.followingWhitespace = whitespace != null ? whitespace : "";
        markModified();
        return this;
    }

    /**
     * Sets following whitespace without marking as modified (for use during parsing)
     */
    void followingWhitespaceInternal(String whitespace) {
        this.followingWhitespace = whitespace != null ? whitespace : "";
    }

    // Modification tracking
    public boolean isModified() {
        return modified;
    }

    public void markModified() {
        this.modified = true;
        // Propagate modification flag up the tree
        if (parent != null) {
            parent.markModified();
        }
    }

    public void clearModified() {
        this.modified = false;
        // Child clearing is handled by ContainerNode subclasses
    }

    /**
     * Gets the Element parent of this node.
     *
     * <p>Returns the parent if it's an Element, or null if the parent is a Document
     * or if this node has no parent. Since parents can only be Element or Document,
     * no traversal is needed.</p>
     *
     * @return the Element parent, or null if parent is Document or no parent exists
     * @see #parent()
     * @see #document()
     */
    public Element parentElement() {
        if (parent instanceof Element) {
            return (Element) parent;
        }
        return null;
    }

    /**
     * Gets the Document that contains this node.
     *
     * <p>Recursively traverses up the tree to find the root Document node.
     * Every node in a properly constructed XML tree should have a Document
     * as its ultimate parent.</p>
     *
     * @return the Document containing this node, or null if not in a document tree
     * @see #parent()
     * @see #parentElement()
     */
    public Document document() {
        if (parent == null) {
            return null;
        }
        if (parent instanceof Document) {
            return (Document) parent;
        }
        // Parent must be an Element, so recurse
        return parent.document();
    }

    /**
     * Gets the depth of this node in the tree (root is 0).
     */
    public int depth() {
        int depth = 0;
        ContainerNode current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    /**
     * Checks if this node is a descendant of the given node.
     */
    public boolean isDescendantOf(Node ancestor) {
        ContainerNode current = this.parent;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }
}
