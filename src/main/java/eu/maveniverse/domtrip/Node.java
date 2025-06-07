package eu.maveniverse.domtrip;

/**
 * Base class for all XML nodes in the lossless XML tree.
 * Preserves formatting information including whitespace and position data.
 */
public abstract class Node {

    /**
     * Types of XML nodes supported by this implementation
     */
    public enum NodeType {
        ELEMENT,
        TEXT,
        COMMENT,
        DOCUMENT,
        PROCESSING_INSTRUCTION
    }

    protected Node parent;
    protected String precedingWhitespace; // Whitespace before this node
    protected String followingWhitespace; // Whitespace after this node
    protected boolean modified; // Flag to track if node has been modified

    public Node() {
        this.precedingWhitespace = "";
        this.followingWhitespace = "";
        this.modified = false;
    }

    /**
     * Returns the type of this XML node
     */
    public abstract NodeType getNodeType();

    /**
     * Serializes this node back to XML string
     */
    public abstract String toXml();

    /**
     * Serializes this node back to XML, appending to the provided StringBuilder
     */
    public abstract void toXml(StringBuilder sb);

    // Parent/Child relationship methods
    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    // Whitespace preservation methods
    public String getPrecedingWhitespace() {
        return precedingWhitespace;
    }

    public void setPrecedingWhitespace(String whitespace) {
        this.precedingWhitespace = whitespace != null ? whitespace : "";
    }

    public String getFollowingWhitespace() {
        return followingWhitespace;
    }

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
