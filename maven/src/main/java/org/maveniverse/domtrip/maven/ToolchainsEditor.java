/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static org.maveniverse.domtrip.maven.MavenToolchainsElements.Elements.*;
import static org.maveniverse.domtrip.maven.MavenToolchainsElements.Namespaces.*;
import static org.maveniverse.domtrip.maven.MavenToolchainsElements.ToolchainTypes.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Specialized editor for Maven toolchains.xml files.
 *
 * <p>The ToolchainsEditor extends the base {@link Editor} class with Maven toolchains-specific functionality,
 * including automatic element ordering according to Maven conventions, intelligent blank line insertion,
 * and convenience methods for common toolchain operations.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Toolchains-aware element ordering</strong> - Automatically orders elements according to Maven toolchains conventions</li>
 *   <li><strong>Formatting preservation</strong> - Maintains original formatting, whitespace, and comments</li>
 *   <li><strong>Intelligent blank lines</strong> - Adds appropriate blank lines between element groups</li>
 *   <li><strong>Convenience methods</strong> - Easy-to-use methods for JDK and other toolchains</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create a new toolchains document
 * ToolchainsEditor editor = new ToolchainsEditor();
 * editor.createToolchainsDocument();
 * Element root = editor.root();
 *
 * // Add JDK toolchains with convenience methods
 * editor.addJdkToolchain(root, "17", "openjdk", "/path/to/jdk/17");
 * editor.addJdkToolchain(root, "11", "adoptium", "/path/to/jdk/11");
 *
 * String result = editor.toXml();
 * }</pre>
 *
 * @since 0.1
 */
public class ToolchainsEditor extends Editor {

    /**
     * Element ordering for toolchain elements.
     */
    private static final List<String> TOOLCHAIN_ELEMENT_ORDER = Arrays.asList(TYPE, PROVIDES, CONFIGURATION);

    /**
     * Creates a new ToolchainsEditor with default configuration.
     */
    public ToolchainsEditor() {
        super();
    }

    /**
     * Creates a new ToolchainsEditor with the specified configuration.
     *
     * @param config the configuration to use
     */
    public ToolchainsEditor(DomTripConfig config) {
        super(config);
    }

    /**
     * Creates a new ToolchainsEditor for the specified document.
     *
     * @param document the document to edit
     */
    public ToolchainsEditor(Document document) {
        super(document);
    }

    /**
     * Creates a new ToolchainsEditor for the specified document with the given configuration.
     *
     * @param document the document to edit
     * @param config the configuration to use
     */
    public ToolchainsEditor(Document document, DomTripConfig config) {
        super(document, config);
    }

    /**
     * Creates a new Maven toolchains document with proper namespace.
     *
     * <p>This method creates a new document with the toolchains root element and sets up
     * the appropriate Maven toolchains namespace.</p>
     */
    public void createToolchainsDocument() {
        createDocument(TOOLCHAINS);
        Element root = root();
        root.attribute("xmlns", TOOLCHAINS_1_1_0_NAMESPACE);
        root.attribute("xmlns:xsi", MavenToolchainsElements.Attributes.XSI_NAMESPACE_URI);
        root.attribute("xsi:schemaLocation", MavenToolchainsElements.SchemaLocations.TOOLCHAINS_1_1_0_SCHEMA_LOCATION);
    }

    /**
     * Inserts an element with toolchains-aware ordering and formatting.
     *
     * <p>This method automatically determines the correct position for the element
     * based on Maven toolchains conventions and adds appropriate blank lines.</p>
     *
     * @param parent the parent element
     * @param elementName the name of the element to insert
     * @return the newly created element
     */
    public Element insertToolchainsElement(Element parent, String elementName) {
        return insertToolchainsElement(parent, elementName, null);
    }

    /**
     * Inserts an element with toolchains-aware ordering, formatting, and text content.
     *
     * @param parent the parent element
     * @param elementName the name of the element to insert
     * @param textContent the text content for the element (can be null)
     * @return the newly created element
     */
    public Element insertToolchainsElement(Element parent, String elementName, String textContent) {
        List<String> orderList = getOrderListForParent(parent);
        return insertElementAtCorrectPosition(parent, elementName, textContent, orderList);
    }

