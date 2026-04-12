/*
 * Copyright (c) 2023-2026 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip;

/**
 * Enumerates the types of changes that can be detected between two XML documents.
 *
 * <p>Change types are classified as either <em>semantic</em> (affecting the meaning
 * of the XML) or <em>formatting-only</em> (affecting only the presentation). This
 * classification enables filtering to focus on meaningful changes while ignoring
 * formatting noise.</p>
 *
 * @see XmlChange
 * @see XmlDiff
 * @since 1.3.0
 */
public enum ChangeType {

    /** A new element was inserted. */
    ELEMENT_ADDED,

    /** An element was deleted. */
    ELEMENT_REMOVED,

    /** An element was reordered among its siblings (same content, different position). */
    ELEMENT_MOVED,

    /** Text content of an element was modified. */
    TEXT_CHANGED,

    /** A new attribute was added to an existing element. */
    ATTRIBUTE_ADDED,

    /** An attribute was removed from an element. */
    ATTRIBUTE_REMOVED,

    /** An attribute value was modified. */
    ATTRIBUTE_CHANGED,

    /** A comment was inserted. */
    COMMENT_ADDED,

    /** A comment was deleted. */
    COMMENT_REMOVED,

    /** Comment content was modified. */
    COMMENT_CHANGED,

    /** A namespace declaration was modified. */
    NAMESPACE_CHANGED,

    /** Indentation or spacing changed (formatting only, no semantic effect). */
    WHITESPACE_CHANGED,

    /** Attribute quote style changed between single and double (formatting only). */
    QUOTE_STYLE_CHANGED,

    /** Entity encoding form changed, e.g. {@code &lt;} vs {@code &#60;} (formatting only). */
    ENTITY_FORM_CHANGED,

    /** Empty element style changed between self-closing and expanded (formatting only). */
    EMPTY_ELEMENT_STYLE_CHANGED;

    /**
     * Returns whether this change type represents a semantic change that affects
     * the meaning of the XML document.
     *
     * @return {@code true} if the change is semantic, {@code false} if formatting-only
     */
    public boolean isSemantic() {
        switch (this) {
            case WHITESPACE_CHANGED:
            case QUOTE_STYLE_CHANGED:
            case ENTITY_FORM_CHANGED:
            case EMPTY_ELEMENT_STYLE_CHANGED:
                return false;
            default:
                return true;
        }
    }

    /**
     * Returns whether this change type represents a formatting-only change that
     * does not affect the semantic meaning of the XML document.
     *
     * @return {@code true} if the change is formatting-only, {@code false} if semantic
     */
    public boolean isFormattingOnly() {
        return !isSemantic();
    }
}
