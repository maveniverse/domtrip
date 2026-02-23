/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
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
    public void addNode(Node node) {
        if (node != null) {
            // Remove from previous parent if it exists
            if (node.parent() != null) {
                node.parent().removeNode(node);
            }
            node.parent(this);
            nodes.add(node);
            // If this is an Element and it was self-closing, make it not self-closing
            if (this instanceof Element element) {
                if (element.selfClosing()) {
                    element.selfClosingInternal(false);
                }
            }
            markModified();
        }
    }

    /**
     * Adds a child without marking as modified (for use during parsing).
     */
    void addNodeInternal(Node node) {
        if (node != null) {
            node.parent(this);
            nodes.add(node);
            // Don't call markModified() here
        }
    }

    /**
     * Inserts a child at the specified index.
     *
     * @param index the position at which to insert the node
     * @param node the node to insert
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index > nodeCount()})
     * @throws IllegalArgumentException if node is null
     */
    public void insertNode(int index, Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        if (index < 0 || index > nodes.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + nodes.size());
        }
        // Remove from previous parent if it exists
        if (node.parent() != null) {
            node.parent().removeNode(node);
        }
        node.parent(this);
        nodes.add(index, node);
        // If this is an Element and it was self-closing, make it not self-closing
        if (this instanceof Element element) {
            if (element.selfClosing()) {
                element.selfClosingInternal(false);
            }
        }
        markModified();
    }

    /**
     * Removes a child node from this container.
     */
    public boolean removeNode(Node node) {
        if (nodes.remove(node)) {
            node.parent(null);
            markModified();
            return true;
        }
        return false;
    }

    /**
     * Gets the child at the specified index.
     *
     * @param index the index of the child node to return
     * @return the child node at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= nodeCount()})
     */
    public Node node(int index) {
        return nodes.get(index);
    }

    /** @deprecated Use {@link #node(int)} instead. */
    @Deprecated
    public Node getNode(int index) {
        return node(index);
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
    public Optional<Text> findTextNode() {
        return nodes.stream()
                .filter(node -> node instanceof Text)
                .map(node -> (Text) node)
                .findFirst();
    }

    /**
     * Gets the text content of this node (concatenates all text children).
     */
    public String textContent() {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            if (node instanceof Text textNode) {
                sb.append(textNode.content());
            }
        }
        return sb.toString();
    }

    /**
     * Checks if this node has any child elements.
     */
    public boolean hasNodeElements() {
        return nodes.stream().anyMatch(node -> node instanceof Element);
    }

    /**
     * Checks if this node has any text content.
     */
    public boolean hasTextContent() {
        return nodes.stream().anyMatch(node -> node instanceof Text);
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
    public void clearNodes() {
        for (Node node : nodes) {
            node.parent(null);
        }
        nodes.clear();
        markModified();
    }

    @Override
    public void clearModified() {
        super.clearModified();
        for (Node node : nodes) {
            node.clearModified();
        }
    }
}
