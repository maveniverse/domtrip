package eu.maveniverse.domtrip;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    protected List<Node> children;
    protected String precedingWhitespace; // Whitespace before this node
    protected String followingWhitespace; // Whitespace after this node
    protected boolean modified; // Flag to track if node has been modified
    
    public Node() {
        this.children = new ArrayList<>();
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

    public List<Node> getChildren() {
        return new ArrayList<>(children);
    }

    public void addChild(Node child) {
        if (child != null) {
            child.setParent(this);
            children.add(child);
            // If this is an Element and it was self-closing, make it not self-closing
            if (this instanceof Element element) {
                if (element.isSelfClosing()) {
                    element.setSelfClosingInternal(false);
                }
            }
            markModified();
        }
    }

    /**
     * Adds a child without marking as modified (for use during parsing)
     */
    void addChildInternal(Node child) {
        if (child != null) {
            child.setParent(this);
            children.add(child);
            // Don't call markModified() here
        }
    }

    public void insertChild(int index, Node child) {
        if (child != null && index >= 0 && index <= children.size()) {
            child.setParent(this);
            children.add(index, child);
            // If this is an Element and it was self-closing, make it not self-closing
            if (this instanceof Element element) {
                if (element.isSelfClosing()) {
                    element.setSelfClosingInternal(false);
                }
            }
            markModified();
        }
    }

    public boolean removeChild(Node child) {
        if (children.remove(child)) {
            child.setParent(null);
            markModified();
            return true;
        }
        return false;
    }

    public Node getChild(int index) {
        if (index >= 0 && index < children.size()) {
            return children.get(index);
        }
        return null;
    }
    
    public int getChildCount() {
        return children.size();
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
        for (Node child : children) {
            child.clearModified();
        }
    }



    /**
     * Finds the first child element with the given name (Optional-based).
     */
    public Optional<Element> findChild(String name) {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .filter(element -> name.equals(element.getName()))
                .findFirst();
    }

    /**
     * Finds all child elements with the given name (Stream-based).
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
        return descendants()
                .filter(element -> name.equals(element.getName()))
                .findFirst();
    }

    /**
     * Returns a stream of all descendant elements.
     */
    public Stream<Element> descendants() {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child)
                .flatMap(element -> Stream.concat(
                    Stream.of(element),
                    element.descendants()
                ));
    }

    /**
     * Returns a stream of direct child elements.
     */
    public Stream<Element> childElements() {
        return children.stream()
                .filter(child -> child instanceof Element)
                .map(child -> (Element) child);
    }

    /**
     * Returns a stream of all children.
     */
    public Stream<Node> childNodes() {
        return children.stream();
    }

    /**
     * Finds the first text node child.
     */
    public Optional<Text> findTextChild() {
        return children.stream()
                .filter(child -> child instanceof Text)
                .map(child -> (Text) child)
                .findFirst();
    }

    /**
     * Gets the text content of this node (concatenates all text children).
     */
    public String getTextContent() {
        StringBuilder sb = new StringBuilder();
        for (Node child : children) {
            if (child instanceof Text textNode) {
                sb.append(textNode.getContent());
            }
        }
        return sb.toString();
    }

    /**
     * Checks if this node has any child elements.
     */
    public boolean hasChildElements() {
        return children.stream().anyMatch(child -> child instanceof Element);
    }

    /**
     * Checks if this node has any text content.
     */
    public boolean hasTextContent() {
        return children.stream().anyMatch(child -> child instanceof Text);
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
