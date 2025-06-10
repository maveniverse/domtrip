package eu.maveniverse.domtrip;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Abstract base class for XML nodes that can contain child nodes.
 * Only Document and Element nodes can have children in XML.
 */
public abstract class ContainerNode extends Node {

    protected List<Node> nodes;

    public ContainerNode() {
        super();
        this.nodes = new ArrayList<>();
    }

    // Child management methods

    /**
     * Returns a stream of child nodes.
     *
     * @return a Stream of child nodes
     */
    public Stream<Node> nodes() {
        return nodes.stream();
    }

    /**
     * Adds a child node to this container.
     */
    public void addChild(Node child) {
        if (child != null) {
            child.parent(this);
            nodes.add(child);
            // If this is an Element and it was self-closing, make it not self-closing
            if (this instanceof Element element) {
                if (element.selfClosing()) {
                    element.setSelfClosingInternal(false);
                }
            }
            markModified();
        }
    }

    /**
     * Adds a child without marking as modified (for use during parsing).
     */
    void addChildInternal(Node child) {
        if (child != null) {
            child.parent(this);
            nodes.add(child);
            // Don't call markModified() here
        }
    }

    /**
     * Inserts a child at the specified index.
     */
    public void insertChild(int index, Node child) {
        if (child != null && index >= 0 && index <= nodes.size()) {
            child.parent(this);
            nodes.add(index, child);
            // If this is an Element and it was self-closing, make it not self-closing
            if (this instanceof Element element) {
                if (element.selfClosing()) {
                    element.setSelfClosingInternal(false);
                }
            }
            markModified();
        }
    }

    /**
     * Removes a child node from this container.
     */
    public boolean removeChild(Node child) {
        if (nodes.remove(child)) {
            child.parent(null);
            markModified();
            return true;
        }
        return false;
    }

    /**
     * Gets the child at the specified index.
     */
    public Node getChild(int index) {
        if (index >= 0 && index < nodes.size()) {
            return nodes.get(index);
        }
        return null;
    }

    /**
     * Returns the number of child nodes.
     */
    public int nodeCount() {
        return nodes.size();
    }

    /**
     * Finds the first text node child.
     */
    public Optional<Text> findTextChild() {
        return nodes.stream()
                .filter(child -> child instanceof Text)
                .map(child -> (Text) child)
                .findFirst();
    }

    /**
     * Gets the text content of this node (concatenates all text children).
     */
    public String textContent() {
        StringBuilder sb = new StringBuilder();
        for (Node child : nodes) {
            if (child instanceof Text textNode) {
                sb.append(textNode.content());
            }
        }
        return sb.toString();
    }

    /**
     * Checks if this node has any child elements.
     */
    public boolean hasChildElements() {
        return nodes.stream().anyMatch(child -> child instanceof Element);
    }

    /**
     * Checks if this node has any text content.
     */
    public boolean hasTextContent() {
        return nodes.stream().anyMatch(child -> child instanceof Text);
    }

    /**
     * Returns a stream of all child elements.
     */
    public Stream<Element> childElements() {
        return nodes.stream().filter(child -> child instanceof Element).map(child -> (Element) child);
    }

    /**
     * Checks if this container is empty (has no children).
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Clears all children from this container.
     */
    public void clearChildren() {
        for (Node child : nodes) {
            child.parent(null);
        }
        nodes.clear();
        markModified();
    }

    @Override
    public void clearModified() {
        super.clearModified();
        for (Node child : nodes) {
            child.clearModified();
        }
    }
}
