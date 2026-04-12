/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

import java.util.Objects;

/**
 * Represents a single change detected between two XML documents.
 *
 * <p>Each change has a {@link ChangeType type}, an XPath-like {@link #path() path}
 * identifying the location, and optional before/after values and node references.</p>
 *
 * <h3>Example output:</h3>
 * <pre>
 * ELEMENT_ADDED: /project/dependencies/dependency[3]
 * TEXT_CHANGED: /project/version: "1.0" &rarr; "1.1"
 * ATTRIBUTE_CHANGED: /project/dependencies/dependency[2]/@scope: "compile" &rarr; "test"
 * </pre>
 *
 * @see ChangeType
 * @see DiffResult
 * @see XmlDiff
 * @since 1.3.0
 */
public class XmlChange {

    private final ChangeType type;
    private final String path;
    private final String beforeValue;
    private final String afterValue;
    private final Node beforeNode;
    private final Node afterNode;

    /**
     * Creates a new XmlChange.
     *
     * @param type the type of change
     * @param path the XPath-like path to the changed node
     * @param beforeValue the value before the change, or {@code null} for additions
     * @param afterValue the value after the change, or {@code null} for removals
     * @param beforeNode the node before the change, or {@code null} for additions
     * @param afterNode the node after the change, or {@code null} for removals
     */
    public XmlChange(
            ChangeType type, String path, String beforeValue, String afterValue, Node beforeNode, Node afterNode) {
        this.type = Objects.requireNonNull(type, "type");
        this.path = Objects.requireNonNull(path, "path");
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.beforeNode = beforeNode;
        this.afterNode = afterNode;
    }

    /**
     * Returns the type of change.
     *
     * @return the change type
     */
    public ChangeType type() {
        return type;
    }

    /**
     * Returns the XPath-like path to the changed node.
     *
     * @return the path
     */
    public String path() {
        return path;
    }

    /**
     * Returns {@code true} if the change affects the semantic meaning of the XML.
     *
     * @return {@code true} if semantic
     */
    public boolean isSemantic() {
        return type.isSemantic();
    }

    /**
     * Returns {@code true} if the change is formatting-only (no semantic effect).
     *
     * @return {@code true} if formatting-only
     */
    public boolean isFormattingOnly() {
        return type.isFormattingOnly();
    }

    /**
     * Returns the value before the change, or {@code null} for additions.
     *
     * @return the before value
     */
    public String beforeValue() {
        return beforeValue;
    }

    /**
     * Returns the value after the change, or {@code null} for removals.
     *
     * @return the after value
     */
    public String afterValue() {
        return afterValue;
    }

    /**
     * Returns the node before the change, or {@code null} for additions.
     *
     * @return the before node
     */
    public Node beforeNode() {
        return beforeNode;
    }

    /**
     * Returns the node after the change, or {@code null} for removals.
     *
     * @return the after node
     */
    public Node afterNode() {
        return afterNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(": ").append(path);
        if (beforeValue != null && afterValue != null) {
            sb.append(": \"")
                    .append(beforeValue)
                    .append("\" \u2192 \"")
                    .append(afterValue)
                    .append("\"");
        } else if (afterValue != null) {
            sb.append(": \"").append(afterValue).append("\"");
        } else if (beforeValue != null) {
            sb.append(": \"").append(beforeValue).append("\"");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XmlChange that = (XmlChange) o;
        return type == that.type
                && Objects.equals(path, that.path)
                && Objects.equals(beforeValue, that.beforeValue)
                && Objects.equals(afterValue, that.afterValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path, beforeValue, afterValue);
    }
}
