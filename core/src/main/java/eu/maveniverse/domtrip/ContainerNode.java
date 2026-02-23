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
 * Abstract base class for {@link Document} and {@link Element} nodes that can contain child nodes.
 */
public abstract class ContainerNode extends Node {

    protected List<Node> children;

    public ContainerNode() {
        super();
        this.children = new ArrayList<>();
    }

    // Child management methods

    /**
     * Returns a {@link Stream} of child nodes.
     *
     * @return a {@link Stream} of child nodes
     */
    public Stream<Node> children() {
        return children.stream();
    }

    /**
     * Adds a child node to this container.
     *
     * @param node the {@link Node} to add
     */
    public void addChild(Node node) {
        if (node != null) {
            // Remove from previous parent if it exists
            if (node.parent() != null) {
                node.parent().removeChild(node);
            }
            node.parent(this);
            children.add(node);
            // If this is an Element and it was self-closing, make it not self-closing
            if (this instanceof Element) {
                Element element = (Element) this;
                if (element.selfClosing()) {
                    element.selfClosingInternal(false);
                }
            }
            markModified();
        }
    }

    /**
     * Adds a child {@link Node} without marking as modified (for use during parsing).
     *
     * @param node the {@link Node} to add
     */
    void addChildInternal(Node node) {
        if (node != null) {
            node.parent(this);
            children.add(node);
            // Don't call markModified() here
        }
    }

    /**
     * Inserts a child {@link Node} at the specified index.
     *
     * @param index a zero based index at which to insert the specified {@link Node}
     * @param node the node to insert
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index > nodeCount()})
     * @throws IllegalArgumentException if node is null
     */
    public void insertChild(int index, Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        if (index < 0 || index > children.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + children.size());
        }
        // Remove from previous parent if it exists
        if (node.parent() != null) {
            node.parent().removeChild(node);
        }
        node.parent(this);
        children.add(index, node);
        // If this is an Element and it was self-closing, make it not self-closing
        if (this instanceof Element) {
            Element element = (Element) this;
            if (element.selfClosing()) {
                element.selfClosingInternal(false);
            }
        }
        markModified();
    }

    /**
     * Removes the given child {@link Node} from this {@link ContainerNode}.
     *
     * @param node the {@link Node} to remove
     * @return {@code true} if this {@link ContainerNode} contained the specified {@link Node} and {@code false} otherwise
     */
    public boolean removeChild(Node node) {
        if (children.remove(node)) {
            node.parent(null);
            markModified();
            return true;
        }
        return false;
    }

    /**
     * Gets the child at the specified index.
     *
     * @param index a zero based index of the child {@link Node} to return
     * @return the child node at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= nodeCount()})
     */
    public Node child(int index) {
        return children.get(index);
    }

    /** @deprecated Use {@link #child(int)} instead. */
    @Deprecated
    public Node getNode(int index) {
        return child(index);
    }

    /**
     * @return the number of child nodes
     */
    public int childCount() {
        return children.size();
    }

    /**
     * @return and {@link Optional} holding the first text node child or an empty {@link Optional} if there is no text child under this {@link ContainerNode}
     */
    public Optional<Text> findTextNode() {
        return children.stream()
                .filter(node -> node instanceof Text)
                .map(node -> (Text) node)
                .findFirst();
    }

    /**
     * @return the text content of this node (concatenates all text children).
     */
    public String textContent() {
        StringBuilder sb = new StringBuilder();
        for (Node node : children) {
            if (node instanceof Text) {
                Text textNode = (Text) node;
                sb.append(textNode.content());
            }
        }
        return sb.toString();
    }

    /**
     * @return {@code true} if this {@link ContainerNode} has any child {@link Element}s or {@code false} otherwise
     */
    public boolean hasChildElements() {
        return children.stream().anyMatch(node -> node instanceof Element);
    }

    /**
     * @return {@code true} if this {@link ContainerNode} has any child {@link Text} nodes or {@code false} otherwise
     */
    public boolean hasTextContent() {
        return children.stream().anyMatch(node -> node instanceof Text);
    }

    /**
     * @return {@code true} if this {@link ContainerNode} has no child nodes or {@code false} otherwise
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }

    /**
     * Removes all child nodes from this {@link ContainerNode}.
     */
    public void clearChildren() {
        for (Node node : children) {
            node.parent(null);
        }
        children.clear();
        markModified();
    }

    /** {@inheritDoc} */
    @Override
    public void clearModified() {
        super.clearModified();
        for (Node node : children) {
            node.clearModified();
        }
    }
}
