package eu.maveniverse.domtrip;

import java.util.ArrayList;
import java.util.List;

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
            if (this instanceof Element) {
                Element element = (Element) this;
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
    public void addChildInternal(Node child) {
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
            if (this instanceof Element) {
                Element element = (Element) this;
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
     * Finds the first child element with the given name
     */
    public Element findChildElement(String name) {
        for (Node child : children) {
            if (child instanceof Element element) {
                if (name.equals(element.getName())) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Finds all child elements with the given name
     */
    public List<Element> findChildElements(String name) {
        List<Element> result = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof Element element) {
                if (name.equals(element.getName())) {
                    result.add(element);
                }
            }
        }
        return result;
    }
}
