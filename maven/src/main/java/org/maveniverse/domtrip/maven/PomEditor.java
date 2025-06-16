/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.maveniverse.domtrip.maven;

import static java.util.Arrays.asList;
import static org.maveniverse.domtrip.maven.MavenPomElements.Elements.*;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialized editor for Maven POM files that extends the base {@link Editor} class
 * with Maven-specific functionality and element ordering.
 *
 * <p>The PomEditor provides lossless XML editing capabilities specifically tailored for Maven POM files,
 * including:</p>
 * <ul>
 *   <li><strong>Maven Element Ordering</strong> - Automatically orders elements according to Maven conventions</li>
 *   <li><strong>Formatting Preservation</strong> - Maintains original formatting, whitespace, and comments</li>
 *   <li><strong>Intelligent Blank Lines</strong> - Adds appropriate blank lines between element groups</li>
 *   <li><strong>Maven-specific Methods</strong> - Convenience methods for common POM operations</li>
 * </ul>
 *
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * // Parse existing POM
 * Document doc = Document.of(pomXmlString);
 * PomEditor editor = new PomEditor(doc);
 *
 * // Add elements with proper ordering
 * Element root = editor.root();
 * editor.insertMavenElement(root, "description", "My project description");
 * editor.insertMavenElement(root, "name", "My Project");  // Will be ordered before description
 *
 * // Serialize with preserved formatting
 * String result = editor.toXml();
 * }</pre>
 *
 * <h3>Element Ordering:</h3>
 * <p>The PomEditor automatically orders elements according to Maven POM conventions:</p>
 * <ul>
 *   <li>Project elements: modelVersion, parent, groupId, artifactId, version, packaging, name, description, etc.</li>
 *   <li>Build elements: defaultGoal, directory, finalName, sourceDirectory, etc.</li>
 *   <li>Plugin elements: groupId, artifactId, version, extensions, executions, etc.</li>
 *   <li>Dependency elements: groupId, artifactId, version, classifier, type, scope, etc.</li>
 * </ul>
 *
 * @see Editor
 * @see MavenPomElements
 * @since 0.1
 */
public class PomEditor extends Editor {

    // Element ordering configuration for Maven POM elements
    private static final Map<String, List<String>> ELEMENT_ORDER = new HashMap<>();

    static {
        // Project element order - follows Maven POM reference order
        ELEMENT_ORDER.put(
                "project",
                asList(
                        MODEL_VERSION,
                        "", // blank line
                        PARENT,
                        "", // blank line
                        GROUP_ID,
                        ARTIFACT_ID,
                        VERSION,
                        PACKAGING,
                        "", // blank line
                        NAME,
                        DESCRIPTION,
                        URL,
                        INCEPTION_YEAR,
                        ORGANIZATION,
                        LICENSES,
                        "", // blank line
                        DEVELOPERS,
                        CONTRIBUTORS,
                        "", // blank line
                        MAILING_LISTS,
                        "", // blank line
                        PREREQUISITES,
                        "", // blank line
                        MODULES,
                        "", // blank line
                        SCM,
                        ISSUE_MANAGEMENT,
                        CI_MANAGEMENT,
                        DISTRIBUTION_MANAGEMENT,
                        "", // blank line
                        PROPERTIES,
                        "", // blank line
                        DEPENDENCY_MANAGEMENT,
                        DEPENDENCIES,
                        "", // blank line
                        REPOSITORIES,
                        PLUGIN_REPOSITORIES,
                        "", // blank line
                        BUILD,
                        "", // blank line
                        REPORTING,
                        "", // blank line
                        PROFILES));

        // Build element order
        ELEMENT_ORDER.put(
                BUILD,
                asList(
                        DEFAULT_GOAL,
                        DIRECTORY,
                        FINAL_NAME,
                        SOURCE_DIRECTORY,
                        SCRIPT_SOURCE_DIRECTORY,
                        TEST_SOURCE_DIRECTORY,
                        OUTPUT_DIRECTORY,
                        TEST_OUTPUT_DIRECTORY,
                        EXTENSIONS,
                        "", // blank line
                        PLUGIN_MANAGEMENT,
                        PLUGINS));

        // Plugin element order
        ELEMENT_ORDER.put(
                PLUGIN,
                asList(
                        GROUP_ID,
                        ARTIFACT_ID,
                        VERSION,
                        EXTENSIONS,
                        EXECUTIONS,
                        DEPENDENCIES,
                        GOALS,
                        INHERITED,
                        CONFIGURATION));

        // Dependency element order
        ELEMENT_ORDER.put(
                DEPENDENCY,
                asList(GROUP_ID, ARTIFACT_ID, VERSION, CLASSIFIER, TYPE, SCOPE, SYSTEM_PATH, OPTIONAL, EXCLUSIONS));
    }

