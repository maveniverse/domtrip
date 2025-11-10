/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.domtrip.maven;

import static eu.maveniverse.domtrip.maven.MavenPomElements.Elements.*;
import static java.util.Arrays.asList;

import eu.maveniverse.domtrip.Document;
import eu.maveniverse.domtrip.DomTripConfig;
import eu.maveniverse.domtrip.DomTripException;
import eu.maveniverse.domtrip.Editor;
import eu.maveniverse.domtrip.Element;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class PomEditor extends AbstractMavenEditor {

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
                        "", // blank line
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
     * Gets the appropriate element order list for the given parent element.
     * This implementation provides POM-specific ordering with blank line markers.
     */
    @Override
    protected List<String> getOrderListForParent(Element parent) {
        return ELEMENT_ORDER.get(parent.name());
    }

    /**
     * Determines whether an element name should be skipped during ordering analysis.
     * For POM files, empty strings represent blank line markers.
     */
    @Override
    protected boolean shouldSkipInOrdering(String elementName) {
        return elementName.isEmpty(); // Skip blank line markers
    }

    /**
     * Enhanced element insertion with POM-specific blank line handling.
     */
    @Override
    protected Element insertElementAtPosition(
            Element parent,
            String elementName,
            Element insertBefore,
            Element insertAfter,
            List<String> order,
            int elementIndex)
            throws DomTripException {
        // Determine if we need blank lines before/after based on ordering
        boolean needsBlankLineBefore = needsBlankLineBefore(parent, order, elementIndex);
        boolean needsBlankLineAfter = needsBlankLineAfter(parent, order, elementIndex);

        // Insert the element at the correct position
        Element newElement;
        if (insertBefore != null) {
            newElement = insertElementBefore(insertBefore, elementName);
        } else if (insertAfter != null) {
            newElement = insertElementAfter(insertAfter, elementName);
        } else {
            newElement = addElement(parent, elementName);
        }
        if (needsBlankLineBefore) {
            addBlankLineBefore(newElement);
        }
        if (needsBlankLineAfter) {
            addBlankLineAfter(newElement);
        }
        return newElement;
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

    // ========== HIGH-LEVEL PROPERTY MANAGEMENT ==========

    /**
     * Updates or inserts a property value in {@code project/properties/key}.
     *
     * <p>If the property already exists, its value is updated. If {@code upsert} is true
     * and the property doesn't exist, it will be created (along with the properties element if needed).</p>
     *
     * @param upsert whether to create the property if it doesn't exist
     * @param key the property name
     * @param value the property value
     * @return true if the property was updated or created, false otherwise
     * @throws DomTripException if an error occurs during editing
     */
    public boolean updateProperty(boolean upsert, String key, String value) throws DomTripException {
        Element properties = root().child(PROPERTIES).orElse(null);
        if (properties == null && upsert) {
            properties = insertMavenElement(root(), PROPERTIES);
        }
        if (properties != null) {
            Element property = properties.child(key).orElse(null);
            if (property == null && upsert) {
                property = addElement(properties, key);
            }
            if (property != null) {
                property.textContent(value);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a property from {@code project/properties/key}.
     *
     * @param key the property name
     * @return true if the property was removed, false if it didn't exist
     */
    public boolean deleteProperty(String key) {
        Element properties = root().child(PROPERTIES).orElse(null);
        if (properties != null) {
            Element property = properties.child(key).orElse(null);
            if (property != null) {
                removeElement(property);
                return true;
            }
        }
        return false;
    }

    // ========== HIGH-LEVEL PARENT MANAGEMENT ==========

    /**
     * Sets {@code project/parent} to the given artifact coordinates.
     *
     * @param artifact the parent artifact (groupId, artifactId, version)
     * @throws DomTripException if an error occurs during editing
     */
    public void setParent(Artifact artifact) throws DomTripException {
        Element parent = findChildElement(root(), PARENT);
        if (parent == null) {
            parent = insertMavenElement(root(), PARENT);
        }
        insertMavenElement(parent, GROUP_ID, artifact.groupId());
        insertMavenElement(parent, ARTIFACT_ID, artifact.artifactId());
        insertMavenElement(parent, VERSION, artifact.version());
    }

    /**
     * Sets {@code project/version} or {@code project/parent/version} (if project version doesn't exist).
     *
     * @param value the version value
     * @throws IllegalArgumentException if no version element can be found
     */
    public void setVersion(String value) {
        Element version = findChildElement(root(), VERSION);
        if (version == null) {
            Element parent = findChildElement(root(), PARENT);
            if (parent != null) {
                version = findChildElement(parent, VERSION);
            }
        }
        if (version != null) {
            version.textContent(value);
            return;
        }
        throw new IllegalArgumentException("Could not set version");
    }

    /**
     * Sets {@code project/packaging} to the given value.
     *
     * @param value the packaging value (e.g., "jar", "pom", "war")
     * @throws DomTripException if an error occurs during editing
     */
    public void setPackaging(String value) throws DomTripException {
        Element packaging = findChildElement(root(), PACKAGING);
        if (packaging == null) {
            insertMavenElement(root(), PACKAGING, value);
        } else {
            packaging.textContent(value);
        }
    }

    // ========== HIGH-LEVEL MODULE MANAGEMENT ==========

    /**
     * Adds a module entry to {@code project/modules/module[]}, if not already present.
     *
     * @param moduleName the module name/path
     * @return true if the module was added, false if it already existed
     * @throws DomTripException if an error occurs during editing
     */
    public boolean addSubProject(String moduleName) throws DomTripException {
        Element modules = findChildElement(root(), MODULES);
        if (modules == null) {
            modules = insertMavenElement(root(), MODULES);
        }
        List<String> existing =
                modules.children(MODULE).map(Element::textContent).toList();
        if (!existing.contains(moduleName)) {
            insertMavenElement(modules, MODULE, moduleName);
            return true;
        }
        return false;
    }

    /**
     * Removes a module entry from {@code project/modules/module[]}, if present.
     *
     * @param moduleName the module name/path
     * @return true if the module was removed, false if it didn't exist
     */
    public boolean removeSubProject(String moduleName) {
        Element modules = findChildElement(root(), MODULES);
        if (modules == null) {
            return false;
        }
        AtomicBoolean removed = new AtomicBoolean(false);
        modules.children(MODULE)
                .filter(e -> Objects.equals(moduleName, e.textContent()))
                .peek(e -> removed.set(true))
                .forEach(this::removeElement);
        return removed.get();
    }

    /**
     * Determines if a blank line should be added before the element based on ordering.
     * Only adds blank lines if there are elements before the insertion point.
     */
    private boolean needsBlankLineBefore(Element parent, List<String> order, int elementIndex) {
        // Don't add blank lines if this is the first element being inserted
        // (even if parent has children, if they come after this element in the order)

        // Check if there are any existing elements that come before this element in the order
        boolean hasElementsBefore = false;
        for (int i = 0; i < elementIndex; i++) {
            String orderElement = order.get(i);
            if (!orderElement.isEmpty() && parent.child(orderElement).isPresent()) {
                hasElementsBefore = true;
                break;
            }
        }

        return hasElementsBefore
                && elementIndex > 0
                && order.get(elementIndex - 1).isEmpty();
    }

    /**
     * Determines if a blank line should be added after the element based on ordering.
     * Only adds blank lines if there are elements after the insertion point.
     */
    private boolean needsBlankLineAfter(Element parent, List<String> order, int elementIndex) {
        // Don't add blank lines if there are no elements that come after this element

        // Check if there are any existing elements that come after this element in the order
        boolean hasElementsAfter = false;
        for (int i = elementIndex + 2; i < order.size(); i++) { // Skip the blank line marker at elementIndex + 1
            String orderElement = order.get(i);
            if (!orderElement.isEmpty() && parent.child(orderElement).isPresent()) {
                hasElementsAfter = true;
                break;
            }
        }

        return hasElementsAfter
                && elementIndex < order.size() - 1
                && order.get(elementIndex + 1).isEmpty();
    }

    // ========== HIGH-LEVEL DEPENDENCY MANAGEMENT ==========

    /**
     * Updates or inserts a managed dependency in {@code project/dependencyManagement/dependencies/dependency[]}.
     *
     * <p>If the dependency exists (matched by GATC), its version is updated. If the version is a property
     * reference (${...}), the property value is updated instead. If {@code upsert} is true and the dependency
     * doesn't exist, it will be created.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact junit = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
     * editor.updateManagedDependency(true, junit);
     * }</pre>
     *
     * @param upsert whether to create the dependency if it doesn't exist
     * @param artifact the artifact coordinates
     * @return true if the dependency was updated or created, false otherwise
     * @throws DomTripException if an error occurs during editing
     * @since 0.3.0
     */
    public boolean updateManagedDependency(boolean upsert, Artifact artifact) throws DomTripException {
        Element root = root();
        Element dependencyManagement = findChildElement(root, DEPENDENCY_MANAGEMENT);
        if (dependencyManagement == null && upsert) {
            dependencyManagement = insertMavenElement(root, DEPENDENCY_MANAGEMENT);
        }
        if (dependencyManagement != null) {
            Element dependencies = findChildElement(dependencyManagement, DEPENDENCIES);
            if (dependencies == null && upsert) {
                dependencies = insertMavenElement(dependencyManagement, DEPENDENCIES);
            }
            if (dependencies != null) {
                Element dependency = dependencies
                        .children(DEPENDENCY)
                        .filter(artifact.predicateGATC())
                        .findFirst()
                        .orElse(null);
                if (dependency == null && upsert) {
                    dependency =
                            addDependency(dependencies, artifact.groupId(), artifact.artifactId(), artifact.version());
                    // Add type if not default "jar"
                    if (artifact.type() != null && !"jar".equals(artifact.type())) {
                        insertMavenElement(dependency, TYPE, artifact.type());
                    }
                    // Add classifier if present
                    if (artifact.classifier() != null) {
                        insertMavenElement(dependency, CLASSIFIER, artifact.classifier());
                    }
                    return true;
                }
                if (dependency != null) {
                    return updateVersionElement(dependency, artifact.version());
                }
            }
        }
        return false;
    }

    /**
     * Removes a managed dependency from {@code project/dependencyManagement/dependencies/dependency[]}.
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact junit = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
     * editor.deleteManagedDependency(junit);
     * }</pre>
     *
     * @param artifact the artifact to remove (matched by GATC)
     * @return true if the dependency was removed, false if it didn't exist
     * @since 0.3.0
     */
    public boolean deleteManagedDependency(Artifact artifact) {
        Element dependencyManagement = findChildElement(root(), DEPENDENCY_MANAGEMENT);
        if (dependencyManagement != null) {
            Element dependencies = findChildElement(dependencyManagement, DEPENDENCIES);
            if (dependencies != null) {
                Element dependency = dependencies
                        .children(DEPENDENCY)
                        .filter(artifact.predicateGATC())
                        .findFirst()
                        .orElse(null);
                if (dependency != null) {
                    return removeElement(dependency);
                }
            }
        }
        return false;
    }

    /**
     * Updates or inserts a dependency in {@code project/dependencies/dependency[]}.
     *
     * <p>If the dependency exists (matched by GATC), its version is updated. If the version is a property
     * reference (${...}), the property value is updated instead. If the dependency has no version element,
     * the managed dependency is updated instead. If {@code upsert} is true and the dependency doesn't exist,
     * it will be created.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact junit = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
     * editor.updateDependency(true, junit);
     * }</pre>
     *
     * @param upsert whether to create the dependency if it doesn't exist
     * @param artifact the artifact coordinates
     * @return true if the dependency was updated or created, false otherwise
     * @throws DomTripException if an error occurs during editing
     * @since 0.3.0
     */
    public boolean updateDependency(boolean upsert, Artifact artifact) throws DomTripException {
        Element dependencies = findChildElement(root(), DEPENDENCIES);
        if (dependencies == null && upsert) {
            dependencies = insertMavenElement(root(), DEPENDENCIES);
        }
        if (dependencies != null) {
            Element dependency = dependencies
                    .children(DEPENDENCY)
                    .filter(artifact.predicateGATC())
                    .findFirst()
                    .orElse(null);
            if (dependency == null && upsert) {
                dependency = addDependency(dependencies, artifact.groupId(), artifact.artifactId(), artifact.version());
                // Add type if not default "jar"
                if (artifact.type() != null && !"jar".equals(artifact.type())) {
                    insertMavenElement(dependency, TYPE, artifact.type());
                }
                // Add classifier if present
                if (artifact.classifier() != null) {
                    insertMavenElement(dependency, CLASSIFIER, artifact.classifier());
                }
                return true;
            }
            if (dependency != null) {
                java.util.Optional<Element> version = dependency.child(VERSION);
                if (version.isPresent()) {
                    return updateVersionElement(dependency, artifact.version());
                } else {
                    return updateManagedDependency(false, artifact);
                }
            }
        }
        return false;
    }

    /**
     * Removes a dependency from {@code project/dependencies/dependency[]}.
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact junit = Artifact.of("org.junit.jupiter", "junit-jupiter", "5.10.0");
     * editor.deleteDependency(junit);
     * }</pre>
     *
     * @param artifact the artifact to remove (matched by GATC)
     * @return true if the dependency was removed, false if it didn't exist
     * @since 0.3.0
     */
    public boolean deleteDependency(Artifact artifact) {
        Element dependencies = findChildElement(root(), DEPENDENCIES);
        if (dependencies != null) {
            Element dependency = dependencies
                    .children(DEPENDENCY)
                    .filter(artifact.predicateGATC())
                    .findFirst()
                    .orElse(null);
            if (dependency != null) {
                return removeElement(dependency);
            }
        }
        return false;
    }
    // ========== CONVENIENCE UTILITY METHODS ==========

    /**
     * Checks if an element exists as a child of the given parent.
     *
     * <p>This is a convenience method that provides a simple boolean check
     * for child element existence without needing to handle Optional.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Element root = editor.root();
     * if (editor.hasChildElement(root, "properties")) {
     *     // Properties section exists
     * }
     * }</pre>
     *
     * @param parent the parent element
     * @param childName the child element name to check
     * @return true if the child element exists, false otherwise
     * @see #findChildElement(Element, String)
     * @see #getChildElementText(Element, String)
     * @since 0.3.0
     */
    public boolean hasChildElement(Element parent, String childName) {
        return parent.child(childName).isPresent();
    }

    /**
     * Gets the text content of a child element, or returns null if not found.
     *
     * <p>This is a convenience method that provides a simple way to get child
     * element text content with null fallback instead of handling Optional.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Element dependency = // ... get dependency element
     * String version = editor.getChildElementText(dependency, "version");
     * if (version != null) {
     *     // Process version
     * }
     * }</pre>
     *
     * @param parent the parent element
     * @param childName the child element name
     * @return the text content of the child element, or null if not found
     * @see #findChildElement(Element, String)
     * @see #hasChildElement(Element, String)
     * @see #updateOrCreateChildElement(Element, String, String)
     * @since 0.3.0
     */
    public String getChildElementText(Element parent, String childName) {
        return parent.child(childName).map(Element::textContent).orElse(null);
    }

    /**
     * Updates or creates a child element with the given content.
     *
     * <p>If the child element exists, updates its text content. If it doesn't exist,
     * creates it with the specified content using proper Maven element ordering.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Element root = editor.root();
     * // This will update existing description or create new one
     * editor.updateOrCreateChildElement(root, "description", "My project description");
     * }</pre>
     *
     * @param parent the parent element
     * @param childName the child element name
     * @param content the content to set
     * @return the updated or created element
     * @throws DomTripException if an error occurs during editing
     * @see #findChildElement(Element, String)
     * @see #insertMavenElement(Element, String, String)
     * @see #getChildElementText(Element, String)
     * @since 0.3.0
     */
    public Element updateOrCreateChildElement(Element parent, String childName, String content)
            throws DomTripException {
        Element child = findChildElement(parent, childName);
        if (child != null) {
            child.textContent(content);
            return child;
        } else {
            return insertMavenElement(parent, childName, content);
        }
    }

    /**
     * Updates a version element, handling property references intelligently.
     *
     * <p>If the version element contains a property reference (${property.name}), this method
     * updates the property value instead of the version element itself.</p>
     *
     * @param parent the parent element containing the version child
     * @param newVersion the new version value
     * @return true if the version was updated, false otherwise
     * @throws DomTripException if an error occurs during editing
     */
    private boolean updateVersionElement(Element parent, String newVersion) throws DomTripException {
        java.util.Optional<Element> version = parent.child(VERSION);
        if (version.isPresent()) {
            String versionValue = version.orElseThrow().textContent();
            if (versionValue != null && versionValue.startsWith("${") && versionValue.endsWith("}")) {
                String propertyKey = versionValue.substring(2, versionValue.length() - 1);
                return updateProperty(false, propertyKey, newVersion);
            } else {
                version.orElseThrow().textContent(newVersion);
                return true;
            }
        }
        return false;
    }

    // ========== HIGH-LEVEL PLUGIN MANAGEMENT ==========

    /**
     * Finds or creates the managed plugins container element.
     *
     * @param upsert whether to create the structure if it doesn't exist
     * @return the plugins element, or null if not found and upsert is false
     * @throws DomTripException if an error occurs during creation
     */
    private Element findOrCreateManagedPlugins(boolean upsert) throws DomTripException {
        Element root = root();
        Element build = findChildElement(root, BUILD);
        if (build == null && upsert) {
            build = insertMavenElement(root, BUILD);
        }
        if (build != null) {
            Element pluginManagement = findChildElement(build, PLUGIN_MANAGEMENT);
            if (pluginManagement == null && upsert) {
                pluginManagement = insertMavenElement(build, PLUGIN_MANAGEMENT);
            }
            if (pluginManagement != null) {
                Element plugins = findChildElement(pluginManagement, PLUGINS);
                if (plugins == null && upsert) {
                    plugins = insertMavenElement(pluginManagement, PLUGINS);
                }
                return plugins;
            }
        }
        return null;
    }

    /**
     * Finds or creates the plugins container element.
     *
     * @param upsert whether to create the structure if it doesn't exist
     * @return the plugins element, or null if not found and upsert is false
     * @throws DomTripException if an error occurs during creation
     */
    private Element findOrCreatePlugins(boolean upsert) throws DomTripException {
        Element build = findChildElement(root(), BUILD);
        if (build == null && upsert) {
            build = insertMavenElement(root(), BUILD);
        }
        if (build != null) {
            Element plugins = findChildElement(build, PLUGINS);
            if (plugins == null && upsert) {
                plugins = insertMavenElement(build, PLUGINS);
            }
            return plugins;
        }
        return null;
    }

    /**
     * Finds a plugin element by artifact coordinates.
     *
     * @param plugins the plugins container element
     * @param artifact the artifact to find
     * @return the plugin element, or null if not found
     */
    private Element findPlugin(Element plugins, Artifact artifact) {
        if (plugins == null) {
            return null;
        }
        return plugins.children(PLUGIN)
                .filter(artifact.predicateGA())
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates or inserts a managed plugin in {@code project/build/pluginManagement/plugins/plugin[]}.
     *
     * <p>If the plugin exists (matched by GA), its version is updated. If the version is a property
     * reference (${...}), the property value is updated instead. If {@code upsert} is true and the plugin
     * doesn't exist, it will be created.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact compilerPlugin = Artifact.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
     * editor.updateManagedPlugin(true, compilerPlugin);
     * }</pre>
     *
     * @param upsert whether to create the plugin if it doesn't exist
     * @param artifact the artifact coordinates
     * @return true if the plugin was updated or created, false otherwise
     * @throws DomTripException if an error occurs during editing
     * @since 0.3.0
     */
    public boolean updateManagedPlugin(boolean upsert, Artifact artifact) throws DomTripException {
        Element plugins = findOrCreateManagedPlugins(upsert);
        if (plugins != null) {
            Element plugin = findPlugin(plugins, artifact);
            if (plugin == null && upsert) {
                addPlugin(plugins, artifact.groupId(), artifact.artifactId(), artifact.version());
                return true;
            }
            if (plugin != null) {
                return updateVersionElement(plugin, artifact.version());
            }
        }
        return false;
    }

    /**
     * Removes a managed plugin from {@code project/build/pluginManagement/plugins/plugin[]}.
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact compilerPlugin = Artifact.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
     * editor.deleteManagedPlugin(compilerPlugin);
     * }</pre>
     *
     * @param artifact the artifact to remove (matched by GA)
     * @return true if the plugin was removed, false if it didn't exist
     * @since 0.3.0
     */
    public boolean deleteManagedPlugin(Artifact artifact) {
        try {
            Element plugins = findOrCreateManagedPlugins(false);
            Element plugin = findPlugin(plugins, artifact);
            if (plugin != null) {
                return removeElement(plugin);
            }
        } catch (DomTripException e) {
            // Should not happen with upsert=false
            return false;
        }
        return false;
    }

    /**
     * Updates or inserts a plugin in {@code project/build/plugins/plugin[]}.
     *
     * <p>If the plugin exists (matched by GA), its version is updated. If the version is a property
     * reference (${...}), the property value is updated instead. If the plugin has no version element,
     * the managed plugin is updated instead. If {@code upsert} is true and the plugin doesn't exist,
     * it will be created.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact compilerPlugin = Artifact.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
     * editor.updatePlugin(true, compilerPlugin);
     * }</pre>
     *
     * @param upsert whether to create the plugin if it doesn't exist
     * @param artifact the artifact coordinates
     * @return true if the plugin was updated or created, false otherwise
     * @throws DomTripException if an error occurs during editing
     * @since 0.3.0
     */
    public boolean updatePlugin(boolean upsert, Artifact artifact) throws DomTripException {
        Element plugins = findOrCreatePlugins(upsert);
        if (plugins != null) {
            Element plugin = findPlugin(plugins, artifact);
            if (plugin == null && upsert) {
                addPlugin(plugins, artifact.groupId(), artifact.artifactId(), artifact.version());
                return true;
            }
            if (plugin != null) {
                java.util.Optional<Element> version = plugin.child(VERSION);
                if (version.isPresent()) {
                    return updateVersionElement(plugin, artifact.version());
                } else {
                    return updateManagedPlugin(false, artifact);
                }
            }
        }
        return false;
    }

    /**
     * Removes a plugin from {@code project/build/plugins/plugin[]}.
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * PomEditor editor = new PomEditor(document);
     * Artifact compilerPlugin = Artifact.of("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0");
     * editor.deletePlugin(compilerPlugin);
     * }</pre>
     *
     * @param artifact the artifact to remove (matched by GA)
     * @return true if the plugin was removed, false if it didn't exist
     * @since 0.3.0
     */
    public boolean deletePlugin(Artifact artifact) {
        try {
            Element plugins = findOrCreatePlugins(false);
            Element plugin = findPlugin(plugins, artifact);
            if (plugin != null) {
                return removeElement(plugin);
            }
        } catch (DomTripException e) {
            // Should not happen with upsert=false
            return false;
        }
        return false;
    }
}
