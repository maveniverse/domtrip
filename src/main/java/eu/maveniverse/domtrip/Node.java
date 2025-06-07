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
 * Node parent = node.getParent();
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
 * @author DomTrip Development Team
 * @since 1.0
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
     * @since 1.0
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
    protected Node parent;
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
     * @since 1.0
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
     * @since 1.0
     */
    public abstract NodeType getNodeType();

    /**
     * Serializes this node to an XML string.
     *
     * <p>Creates a complete XML representation of this node and its children
     * (if any), preserving original formatting for unmodified content.</p>
     *
     * @return the XML string representation of this node
     * @since 1.0
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
     * @since 1.0
     * @see #toXml()
     */
    public abstract void toXml(StringBuilder sb);

    /**
     * Gets the parent node of this node.
     *
     * <p>Returns the parent node in the XML tree, or null if this is the
     * root node or if the node has not been added to a tree.</p>
     *
     * @return the parent node, or null if this node has no parent
     * @since 1.0
     * @see #setParent(Node)
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the parent node of this node.
     *
     * <p>This method is typically called automatically when adding nodes
     * to containers. Manual use should be done carefully to maintain
     * tree consistency.</p>
     *
     * @param parent the parent node to set, or null to clear the parent
     * @since 1.0
     * @see #getParent()
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Gets the whitespace that precedes this node in the original XML.
     *
     * <p>This includes any whitespace characters (spaces, tabs, newlines)
     * that appeared before this node in the source XML. Preserving this
     * whitespace enables lossless round-trip processing.</p>
     *
     * @return the preceding whitespace string, never null
     * @since 1.0
     * @see #setPrecedingWhitespace(String)
     */
    public String getPrecedingWhitespace() {
        return precedingWhitespace;
    }

    /**
     * Sets the whitespace that precedes this node.
     *
     * <p>This method allows control over the whitespace formatting
     * before this node when serializing to XML.</p>
     *
     * @param whitespace the whitespace string to set, null is treated as empty string
     * @since 1.0
     * @see #getPrecedingWhitespace()
     */
    public void setPrecedingWhitespace(String whitespace) {
        this.precedingWhitespace = whitespace != null ? whitespace : "";
    }

    /**
     * Gets the whitespace that follows this node in the original XML.
     *
     * <p>This includes any whitespace characters that appeared after this
     * node in the source XML, enabling preservation of original formatting.</p>
     *
     * @return the following whitespace string, never null
     * @since 1.0
     * @see #setFollowingWhitespace(String)
     */
    public String getFollowingWhitespace() {
        return followingWhitespace;
    }

    /**
     * Sets the whitespace that follows this node.
     *
     * <p>This method allows control over the whitespace formatting
     * after this node when serializing to XML.</p>
     *
     * @param whitespace the whitespace string to set, null is treated as empty string
     * @since 1.0
     * @see #getFollowingWhitespace()
     */
    public void setFollowingWhitespace(String whitespace) {
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
     * Gets the depth of this node in the tree (root is 0).
     */
    public int getDepth() {
        int depth = 0;
        Node current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    /**
     * Gets the root node of the tree.
     */
    public Node getRoot() {
        Node current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * Checks if this node is a descendant of the given node.
     */
    public boolean isDescendantOf(Node ancestor) {
        Node current = this.parent;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }
}