    /**
     * Creates a new PomEditor with default configuration.
     */
    public PomEditor() {
        super();
    }

    /**
     * Creates a new PomEditor with custom configuration.
     *
     * @param config the configuration to use
     */
    public PomEditor(DomTripConfig config) {
        super(config);
    }

    /**
     * Creates a new PomEditor with an existing Document.
     *
     * @param document the existing Document to edit
     */
    public PomEditor(Document document) {
        super(document);
    }

    /**
     * Creates a new PomEditor with an existing Document and custom configuration.
     *
     * @param document the existing Document to edit
     * @param config the configuration to use
     */
    public PomEditor(Document document, DomTripConfig config) {
        super(document, config);
    }

    /**
     * Inserts a new Maven element with proper ordering and formatting.
     *
     * <p>This method automatically determines the correct position for the new element
     * based on Maven POM conventions and adds appropriate blank lines.</p>
     *
     * @param parent the parent element
     * @param elementName the name of the new element
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    public Element insertMavenElement(Element parent, String elementName) throws DomTripException {
        return insertElementAtCorrectPosition(parent, elementName, null);
    }

    /**
     * Inserts a new Maven element with text content and proper ordering.
     *
     * @param parent the parent element
     * @param elementName the name of the new element
     * @param textContent the text content for the element
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    public Element insertMavenElement(Element parent, String elementName, String textContent) throws DomTripException {
        return insertElementAtCorrectPosition(parent, elementName, textContent);
    }

    /**
     * Finds a child element by name under the specified parent.
     *
     * @param parent the parent element
     * @param elementName the child element name to find
     * @return the child element if found, null otherwise
     */
    public Element findChildElement(Element parent, String elementName) {
        return parent.child(elementName).orElse(null);
    }

    /**
     * Creates a new Maven POM document with the specified root element name.
     *
     * @param rootElementName the name of the root element (typically "project")
     * @throws DomTripException if the document cannot be created
     */
    public void createMavenDocument(String rootElementName) throws DomTripException {
        createDocument(rootElementName);

        // Set the Maven namespace on the root element if it's a project
        if ("project".equals(rootElementName)) {
            Element root = root();
            root.attribute("xmlns", MavenPomElements.Namespaces.MAVEN_4_0_0_NAMESPACE);
        }
    }

    /**
     * Adds a dependency element with the specified coordinates.
     *
     * @param dependenciesElement the dependencies container element
     * @param groupId the dependency groupId
     * @param artifactId the dependency artifactId
     * @param version the dependency version (can be null)
     * @return the newly created dependency element
     * @throws DomTripException if the dependency cannot be added
     */
    public Element addDependency(Element dependenciesElement, String groupId, String artifactId, String version)
            throws DomTripException {
        Element dependency = insertMavenElement(dependenciesElement, DEPENDENCY);
        insertMavenElement(dependency, GROUP_ID, groupId);
        insertMavenElement(dependency, ARTIFACT_ID, artifactId);
        if (version != null && !version.trim().isEmpty()) {
            insertMavenElement(dependency, VERSION, version);
        }
        return dependency;
    }

