/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static org.maveniverse.domtrip.maven.MavenExtensionsElements.Elements.*;
import static org.maveniverse.domtrip.maven.MavenExtensionsElements.Namespaces.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Specialized editor for Maven extensions.xml files.
 *
 * <p>The ExtensionsEditor extends the base {@link Editor} class with Maven core extensions-specific functionality,
 * including automatic element ordering according to Maven conventions, intelligent blank line insertion,
 * and convenience methods for common extensions operations.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Extensions-aware element ordering</strong> - Automatically orders elements according to Maven extensions conventions</li>
 *   <li><strong>Formatting preservation</strong> - Maintains original formatting, whitespace, and comments</li>
 *   <li><strong>Intelligent blank lines</strong> - Adds appropriate blank lines between element groups</li>
 *   <li><strong>Convenience methods</strong> - Easy-to-use methods for adding extensions</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create a new extensions document
 * ExtensionsEditor editor = new ExtensionsEditor();
 * editor.createExtensionsDocument();
 * Element root = editor.root();
 *
 * // Add extensions with convenience methods
 * editor.addExtension(root, "org.apache.maven.wagon", "wagon-ssh", "3.5.1");
 * editor.addExtension(root, "io.takari.maven", "takari-smart-builder", "0.6.1");
 *
 * String result = editor.toXml();
 * }</pre>
 *
 * @since 0.1
 */
public class ExtensionsEditor extends Editor {

    /**
     * Element ordering for extension elements.
     */
    private static final List<String> EXTENSION_ELEMENT_ORDER =
            Arrays.asList(GROUP_ID, ARTIFACT_ID, VERSION, CLASSIFIER, TYPE, CONFIGURATION);

    /**
     * Creates a new ExtensionsEditor with default configuration.
     */
    public ExtensionsEditor() {
        super();
    }

    /**
     * Creates a new ExtensionsEditor with the specified configuration.
     *
     * @param config the configuration to use
     */
    public ExtensionsEditor(DomTripConfig config) {
        super(config);
    }

    /**
     * Creates a new ExtensionsEditor for the specified document.
     *
     * @param document the document to edit
     */
    public ExtensionsEditor(Document document) {
        super(document);
    }

    /**
     * Creates a new ExtensionsEditor for the specified document with the given configuration.
     *
     * @param document the document to edit
     * @param config the configuration to use
     */
    public ExtensionsEditor(Document document, DomTripConfig config) {
        super(document, config);
    }

    /**
     * Creates a new Maven extensions document with proper namespace.
     *
     * <p>This method creates a new document with the extensions root element and sets up
     * the appropriate Maven extensions namespace.</p>
     */
    public void createExtensionsDocument() {
        createDocument(EXTENSIONS);
        Element root = root();
        root.attribute("xmlns", EXTENSIONS_1_2_0_NAMESPACE);
        root.attribute("xmlns:xsi", MavenExtensionsElements.Attributes.XSI_NAMESPACE_URI);
        root.attribute("xsi:schemaLocation", MavenExtensionsElements.SchemaLocations.EXTENSIONS_1_2_0_SCHEMA_LOCATION);
    }

    /**
     * Inserts an element with extensions-aware ordering and formatting.
     *
     * <p>This method automatically determines the correct position for the element
     * based on Maven extensions conventions and adds appropriate blank lines.</p>
     *
     * @param parent the parent element
     * @param elementName the name of the element to insert
     * @return the newly created element
     */
    public Element insertExtensionsElement(Element parent, String elementName) {
        return insertExtensionsElement(parent, elementName, null);
    }

    /**
     * Inserts an element with extensions-aware ordering, formatting, and text content.
     *
     * @param parent the parent element
     * @param elementName the name of the element to insert
     * @param textContent the text content for the element (can be null)
     * @return the newly created element
     */
    public Element insertExtensionsElement(Element parent, String elementName, String textContent) {
        List<String> orderList = getOrderListForParent(parent);
        return insertElementAtCorrectPosition(parent, elementName, textContent, orderList);
    }

    /**
     * Adds an extension with the specified coordinates.
     *
     * @param extensionsElement the extensions parent element (root element)
     * @param groupId the extension group ID
     * @param artifactId the extension artifact ID
     * @param version the extension version
     * @return the newly created extension element
     */
    public Element addExtension(Element extensionsElement, String groupId, String artifactId, String version) {
        return addExtension(extensionsElement, groupId, artifactId, version, null, null);
    }

    /**
     * Adds an extension with the specified coordinates and optional classifier and type.
     *
     * @param extensionsElement the extensions parent element (root element)
     * @param groupId the extension group ID
     * @param artifactId the extension artifact ID
     * @param version the extension version
     * @param classifier the extension classifier (can be null)
     * @param type the extension type (can be null, defaults to "jar")
     * @return the newly created extension element
     */
    public Element addExtension(
            Element extensionsElement,
            String groupId,
            String artifactId,
            String version,
            String classifier,
            String type) {
        Element extension = addElement(extensionsElement, EXTENSION);

        insertElementAtCorrectPosition(extension, GROUP_ID, groupId, EXTENSION_ELEMENT_ORDER);
        insertElementAtCorrectPosition(extension, ARTIFACT_ID, artifactId, EXTENSION_ELEMENT_ORDER);
        insertElementAtCorrectPosition(extension, VERSION, version, EXTENSION_ELEMENT_ORDER);

        if (classifier != null) {
            insertElementAtCorrectPosition(extension, CLASSIFIER, classifier, EXTENSION_ELEMENT_ORDER);
        }

        if (type != null) {
            insertElementAtCorrectPosition(extension, TYPE, type, EXTENSION_ELEMENT_ORDER);
        }

        return extension;
    }

    /**
     * Finds a direct child element by name.
     *
     * @param parent the parent element
     * @param elementName the name of the child element to find
     * @return the child element if found, null otherwise
     */
    public Element findChildElement(Element parent, String elementName) {
        Optional<Element> child = parent.child(elementName);
        return child.orElse(null);
    }

    /**
     * Gets the appropriate element order list for the given parent element.
     */
    private List<String> getOrderListForParent(Element parent) {
        String parentName = parent.name();
        return switch (parentName) {
            case EXTENSION -> EXTENSION_ELEMENT_ORDER;
            default -> null;
        };
    }

    /**
     * Inserts an element at the correct position based on element ordering.
     */
    private Element insertElementAtCorrectPosition(
            Element parent, String elementName, String textContent, List<String> order) {
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
            Element existing = parent.child(beforeElementName).orElse(null);
            if (existing != null) {
                insertAfter = existing;
                break;
            }
        }

        // Look for elements that should come after this one
        for (int i = newElementIndex + 1; i < order.size(); i++) {
            String afterElementName = order.get(i);
            Element existing = parent.child(afterElementName).orElse(null);
            if (existing != null) {
                insertBefore = existing;
                break;
            }
        }

        // Insert the element at the correct position
        Element element;
        if (insertBefore != null) {
            element = insertElementBefore(insertBefore, elementName);
        } else if (insertAfter != null) {
            element = insertElementAfter(insertAfter, elementName);
        } else {
            element = addElement(parent, elementName);
        }

        // Set text content if provided
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }

        return element;
    }
}
