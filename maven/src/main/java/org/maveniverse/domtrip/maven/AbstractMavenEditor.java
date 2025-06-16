/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.List;

/**
 * Abstract base class for Maven-specific editors that provides common element ordering functionality.
 *
 * <p>This class encapsulates the logic for inserting elements at the correct position based on
 * Maven-specific element ordering rules. Subclasses should implement {@link #getOrderListForParent(Element)}
 * to provide the appropriate ordering for their specific document types.</p>
 */
public abstract class AbstractMavenEditor extends Editor {

    /**
     * Creates a new editor with default configuration.
     */
    protected AbstractMavenEditor() {
        super();
    }

    /**
     * Creates a new editor with the specified configuration.
     *
     * @param config the configuration to use
     */
    protected AbstractMavenEditor(DomTripConfig config) {
        super(config);
    }

    /**
     * Creates a new editor for the specified document.
     *
     * @param document the document to edit
     */
    protected AbstractMavenEditor(Document document) {
        super(document);
    }

    /**
     * Creates a new editor for the specified document with the given configuration.
     *
     * @param document the document to edit
     * @param config the configuration to use
     */
    protected AbstractMavenEditor(Document document, DomTripConfig config) {
        super(document, config);
    }

    /**
     * Gets the appropriate element order list for the given parent element.
     * Subclasses must implement this method to provide ordering specific to their document type.
     *
     * @param parent the parent element
     * @return the ordered list of element names, or null if no specific ordering is defined
     */
    protected abstract List<String> getOrderListForParent(Element parent);

    /**
     * Inserts an element at the correct position based on Maven element ordering.
     *
     * <p>This method respects the element ordering defined by {@link #getOrderListForParent(Element)}
     * and handles proper positioning of new elements relative to existing siblings.</p>
     *
     * @param parent the parent element to insert into
     * @param elementName the name of the element to insert
     * @param textContent the text content (null for empty element)
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    protected Element insertElementAtCorrectPosition(Element parent, String elementName, String textContent)
            throws DomTripException {
        List<String> order = getOrderListForParent(parent);
        return insertElementAtCorrectPosition(parent, elementName, textContent, order);
    }

    /**
     * Inserts an element at the correct position based on the provided element ordering.
     *
     * <p>This is the core implementation that handles element positioning logic. It can be used
     * directly when a specific ordering list is available, or indirectly through
     * {@link #insertElementAtCorrectPosition(Element, String, String)} which uses the ordering
     * from {@link #getOrderListForParent(Element)}.</p>
     *
     * @param parent the parent element to insert into
     * @param elementName the name of the element to insert
     * @param textContent the text content (null for empty element)
     * @param order the ordered list of element names, or null if no specific ordering
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    protected Element insertElementAtCorrectPosition(
            Element parent, String elementName, String textContent, List<String> order) throws DomTripException {
        if (order == null) {
            // No specific ordering defined, just append at the end
            Element element = addElement(parent, elementName);
            if (textContent != null && !textContent.isEmpty()) {
                element.textContent(textContent);
            }
            return element;
        }

        // Find the position of the new element in the ordering
        int newElementIndex = order.indexOf(elementName);
        if (newElementIndex == -1) {
            // Element not in ordering, append at the end
            Element element = addElement(parent, elementName);
            if (textContent != null && !textContent.isEmpty()) {
                element.textContent(textContent);
            }
            return element;
        }

        // Find the correct insertion position by looking at existing children
        Element insertAfter = null;
        Element insertBefore = null;

        // Look for elements that should come before this one
        for (int i = newElementIndex - 1; i >= 0; i--) {
            String beforeElementName = order.get(i);
            if (shouldSkipInOrdering(beforeElementName)) {
                continue; // Skip special markers (like blank lines in POM)
            }
            Element existing = parent.child(beforeElementName).orElse(null);
            if (existing != null) {
                insertAfter = existing;
                break;
            }
        }

        // Look for elements that should come after this one
        for (int i = newElementIndex + 1; i < order.size(); i++) {
            String afterElementName = order.get(i);
            if (shouldSkipInOrdering(afterElementName)) {
                continue; // Skip special markers (like blank lines in POM)
            }
            Element existing = parent.child(afterElementName).orElse(null);
            if (existing != null) {
                insertBefore = existing;
                break;
            }
        }

        // Insert the element at the correct position
        Element element =
                insertElementAtPosition(parent, elementName, insertBefore, insertAfter, order, newElementIndex);

        // Set text content if provided
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }

        return element;
    }

    /**
     * Determines whether an element name should be skipped during ordering analysis.
     * This allows subclasses to handle special markers (like blank line indicators).
     *
     * @param elementName the element name to check
     * @return true if this element should be skipped during ordering
     */
    protected boolean shouldSkipInOrdering(String elementName) {
        return false; // Default: don't skip anything
    }

    /**
     * Inserts an element at the determined position, with optional enhanced formatting.
     * This method can be overridden by subclasses to provide specialized insertion logic.
     *
     * @param parent the parent element
     * @param elementName the element name to insert
     * @param insertBefore element to insert before (may be null)
     * @param insertAfter element to insert after (may be null)
     * @param order the complete ordering list
     * @param elementIndex the index of this element in the ordering
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    protected Element insertElementAtPosition(
            Element parent,
            String elementName,
            Element insertBefore,
            Element insertAfter,
            List<String> order,
            int elementIndex)
            throws DomTripException {
        if (insertBefore != null) {
            return insertElementBefore(insertBefore, elementName);
        } else if (insertAfter != null) {
            return insertElementAfter(insertAfter, elementName);
        } else {
            return addElement(parent, elementName);
        }
    }
}