    /**
     * Adds a plugin element with the specified coordinates.
     *
     * @param pluginsElement the plugins container element
     * @param groupId the plugin groupId
     * @param artifactId the plugin artifactId
     * @param version the plugin version (can be null)
     * @return the newly created plugin element
     * @throws DomTripException if the plugin cannot be added
     */
    public Element addPlugin(Element pluginsElement, String groupId, String artifactId, String version)
            throws DomTripException {
        Element plugin = insertMavenElement(pluginsElement, PLUGIN);
        if (groupId != null && !groupId.trim().isEmpty()) {
            insertMavenElement(plugin, GROUP_ID, groupId);
        }
        insertMavenElement(plugin, ARTIFACT_ID, artifactId);
        if (version != null && !version.trim().isEmpty()) {
            insertMavenElement(plugin, VERSION, version);
        }
        return plugin;
    }

    /**
     * Adds a module to the modules section.
     *
     * @param modulesElement the modules container element
     * @param moduleName the name of the module
     * @return the newly created module element
     * @throws DomTripException if the module cannot be added
     */
    public Element addModule(Element modulesElement, String moduleName) throws DomTripException {
        return insertMavenElement(modulesElement, MODULE, moduleName);
    }

    /**
     * Adds a property to the properties section.
     *
     * @param propertiesElement the properties container element
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     * @return the newly created property element
     * @throws DomTripException if the property cannot be added
     */
    public Element addProperty(Element propertiesElement, String propertyName, String propertyValue)
            throws DomTripException {
        return addElement(propertiesElement, propertyName, propertyValue);
    }

    /**
     * Inserts an element at the correct position based on Maven POM element ordering.
     * This method respects the ELEMENT_ORDER configuration and handles blank lines.
     *
     * @param parent the parent element to insert into
     * @param elementName the name of the element to insert
     * @param textContent the text content (null for empty element)
     * @return the newly created element
     * @throws DomTripException if the element cannot be added
     */
    private Element insertElementAtCorrectPosition(Element parent, String elementName, String textContent)
            throws DomTripException {
        // Get the ordering for this parent element type
        List<String> order = ELEMENT_ORDER.get(parent.name());
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
            if (!beforeElementName.isEmpty()) { // Skip blank line markers
                Element existing = parent.child(beforeElementName).orElse(null);
                if (existing != null) {
                    insertAfter = existing;
                    break;
                }
            }
        }

        // Look for elements that should come after this one
        for (int i = newElementIndex + 1; i < order.size(); i++) {
            String afterElementName = order.get(i);
            if (!afterElementName.isEmpty()) { // Skip blank line markers
                Element existing = parent.child(afterElementName).orElse(null);
                if (existing != null) {
                    insertBefore = existing;
                    break;
                }
            }
        }

        // Determine if we need blank lines before/after based on ordering
        boolean needsBlankLineBefore = needsBlankLineBefore(order, newElementIndex);
        boolean needsBlankLineAfter = needsBlankLineAfter(order, newElementIndex);

        // Insert the element at the correct position
        Element element;
        if (insertBefore != null) {
            element = insertElementBefore(insertBefore, elementName);
        } else if (insertAfter != null) {
            element = insertElementAfter(insertAfter, elementName);
        } else {
            // No reference elements found, use enhanced whitespace control
            element = addElement(parent, elementName, needsBlankLineBefore, needsBlankLineAfter);
        }

        // Set text content if provided
        if (textContent != null && !textContent.isEmpty()) {
            element.textContent(textContent);
        }

        return element;
    }

    /**
     * Determines if a blank line should be added before the element based on ordering.
     */
    private boolean needsBlankLineBefore(List<String> order, int elementIndex) {
        return elementIndex > 0 && order.get(elementIndex - 1).isEmpty();
    }

    /**
     * Determines if a blank line should be added after the element based on ordering.
     */
    private boolean needsBlankLineAfter(List<String> order, int elementIndex) {
        return elementIndex < order.size() - 1 && order.get(elementIndex + 1).isEmpty();
    }
}