    /**
     * Adds a JDK toolchain with the specified version, vendor, and JDK home.
     *
     * @param toolchainsElement the toolchains parent element (root element)
     * @param version the JDK version (e.g., "17", "11", "1.8")
     * @param vendor the JDK vendor (e.g., "openjdk", "adoptium", "azul")
     * @param jdkHome the path to the JDK installation
     * @return the newly created toolchain element
     */
    public Element addJdkToolchain(Element toolchainsElement, String version, String vendor, String jdkHome) {
        Element toolchain = addElement(toolchainsElement, TOOLCHAIN);

        // Add type
        insertElementAtCorrectPosition(toolchain, TYPE, JDK, TOOLCHAIN_ELEMENT_ORDER);

        // Add provides section
        Element provides = insertElementAtCorrectPosition(toolchain, PROVIDES, null, TOOLCHAIN_ELEMENT_ORDER);
        addElement(provides, VERSION, version);
        addElement(provides, VENDOR, vendor);

        // Add configuration section
        Element configuration = insertElementAtCorrectPosition(toolchain, CONFIGURATION, null, TOOLCHAIN_ELEMENT_ORDER);
        addElement(configuration, JDK_HOME, jdkHome);

        return toolchain;
    }

    /**
     * Adds a generic toolchain with the specified type.
     *
     * @param toolchainsElement the toolchains parent element (root element)
     * @param type the toolchain type (e.g., "jdk", "netbeans", "protobuf")
     * @return the newly created toolchain element
     */
    public Element addToolchain(Element toolchainsElement, String type) {
        Element toolchain = addElement(toolchainsElement, TOOLCHAIN);
        insertElementAtCorrectPosition(toolchain, TYPE, type, TOOLCHAIN_ELEMENT_ORDER);
        return toolchain;
    }

    /**
     * Adds a NetBeans toolchain with the specified version and installation directory.
     *
     * @param toolchainsElement the toolchains parent element (root element)
     * @param version the NetBeans version
     * @param installDir the path to the NetBeans installation
     * @return the newly created toolchain element
     */
    public Element addNetBeansToolchain(Element toolchainsElement, String version, String installDir) {
        Element toolchain = addElement(toolchainsElement, TOOLCHAIN);

        // Add type
        insertElementAtCorrectPosition(toolchain, TYPE, NETBEANS, TOOLCHAIN_ELEMENT_ORDER);

        // Add provides section
        Element provides = insertElementAtCorrectPosition(toolchain, PROVIDES, null, TOOLCHAIN_ELEMENT_ORDER);
        addElement(provides, VERSION, version);

        // Add configuration section
        Element configuration = insertElementAtCorrectPosition(toolchain, CONFIGURATION, null, TOOLCHAIN_ELEMENT_ORDER);
        addElement(configuration, INSTALL_DIR, installDir);

        return toolchain;
    }

    /**
     * Adds a provides element to a toolchain.
     *
     * @param toolchainElement the toolchain element
     * @param key the provides key
     * @param value the provides value
     * @return the provides element (created if it doesn't exist)
     */
    public Element addProvides(Element toolchainElement, String key, String value) {
        Element provides = findChildElement(toolchainElement, PROVIDES);
        if (provides == null) {
            provides = insertElementAtCorrectPosition(toolchainElement, PROVIDES, null, TOOLCHAIN_ELEMENT_ORDER);
        }
        addElement(provides, key, value);
        return provides;
    }

    /**
     * Adds a configuration element to a toolchain.
     *
     * @param toolchainElement the toolchain element
     * @param key the configuration key
     * @param value the configuration value
     * @return the configuration element (created if it doesn't exist)
     */
    public Element addConfiguration(Element toolchainElement, String key, String value) {
        Element configuration = findChildElement(toolchainElement, CONFIGURATION);
        if (configuration == null) {
            configuration =
                    insertElementAtCorrectPosition(toolchainElement, CONFIGURATION, null, TOOLCHAIN_ELEMENT_ORDER);
        }
        addElement(configuration, key, value);
        return configuration;
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
            case TOOLCHAIN -> TOOLCHAIN_ELEMENT_ORDER;
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
